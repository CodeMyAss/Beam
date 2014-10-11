package me.aventium.projectbeam.commands.admin;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import me.aventium.projectbeam.Database;
import me.aventium.projectbeam.collections.Servers;
import me.aventium.projectbeam.commands.DatabaseCommand;
import me.aventium.projectbeam.documents.DBServer;
import org.bson.types.ObjectId;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ServerAdminCommands {

    @com.sk89q.minecraft.util.commands.Command(
            aliases = {"createserver"},
            desc = "Create a new server in the database",
            min = 4,
            max = 4,
            usage = "<name> <bungeecordname> <visibility> <family>"
    )
    public static void createServer(final CommandContext args, final CommandSender sender) throws CommandException {

        if (!(sender instanceof Player)) throw new CommandException("You must be a player to use this command!");

        if(!sender.isOp()) throw new CommandPermissionsException();

        String name = args.getString(0);
        String bungee_name = args.getString(1);
        DBServer.Visibility vis = DBServer.Visibility.fromDatabase(args.getString(2).toLowerCase());

        if(vis == null || vis.equals(DBServer.Visibility.UNKNOWN)) {
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

        String family = args.getString(3);

        DBObject object = new BasicDBObject(DBServer.NAME_FIELD, name);
        object.put(DBServer.BUNGEE_NAME_FIELD, bungee_name);
        object.put(DBServer.VISIBILITY_FIELD, vis.getDatabaseRepresentation());
        object.put(DBServer.SERVER_FAMILY_FIELD, family);

        final DBServer server = new DBServer(object);
        server.setId(ObjectId.get());

        Database.getExecutorService().submit(new DatabaseCommand() {
            @Override
            public void run() {
                Database.getCollection(Servers.class).save(server);
            }
        });

        sender.sendMessage("§aCreated new server. Printing ObjectId to console and sending you it.");
        sender.sendMessage("§aServer ObjectId: §2" + server.getId().toString());
        System.out.println("New ObjectId for server: " + server.getId().toString());
    }

    @com.sk89q.minecraft.util.commands.Command(
            aliases = {"setvis"},
            desc = "Change visibility of server you are currently on",
            min = 1,
            usage = "<visibility>"
    )
    @CommandPermissions({"beam.setvisibility"})
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
    @CommandPermissions({"beam.setfamily"})
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
    @CommandPermissions({"beam.getfamily"})
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
    @CommandPermissions({"beam.restartfamily"})
    public static void restartFamily(final CommandContext args, final CommandSender sender) throws CommandException {

        if (!(sender instanceof Player)) throw new CommandException("You must be a player to use this command!");

        final List<DBServer> servers = Database.getCollection(Servers.class).findPublicServers(args.getString(0));

        if(servers != null) {
            sender.sendMessage("§aRestarting " + servers.size() + " public servers in 30 seconds.");
            Database.getExecutorService().submit(new DatabaseCommand() {
                @Override
                public void run() {
                    for(final DBServer server : servers) {
                        Database.getCollection(Servers.class).queueRestart(server.getServerId());
                    }
                }
            });
        }
    }

    @com.sk89q.minecraft.util.commands.Command(
            aliases = {"restart"},
            desc = "Restart a server",
            min = 0
    )
    @CommandPermissions("beam.restart")
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

}
