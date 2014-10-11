package me.aventium.projectbeam.listeners;

import com.mongodb.MongoException;
import me.aventium.projectbeam.Beam;
import me.aventium.projectbeam.Database;
import me.aventium.projectbeam.utils.Messages;
import me.aventium.projectbeam.collections.Punishments;
import me.aventium.projectbeam.collections.Sessions;
import me.aventium.projectbeam.collections.Users;
import me.aventium.projectbeam.commands.DatabaseCommand;
import me.aventium.projectbeam.commands.SaveCommand;
import me.aventium.projectbeam.documents.DBPunishment;
import me.aventium.projectbeam.documents.DBSession;
import me.aventium.projectbeam.events.AsyncUserPreLoginEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.joda.time.Instant;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class SessionListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void wrapAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        Bukkit.getPluginManager().callEvent(new AsyncUserPreLoginEvent(event));
    }

    // Update database
    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(final PlayerJoinEvent event) {
        final String name = event.getPlayer().getName();
        final String ip = event.getPlayer().getAddress().getAddress().getHostAddress();
        final UUID uuid = event.getPlayer().getUniqueId();

        Database.getExecutorService().submit(new DatabaseCommand() {
            @Override
            public void run() {
                Database.getCollection(Users.class).handleLogin(name, uuid, ip);
            }
        });

        event.setJoinMessage(null);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void handleJoin(PlayerJoinEvent event) {
        startSession(event.getPlayer());
    }

    public void startSession(Player player) {
        DBSession session = new DBSession();
        session.setUser(player.getUniqueId().toString());
        session.setServerId(Database.getServerId());
        session.setStart(new Date());
        session.setIPAddress(player.getAddress().getAddress().getHostAddress());

        Database.getExecutorService().submit(new SaveCommand(Database.getCollection(Sessions.class), session));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void handleQuit(PlayerQuitEvent event) {
        endSession(event.getPlayer());
        event.setQuitMessage(null);
    }

    public void endSession(Player player) {
        final String username = player.getUniqueId().toString();
        final Instant end = Instant.now();

        Database.getExecutorService().submit(new DatabaseCommand() {
            @Override
            public void run() {
                Database.getCollection(Sessions.class).updateEnd(Database.getServerId(), username, end);
            }
        });
    }

}
