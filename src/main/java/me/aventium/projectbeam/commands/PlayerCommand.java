package me.aventium.projectbeam.commands;

import me.aventium.projectbeam.documents.DBUser;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A DatabaseCommand that operates on a player, who may or may not be online.
 */
public abstract class PlayerCommand extends DatabaseCommand {
    @Nullable protected Player player;
    @Nullable protected DBUser user;
    @Nullable protected String username;

    public void setUser(@Nonnull DBUser user) {
        this.user = user;
        this.username = user.getUsername();
    }

    public void setUser(@Nonnull DBUser user, String query) {
        this.setUser(user);
    }

    public void setUser(OfflinePlayer player) {
        if(player instanceof Player) {
            this.player = (Player) player;
        }
        this.username = player.getName();
    }

    public void setUser(OfflinePlayer player, String query) {
        this.setUser(player);
    }

    public String getMatchedName() {
        return this.username;
    }
}