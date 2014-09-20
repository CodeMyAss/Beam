package me.aventium.projectbeam.tasks;

import me.aventium.projectbeam.Database;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class RestartChecker extends BukkitRunnable {

    private Plugin plugin;

    public RestartChecker(Plugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 20L, 20L);
    }

    @Override
    public void run() {
        if(Database.getServer().isRestartNeeded()) {
            Bukkit.broadcastMessage("§c[§4§lServer§c] §4§lServer restarting in 30 seconds...");
            plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    Bukkit.shutdown();
                }
            }, 30 * 20L);
            cancel();
        }
    }
}
