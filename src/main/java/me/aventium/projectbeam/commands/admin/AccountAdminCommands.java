package me.aventium.projectbeam.commands.admin;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import me.aventium.projectbeam.Database;
import me.aventium.projectbeam.collections.Users;
import me.aventium.projectbeam.commands.DatabaseCommand;
import me.aventium.projectbeam.documents.DBUser;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AccountAdminCommands {

    @Command(
            aliases = {"getemail"},
            desc = "Retrieve a player's email address associated with their account",
            usage = "<player>",
            min = 1
    )
    @CommandPermissions("beam.getemail")
    public static void getEmail(final CommandContext args, final CommandSender sender) throws CommandException {
        final String playerName = args.getString(0);

        new DatabaseCommand() {
            @Override
            public void run() {
                final Users users = Database.getCollection(Users.class);
                final DBUser user = users.findByName(playerName);

                if(user == null) {
                    sender.sendMessage("§cPlayer not found!");
                    return;
                }

                if(user.getConfirmedDate() == null) {
                    sender.sendMessage("§cThat player has not registered yet!");
                    return;
                }

                sender.sendMessage("§a" + user.getUsername() + "'s email: §l" + user.getEmail() + "§a.");
            }
        }.execute();
    }

}
