package me.aventium.projectbeam.tutorial;

import org.bukkit.Location;

public class TutorialPhase {

    protected final Location location;
    protected final String topic;
    protected final String[] text;

    public TutorialPhase(Location location, String topic, String[] text) {
        this.location = location;
        this.topic = topic;
        this.text = text;
    }

}
