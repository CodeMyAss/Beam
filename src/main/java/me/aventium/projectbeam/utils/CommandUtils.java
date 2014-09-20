package me.aventium.projectbeam.utils;

import me.aventium.projectbeam.Database;
import me.aventium.projectbeam.commands.FindPlayerCommand;
import me.aventium.projectbeam.commands.PlayerCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandUtils {

    public static void matchSinglePlayer(PlayerCommand cmd, CommandSender sender, String rawUsername) {
        if(rawUsername.charAt(0) == '@') {
            // Remove legacy offline lookup prefix
            rawUsername = rawUsername.substring(1);
        }

        // look up player according to the who is online now
        List<Player> players = Bukkit.getServer().matchPlayer(rawUsername);
        switch(players.size()) {
            case 0:
                Database.getExecutorService().submit(new FindPlayerCommand(rawUsername, sender, cmd));
                break;
            case 1:
                Player player = players.get(0);
                cmd.setUser(player, rawUsername);
                Database.getExecutorService().submit(cmd);
                break;
            default:
                sender.sendMessage("§cMore than one player found, please be more specific.");
                break;
        }
    }

}
