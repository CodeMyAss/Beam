package me.aventium.projectbeam.commands;

import com.google.common.collect.Lists;
import com.sk89q.bukkit.util.BukkitWrappedCommandSender;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import me.aventium.projectbeam.Beam;
import me.aventium.projectbeam.Database;
import me.aventium.projectbeam.channels.ChannelManager;
import me.aventium.projectbeam.collections.Servers;
import me.aventium.projectbeam.documents.DBServer;
import me.aventium.projectbeam.utils.PrettyPaginatedResult;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ServerCommands {

    @Command(
            aliases = {"server"},
            desc = "Shows what server you are on",
            min = 0
    )
    public static void server(final CommandContext args, final CommandSender sender) throws CommandException {
        Database.getExecutorService().submit(new DatabaseCommand() {
            @Override
            public void run() {
                if (args.argsLength() == 0) {
                    String serverName = Database.getCollection(Servers.class).findServerName(Database.getServerId());
                    sender.sendMessage("§aYou are currently connected to §2" + serverName);

                    if (sender instanceof Player) {
                        sender.sendMessage("§aTo connect to another server, type §2/server <name>");
                        sender.sendMessage("§aTo view all servers, type §2/servers");
                    }
                } else if (args.argsLength() == 1) {
                    DBServer server = null;
                    String serverName = "Lobby";
                    String arg = args.getJoinedStrings(0);

                    server = Database.getCollection(Servers.class).findPublicServer(arg);

                    if (server == null) {
                        sender.sendMessage("§cCould not find any server by the name of §4" + args.getString(0) + "§c!");
                        return;
                    }

                    serverName = server.getName();

                    if(!server.isOnline()) {
                        sender.sendMessage("§cThat server is currently offline!");
                        return;
                    }

                    if (sender instanceof Player) {
                        sender.sendMessage("§aTeleporting you to §2" + server.getName() + "§a!");
                        Beam.getInstance().getPortal().sendPlayerToServer((Player) sender, server.getBungeeName());
                    } else {
                    }
                }
            }
        });
    }

    @Command(
            aliases = {"lobby", "hub"},
            desc = "Connect to the lobby.",
            min = 0
    )
    public static void lobby(final CommandContext args, final CommandSender sender) throws CommandException {
        if(Database.getServer().getFamily() == null) {
            Database.getExecutorService().submit(new DatabaseCommand() {
                @Override
                public void run() {
                    Database.getCollection(Servers.class).setFamily(Database.getServerId(), "lobbies");
                }
            });
            sender.sendMessage("§cYou are already connected to the lobby!");
            return;
        } else {
            if(Database.getServer().getFamily().equalsIgnoreCase("lobbies")) {
                sender.sendMessage("§cYou are already connected to the lobby!");
                return;
            }
        }

        Database.getExecutorService().submit(new DatabaseCommand() {
            @Override
            public void run() {
                List<DBServer> lobbies = Database.getCollection(Servers.class).findPublicServers("lobbies");
                DBServer lowest = lobbies.get(0);
                if(lobbies.size() > 1) {
                    for(DBServer server : lobbies) {
                        if(server.getOnlinePlayers() == null || server.getOnlinePlayers().size() == 0 || server.getOnlinePlayers().size() < lowest.getOnlinePlayers().size() || lowest == null) lowest = server;
                    }
                }

                sender.sendMessage("§aTeleporting you to §2" + lowest.getName() + "§a!");
                Beam.getInstance().getPortal().sendPlayerToServer((Player) sender, lowest.getBungeeName());
            }
        });
    }

    @Command(
            aliases = {"servers"},
            desc = "Show a listing of all servers on the network",
            usage = "[page]",
            min = 0,
            max = 1
    )
    public static void servers(final CommandContext args, final CommandSender sender) throws CommandException {
        Database.getExecutorService().submit(new DatabaseCommand() {
            @Override
            public void run() {
                List<DBServer> servers = Database.getCollection(Servers.class).findPublicServers();
                if(servers == null) servers =  Lists.newArrayList();
                ChannelManager.getSyncChannel().queue(new SendServersInfo(sender, servers, args));
            }
        });
    }

    @Command(
            aliases = {"authors", "developers", "coders", "programmers"},
            desc = "Show a list of the developers for the network.",
            usage = "",
            min = 0
    )
    public static void authors(final CommandContext args, final CommandSender sender) throws CommandException {
        sender.sendMessage("§cDevelopers: §4Aventium§c, §4rbrick§c, §4Young_Explicit§c.");
    }

    public static class SendServersInfo implements Runnable {
        protected final CommandSender recipient;
        protected final List<DBServer> servers;
        protected final CommandContext args;

        public SendServersInfo(CommandSender recipient, List<DBServer> servers, CommandContext args) {
            this.recipient = recipient;
            this.servers = servers;
            this.args = args;
        }

        @Override
        public void run() {
            final Map<String, Integer> playerMapping = new HashMap<String, Integer>();
            final Map<String, DBServer> serverMapping = new LinkedHashMap<String, DBServer>();
            final Map<String, DBServer> offline = new LinkedHashMap();

            for (DBServer server : this.servers) {
                if(server != null) {
                    if(server.isOnline() && !server.getVisibility().equals(DBServer.Visibility.UNLISTED)) serverMapping.put(server.getName(), server);
                    else offline.put(server.getName(), server);

                    Integer count = playerMapping.get(server.getName());
                    if (count != null) {
                        playerMapping.put(server.getName(), count + server.getOnlinePlayers().size());
                    } else {
                        playerMapping.put(server.getName(), server.getOnlinePlayers().size());
                    }
                }
            }

            serverMapping.putAll(offline);
            offline.clear();

            try {
                new PrettyPaginatedResult<Map.Entry<String, DBServer>>("Servers") {
                    @Override
                    public String format(Map.Entry<String, DBServer> entry, int index) {
                        StringBuilder message = new StringBuilder();
                        String name = entry.getKey();
                        DBServer server = entry.getValue();

                        message.append("§a").append(server.getName()).append("§7").append(" - ");
                        message.append(server.isOnline() ? "§2" : "§4").append(server.isOnline() ? "Online" : "Offline").append(" §7[");
                        message.append("§c").append(playerMapping.get(name)).append(" §7" + (playerMapping.get(name) == 1 ? "player" : "players") + "]");

                        return message.toString();
                    }
                }.display(new BukkitWrappedCommandSender(this.recipient), serverMapping.entrySet(), this.args.getInteger(0, 1));
            } catch (NumberFormatException | CommandException e) {
                this.recipient.sendMessage("§cPage doesn't exist!");
            }
        }
    }
}
