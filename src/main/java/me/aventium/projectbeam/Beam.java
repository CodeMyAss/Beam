package me.aventium.projectbeam;

import com.sk89q.bukkit.util.CommandsManagerRegistration;
import com.sk89q.minecraft.util.commands.*;
import me.aventium.projectbeam.channels.ChannelManager;
import me.aventium.projectbeam.collections.*;
import me.aventium.projectbeam.commands.*;
import me.aventium.projectbeam.config.file.FileConfiguration;
import me.aventium.projectbeam.config.file.YamlConfiguration;
import me.aventium.projectbeam.documents.DBServer;
import me.aventium.projectbeam.listeners.FallbackListener;
import me.aventium.projectbeam.listeners.LobbyListener;
import me.aventium.projectbeam.listeners.ServerListener;
import me.aventium.projectbeam.listeners.SessionListener;
import me.aventium.projectbeam.tasks.PollingTaskManager;
import me.aventium.projectbeam.tasks.RestartChecker;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.joda.time.Instant;

import java.io.File;
import java.io.IOException;

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

    public Portal getPortal() {
        return portal;
    }

    public void onEnable() {
        instance = this;

        final Instant now = Instant.now();

        mainThread = Thread.currentThread();

        getConfig().options().copyDefaults(true);
        saveConfig();

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

        if (Database.getServer().getName().toLowerCase().contains("lobby") || Database.getServer().getName().toLowerCase().contains("hub")) {
            getServer().getPluginManager().registerEvents(new LobbyListener(), this);
        }

        Database.getCollection(Servers.class).cacheAllServers();

        Database.registerCollection(new Sessions());
        Database.registerCollection(new Users());
        Database.registerCollection(new Punishments());
        Database.registerCollection(new Groups());

        if (Database.getServer().getName().equals(Config.Bungee.fallbackServer())) {
            getServer().getPluginManager().registerEvents(new FallbackListener(), this);
            System.out.println("Registered as fallback server.");
        }

        /*getServer().getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                for(Player player : Bukkit.getOnlinePlayers()) {
                    DBUser user = Database.getCollection(Users.class).findByName(player.getName());
                    if(user != null) {
                        if(user.getGroups() == null || user.getGroups().size() == 0) {
                            user.addGroup(Database.getCollection(Groups.class).getDefaultGroup());
                            Database.getCollection(Users.class).save(user);
                        } else {
                            for(DBGroup group : user.getGroups()) {
                                PermissionsHandler.removeGroupPermissions(player, group.getName());
                                PermissionsHandler.giveGroupPermissions(player, group);
                            }
                        }
                    }
                }
            }
        }, 20 * 5L, 20 * 5L);*/

        System.out.println("Finished initializing.\nRegistered with following credentials:\nServer Name: " + Database.getServer().getName() +
                "\nBungeeCord Server Name: " + Database.getServer().getBungeeName() +
                "\nFamily: " + Database.getServer().getFamily() +
                "\nVisibility: " + Database.getServer().getVisibility().toString());

        new RestartChecker(this).runTaskTimer(this, 20L, 20L);

        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                if (!getConfig().getBoolean("pex.updated")) {
                    File file = new File(getDataFolder(), "permissions.yml");
                    File newFile = new File(getDataFolder(), "newperms.yml");

                    if (newFile.exists()) {
                        try {
                            newFile.createNewFile();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }

                    try {
                        FileConfiguration old = YamlConfiguration.loadConfiguration(file);
                        FileConfiguration neww = YamlConfiguration.loadConfiguration(newFile);
                        for (String name : old.getConfigurationSection("users").getKeys(false)) {
                            if(name != null && old.get(name) != null) {
                                if (name.contains("-")) {
                                    name = old.getString("users." + name + ".options.name");
                                }
                                neww.set(name, old.getStringList("users." + name + ".group").get(0));
                                try {
                                    neww.save(newFile);
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }

                        getConfig().set("pex.updated", true);
                        saveConfig();
                        for (int i = 0; i < 20; i++) System.out.println("UPDATED PERMISSIONS");
                    } catch(IllegalArgumentException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }, 0L);
    }

    public void onDisable() {
        final Instant now = Instant.now();

        // Update session ends
        for (Player player : Bukkit.getOnlinePlayers()) {
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

        if (!done) {
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
        cmdRegister.register(HackerDetection.class);
        cmdRegister.register(BasicCommands.class);
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
