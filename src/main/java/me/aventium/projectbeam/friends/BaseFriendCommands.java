package me.aventium.projectbeam.friends;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.NestedCommand;
import me.aventium.projectbeam.Beam;
import me.aventium.projectbeam.Database;
import me.aventium.projectbeam.collections.Friendships;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.List;

public class BaseFriendCommands {

    @NestedCommand(value = FriendCommands.class, executeBody = true)
    @Command(
            aliases = {"friend", "fr"},
            desc = "Use the in-game friend system"
    )
    public static void friendCommand(final CommandContext args, final CommandSender sender) throws CommandException {
        if(args.argsLength() == 0) {
            sender.sendMessage("§7---§b[§9Friends§b]§7---");
            for(Method method : FriendCommands.class.getMethods()) {
                if(method.isAnnotationPresent(Command.class)) {
                    Command info = (Command) method.getAnnotation(Command.class);
                    StringBuilder builder = new StringBuilder();
                    builder.append("§b/friend " + info.aliases()[0]);
                    builder.append(" " + info.usage());
                    builder.append(" §7- §b" + info.desc());
                    sender.sendMessage(builder.toString().trim());
                }
            }
        }
    }

    @Command(
            aliases = {"friends"},
            desc = "Show what servers your friends are on",
            usage = "[page]",
            min = 0,
            max = 1
    )
    public static void friendsCommand(final CommandContext args, final CommandSender sender) throws CommandException {
        if(!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players may have friends!");
        }

        Player player = (Player) sender;

        List<String> friends = Database.getCollection(Friendships.class).findFriends(player.getUniqueId().toString());

        int pageNum = args.getInteger(0, 1);
        if(pageNum <= 0) pageNum = 1;

        Database.getExecutorService().submit(new ShowOnlineFriendsCommand(player, friends, pageNum));
    }

}
