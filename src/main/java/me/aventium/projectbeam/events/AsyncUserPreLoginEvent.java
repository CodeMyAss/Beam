package me.aventium.projectbeam.events;

import me.aventium.projectbeam.Database;
import me.aventium.projectbeam.collections.Users;
import me.aventium.projectbeam.documents.DBUser;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.net.InetAddress;

public class AsyncUserPreLoginEvent extends Event {

    private final AsyncPlayerPreLoginEvent cause;
    private DBUser user;

    public AsyncUserPreLoginEvent(AsyncPlayerPreLoginEvent cause) {
        this.cause = cause;
    }

    public DBUser getUser() {
        if(user == null) {
            user = Database.getCollection(Users.class).findOrCreateByName(this.getName());
        }
        return user;
    }

    public AsyncPlayerPreLoginEvent getCause() {
        return cause;
    }

    /**
     * Delegated methods
     */

    public AsyncPlayerPreLoginEvent.Result getLoginResult() {return cause.getLoginResult();}

    public void setLoginResult(AsyncPlayerPreLoginEvent.Result result) {cause.setLoginResult(result);}

    public String getKickMessage() {return cause.getKickMessage();}

    public void setKickMessage(String message) {cause.setKickMessage(message);}

    public String getName() {return cause.getName();}

    public InetAddress getAddress() {return cause.getAddress();}

    public void disallow(AsyncPlayerPreLoginEvent.Result result, String message) {cause.disallow(result, message);}

    public void allow() {cause.allow();}

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
