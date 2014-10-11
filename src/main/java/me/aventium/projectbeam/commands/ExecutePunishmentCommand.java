package me.aventium.projectbeam.commands;

import me.aventium.projectbeam.Database;
import me.aventium.projectbeam.collections.Servers;
import me.aventium.projectbeam.documents.DBPunishment;
import me.aventium.projectbeam.utils.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ExecutePunishmentCommand implements Runnable {

    public static final String WEBSITE = "";

    private final @Nonnull DBPunishment punishment;
    private final @Nullable String serverName;

    public ExecutePunishmentCommand(@Nonnull DBPunishment punishment) {
        this(punishment, Database.getCollection(Servers.class).findServerName(punishment.getServerId()));
    }

    public ExecutePunishmentCommand(@Nonnull DBPunishment punishment, @Nullable String serverName) {
        this.punishment = punishment;
        this.serverName = serverName;
    }

    @Override
    public void run() {
        Player player = Bukkit.getPlayerExact(this.punishment.getPlayer());

        StringBuilder broadcast = new StringBuilder();

        switch(this.punishment.getType()) {
            case WARN:
                if(player != null) {
                    StringBuilder warn = new StringBuilder();
                    if(Bukkit.getPlayer(punishment.getIssuer()) != null) Bukkit.getPlayer(punishment.getIssuer()).sendMessage("§aPlayer warned.");
                    warn.append("§cYou have been warned by §4" + this.punishment.getIssuer() + "§c.");
                    warn.append("\n§cReason: §4" + this.punishment.getReason() + "§c.");
                    warn.append("\n§cPlease read our rules at §4§lwww.breakmc.com/rules §cto ensure no misunderstandings.");
                    player.sendMessage(warn.toString().trim());
                }
                break;
            case KICK:
                if(player != null) {
                    if(Bukkit.getPlayer(punishment.getIssuer()) != null) Bukkit.getPlayer(punishment.getIssuer()).sendMessage("§aPlayer kicked.");
                    player.kickPlayer("§cYou have been kicked by §4" + this.punishment.getIssuer() + "§c\nReason: §4" + this.punishment.getReason() + "§c.");
                }
                break;
            case MUTE:
                if(player != null) {
                    StringBuilder mute = new StringBuilder();
                    if(Bukkit.getPlayer(punishment.getIssuer()) != null) Bukkit.getPlayer(punishment.getIssuer()).sendMessage("§aPlayer muted for " + TimeUtils.formatDateDiff(this.punishment.getExpiry().getTime()) + ".");
                    mute.append("§cYou have been muted by §4" + this.punishment.getIssuer() + " §cfor §4'" + this.punishment.getReason() + "'§c.");
                    if(this.punishment.getExpiry() != null) mute.append("\n§cYour mute will be lifted in §4" + TimeUtils.formatDateDiff(this.punishment.getExpiry().getTime()) + "§c.");
                    mute.append("\n§4Please read our rules at §lwww.breakmc.com/rules §4to ensure no misunderstandings.");
                    player.sendMessage(mute.toString().trim());
                }
                break;
            case BAN:
                for(Player online : Bukkit.getOnlinePlayers()) {
                    online.sendMessage("§4" + this.punishment.getPlayer() + " §chas been " + (this.punishment.getExpiry() == null ? "permanently" : "temporarily") + " banned by §4" + this.punishment.getIssuer() + "§c.");
                }

                if(player != null) {
                    StringBuilder ban = new StringBuilder();
                    ban.append("§cYou have been §4" + (this.punishment.getExpiry() == null ? "permanently" : "temporarily") + " §cbanned by §4" + this.punishment.getIssuer() + "§c.");
                    ban.append("\n§cReason: §4" + this.punishment.getReason() + "§c.");
                    if(this.punishment.getExpiry() != null) ban.append("\n§cYour ban will be lifted in §4" + TimeUtils.formatDateDiff(this.punishment.getExpiry().getTime()) + "§c.");
                    player.kickPlayer(ban.toString());
                }

                if(Bukkit.getPlayer(punishment.getIssuer()) != null) Bukkit.getPlayer(punishment.getIssuer()).sendMessage("§aPlayer banned.");

                break;
        }
    }

}
