package me.aventium.projectbeam.listeners;

import com.mongodb.MongoException;
import me.aventium.projectbeam.Beam;
import me.aventium.projectbeam.Database;
import me.aventium.projectbeam.collections.Punishments;
import me.aventium.projectbeam.documents.DBPunishment;
import me.aventium.projectbeam.utils.Messages;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PunishmentListener implements Listener {

    @EventHandler(priority = EventPriority.LOW)
    public void checkPunishments(final AsyncPlayerPreLoginEvent event) {

        if(!Database.isConnected()) {
            event.disallow(PlayerPreLoginEvent.Result.KICK_OTHER, Beam.INTERNAL_ERROR);
            return;
        }

        if(event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            return;
        }

        Punishments punishments = Database.getCollection(Punishments.class);

        // check for bans
        List<DBPunishment> activePunishments;
        try {
            activePunishments = punishments.getActivePunishments(event.getUniqueId());
        } catch (MongoException.Network e) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Beam.INTERNAL_ERROR);
            return;
        }

        Date longest = new Date();
        String reason = "";

        ArrayList<DBPunishment> remove = new ArrayList<>();

        for(DBPunishment p : activePunishments) {
            if(!(p.getType().equals(DBPunishment.Type.BAN)) && !(p.getType().equals(DBPunishment.Type.BLACKLIST))) {
                continue;
            }

            if(p.getType().equals(DBPunishment.Type.BLACKLIST) && p.isActive()) {
                reason = p.getReason();
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, Messages.generateBlacklistMessage(reason));
                return;
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
            activePunishments = punishments.getActivePunishments(event.getPlayer().getUniqueId());
        } catch (MongoException.Network e) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Beam.INTERNAL_ERROR);
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

}
