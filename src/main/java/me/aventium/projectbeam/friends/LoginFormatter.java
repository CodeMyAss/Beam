package me.aventium.projectbeam.friends;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

public class LoginFormatter {

    public static String formatLogin(String displayName) {
        return displayName + ChatColor.YELLOW + " joined the game";
    }

    public static String formatLogout(String displayName) {
        return displayName + ChatColor.YELLOW + " left the game";
    }

    public static String formatLocalLogin(Player player, Player viewer) {
        return formatLogin(player.getDisplayName(viewer));
    }

    public static String formatLocalLogout(Player player, Player viewer) {
        return formatLogout(player.getDisplayName(viewer));
    }

    public static String formatFriendLogin(@Nullable String server, String playerName) {
        return formatServerTag(server) + formatLogin(playerName);
    }

    public static String formatFriendLogout(@Nullable String server, String playerName) {
        return formatServerTag(server) + formatLogout(playerName);
    }

    public static String formatFriendChange(String serverFrom, String serverTo, String playerName) {
        StringBuilder builder = new StringBuilder(formatServerTag(serverFrom, serverTo));

        builder.append(playerName).append(ChatColor.YELLOW).append(" changed servers");

        return builder.toString();
    }

    public static String formatServerTag(@Nullable String login) {
        return formatServerTag(login, null);
    }

    public static String formatServerTag(@Nullable String firstServer, @Nullable String secondServer) {
        if(firstServer != null) {
            StringBuilder builder = new StringBuilder();

            builder.append(ChatColor.RESET).append("[").append(ChatColor.GOLD).append(firstServer).append(ChatColor.RESET);
            if(secondServer != null) {
                builder.append(ChatColor.YELLOW).append(" \u00BB ").append(ChatColor.GOLD).append(secondServer).append(ChatColor.RESET);
            }
            builder.append("] ");

            return builder.toString();
        } else {
            return "";
        }
    }

}
