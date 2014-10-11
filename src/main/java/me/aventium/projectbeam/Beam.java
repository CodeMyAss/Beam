package me.aventium.projectbeam;

import com.sk89q.bukkit.util.CommandsManagerRegistration;
import com.sk89q.minecraft.util.commands.*;
import me.aventium.projectbeam.channels.ChannelManager;
import me.aventium.projectbeam.collections.*;
import me.aventium.projectbeam.commands.DatabaseCommand;
import me.aventium.projectbeam.commands.admin.*;
import me.aventium.projectbeam.commands.player.AccountCommands;
import me.aventium.projectbeam.commands.player.ServerCommands;
import me.aventium.projectbeam.documents.DBServer;
import me.aventium.projectbeam.friends.Friends;
import me.aventium.projectbeam.listeners.ServerListener;
import me.aventium.projectbeam.listeners.SessionListener;
import me.aventium.projectbeam.tasks.PollingTaskManager;
import me.aventium.projectbeam.tasks.RestartChecker;
import me.aventium.projectbeam.tutorial.TutorialManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.joda.time.Instant;
import org.ocpsoft.prettytime.PrettyTime;
import org.ocpsoft.prettytime.TimeFormat;
import org.ocpsoft.prettytime.TimeUnit;
import org.ocpsoft.prettytime.format.SimpleTimeFormat;
import org.ocpsoft.prettytime.units.JustNow;

import java.util.List;

public class Beam extends JavaPlugin {

    public static final String INTERNAL_ERROR = "§4Sorry, but there was an internal server error.\nWe are working to resolve the issue, please check back soon.";

    public static final PrettyTime TIME_FORMATTER;

    static {
        TIME_FORMATTER = new PrettyTime();
        TIME_FORMATTER.removeUnit(JustNow.class);
        TimeFormat format = new SimpleTimeFormat()
                .setSingularName("a second")
                .setPluralName("seconds")
                .setFutureSuffix("from now")
                .setPastSuffix("ago")
                .setPattern("%u");
        TIME_FORMATTER.registerUnit(new TimeUnit() {
            @Override
            public long getMillisPerUnit() {
                return 1L;
            }

            @Override
            public long getMaxQuantity() {
                return 60000L;
            }
        }, format);
    }


    private static Beam instance;
    private static Thread mainThread;
    private static PollingTaskManager pollingTaskManager = new PollingTaskManager();
    private ServerListener serverListener;
    private SessionListener sessionListener;
    private Portal portal;

    private TutorialManager tutorialManager;

    public TutorialManager getTutorialManager() {
        return tutorialManager;
    }

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

    public Portal getPortal() {
        return portal;
    }

    public void onEnable() {
        instance = this;

        final Instant now = Instant.now();

        mainThread = Thread.currentThread();

        getConfig().options().copyDefaults(true);
        saveConfig();

        // Friends initialization
        // Friends.init(new CommandsManagerRegistration(this, this.commands), pollingTaskManager);

        Database.setUpExecutorService(10);

        DatabaseConfiguration dbConf = new MongoConfigParser(getLogger()).parse(getConfig());
        Database.setConfig(dbConf);

        try {
            Database.reconnect();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        // initialize channels
        ChannelManager.start(this);

        tutorialManager = new TutorialManager(this);

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
        } catch (Throwable t) {
        }

        portal = new Portal(this);

        // register commands
        registerCommands();

        getServer().getPluginManager().registerEvents(new HackerDetection(), this);

        // clean up server info
        Database.getExecutorService().submit(new DatabaseCommand() {
            @Override
            public void run() {
                Database.getCollection(Servers.class).cleanUp(Database.getServerId(), true);
            }
        });

        // update start time
        Database.getExecutorService().submit(new DatabaseCommand() {
            @Override
            public void run() {
                Database.getCollection(Servers.class).updateStartTime(Database.getServerId(), now);
            }
        });

        Database.getCollection(Servers.class).cacheAllServers();

        registerCollections();

        System.out.println("Finished initializing.\nRegistered with following credentials:\nServer Name: " + Database.getServer().getName() +
                "\nBungeeCord Server Name: " + Database.getServer().getBungeeName() +
                "\nFamily: " + Database.getServer().getFamily() +
                "\nVisibility: " + Database.getServer().getVisibility().toString());

        new RestartChecker(this).runTaskTimer(this, 20L, 20L);
    }

    public void onDisable() {
        final Instant now = Instant.now();

        List<DBServer> hubs = Database.getCollection(Servers.class).findPublicServers("hubs");
        hubs.addAll(Database.getCollection(Servers.class).findPublicServers("lobbies"));

        // Update session ends
        for (Player player : Bukkit.getOnlinePlayers()) {
            final String username = player.getName();
            Database.getExecutorService().submit(new DatabaseCommand() {
                @Override
                public void run() {
                    Database.getCollection(Sessions.class).updateEnd(Database.getServerId(), username, now);
                }
            });

            player.sendMessage("§cThe server you were previously on is restarting, please reconnect.");

            portal.sendPlayerToServer(player, hubs.get(0).getBungeeName());
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

        if (!done) {
            Bukkit.getLogger().severe("Failed to empty executor task queue in 5 seconds");
        }

        getPollingTaskManager().stopAll();
    }

    private CommandsManager<CommandSender> commands;

    private void registerCollections() {
        Database.registerCollection(new Sessions());
        Database.registerCollection(new Users());
        Database.registerCollection(new Punishments());
        Database.registerCollection(new Groups());
        Database.registerCollection(new Friendships());
    }

    private void registerCommands() {
        this.commands = new CommandsManager<CommandSender>() {
            @Override
            public boolean hasPermission(CommandSender sender, String perm) {
                return sender instanceof ConsoleCommandSender || sender.hasPermission(perm) || sender.hasPermission("*") || sender.hasPermission("beam.*") || sender.isOp();
            }
        };

        CommandsManagerRegistration cmdRegister = new CommandsManagerRegistration(this, this.commands);

        // Player commands
        cmdRegister.register(ServerCommands.class);
        cmdRegister.register(AccountCommands.class);

        // Admin commands
        cmdRegister.register(BasicAdminCommands.class);
        cmdRegister.register(PermissionCommands.class);
        cmdRegister.register(PunishmentCommands.class);
        cmdRegister.register(ServerAdminCommands.class);
        cmdRegister.register(AccountAdminCommands.class);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        boolean ret = false;
        try {
            this.commands.execute(cmd.getName(), args, sender, sender);
            ret = true;
        } catch (CommandPermissionsException e) {
            sender.sendMessage("§cYou do not have permission to do this.");
            ret = false;
        } catch (MissingNestedCommandException e) {
            sender.sendMessage(ChatColor.RED + e.getUsage());
            ret = false;
        } catch (CommandUsageException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
            sender.sendMessage(ChatColor.RED + e.getUsage());
            ret = false;
        } catch (WrappedCommandException e) {
            if (e.getCause() instanceof NumberFormatException) {
                sender.sendMessage(ChatColor.RED + "Number expected, string received instead.");
            } else {
                sender.sendMessage(ChatColor.RED + "An error has occurred. See console.");
                e.printStackTrace();
            }
            ret = false;
        } catch (CommandException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
            ret = false;
        }
        return ret;
    }

}
