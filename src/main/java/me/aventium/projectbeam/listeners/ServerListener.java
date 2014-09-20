package me.aventium.projectbeam.listeners;

import me.aventium.projectbeam.Database;
import me.aventium.projectbeam.collections.Servers;
import me.aventium.projectbeam.commands.DatabaseCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ServerListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void handleJoin(PlayerJoinEvent event) {
        addOnlinePlayer(event.getPlayer());
    }

    public void addOnlinePlayer(final Player player) {

        Database.getExecutorService().submit(new DatabaseCommand() {
            @Override
            public void run() {
                Database.getCollection(Servers.class).addOnlinePlayer(player.getName(), player.getName());
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void handleQuit(PlayerQuitEvent event) {
        removeOnlinePlayer(event.getPlayer());
    }

    public void removeOnlinePlayer(final Player player) {

        Database.getExecutorService().submit(new DatabaseCommand() {
            @Override
            public void run() {
                Database.getCollection(Servers.class).removeOnlinePlayer(player.getName(), player.getName());
            }
        });
    }

}
