package me.aventium.projectbeam.friends;

import me.aventium.projectbeam.Beam;
import me.aventium.projectbeam.Database;
import me.aventium.projectbeam.documents.DBSession;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class LastSeenFormatter {

    public static String format(CommandSender sender, String matchedName, DBSession session, String serverName, boolean displayServer) {
        StringBuilder msg = new StringBuilder(matchedName);

        if(session.getEnd() == null) {
            msg.append(ChatColor.GRAY).append(" is ").append(ChatColor.GOLD).append(ChatColor.BOLD).append("online");
            if(displayServer && session.getServerId() != null && !session.getServerId().equals(Database.getServerId())) {
                msg.append(ChatColor.GRAY).append(" on ").append(ChatColor.GREEN).append(serverName);
            }
        } else {
            msg.append(ChatColor.GRAY).append(" seen ").append(ChatColor.DARK_GREEN).append(Beam.TIME_FORMATTER.format(session.getEnd()));
            if (displayServer) {
                msg.append(ChatColor.GRAY).append(" on ").append(ChatColor.GREEN).append(serverName);
            }
        }

        return msg.toString();
    }

}
