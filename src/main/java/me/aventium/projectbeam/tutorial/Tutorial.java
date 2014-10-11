package me.aventium.projectbeam.tutorial;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Tutorial {

    protected final String name;
    protected final int reward;
    protected final List<TutorialPhase> phases = new ArrayList<>();
    protected final ConcurrentHashMap<Player, TutorialInfo> inTutorial = new ConcurrentHashMap<>();

    public Tutorial(String name, int reward) {
        this.name = name;
        this.reward = reward;

    }

    public String getName() {
        return name;
    }

    public int getReward() {
        return reward;
    }

    public void begin(Player player) {
        this.inTutorial.put(player, new TutorialInfo(player, this.phases.get(0)));
    }

    public void end(Player player) {
        this.inTutorial.remove(player.getName());
    }

    public List<TutorialPhase> getPhases() {
        return phases;
    }

}
