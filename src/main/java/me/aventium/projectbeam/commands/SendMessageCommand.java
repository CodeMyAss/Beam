package me.aventium.projectbeam.commands;

import me.aventium.projectbeam.channels.SyncCommand;
import org.bukkit.command.CommandSender;

public class SendMessageCommand extends SyncCommand {
    protected final CommandSender recipient;
    protected final String message;

    public SendMessageCommand(CommandSender recipient, String message) {
        this.recipient = recipient;
        this.message = message;
    }

    @Override
    public void run() {
        this.recipient.sendMessage(this.message);
    }
}
