package me.aventium.projectbeam.channels;

import me.aventium.projectbeam.Beam;
import me.aventium.projectbeam.commands.Command;

public abstract class SyncCommand extends Command {
    @Override
    public void execute() {
        if(Beam.getMainThread() == Thread.currentThread()) {
            this.run();
        } else {
            ChannelManager.getSyncChannel().queue(this);
        }
    }
}
