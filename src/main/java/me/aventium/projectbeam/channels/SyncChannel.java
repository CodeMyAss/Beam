package me.aventium.projectbeam.channels;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SyncChannel implements Channel<Runnable>, Runnable {
    private final BlockingQueue<Runnable> queue;

    public SyncChannel() {
        this.queue = new LinkedBlockingQueue<>(); //Queues.newLinkedBlockingQueue();
    }

    @Override
    public boolean queue(Runnable obj) {
        return this.queue.offer(obj);
    }

    @Override
    public void flush() {
        while(true) {
            Runnable command = this.queue.poll(); // returns null if no command awaits

            if(command != null) {
                command.run();
            } else {
                break; // done processing commands
            }
        }
    }

    @Override
    public void shutdown() {
        // silently ignore
    }

    @Override
    public void terminate() {
        // silently ignore
    }

    @Override
    public void run() {
        this.flush();
    }
}