package me.aventium.projectbeam.tasks;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import java.util.concurrent.ConcurrentMap;

public class PollingTaskManager {
    protected final ConcurrentMap<PollingTask, PollingTaskRunner> tasks = Maps.newConcurrentMap();

    public void start(PollingTask task) {
        Preconditions.checkNotNull(task, "task may not be null");

        PollingTaskRunner newRunner = new PollingTaskRunner(task);
        PollingTaskRunner oldRunner = this.tasks.putIfAbsent(task, newRunner);
        if(oldRunner == null) {
            newRunner.start();
        } else {
            throw new IllegalArgumentException("task has already been started");
        }
    }

    public void stop(PollingTask task) {
        Preconditions.checkNotNull(task, "task may not be null");

        PollingTaskRunner runner = this.tasks.remove(task);
        if(runner != null) {
            runner.stop();
        }
    }

    public void stopAll(Class<? extends PollingTask> taskClass) {
        Preconditions.checkNotNull(taskClass, "task class may not be null");

        synchronized(this.tasks) {
            for(PollingTask task : this.tasks.keySet()) {
                if(taskClass.isInstance(task)) {
                    this.stop(task);
                }
            }
        }
    }

    public void stopAll() {
        synchronized(this.tasks) {
            for(PollingTask task : this.tasks.keySet()) {
                this.stop(task);
            }
        }
    }
}
