package me.aventium.projectbeam.utils;

import java.util.Date;

public class Messages {

    public static String generateBanMessage(String reason, Date expiry) {
        StringBuilder ban = new StringBuilder();
        ban.append("§cYou are §4" + (expiry == null ? "permanently" : "temporarily") + " §cbanned.");
        ban.append("\n§cReason: §4" + reason + "§c.");
        if(expiry != null) ban.append("\n§cYour ban will be lifted in §4" + TimeUtils.formatDateDiff(expiry.getTime()) + "§c.");
        return ban.toString();
    }

    public static String generateBlacklistMessage(String reason) {
        StringBuilder blacklist = new StringBuilder();
        blacklist.append("§cYou have blacklisted.");
        blacklist.append("\n§cReason: §4" + reason + "§c.");
        return blacklist.toString();
    }

    public static String generateMuteMessage(String reason, Date expiry) {
        StringBuilder mute = new StringBuilder();
        mute.append("§cYou have been muted for §4'" + reason + "'§c.");
        if(expiry != null) mute.append("\n§cYour mute will be lifted in §4" + TimeUtils.formatDateDiff(expiry.getTime()) + "§c.");
        mute.append("\n§4Please read our rules at §lwww.breakmc.com/rules §4to ensure no misunderstandings.");
        return mute.toString();
    }

}
