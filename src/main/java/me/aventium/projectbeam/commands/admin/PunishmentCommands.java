package me.aventium.projectbeam.commands.admin;

import com.mongodb.MongoException;
import com.sk89q.minecraft.util.commands.*;
import me.aventium.projectbeam.Database;
import me.aventium.projectbeam.channels.ChannelManager;
import me.aventium.projectbeam.collections.Punishments;
import me.aventium.projectbeam.collections.Users;
import me.aventium.projectbeam.commands.ExecutePunishmentCommand;
import me.aventium.projectbeam.commands.PlayerCommand;
import me.aventium.projectbeam.documents.DBPunishment;
import me.aventium.projectbeam.documents.DBUser;
import me.aventium.projectbeam.utils.CommandUtils;
import me.aventium.projectbeam.utils.TimeUtils;
import org.bson.types.ObjectId;
import org.bukkit.command.CommandSender;
import org.joda.time.DateTime;

import java.util.Date;
import java.util.List;

public class PunishmentCommands {

    @com.sk89q.minecraft.util.commands.Command(
            aliases = {"warn"},
            desc = "Warn a player",
            min = 2,
            usage = "<player> <reason>"
    )
    @CommandPermissions({"beam.warn"})
    public static void warn(final CommandContext args, final CommandSender sender) throws CommandException {

        String playerName = args.getString(0);

        final ObjectId serverId = Database.getServerId();

        PlayerCommand command = createPunishmentCommand(sender, serverId, DBPunishment.Type.WARN, args.getJoinedStrings(1), null);

        CommandUtils.matchSinglePlayer(command, sender, playerName);
    }

    @com.sk89q.minecraft.util.commands.Command(
            aliases = {"kick"},
            desc = "Kick a player from the network",
            min = 2,
            usage = "<player> <reason>"
    )
    @CommandPermissions({"beam.kick"})
    public static void kick(final CommandContext args, final CommandSender sender) throws CommandException {

        String playerName = args.getString(0);

        final ObjectId serverId = Database.getServerId();

        PlayerCommand command = createPunishmentCommand(sender, serverId, DBPunishment.Type.KICK, args.getJoinedStrings(1), null);

        CommandUtils.matchSinglePlayer(command, sender, playerName);
    }

    @com.sk89q.minecraft.util.commands.Command(
            aliases = {"mute"},
            desc = "Mute a player permanently from speaking on the network.",
            min = 2,
            usage = "<player> <reason>"
    )
    @CommandPermissions({"beam.mute"})
    public static void mute(final CommandContext args, final CommandSender sender) throws CommandException {

        String playerName = args.getString(0);

        if(isMuted(playerName)) {
            sender.sendMessage("§cThat player is already muted!");
            return;
        }

        final ObjectId serverId = Database.getServerId();

        PlayerCommand command = createPunishmentCommand(sender, serverId, DBPunishment.Type.MUTE, args.getJoinedStrings(1), null);

        CommandUtils.matchSinglePlayer(command, sender, playerName);
    }

    @com.sk89q.minecraft.util.commands.Command(
            aliases = {"tempmute", "tmute"},
            desc = "Mute a player temporarily from speaking on the network.",
            min = 3,
            usage = "<player> <time> <reason>"
    )
    @CommandPermissions({"beam.mute"})
    public static void tempmute(final CommandContext args, final CommandSender sender) throws CommandException {

        String playerName = args.getString(0);

        if(isMuted(playerName)) {
            sender.sendMessage("§cThat player is already muted!");
            return;
        }

        final ObjectId serverId = Database.getServerId();

        long expires;

        try {
            expires = TimeUtils.getTimeStamp(args.getString(1));
        } catch(Exception ex) {
            sender.sendMessage("§cThere was an error in your command syntax, please check your time format.");
            return;
        }

        DateTime expiry = new DateTime(expires);


        PlayerCommand command = createPunishmentCommand(sender, serverId, DBPunishment.Type.MUTE, args.getJoinedStrings(2), expiry);

        CommandUtils.matchSinglePlayer(command, sender, playerName);
    }

    @com.sk89q.minecraft.util.commands.Command(
            aliases = {"unmute"},
            desc = "Unmute a player that was previously muted",
            min = 1,
            usage = "<player>"
    )
    @CommandPermissions({"beam.unmute"})
    public static void unmute(final CommandContext args, final CommandSender sender) throws CommandException {

        String playerName = args.getString(0);

        Punishments punishments = Database.getCollection(Punishments.class);

        // check for bans
        List<DBPunishment> activePunishments;
        try {
            activePunishments = punishments.getActivePunishments(playerName);
        } catch (MongoException.Network e) {
            sender.sendMessage("§4There was in internal error on our end, we're working to fix it, please check in later.");
            return;
        }

        boolean unmuted = false;

        for(DBPunishment punishment : activePunishments) {
            if(punishment.getType() != DBPunishment.Type.MUTE) {
                break;
            }

            punishment.setActive(false);
            punishments.save(punishment);
            unmuted = true;
        }

        if(unmuted) {
            sender.sendMessage("§2" + playerName + " §ahas been unmuted.");
        } else {
            sender.sendMessage("§4" + playerName + " §cis not currently muted.");
        }
    }

    @com.sk89q.minecraft.util.commands.Command(
            aliases = {"ban", "b"},
            desc = "Ban a player permanently from the network.",
            min = 2,
            usage = "<player> <reason>"
    )
    @CommandPermissions({"beam.ban"})
    public static void ban(final CommandContext args, final CommandSender sender) throws CommandException {

        String playerName = args.getString(0);

        if(isBanned(playerName)) {
            sender.sendMessage("§cThat player is already banned!");
            return;
        }

        final ObjectId serverId = Database.getServerId();

        PlayerCommand command = createPunishmentCommand(sender, serverId, DBPunishment.Type.BAN, args.getJoinedStrings(1), null);

        CommandUtils.matchSinglePlayer(command, sender, playerName);
    }

    @com.sk89q.minecraft.util.commands.Command(
            aliases = {"tempban", "tban"},
            desc = "Ban a player temporarily from the network.",
            min = 3,
            usage = "<player> <time> <reason>"
    )
    @CommandPermissions({"beam.ban"})
    public static void tempban(final CommandContext args, final CommandSender sender) throws CommandException {

        String playerName = args.getString(0);

        if(isBanned(playerName)) {
            sender.sendMessage("§cThat player is already banned!");
            return;
        }

        final ObjectId serverId = Database.getServerId();

        long expires;

        try {
            expires = TimeUtils.getTimeStamp(args.getString(1));
        } catch(Exception ex) {
            sender.sendMessage("§cThere was an error in your command syntax, please check your time format.");
            return;
        }

        DateTime expiry = new DateTime(expires);


        PlayerCommand command = createPunishmentCommand(sender, serverId, DBPunishment.Type.BAN, args.getJoinedStrings(2), expiry);

        CommandUtils.matchSinglePlayer(command, sender, playerName);
    }

    @com.sk89q.minecraft.util.commands.Command(
            aliases = {"unban"},
            desc = "Unban a player from the network",
            min = 1,
            usage = "<player>"
    )
    @CommandPermissions({"beam.unban"})
    public static void unban(final CommandContext args, final CommandSender sender) throws CommandException {

        String playerName = args.getString(0);

        Punishments punishments = Database.getCollection(Punishments.class);

        // check for bans
        List<DBPunishment> activePunishments;
        try {
            activePunishments = punishments.getActivePunishments(playerName);
        } catch (MongoException.Network e) {
            sender.sendMessage("§4There was in internal error on our end, we're working to fix it, please check in later.");
            return;
        }

        boolean unbanned = false;

        for(DBPunishment punishment : activePunishments) {
            if(punishment.getType() != DBPunishment.Type.BAN) {
                break;
            }

            punishment.setActive(false);
            punishments.save(punishment);
            unbanned = true;
        }

        if(unbanned) {
            sender.sendMessage("§2" + playerName + " §ahas been unbanned.");
        } else {
            sender.sendMessage("§4" + playerName + " §cis not currently banned.");
        }
    }

    @com.sk89q.minecraft.util.commands.Command(
            aliases = {"lookup", "look"},
            desc = "Look up a player's record",
            min = 1,
            usage = "[-p] <player>",
            flags = "p"
    )
    @CommandPermissions("beam.lookup")
    public static void lookup(final CommandContext args, final CommandSender sender) throws CommandException {
        String playerName = args.getString(0);

        DBUser user = Database.getCollection(Users.class).findByName(playerName);

        if(user == null) {
            sender.sendMessage("§cPlayer not found!");
            return;
        }

        boolean showPunishments = args.hasFlag('p');

        if(!showPunishments) {
            sender.sendMessage("§6Username: §f" + user.getUsername());
            sender.sendMessage("§6UUID: §f" + user.getUUID().toString().replaceAll("-", ""));
            sender.sendMessage("§6Last IP used: §f" + user.getLastSignInIP().replaceAll("/", ""));
            sender.sendMessage("§6Join date: §f" + user.getDateJoined().toString());
            sender.sendMessage("§6§oUse /lookup -p " + playerName + " to check punishments.");
        } else {
            sender.sendMessage("§6Punishments for §f" + user.getUsername());

            Punishments punishments = Database.getCollection(Punishments.class);

            // check for bans
            List<DBPunishment> activePunishments;
            try {
                activePunishments = punishments.getPunishments(playerName);
            } catch (MongoException.Network e) {
                sender.sendMessage("§4There was in internal error on our end, we're working to fix it, please check in later.");
                return;
            }

            if(activePunishments != null && activePunishments.size() != 0) {
                sender.sendMessage("§6§oType, Issuer, Reason, Date, Expires");
                for(DBPunishment punishment : activePunishments) {
                    sender.sendMessage("§6" + punishment.getType().name() + ", " + punishment.getIssuer() + ", §o'" + punishment.getReason() + "'§6, " + punishment.getTimeCreated().toString() + ", " + (punishment.getExpiry() == null ? "Never" : (punishment.getExpiry().before(new Date()) ? "Expired" : punishment.getExpiry().toString())));
                }
            }
        }


    }

    private static PlayerCommand createPunishmentCommand(final CommandSender issuer, final ObjectId serverId, final DBPunishment.Type type, final String reason, final DateTime expires) {
        return new PlayerCommand() {
            @Override
            public void run() {
                DBPunishment punishment = new DBPunishment();

                punishment.setServerId(serverId);
                punishment.setActive(true);
                punishment.setReason(reason);
                punishment.setTimeCreated(new Date());
                if(expires != null) punishment.setExpiry(expires.toDate());
                punishment.setIssuer(issuer.getName());
                punishment.setPlayer(this.user.getUUID());
                punishment.setType(type);

                Database.getCollection(Punishments.class).save(punishment);

                ChannelManager.getSyncChannel().queue(new ExecutePunishmentCommand(punishment));
            }
        };
    }

    private static boolean isBanned(String player) {
        Punishments punishments = Database.getCollection(Punishments.class);
        // check for bans
        List<DBPunishment> activePunishments;
        try {
            activePunishments = punishments.getActivePunishments(player);
        } catch (MongoException.Network e) {
            return false;
        }
        for(DBPunishment punishment : activePunishments) {
            if(punishment.getType().equals(DBPunishment.Type.BAN)) return true;
        }
        return false;
    }

    private static boolean isMuted(String player) {
        Punishments punishments = Database.getCollection(Punishments.class);
        // check for bans
        List<DBPunishment> activePunishments;
        try {
            activePunishments = punishments.getActivePunishments(player);
        } catch (MongoException.Network e) {
            return false;
        }
        for(DBPunishment punishment : activePunishments) {
            if(punishment.getType().equals(DBPunishment.Type.MUTE)) return true;
        }
        return false;
    }

}
