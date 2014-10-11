package me.aventium.projectbeam.events;

import me.aventium.projectbeam.documents.DBGroup;
import me.aventium.projectbeam.documents.DBUser;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class PlayerGroupChangeEvent extends Event {

    private DBUser player;
    private DBGroup newGroup;

    public PlayerGroupChangeEvent(DBUser player, DBGroup newGroup) {
        this.player = player;
        this.newGroup = newGroup;
    }

    public DBUser getPlayer() {
        return player;
    }

    public DBGroup getNewGroup() {
        return newGroup;
    }

    /**
     * HandlerList stuff
     */
    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
