package me.aventium.projectbeam.commands.admin;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import me.aventium.projectbeam.Database;
import me.aventium.projectbeam.PermissionsHandler;
import me.aventium.projectbeam.collections.Groups;
import me.aventium.projectbeam.collections.Servers;
import me.aventium.projectbeam.collections.Users;
import me.aventium.projectbeam.commands.DatabaseCommand;
import me.aventium.projectbeam.documents.DBGroup;
import me.aventium.projectbeam.documents.DBServer;
import me.aventium.projectbeam.documents.DBUser;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class BasicAdminCommands {

    @com.sk89q.minecraft.util.commands.Command(
            aliases = {"teleport", "tp"},
            desc = "Teleport to a player or teleport a player to you",
            min = 1,
            max = 2
    )
    @CommandPermissions({"beam.teleport"})
    public static void teleport(final CommandContext args, final CommandSender sender) throws CommandException {

        Player player = null;
        Player to = null;

        if(args.argsLength() == 1) {
            if(!(sender instanceof Player)) {
                sender.sendMessage("§cYou can only use this command in-game!");
                return;
            }

            player = (Player) sender;
            if(Bukkit.getPlayer(args.getString(0)) == null) {
                player.sendMessage("§cPlayer not found!");
                return;
            }

            player.teleport(Bukkit.getPlayer(args.getString(0)));
            player.sendMessage("§7Teleported to §6" + Bukkit.getPlayer(args.getString(0)).getName() + "§7.");
        } else {

            if(Bukkit.getPlayer(args.getString(0)) == null || Bukkit.getPlayer(args.getString(1)) == null) {
                player.sendMessage("§cPlayer not found!");
                return;
            }

            player = Bukkit.getPlayer(args.getString(0));
            to = Bukkit.getPlayer(args.getString(1));

            player.teleport(to);
            player.sendMessage("§aYou have been teleported to §2" + player.getName() + " §aby §2" + player.getName() + "§a!");

        }
    }

    @com.sk89q.minecraft.util.commands.Command(
            aliases = {"fly"},
            desc = "Toggle your flying",
            usage = "[player]",
            min = 0,
            max = 1
    )
    @CommandPermissions({"beam.fly"})
    public static void fly(final CommandContext args, final CommandSender sender) {
        if(args.argsLength() > 0) {
            if(Bukkit.getPlayer(args.getString(0)) == null) {
                sender.sendMessage("§cPlayer not found!");
                return;
            }

            Player player = Bukkit.getPlayer(args.getString(0));

            player.setAllowFlight(!player.getAllowFlight());
            player.setFlying(!player.isFlying());
            player.sendMessage("§7Flying set to §6" + player.getAllowFlight());
            sender.sendMessage("§6" + player.getName() + "'s §7Flying set to §6" + player.getAllowFlight());
        } else {
            if(!(sender instanceof Player)) {
                sender.sendMessage("§cYou must be ingame to use this command!");
                return;
            }

            Player player = (Player) sender;

            player.setAllowFlight(!player.getAllowFlight());
            player.setFlying(!player.isFlying());

            player.sendMessage("§7Flying set to §6" + player.getAllowFlight());
        }
    }

    @com.sk89q.minecraft.util.commands.Command(
            aliases = {"gamemode", "gm"},
            desc = "Change your gamemode",
            usage = "[player] <gamemode>",
            min = 1,
            max = 2
    )
    @CommandPermissions({"beam.gamemode"})
    public static void gamemode(final CommandContext args, final CommandSender sender) {
        if(!(sender instanceof Player)) {
            sender.sendMessage("§cYou must be ingame to use this command!");
            return;
        }

        if(args.argsLength() > 1) {
            if(Bukkit.getPlayer(args.getString(0)) == null) {
                sender.sendMessage("§cPlayer not found!");
                return;
            }

            Player player = Bukkit.getPlayer(args.getString(0));

            GameMode gm = null;

            if(args.getString(0).equalsIgnoreCase("0") || args.getString(0).equalsIgnoreCase("s")) gm = GameMode.SURVIVAL;
            else if(args.getString(0).equalsIgnoreCase("2") || args.getString(0).equalsIgnoreCase("a")) gm = GameMode.ADVENTURE;
            else if(args.getString(0).equalsIgnoreCase("1") || args.getString(0).equalsIgnoreCase("c")) gm = GameMode.CREATIVE;
            else gm = GameMode.SURVIVAL;

            player.setGameMode(gm);

            player.sendMessage("§7Gamemode set to §6" + gm.name() + " §7by §6" + sender.getName());
            sender.sendMessage("§6" + player.getName() + "'s §7gamemode set to §6" + gm.name());
        } else {
            Player player = (Player) sender;

            GameMode gm = null;

            if(args.getString(0).equalsIgnoreCase("0") || args.getString(0).equalsIgnoreCase("s")) gm = GameMode.SURVIVAL;
            else if(args.getString(0).equalsIgnoreCase("2") || args.getString(0).equalsIgnoreCase("a")) gm = GameMode.ADVENTURE;
            else if(args.getString(0).equalsIgnoreCase("1") || args.getString(0).equalsIgnoreCase("c")) gm = GameMode.CREATIVE;
            else gm = GameMode.SURVIVAL;

            player.setGameMode(gm);

            player.sendMessage("§7Gamemode set to §6" + gm.name());
        }
    }

    private GameMode getGameMode(String str) {
        GameMode gm = null;
        if(str.equalsIgnoreCase("0") || str.equalsIgnoreCase("s")) gm = GameMode.SURVIVAL;
        else if(str.equalsIgnoreCase("2") || str.equalsIgnoreCase("a")) gm = GameMode.ADVENTURE;
        else if(str.equalsIgnoreCase("1") || str.equalsIgnoreCase("c")) gm = GameMode.CREATIVE;
        else gm = GameMode.SURVIVAL;
        return gm;
    }
}
