package me.aventium.projectbeam.tasks;

import me.aventium.projectbeam.Database;
import me.aventium.projectbeam.collections.Servers;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class RestartChecker extends BukkitRunnable {

    private Plugin plugin;

    public RestartChecker(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if(Database.getCollection(Servers.class).findPublicServer(Database.getServerId()).isRestartNeeded()) {
            Bukkit.broadcastMessage("§c[§4§lServer§c] §4§lServer restarting for update in 30 seconds...");
            plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    for(Player player : Bukkit.getOnlinePlayers()) {
                        player.kickPlayer("§cServer restarting for update. Please rejoin");
                    }

                    Bukkit.shutdown();
                }
            }, 30 * 20L);
            cancel();
        }
    }
}
