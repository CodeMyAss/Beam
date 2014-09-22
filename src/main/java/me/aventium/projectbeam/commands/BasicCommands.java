package me.aventium.projectbeam.commands;

import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import me.aventium.projectbeam.HackerDetection;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BasicCommands {

    @com.sk89q.minecraft.util.commands.Command(
            aliases = {"teleport", "tp"},
            desc = "Teleport to a player or teleport a player to you",
            min = 1,
            max = 2
    )
    public static void teleport(final CommandContext args, final CommandSender sender) throws CommandException {

        if(!sender.hasPermission("beam.*") && !sender.hasPermission("beam.teleport") && !sender.isOp() && !HackerDetection.inDetectionMode.contains(sender.getName())) {
            sender.sendMessage("§cYou do not have permission to do this.");
            return;
        }

        Player player = null;
        Player to = null;

        if(args.argsLength() == 1) {
            if(!(sender instanceof Player)) {
                sender.sendMessage("§cYou can only use this command in-game!");
                return;
            }

            player = (Player) sender;
            if(Bukkit.getPlayer(args.getString(0)) == null) {
                player.sendMessage("§cPlayer not found!");
                return;
            }

            player.teleport(Bukkit.getPlayer(args.getString(0)));
            Bukkit.getPlayer(args.getString(0)).sendMessage("§aYou have been teleported to §2" + player.getName() + " §aby §2" + player.getName() + "§a!");
        } else {

            if(Bukkit.getPlayer(args.getString(0)) == null || Bukkit.getPlayer(args.getString(1)) == null) {
                player.sendMessage("§cPlayer not found!");
                return;
            }

            player = Bukkit.getPlayer(args.getString(0));
            to = Bukkit.getPlayer(args.getString(1));

            player.teleport(to);
            player.sendMessage("§aYou have been teleported to §2" + player.getName() + " §aby §2" + player.getName() + "§a!");

        }
    }

}
