package me.aventium.projectbeam;

import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Experimental hacker detection mode for the hardcore family servers.
 * @author Aventium
 */
public class HackerDetection implements Listener {

    public static List<String> inDetectionMode = new ArrayList<>();


    @com.sk89q.minecraft.util.commands.Command(
            aliases = {"hackdetection", "h"},
            desc = "Enter/leave hacker detection mode",
            min = 0
    )
    @CommandPermissions({"beam.hackerdetection","beam.*"})
    public static void hackerDetection(final CommandContext args, final CommandSender sender) throws CommandException {

        if(!(sender instanceof Player)) {
            sender.sendMessage("§cYou can only use this command in-game!");
            return;
        }

        Player player = (Player) sender;

        boolean join = true;

        if(inDetectionMode.contains(player.getName())) {
            join = false;
        }

        if(join) {
            inDetectionMode.add(player.getName());
            player.setAllowFlight(true);
            player.setFlying(true);
            for(Player p : Bukkit.getOnlinePlayers()) {
                if(!p.hasPermission("beam.hackerdetection") && !p.hasPermission("beam.*") && !p.isOp()) p.hidePlayer(player);
            }
        } else {
            inDetectionMode.remove(player.getName());
            player.setFlying(false);
            player.setAllowFlight(false);
            for(Player p : Bukkit.getOnlinePlayers()) {
                p.showPlayer(player);
            }
        }

        player.sendMessage((join ? "§a" : "§c") + "You have " + (join ? "entered " : "left ") + " hacker detection mode!");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        if(!p.hasPermission("beam.hackerdetection") && !p.hasPermission("beam.*") && !p.isOp()) {
            for(String str : inDetectionMode) {
                if(Bukkit.getPlayer(str) != null) {
                    p.hidePlayer(Bukkit.getPlayer(str));
                }
            }
        }
    }

}
