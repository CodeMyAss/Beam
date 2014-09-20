package me.aventium.projectbeam.tasks;

import com.google.common.base.Preconditions;
import com.mongodb.MongoException;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.Interval;

public class PollingTaskRunner implements Runnable {
    protected final PollingTask task;
    protected Thread thread = null;
    protected boolean running = false;

    public PollingTaskRunner(PollingTask task) {
        this.task = task;
    }

    public synchronized boolean isRunning() {
        return this.running;
    }

    public synchronized void start() {
        Preconditions.checkState(!this.running, "task is already running");
        this.running = true;
        this.thread = new Thread(this);
        this.thread.start();
    }

    public synchronized void stop() {
        Preconditions.checkState(this.running, "task is not running");
        this.running = false;
        this.thread.interrupt();
        this.thread = null;
    }

    @Override
    public void run() {
        Instant lastRun = Instant.now();
        while(this.running) {
            Instant now = Instant.now();

            try {
                this.task.process(new Interval(lastRun, now));
            } catch (MongoException.Network e) {
                // silently ignore
            } catch (Throwable t) {
                t.printStackTrace();
            }

            // sleep till next cycle
            lastRun = now;
            Instant sleepTo = now.plus(this.task.getPollingInterval());

            long sleepMillis = new Duration(Instant.now(), sleepTo).getMillis();
            if(sleepMillis > 0) {
                try {
                    Thread.sleep(sleepMillis);
                } catch (InterruptedException e) {
                    // ignore - running flag will be off if it's necessary to exit
                }
            }
        }
    }
}
