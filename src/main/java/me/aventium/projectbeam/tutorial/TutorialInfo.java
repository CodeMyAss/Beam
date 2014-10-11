package me.aventium.projectbeam.tutorial;

import com.sk89q.minecraft.util.commands.ChatColor;
import org.bukkit.entity.Player;

public class TutorialInfo {

    protected final Player player;
    protected TutorialPhase phase;
    protected int phaseStep;
    protected int textStep;
    protected long sleep;

    public TutorialInfo(Player player, TutorialPhase phase) {
        this.player = player;
        this.phase = phase;
        this.textStep = 0;
        this.phaseStep = 0;

        this.sleep = (System.currentTimeMillis() + 3000L);
    }

    public boolean update() {
        if(!this.player.getLocation().equals(this.phase.location)) {
            this.player.teleport(this.phase.location);
        }

        if(System.currentTimeMillis() < this.sleep) {
            return false;
        }

        if(this.textStep >= this.phase.text.length) {
            this.phaseStep += 1;
            this.sleep = System.currentTimeMillis() + 2000L;
            return true;
        }

        String text = this.phase.text[this.textStep];

        this.player.sendMessage(" ");
        this.player.sendMessage(" ");
        this.player.sendMessage(" ");
        //this.player.sendMessage("§f§l===================");
        this.player.sendMessage("§c§l§n" + this.phase.topic);
        this.player.sendMessage(" ");

        for(int i = 0; i <= this.textStep; i++) {
            this.player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.phase.text[i]));
        }

        for(int i = this.textStep; i <= 5; i++) {
            this.player.sendMessage(" ");
        }

        //this.player.sendMessage("§f§l===================");

        if(text.length() > 0) {
            this.sleep = (System.currentTimeMillis() + 1000L + 50* text.length());
        } else {
            this.sleep = (System.currentTimeMillis() + 600L);
        }

        this.textStep += 1;
        return false;
    }

    public void setNextPhase(TutorialPhase phase) {
        this.phase = phase;
        this.textStep = 0;
        this.player.teleport(this.phase.location);
    }

}
