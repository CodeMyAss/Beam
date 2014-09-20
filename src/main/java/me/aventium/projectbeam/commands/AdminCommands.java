package me.aventium.projectbeam.commands;

import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import me.aventium.projectbeam.Database;
import me.aventium.projectbeam.PermissionsHandler;
import me.aventium.projectbeam.collections.Groups;
import me.aventium.projectbeam.collections.Servers;
import me.aventium.projectbeam.collections.Users;
import me.aventium.projectbeam.documents.DBGroup;
import me.aventium.projectbeam.documents.DBServer;
import me.aventium.projectbeam.documents.DBUser;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class AdminCommands {

    @com.sk89q.minecraft.util.commands.Command(
            aliases = {"setvis"},
            desc = "Change visibility of server you are currently on",
            min = 1,
            usage = "<visibility>"
    )
    @CommandPermissions({"beam.setvisibility","beam.*"})
    public static void setVisibility(final CommandContext args, final CommandSender sender) throws CommandException {

        if (!(sender instanceof Player)) throw new CommandException("You must be a player to use this command!");

        String visGiven = args.getJoinedStrings(0);

        final DBServer.Visibility vis = DBServer.Visibility.fromDatabase(visGiven);

        if (vis == null || vis.equals(DBServer.Visibility.UNKNOWN)) {
            sender.sendMessage("§cInvalid visibility!");
            StringBuilder sb = new StringBuilder();
            sb.append("§aSupported visibilities: ");
            for (DBServer.Visibility visibility : DBServer.Visibility.values()) {
                if (!visibility.equals(DBServer.Visibility.UNKNOWN))
                    sb.append("§l" + visibility.getDatabaseRepresentation() + "§r§a, ");
            }

            sender.sendMessage(sb.toString().trim().substring(0, sb.toString().length() - 2));
            return;
        }

        Database.getExecutorService().submit(new DatabaseCommand() {
            @Override
            public void run() {
                Database.getCollection(Servers.class).setVisibility(Database.getServerId(), vis);
            }
        });

        sender.sendMessage("§a" + Database.getServer().getName() + "'s visibility changed to §l" + vis.getDatabaseRepresentation() + "§r§a!");
    }

    @com.sk89q.minecraft.util.commands.Command(
            aliases = {"setfamily"},
            desc = "Change the family of the server you're currently on",
            min = 1,
            usage = "<family>"
    )
    @CommandPermissions({"beam.setfamily","beam.*"})
    public static void setFamily(final CommandContext args, final CommandSender sender) throws CommandException {

        if (!(sender instanceof Player)) throw new CommandException("You must be a player to use this command!");

        final String family = args.getJoinedStrings(0);

        Database.getExecutorService().submit(new DatabaseCommand() {
            @Override
            public void run() {
                Database.getCollection(Servers.class).setFamily(Database.getServerId(), family);
            }
        });

        sender.sendMessage("§a" + Database.getServer().getName() + "'s family changed to §2" + family + "§a!");
    }

    @com.sk89q.minecraft.util.commands.Command(
            aliases = {"getfamily"},
            desc = "Get the family of a server",
            min = 1,
            usage = "<server>"
    )
    @CommandPermissions({"beam.getfamily","beam.*"})
    public static void getFamily(final CommandContext args, final CommandSender sender) throws CommandException {

        if (!(sender instanceof Player)) throw new CommandException("You must be a player to use this command!");

        DBServer server = Database.getCollection(Servers.class).findPublicServer(args.getJoinedStrings(0));

        if (server == null) {
            sender.sendMessage("§cServer '" + args.getJoinedStrings(0) + "' not found!");
            return;
        }

        final String family = server.getFamily();

        sender.sendMessage("§a" + server.getName() + "'s family: §2" + family);
    }

    @com.sk89q.minecraft.util.commands.Command(
            aliases = {"restartfamily"},
            desc = "Restart a whole family of servers",
            min = 1,
            usage = "<family>"
    )
    @CommandPermissions({"beam.restartfamily","beam.*"})
    public static void restartFamily(final CommandContext args, final CommandSender sender) throws CommandException {

        if (!(sender instanceof Player)) throw new CommandException("You must be a player to use this command!");

        List<DBServer> servers = Database.getCollection(Servers.class).findPublicServers(args.getString(0));

        if(servers != null) {
            sender.sendMessage("§aRestarting " + servers.size() + " public servers in 30 seconds.");
            for(final DBServer server : servers) {
                Database.getExecutorService().submit(new DatabaseCommand() {
                    @Override
                    public void run() {
                        Database.getCollection(Servers.class).queueRestart(server.getServerId());
                    }
                });
            }
        }
    }

    @com.sk89q.minecraft.util.commands.Command(
            aliases = {"restart"},
            desc = "Restart a server",
            min = 0
    )
    @CommandPermissions({"beam.restartserver","beam.*"})
    public static void restartServer(final CommandContext args, final CommandSender sender) throws CommandException {

        if (!(sender instanceof Player)) throw new CommandException("You must be a player to use this command!");

        sender.sendMessage("§aRestarting server in 30 seconds");

        Database.getExecutorService().submit(new DatabaseCommand() {
            @Override
            public void run() {
                Database.getCollection(Servers.class).queueRestart(Database.getServerId());
            }
        });

    }

    @com.sk89q.minecraft.util.commands.Command(
            aliases = {"creategroup"},
            desc = "Create a new network permissions group",
            min = 1,
            usage = "<groupName>"
    )
    @CommandPermissions({"beam.permissions.creategroup","beat.permissions.*","beam.*"})
    public static void createGroup(final CommandContext args, final CommandSender sender) throws CommandException {
        if (!sender.hasPermission("beam.creategroup") && !sender.hasPermission("beam.*")) throw new CommandPermissionsException();

        if (Database.getCollection(Groups.class).findGroup(args.getString(0), null) != null) {
            sender.sendMessage("§cA group with that name already exists!");
            return;
        }

        Database.getExecutorService().submit(new DatabaseCommand() {
            @Override
            public void run() {
                DBGroup group = new DBGroup();
                group.setName(args.getString(0));
                Database.getCollection(Groups.class).save(group);
            }
        });

        sender.sendMessage("§aGroup created!");
    }

    @com.sk89q.minecraft.util.commands.Command(
            aliases = {"setgroupprefix", "setgprefix"},
            desc = "Change a group's prefix",
            min = 1,
            max = 2,
            usage = "<groupName> [prefix]"
    )
    @CommandPermissions({"beam.permissions.setgroupprefix","beat.permissions.*","beam.*"})
    public static void setGroupPrefix(final CommandContext args, final CommandSender sender) throws CommandException {

        if (Database.getCollection(Groups.class).findGroup(args.getString(0), null) == null) {
            sender.sendMessage("§cA group with that name does not exist!");
            return;
        }

        final DBGroup group = Database.getCollection(Groups.class).findGroup(args.getString(0), null);

        if (args.argsLength() == 1) {
            Database.getExecutorService().submit(new DatabaseCommand() {
                @Override
                public void run() {
                    group.setPrefix("&f");
                    Database.getCollection(Groups.class).save(group);
                }
            });

            sender.sendMessage("§2" + group.getName() + " §a's prefix set to '§r&f§a'.");
        } else if (args.argsLength() == 2) {
            Database.getExecutorService().submit(new DatabaseCommand() {
                @Override
                public void run() {
                    group.setPrefix(args.getString(1));
                    Database.getCollection(Groups.class).save(group);
                }
            });

            sender.sendMessage("§2" + group.getName() + "§a's prefix set to '§r" + args.getString(1) + "§a'.");
        }
    }

    @com.sk89q.minecraft.util.commands.Command(
            aliases = {"setdefaultgroup", "setdgroup"},
            desc = "Set the default group",
            min = 1,
            usage = "<groupName>"
    )
    @CommandPermissions({"beam.permissions.setdefaultgroup","beat.permissions.*","beam.*"})
    public static void setDefaultGroup(final CommandContext args, final CommandSender sender) throws CommandException {

        if (Database.getCollection(Groups.class).findGroup(args.getString(0), null) == null) {
            sender.sendMessage("§cA group with that name does not exist!");
            return;
        }

        for (final DBGroup group : Database.getCollection(Groups.class).findAllGroups()) {
            Database.getExecutorService().submit(new DatabaseCommand() {
                @Override
                public void run() {
                    group.setDefaultGroup(false);
                    Database.getCollection(Groups.class).save(group);
                }
            });
        }

        final DBGroup group = Database.getCollection(Groups.class).findGroup(args.getString(0), null);

        Database.getExecutorService().submit(new DatabaseCommand() {
            @Override
            public void run() {
                group.setDefaultGroup(true);
                Database.getCollection(Groups.class).save(group);
            }
        });
        sender.sendMessage("§2" + group.getName() + " §awas set to the default group!");
    }

    @com.sk89q.minecraft.util.commands.Command(
            aliases = {"getgroup"},
            desc = "Get a player's group",
            min = 1,
            usage = "<player>"
    )
    @CommandPermissions({"beam.permissions.checkplayergroup","beat.permissions.*","beam.*"})
    public static void getGroup(final CommandContext args, final CommandSender sender) throws CommandException {

        Database.getExecutorService().submit(new DatabaseCommand() {
            @Override
            public void run() {
                DBUser user = Database.getCollection(Users.class).findByName(args.getString(0));
                sender.sendMessage("§2" + user.getUsername() + "§a's group: §2" + user.getGroup().getName() + "§a.");
            }
        });
    }

    @com.sk89q.minecraft.util.commands.Command(
            aliases = {"setgroup"},
            desc = "Set a player's group",
            min = 2,
            usage = "<player> <groupName>"
    )
    @CommandPermissions({"beam.permissions.setplayergroup","beam.permissions.*","beam.*"})
    public static void setGroup(final CommandContext args, final CommandSender sender) throws CommandException {

        final DBUser user = Database.getCollection(Users.class).findByName(args.getString(0));
        final DBGroup group = Database.getCollection(Groups.class).findGroup(args.getString(1), null);

        if (user == null) {
            sender.sendMessage("§cPlayer not found!");
            return;
        }

        if (group == null) {
            sender.sendMessage("§cGroup not found!");
            return;
        }

        final String oldGroup = user.getGroup().getName();

        Database.getExecutorService().submit(new DatabaseCommand() {
            @Override
            public void run() {
                user.setGroup(group);
                Database.getCollection(Users.class).save(user);
                if (Bukkit.getPlayerExact(user.getUsername()) != null) {
                    PermissionsHandler.removeGroupPermissions(Bukkit.getPlayerExact(user.getUsername()), oldGroup);
                    PermissionsHandler.giveGroupPermissions(Bukkit.getPlayerExact(user.getUsername()), group);
                }
                sender.sendMessage("§2" + user.getUsername() + "§a's group set to: §2" + user.getGroup().getName() + "§a.");
            }
        });
    }

    @com.sk89q.minecraft.util.commands.Command(
            aliases = {"addpermission", "addperm"},
            desc = "Add a permission to a group",
            min = 2,
            usage = "<groupName> <permission>"
    )
    @CommandPermissions({"beam.permissions.addpermission","beat.permissions.*","beam.*"})
    public static void addPermission(final CommandContext args, final CommandSender sender) throws CommandException {
        if (!sender.hasPermission("beam.addpermission") && !sender.hasPermission("beam.*")) throw new CommandPermissionsException();

        if (Database.getCollection(Groups.class).findGroup(args.getString(0), null) == null) {
            sender.sendMessage("§cA group with that name does not exist!");
            return;
        }

        final DBGroup group = Database.getCollection(Groups.class).findGroup(args.getString(0), null);

        final String permission = args.getString(1);

        Database.getExecutorService().submit(new DatabaseCommand() {
            @Override
            public void run() {
                Map<String, Boolean> perms = group.getPermissions();
                perms.put(permission, true);
                group.setPermissions(perms);
                Database.getCollection(Groups.class).save(group);
            }
        });

        sender.sendMessage("§aPermission '§2" + permission + "§a' added to group §2" + group.getName() + "§a!");
    }

    @com.sk89q.minecraft.util.commands.Command(
            aliases = {"removepermission", "removeperm"},
            desc = "Add a permission to a group",
            min = 2,
            usage = "<groupName> <permission>"
    )
    @CommandPermissions({"beam.permissions.removepermission","beat.permissions.*","beam.*"})
    public static void removePermission(final CommandContext args, final CommandSender sender) throws CommandException {

        if (!sender.hasPermission("beam.removepermission") && !sender.hasPermission("beam.*")) throw new CommandPermissionsException();

        if (Database.getCollection(Groups.class).findGroup(args.getString(0), null) == null) {
            sender.sendMessage("§cA group with that name does not exist!");
            return;
        }

        final DBGroup group = Database.getCollection(Groups.class).findGroup(args.getString(0), null);

        final String permission = args.getString(1);

        Database.getExecutorService().submit(new DatabaseCommand() {
            @Override
            public void run() {
                if(!group.getPermissions().containsKey(permission)) {
                    return;
                }
                Map<String, Boolean> perms = group.getPermissions();
                perms.remove(permission);
                group.setPermissions(perms);
                Database.getCollection(Groups.class).save(group);
            }
        });

        sender.sendMessage("§aPermission '§2" + permission + "§a' removed from group §2" + group.getName() + "§a!");
    }

    @com.sk89q.minecraft.util.commands.Command(
            aliases = {"getpermissions", "getperms"},
            desc = "Retrieve the list of a permissions each group has",
            min = 1,
            usage = "<groupName>"
    )
    @CommandPermissions({"beam.permissions.getpermissions","beat.permissions.*","beam.*"})
    public static void getPermissions(final CommandContext args, final CommandSender sender) throws CommandException {
        if (!sender.hasPermission("beam.getpermissions") && !sender.hasPermission("beam.*")) throw new CommandPermissionsException();

        if (Database.getCollection(Groups.class).findGroup(args.getString(0), null) == null) {
            sender.sendMessage("§cA group with that name does not exist!");
            return;
        }

        final DBGroup group = Database.getCollection(Groups.class).findGroup(args.getString(0), null);

        StringBuilder stringBuilder = new StringBuilder();
        for(String permission : group.getPermissions().keySet()) {
            stringBuilder.append("§a" + permission + "§7, ");
        }

        sender.sendMessage("§2" + group.getName() + " §ahas the following permissions:");
        sender.sendMessage(stringBuilder.toString().substring(0, (stringBuilder.toString().length() == 0 ? 0 : stringBuilder.toString().length() - 2)));
    }
}
