package me.aventium.projectbeam;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;

public class Portal {

    private HashSet<String> connectingPlayers = new HashSet<>();

    private Plugin plugin;

    public Portal(Plugin plugin) {
        this.plugin = plugin;
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
    }

    public void sendAllPlayers(String serverName) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendPlayerToServer(player, serverName);
        }
    }

    public void sendPlayerToServer(final Player player, String serverName) {
        if (connectingPlayers.contains(player.getName())) {
            return;
        }
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            out.writeUTF("Connect");
            out.writeUTF(serverName);
        } catch (IOException localIOException1) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        player.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
        connectingPlayers.add(player.getName());

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
                connectingPlayers.remove(player.getName());
            }
        }, 20L);
    }

}
