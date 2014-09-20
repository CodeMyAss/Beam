package me.aventium.projectbeam.commands;

import me.aventium.projectbeam.Beam;
import me.aventium.projectbeam.Database;

public abstract class DatabaseCommand extends Command {

    @Override
    public void execute() {
        if(Beam.getMainThread() == Thread.currentThread()) {
            Database.getExecutorService().submit(this);
        } else {
            this.run();
        }
    }
}
