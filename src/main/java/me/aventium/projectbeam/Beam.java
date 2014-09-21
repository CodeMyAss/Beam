package me.aventium.projectbeam;

import com.sk89q.bukkit.util.CommandsManagerRegistration;
import com.sk89q.minecraft.util.commands.*;
import me.aventium.projectbeam.channels.ChannelManager;
import me.aventium.projectbeam.collections.*;
import me.aventium.projectbeam.commands.AdminCommands;
import me.aventium.projectbeam.commands.DatabaseCommand;
import me.aventium.projectbeam.commands.ServerCommands;
import me.aventium.projectbeam.commands.StaffCommands;
import me.aventium.projectbeam.documents.DBGroup;
import me.aventium.projectbeam.documents.DBServer;
import me.aventium.projectbeam.listeners.FallbackListener;
import me.aventium.projectbeam.listeners.LobbyListener;
import me.aventium.projectbeam.listeners.ServerListener;
import me.aventium.projectbeam.listeners.SessionListener;
import me.aventium.projectbeam.tasks.PollingTaskManager;
import me.aventium.projectbeam.tasks.RestartChecker;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.joda.time.Instant;

public class Beam extends JavaPlugin {

    private static Beam instance;
    private static Thread mainThread;
    private static PollingTaskManager pollingTaskManager = new PollingTaskManager();
    private ServerListener serverListener;
    private SessionListener sessionListener;
    private Portal portal;

    public static Beam getInstance() {
        return instance;
    }

    public static Thread getMainThread() {
        return mainThread;
    }

    public static PollingTaskManager getPollingTaskManager() {
        return pollingTaskManager;
    }

    public ServerListener getServerListener() {
        return serverListener;
    }

    public SessionListener getSessionListener() {
        return sessionListener;
    }

    public Portal getPortal() { return portal; }

    public void onEnable() {
        instance = this;

        final Instant now = Instant.now();

        mainThread = Thread.currentThread();

        if(!getDataFolder().exists()) {
            getDataFolder().mkdir();
            getConfig().options().copyDefaults(true);
            saveConfig();
            getConfig().set("mongo.server_id", ObjectId.get().toString());
            saveConfig();
        }

        Database.setUpExecutorService(10);

        DatabaseConfiguration dbConf = new MongoConfigParser(getLogger()).parse(getConfig());
        Database.setConfig(dbConf);

        try {
            Database.reconnect();
        } catch(Throwable throwable) {
            throwable.printStackTrace();
        }

        // initialize channels
        ChannelManager.start(this);


        this.serverListener = new ServerListener();
        getServer().getPluginManager().registerEvents(this.serverListener, this);

        this.sessionListener = new SessionListener();
        getServer().getPluginManager().registerEvents(this.sessionListener, this);

        getServer().getPluginManager().registerEvents(new PermissionsHandler(), this);

        // try to initialize certain database features
        try {
            // reset server online players
            Database.getCollection(Servers.class).resetOnlinePlayers();

            // ensure that all sessions have terminated
            Database.getCollection(Sessions.class).updateEnds(Database.getServerId(), now);
        } catch (Throwable t) {}

        portal = new Portal(this);

        // register commands
        registerCommands();

        // clean up server info
        Database.getExecutorService().submit(new DatabaseCommand() {
            @Override public void run() {
                Database.getCollection(Servers.class).cleanUp(Database.getServerId(), true);
            }
        });

        // update start time
        Database.getExecutorService().submit(new DatabaseCommand() {
            @Override public void run() {
                Database.getCollection(Servers.class).updateStartTime(Database.getServerId(), now);
            }
        });

        if(Database.getServer().getName().toLowerCase().contains("lobby") || Database.getServer().getName().toLowerCase().contains("hub")) {
            getServer().getPluginManager().registerEvents(new LobbyListener(), this);
        }

        Database.getCollection(Servers.class).cacheAllServers();

        Database.registerCollection(new Sessions());
        Database.registerCollection(new Users());
        Database.registerCollection(new Punishments());
        Database.registerCollection(new Groups());

        if(Database.getServer().getName().equals(Config.Bungee.fallbackServer())) {
            getServer().getPluginManager().registerEvents(new FallbackListener(), this);
            System.out.println("Registered as fallback server.");
        }

        getServer().getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                for(Player player : Bukkit.getOnlinePlayers()) {
                    DBGroup group = Database.getCollection(Users.class).findByName(player.getName()).getGroup();
                    PermissionsHandler.removeGroupPermissions(player, group.getName());
                    PermissionsHandler.giveGroupPermissions(player, group);
                }
            }
        }, 20 * 5L, 20 * 5L);

        System.out.println("Finished initializing.\nRegistered with following credentials:\nServer Name: " + Database.getServer().getName() +
        "\nBungeeCord Server Name: " + Database.getServer().getBungeeName() +
        "\nFamily: " + Database.getServer().getFamily() +
        "\nVisibility: " + Database.getServer().getVisibility().toString());

        new RestartChecker(this).runTaskTimer(this, 20L, 20L);
    }

    public void onDisable() {
        final Instant now = Instant.now();

        // Update session ends
        for(Player player : Bukkit.getOnlinePlayers()) {
            final String username = player.getName();
            Database.getExecutorService().submit(new DatabaseCommand() {
                @Override
                public void run() {
                    Database.getCollection(Sessions.class).updateEnd(Database.getServerId(), username, now);
                }
            });

            DBServer server = Database.getCollection(Servers.class).findPublicServer(Config.Bungee.fallbackServer());

            player.kickPlayer("§cThe server you were previously has gone offline, please reconnect.");

        }

        // Clean up server information
        Database.getExecutorService().submit(new DatabaseCommand() {
            @Override
            public void run() {
                Database.getCollection(Servers.class).cleanUp(Database.getServerId(), false);
            }
        });

        // Update server stop time
        Database.getExecutorService().submit(new DatabaseCommand() {
            @Override
            public void run() {
                Database.getCollection(Servers.class).updateStopTime(Database.getServerId(), now);
            }
        });

        MongoExecutorService executorService = Database.getExecutorService();
        boolean done = true;

        try {
            executorService.shutdown();
            done = executorService.awaitTermination(5L, java.util.concurrent.TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            done = false;
        }

        if(!done) {
            Bukkit.getLogger().severe("Failed to empty executor task queue in 5 seconds");
        }

        getPollingTaskManager().stopAll();
    }

    private CommandsManager<CommandSender> commands;

    private void registerCommands() {
        this.commands = new CommandsManager<CommandSender>() {
            @Override
            public boolean hasPermission(CommandSender sender, String perm) {
                return sender instanceof ConsoleCommandSender || sender.hasPermission(perm);
            }
        };

        CommandsManagerRegistration cmdRegister = new CommandsManagerRegistration(this, this.commands);
        cmdRegister.register(ServerCommands.class);
        cmdRegister.register(AdminCommands.class);
        cmdRegister.register(StaffCommands.class);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        try {
            this.commands.execute(cmd.getName(), args, sender, sender);
        } catch (CommandPermissionsException e) {
            sender.sendMessage("§cYou do not have permission to do this.");
        } catch (MissingNestedCommandException e) {
            sender.sendMessage(ChatColor.RED + e.getUsage());
        } catch (CommandUsageException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
            sender.sendMessage(ChatColor.RED + e.getUsage());
        } catch (WrappedCommandException e) {
            if (e.getCause() instanceof NumberFormatException) {
                sender.sendMessage(ChatColor.RED + "Number expected, string received instead.");
            } else {
                sender.sendMessage(ChatColor.RED + "An error has occurred. See console.");
                e.printStackTrace();
            }
        } catch (CommandException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
        }

        return true;
    }

}
