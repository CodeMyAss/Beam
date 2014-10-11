package me.aventium.projectbeam.friends;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.NestedCommand;
import me.aventium.projectbeam.Database;
import me.aventium.projectbeam.collections.Friendships;
import me.aventium.projectbeam.collections.Users;
import me.aventium.projectbeam.commands.DatabaseCommand;
import me.aventium.projectbeam.documents.DBFriendship;
import me.aventium.projectbeam.documents.DBUser;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class FriendCommands {

    @Command(
            aliases = {"request"},
            desc = "Send a friend request to a player",
            usage = "<player>",
            min = 1
    )
    public static void request(final CommandContext args, final CommandSender sender) throws CommandException {
        if(!(sender instanceof Player)) {
            sender.sendMessage("§cYou must be a player to have friends!");
            return;
        }

        final Player player = (Player) sender;

        final DBUser user = Database.getCollection(Users.class).findByName(args.getString(0));

        if(user == null) {
            player.sendMessage("§cNo players matched query.");
            return;
        }

        if(Database.getCollection(Friendships.class).findFriends(player.getUniqueId().toString()).contains(user.getUUID().toString())) {
            player.sendMessage("§cYou and " + user.getUsername() + " are already friends!");
            return;
        }

        Database.getExecutorService().submit(new DatabaseCommand() {
            @Override
            public void run() {
                Database.getCollection(Friendships.class).createFriendship(player.getUniqueId().toString(), user.getUUID().toString());
                player.sendMessage("§bFriend request sent!");
            }
        });
    }

    @Command(
            aliases = {"requests", "reqs"},
            desc = "View all your pending friend requests",
            min = 0
    )
    public static void requests(final CommandContext args, final CommandSender sender) throws CommandException {
        if(!(sender instanceof Player)) {
            sender.sendMessage("§cYou must be a player to have friends!");
            return;
        }

        final Player player = (Player) sender;

        Database.getExecutorService().submit(new DatabaseCommand() {
            @Override
            public void run() {
                List<DBFriendship> friendships = Database.getCollection(Friendships.class).findRequests(player.getUniqueId().toString());

                if(friendships == null || friendships.size() == 0) {
                    player.sendMessage("§cYou do not have any pending friend requests!");
                    return;
                }

                player.sendMessage("§9Pending friend requests:");

                for(DBFriendship friendship : friendships) {
                    player.sendMessage("§9" + friendship.getFriender() + " §b- §9" + friendship.getSendDate().toString());
                }
            }
        });
    }

    /*@Command(
            aliases = {"accept"},
            desc = "Accept a friend request",
            min = 1,
            usage = "<player>"
    )
    public static void accept(final CommandContext args, final CommandSender sender) throws CommandException {
        if(!(sender instanceof Player)) {
            sender.sendMessage("§cYou must be a player to have friends!");
            return;
        }

        final Player player = (Player) sender;

        final DBUser user = Database.getCollection(Users.class).findByName(args.getString(0));

        if(user == null) {
            player.sendMessage("§cNo players matched query.");
            return;
        }

        if(Database.getCollection(Friendships.class).findFriends(player.getUniqueId().toString()).contains(user.getUUID().toString())) {
            player.sendMessage("§cYou and " + user.getUsername() + " are already friends!");
            return;
        }

        Database.getExecutorService().submit(new DatabaseCommand() {
            @Override
            public void run() {
                for(DBFriendship fr : Database.getCollection(Friendships.class).findRequests(player.getUniqueId().toString())) {
                    
                }
                player.sendMessage("§bFriend request sent!");
            }
        });
    }*/

}
