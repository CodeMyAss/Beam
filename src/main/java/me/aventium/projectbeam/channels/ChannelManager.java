package me.aventium.projectbeam.channels;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.joda.time.Instant;

public class ChannelManager {
    private static boolean running = false;

    private static final Instant started = Instant.now();

    private static final SyncChannel syncChannel = new SyncChannel();
    private static int syncChannelId;

    public static Instant getStarted() {
        return started;
    }

    public static SyncChannel getSyncChannel() {
        return syncChannel;
    }

    public static void flush() {
        syncChannel.flush();
    }

    public static synchronized void start(Plugin plugin) {
        if(!running) {
            syncChannelId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, syncChannel, 0, 1);

            running = true;
        }
    }

    public static synchronized void shutdown() {
        if(running) {
            flush();

            Bukkit.getScheduler().cancelTask(syncChannelId);

            running = false;
        }
    }
}