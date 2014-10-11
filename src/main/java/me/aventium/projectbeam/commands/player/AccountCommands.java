package me.aventium.projectbeam.commands.player;

import com.sk89q.minecraft.util.commands.*;
import me.aventium.projectbeam.Database;
import me.aventium.projectbeam.collections.Servers;
import me.aventium.projectbeam.collections.Users;
import me.aventium.projectbeam.commands.DatabaseCommand;
import me.aventium.projectbeam.documents.DBUser;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Random;

/**
 * Created by Cameron on 10/3/2014.
 */
public class AccountCommands {

    @com.sk89q.minecraft.util.commands.Command(
            aliases = {"email", "viewemail"},
            desc = "View your email you have registered with"
    )
    public static void email(final CommandContext args, final CommandSender sender) throws CommandException {
        if (!(sender instanceof Player)) throw new CommandException("You must be a player to use this command!");

        new DatabaseCommand() {
            @Override
            public void run() {
                final Users users = Database.getCollection(Users.class);
                final DBUser user = users.findOrCreateByName(sender.getName(), ((Player) sender).getUniqueId());

                if(user.getConfirmedDate() == null) {
                    sender.sendMessage("§cYou have not yet registered! Use /register to do so!");
                    return;
                }

                sender.sendMessage("§aYour email: §l" + user.getEmail() + "§a.");
            }
        }.execute();
    }


    @com.sk89q.minecraft.util.commands.Command(
            aliases = {"register"},
            desc = "Register your email and link it to your account",
            usage = "<email>",
            min = 1,
            max = 1
    )
    public static void register(final CommandContext args, final CommandSender sender) throws CommandException {

        if (!(sender instanceof Player)) throw new CommandException("You must be a player to use this command!");

        new DatabaseCommand() {
            @Override
            public void run() {
                final Users users = Database.getCollection(Users.class);
                final DBUser user = users.findOrCreateByName(sender.getName(), ((Player) sender).getUniqueId());

                if(user.getConfirmedDate() != null) {
                    sender.sendMessage("§aYou have already registered! Email: §2" + user.getEmail() + "§a.");
                    return;
                }

                final String email = args.getString(0).toLowerCase();

                if(users.usersWithEmail(email) != 0) {
                    sender.sendMessage("§c" + email + " is already a registered email!");
                    return;
                }

                try {
                    user.setEmail(email);
                } catch(IllegalArgumentException ex) {
                    sender.sendMessage("§cInvalid email!");
                    return;
                }

                if(user.sendConfirmationEmail()) {
                    sender.sendMessage("§aA confirmation email has been sent to §2" + email + "§a.");
                    user.setConfirmationToken(getConfirmationToken());
                } else {
                    sender.sendMessage("§aThere was a problem while sending a confirmation email to §2" + email + "§a.");
                    user.setEmail(null);
                }
                users.save(user);
            }
        }.execute();

    }

    private static String getConfirmationToken() {
        String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        Random rand = new Random();

        final StringBuilder stringBuilder = new StringBuilder(20);

        for(int i = 0; i < 20; i++) {
            stringBuilder.append(AB.charAt(rand.nextInt(AB.length())));
        }

        return stringBuilder.toString();
    }

}
