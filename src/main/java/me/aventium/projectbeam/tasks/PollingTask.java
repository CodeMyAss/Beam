package me.aventium.projectbeam.tasks;

import org.joda.time.Duration;
import org.joda.time.Interval;

public abstract class PollingTask {
    public abstract void process(Interval interval);

    public abstract Duration getPollingInterval();
}
