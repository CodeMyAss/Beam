package me.aventium.projectbeam.friends;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mongodb.BasicDBObject;
import me.aventium.projectbeam.Database;
import me.aventium.projectbeam.channels.ChannelManager;
import me.aventium.projectbeam.collections.Servers;
import me.aventium.projectbeam.collections.Sessions;
import me.aventium.projectbeam.collections.Users;
import me.aventium.projectbeam.commands.DatabaseCommand;
import me.aventium.projectbeam.documents.DBServer;
import me.aventium.projectbeam.documents.DBSession;
import me.aventium.projectbeam.documents.DBUser;
import org.bukkit.entity.Player;

import java.util.*;

public class ShowOnlineFriendsCommand extends DatabaseCommand {

    private static class Result {
        public DBSession session;
        public DBServer server;
    }

    public static final int RESULTS_PER_PAGE = 8;

    protected final Player player;
    protected final List<String> friendIds;
    protected final int page;

    public ShowOnlineFriendsCommand(Player player, List<String> friendIds, int page) {
        this.player = player;
        this.friendIds = friendIds;
        this.page = page;
    }

    @Override
    public void run() {

        Servers servers = Database.getCollection(Servers.class);
        final DBServer server = Database.getServer();

        final List<Result> results = new ArrayList<>();
        for(String friendId : this.friendIds) {
            DBSession session = Database.getCollection(Sessions.class).getLastSessionById(friendId);

            if(session != null) {
                Result result = new Result();
                result.session = session;
                result.server = server;
                results.add(result);
            }
        }

        ChannelManager.getSyncChannel().queue(new Runnable() {
            @Override
            public void run() {
                final Multimap<DBServer, String> friendNamesByServer = ArrayListMultimap.create();
                final List<Result> offlineFriends = new ArrayList<>();
                Player recipient = ShowOnlineFriendsCommand.this.player;

                for(Result result : results) {
                    if(result.session.getEnd() == null) {
                        String username = Database.getCollection(Users.class).find(new BasicDBObject(DBUser.UUID_FIELD, result.session.getUser())).getUsername();
                        friendNamesByServer.put(result.server, username);
                    } else {
                        offlineFriends.add(result);
                    }
                }

                // sort offline friends by last seen date
                Collections.sort(offlineFriends, new SessionSorter());

                // build output
                List<String> lines = new ArrayList<String>();

                if(friendNamesByServer.containsKey(server)) {
                    lines.add("§7[§a" + server.getName() + "§7] " + Joiner.on("§7, ").join(friendNamesByServer.get(server)));
                }

                for(Map.Entry<DBServer, Collection<String>> entry : friendNamesByServer.asMap().entrySet()) {
                    if(entry.getKey() != server) {
                        lines.add("§7[§a" + entry.getKey().getName() + "§7] " + Joiner.on("§7, ").join(entry.getValue()));
                    }
                }

                for(Result result : offlineFriends) {
                    lines.add(LastSeenFormatter.format(recipient, Database.getCollection(Users.class).find(new BasicDBObject(DBUser.UUID_FIELD, result.session.getUser())).getUsername(), result.session, result.server == null ? "unknown" : result.server.getName(), true));
                }

                final int totalPages = (lines.size() + RESULTS_PER_PAGE - 1) / RESULTS_PER_PAGE;
                final List<String> pageLines = lines.subList(Math.min(lines.size(), RESULTS_PER_PAGE * (ShowOnlineFriendsCommand.this.page - 1)),
                                                             Math.min(lines.size(), RESULTS_PER_PAGE * ShowOnlineFriendsCommand.this.page));

                String title = "§7---§b[§9Friends§2(Page " + ShowOnlineFriendsCommand.this.page + " of " + totalPages + ")§b]§7---";
                recipient.sendMessage(title);
                for(String line : pageLines) {
                   recipient.sendMessage(line);
                }

            }
        });
    }

    private static class SessionSorter implements Comparator<Result> {
        @Override
        public int compare(Result first, Result second) {
            return second.session.getStart().compareTo(first.session.getStart());
        }
    }

}
