package me.aventium.projectbeam.listeners;

import me.aventium.projectbeam.Beam;
import me.aventium.projectbeam.Database;
import me.aventium.projectbeam.collections.Servers;
import me.aventium.projectbeam.documents.DBServer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;

public class FallbackListener implements Listener {

    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(Beam.getInstance(), new Runnable() {
            public void run() {
                new BukkitRunnable() {
                    public void run() {
                        if(event.getPlayer().isOnline()) {
                            List<DBServer> serverList = Database.getCollection(Servers.class).findPublicServers("lobbies");

                            Random random = new Random();

                            int pos = random.nextInt(serverList.size());

                            Beam.getInstance().getPortal().sendPlayerToServer(event.getPlayer(), serverList.get(pos).getBungeeName());
                        } else {
                            this.cancel();
                        }
                    }
                }.runTaskTimer(Beam.getInstance(), 0L, 15L);
            }
        }, 4L);
    }

}
