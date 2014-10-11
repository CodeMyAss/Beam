package me.aventium.projectbeam.friends;

import me.aventium.projectbeam.Database;
import me.aventium.projectbeam.channels.ChannelManager;
import me.aventium.projectbeam.collections.Friendships;
import me.aventium.projectbeam.collections.Servers;
import me.aventium.projectbeam.collections.Sessions;
import me.aventium.projectbeam.collections.Users;
import me.aventium.projectbeam.documents.DBSession;
import me.aventium.projectbeam.tasks.PollingTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.joda.time.Duration;
import org.joda.time.Interval;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendLoginTask extends PollingTask {

    @Override
    public void process(Interval interval) {
        Sessions sessions = Database.getCollection(Sessions.class);
        Map<String, String> newLogins = processSessions(sessions.findSessionsStartingBetween(interval));
        Map<String, String> newLogouts = processSessions(sessions.findSessionsEndingBetween(interval));

        ChannelManager.getSyncChannel().queue(new LoginDispatch(newLogins, newLogouts));
    }

    private static Map<String, String> processSessions(List<DBSession> sessions) {
        Map<String, String> result = new HashMap<>();
        for(DBSession session : sessions) {
            if(!session.getServerId().equals(Database.getServerId())) {
                result.put(session.getUser(), Database.getCollection(Servers.class).findServerName(session.getServerId()));
            }
        }
        return result;
    }

    @Override
    public Duration getPollingInterval() {
        return Duration.millis(1000);
    }

    public static final class LoginDispatch implements Runnable {
        private final Map<String, String> newLogins;
        private final Map<String, String> newLogouts;

        public LoginDispatch(Map<String, String> newLogins, Map<String, String> newLogouts) {
            this.newLogins = newLogins;
            this.newLogouts = newLogouts;
        }

        @Override
        public void run() {
            for(Map.Entry<String, String> entry : this.newLogins.entrySet()) {
                String name = entry.getKey();
                String server = entry.getValue();

                String result = "";
                if(this.newLogouts.containsKey(name)) {
                    result = LoginFormatter.formatFriendChange(this.newLogouts.get(name), server, name);
                } else {
                    result = LoginFormatter.formatFriendLogin(server, name);
                }

                broadcastJoin(name, result);
            }

            for(Map.Entry<String, String> entry : this.newLogouts.entrySet()) {
                String name = entry.getKey();
                String server = entry.getValue();

                if(this.newLogins.containsKey(name)) {
                    continue;
                }

                broadcastJoin(name, LoginFormatter.formatFriendLogout(server, name));
            }
        }

        private static void broadcastJoin(String friendName, String message) {
            for(String player : Database.getCollection(Friendships.class).findFriends(Database.getCollection(Users.class).findByName(friendName).getUUID().toString())) {
                if(Bukkit.getPlayer(player) != null) Bukkit.getPlayer(player).sendMessage(message);
            }
        }
    }

}
