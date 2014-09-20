package me.aventium.projectbeam.listeners;

import com.mongodb.MongoException;
import me.aventium.projectbeam.Database;
import me.aventium.projectbeam.Messages;
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
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
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

    @EventHandler(priority = EventPriority.LOW)
    public void checkPunishments(final AsyncPlayerPreLoginEvent event) {

        if(event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            return;
        }

        Punishments punishments = Database.getCollection(Punishments.class);

        // check for bans
        List<DBPunishment> activePunishments;
        try {
            activePunishments = punishments.getActivePunishments(event.getName());
        } catch (MongoException.Network e) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "§4There was in internal error on our end, we're working to fix it, please check in later.");
            return;
        }

        Date longest = new Date();
        String reason = "";

        ArrayList<DBPunishment> remove = new ArrayList<>();

        for(DBPunishment p : activePunishments) {
            if(p.getType() != DBPunishment.Type.BAN) {
                continue;
            }

            if(p.getExpiry() == null) {
                longest = null;
                reason = p.getReason();
                break;
            }

            if(p.getExpiry().compareTo(longest) > 0) {
                longest = p.getExpiry();
                reason = p.getReason();
            }

            if(p.getExpiry().compareTo(longest) <= 0) {
                remove.add(p);
                break;
            }
        }

        for(DBPunishment toRemove : remove) {
            toRemove.setActive(false);
            punishments.save(toRemove);
        }

        if(longest == null) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, Messages.generateBanMessage(reason, null));
        } else if(longest.compareTo(new Date()) > 0) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, Messages.generateBanMessage(reason, longest));
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Punishments punishments = Database.getCollection(Punishments.class);

        // check for bans
        List<DBPunishment> activePunishments;
        try {
            activePunishments = punishments.getActivePunishments(event.getPlayer().getName());
        } catch (MongoException.Network e) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§4There was in internal error on our end, we're working to fix it, please check in later.");
            return;
        }

        Date longest = new Date();
        String reason = "";

        ArrayList<DBPunishment> remove = new ArrayList<>();

        for(DBPunishment p : activePunishments) {
            if(p.getType() != DBPunishment.Type.MUTE) {
                continue;
            }

            if(p.getExpiry() == null) {
                longest = null;
                reason = p.getReason();
                break;
            }

            if(p.getExpiry().compareTo(longest) > 0) {
                longest = p.getExpiry();
                reason = p.getReason();
            }

            if(p.getExpiry().compareTo(longest) <= 0) {
                remove.add(p);
                break;
            }
        }

        for(DBPunishment toRemove : remove) {
            toRemove.setActive(false);
            punishments.save(toRemove);
        }

        if(longest == null) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Messages.generateMuteMessage(reason, null));
        } else if(longest.compareTo(new Date()) > 0) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Messages.generateMuteMessage(reason, longest));
        }
    }

    // Update database
    @EventHandler(priority = EventPriority.HIGH)
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

    @EventHandler(priority = EventPriority.MONITOR)
    public void handleJoin(PlayerJoinEvent event) {
        startSession(event.getPlayer());
    }

    public void startSession(Player player) {
        DBSession session = new DBSession();
        session.setUser(player.getName());
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
        final String username = player.getName();
        final Instant end = Instant.now();

        Database.getExecutorService().submit(new DatabaseCommand() {
            @Override
            public void run() {
                Database.getCollection(Sessions.class).updateEnd(Database.getServerId(), username, end);
            }
        });
    }

}
