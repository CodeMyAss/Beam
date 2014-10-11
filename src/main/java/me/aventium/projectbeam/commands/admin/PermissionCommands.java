package me.aventium.projectbeam.commands.admin;

import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import me.aventium.projectbeam.Database;
import me.aventium.projectbeam.PermissionsHandler;
import me.aventium.projectbeam.collections.Groups;
import me.aventium.projectbeam.collections.Users;
import me.aventium.projectbeam.commands.DatabaseCommand;
import me.aventium.projectbeam.documents.DBGroup;
import me.aventium.projectbeam.documents.DBUser;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Map;

public class PermissionCommands {

    @com.sk89q.minecraft.util.commands.Command(
            aliases = {"creategroup"},
            desc = "Create a new network permissions group",
            min = 1,
            usage = "<groupName>"
    )
    @CommandPermissions({"beam.permissions.creategroup"})
    public static void createGroup(final CommandContext args, final CommandSender sender) throws CommandException {
        if (!sender.hasPermission("beam.creategroup") && !sender.hasPermission("beam.*")) throw new CommandPermissionsException();

        if(!args.getString(1).equalsIgnoreCase("server") && !args.getString(1).equalsIgnoreCase("network")) {
            sender.sendMessage("§cInvalid group type! Supported types are: network, server");
            return;
        }

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
            min = 2,
            usage = "<groupName> <prefix>"
    )
    @CommandPermissions({"beam.permissions.setgroupprefix"})
    public static void setGroupPrefix(final CommandContext args, final CommandSender sender) throws CommandException {

        if (Database.getCollection(Groups.class).findGroup(args.getString(0), null) == null) {
            sender.sendMessage("§cA group with that name does not exist!");
            return;
        }

        final DBGroup group = Database.getCollection(Groups.class).findGroup(args.getString(0), null);

        if (args.argsLength() == 2) {
            Database.getExecutorService().submit(new DatabaseCommand() {
                @Override
                public void run() {
                    group.setPrefix("&f");
                    Database.getCollection(Groups.class).save(group);
                }
            });

            sender.sendMessage("§2" + group.getName() + " §a's prefix set to '§r&f§a'.");
        } else if (args.argsLength() == 3) {
            Database.getExecutorService().submit(new DatabaseCommand() {
                @Override
                public void run() {
                    group.setPrefix(args.getString(2));
                    Database.getCollection(Groups.class).save(group);
                }
            });

            sender.sendMessage("§2" + group.getName() + "§a's prefix set to '§r" + args.getString(2) + "§a'.");
        }
    }

    @com.sk89q.minecraft.util.commands.Command(
            aliases = {"setdefaultgroup", "setdgroup"},
            desc = "Set the default group",
            min = 1,
            usage = "<groupName>"
    )
    @CommandPermissions({"beam.permissions.setdefaultgroup"})
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
    @CommandPermissions({"beam.permissions.checkplayergroup"})
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
    @CommandPermissions({"beam.permissions.setplayergroup"})
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

        Database.getExecutorService().submit(new DatabaseCommand() {
            @Override
            public void run() {

                DBGroup oldGroup = user.getGroup();
                user.setGroup(group);
                Database.getCollection(Users.class).save(user);
                if (Bukkit.getPlayerExact(user.getUsername()) != null) {
                    if(oldGroup != null) PermissionsHandler.removeGroupPermissions(Bukkit.getPlayerExact(user.getUsername()), oldGroup.getName());
                    PermissionsHandler.givePermissions(Bukkit.getPlayerExact(user.getUsername()), user);
                }
                sender.sendMessage("§2" + user.getUsername() + "§a's group set to: §2" + group.getName() + "§a.");
            }
        });
    }

    @com.sk89q.minecraft.util.commands.Command(
            aliases = {"addpermission", "addperm"},
            desc = "Add a permission to a group",
            min = 2,
            usage = "<groupName> <permission>"
    )
    @CommandPermissions({"beam.permissions.addpermission"})
    public static void addPermission(final CommandContext args, final CommandSender sender) throws CommandException {

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
            aliases = {"givepermission", "giveperm"},
            desc = "Add a permission to a user",
            min = 2,
            usage = "<user> <permission>"
    )
    @CommandPermissions({"beam.permissions.givepermission"})
    public static void givePermission(final CommandContext args, final CommandSender sender) throws CommandException {

        if (Database.getCollection(Users.class).findByName(args.getString(0)) == null) {
            sender.sendMessage("§cA user with that name does not exist!");
            return;
        }

        final DBUser user = Database.getCollection(Users.class).findByName(args.getString(0));

        final String permission = args.getString(1);

        Database.getExecutorService().submit(new DatabaseCommand() {
            @Override
            public void run() {
                Map<String, Boolean> perms = user.getPermissions();
                perms.put(permission, true);
                user.setPermissions(perms);
                Database.getCollection(Users.class).save(user);
            }
        });

        sender.sendMessage("§aPermission '§2" + permission + "§a' added to user §2" + user.getUsername() + "§a!");
    }

    @com.sk89q.minecraft.util.commands.Command(
            aliases = {"deletepermission", "delperm"},
            desc = "Delete a permission from a user",
            min = 2,
            usage = "<user> <permission>"
    )
    @CommandPermissions({"beam.permissions.deletepermission"})
    public static void deletePermission(final CommandContext args, final CommandSender sender) throws CommandException {

        if (Database.getCollection(Users.class).findByName(args.getString(0)) == null) {
            sender.sendMessage("§cA user with that name does not exist!");
            return;
        }

        final DBUser user = Database.getCollection(Users.class).findByName(args.getString(0));

        final String permission = args.getString(2);

        Database.getExecutorService().submit(new DatabaseCommand() {
            @Override
            public void run() {
                Map<String, Boolean> perms = user.getPermissions();
                perms.remove(args.getString(1));
                user.setPermissions(perms);
                Database.getCollection(Users.class).save(user);
            }
        });

        sender.sendMessage("§aPermission '§2" + permission + "§a' added to user §2" + user.getUsername() + "§a!");
    }

    @com.sk89q.minecraft.util.commands.Command(
            aliases = {"addtoall"},
            desc = "Add a permission to all group",
            min = 1,
            usage = "<permission>"
    )
    @CommandPermissions({"beam.permissions.addpermissiontoall"})
    public static void addPermissionToAll(final CommandContext args, final CommandSender sender) throws CommandException {

        for(DBGroup gr : Database.getCollection(Groups.class).findAllGroups()) {
            if(gr.getFamily().equalsIgnoreCase(Database.getServer().getFamily())) {
                boolean add = !args.getString(0).startsWith("-");
                Map<String, Boolean> perm = gr.getPermissions();
                perm.put(args.getString(0), add);
                gr.setPermissions(perm);
                Database.getCollection(Groups.class).save(gr);
            }
        }

        sender.sendMessage("§aPermission added to all groups!");
    }

    @com.sk89q.minecraft.util.commands.Command(
            aliases = {"removepermission", "removeperm"},
            desc = "Add a permission to a group",
            min = 2,
            usage = "<groupName> <permission>"
    )
    @CommandPermissions({"beam.permissions.removepermission"})
    public static void removePermission(final CommandContext args, final CommandSender sender) throws CommandException {

        if (Database.getCollection(Groups.class).findGroup(args.getString(0), null) == null) {
            sender.sendMessage("§cA group with that name already exists!");
            return;
        }

        final DBGroup group = Database.getCollection(Groups.class).findGroup(args.getString(0), null);

        final String permission = args.getString(2);

        Database.getExecutorService().submit(new DatabaseCommand() {
            @Override
            public void run() {
                if(!group.getPermissions().containsKey(permission)) {
                    sender.sendMessage("§cThat group does not have that permission!");
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
    @CommandPermissions({"beam.permissions.getpermissions"})
    public static void getPermissions(final CommandContext args, final CommandSender sender) throws CommandException {

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
