package me.aventium.projectbeam.commands;

import me.aventium.projectbeam.Database;
import me.aventium.projectbeam.collections.Users;
import me.aventium.projectbeam.documents.DBUser;
import org.bukkit.command.CommandSender;

public class FindPlayerCommand extends DatabaseCommand {

    private final String username;
    private final CommandSender sender;
    private final PlayerCommand res;

    public FindPlayerCommand(String username, CommandSender sender, PlayerCommand res) {
        this.username = username;
        this.sender = sender;
        this.res = res;
    }

    public FindPlayerCommand(String username, CommandSender sender) {
        this(username, sender, null);
    }

    @Override
    public void run() {
        DBUser user = Database.getCollection(Users.class).findByName(this.username);

        if(user != null && user.getSignInCount() > 0) {
            if(this.res != null) {
                this.res.setUser(user);
                this.res.execute();
            }
        } else {
            new SendMessageCommand(this.sender, "§cNo players matched given query.").execute();
        }
    }
}
