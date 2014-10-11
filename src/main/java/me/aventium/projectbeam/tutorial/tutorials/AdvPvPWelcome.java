package me.aventium.projectbeam.tutorial.tutorials;

import me.aventium.projectbeam.Beam;
import me.aventium.projectbeam.tutorial.Tutorial;
import me.aventium.projectbeam.tutorial.TutorialPhase;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;

public class AdvPvPWelcome extends Tutorial {

    public AdvPvPWelcome() {
        super("§a§lTutorial - $500 Reward", 500);

        final Villager V = (Villager) Bukkit.getWorld("world").spawnEntity(new Location(Bukkit.getWorld("world"), -3.5, 77, 4.5, 223.8F, 10.8F), EntityType.VILLAGER);
        V.setCustomName("§a§lTutorial - $500 Reward");
        V.setCustomNameVisible(true);
        V.setNoDamageTicks(Integer.MAX_VALUE);
        V.setProfession(Villager.Profession.FARMER);

        Bukkit.getScheduler().runTaskTimer(Beam.getInstance(), new Runnable() {
            @Override
            public void run() {
                V.teleport(new Location(Bukkit.getWorld("world"), -3.5, 77, 4.5, 223.8F, 10.8F));
            }
        }, 1L, 1L);

        this.phases.add(new TutorialPhase(
                new Location(Bukkit.getWorld("world"), -37.5, 52, -38.5, 180F, 0F), "§c§l§nMushroom Soup", new String[]{
                        "§c§lIt heals 3.5 hearts when consumed!",
                        "§c§lStock up on it, it's vital for PvP!"}
                )
        );

        this.phases.add(new TutorialPhase(
                        new Location(Bukkit.getWorld("world"), -16.5, 55, -38.5, 180F, 0F), "§c§l§nTeams", new String[]{
                        "§c§lThe group system of AdvPvP!",
                        "§c§lUse /team to learn more!"}
                )
        );

        this.phases.add(new TutorialPhase(
                        new Location(Bukkit.getWorld("world"), 1.5, 55, -38.5, 180F, 0F), "§c§l§nTracking", new String[]{
                        "§c§lThis is a tracker!",
                        "§c§lYou MUST be on the diamond block to track!",
                        "§c§lTry typing \"/track all\" now!"}
                )
        );

        this.phases.add(new TutorialPhase(
                        new Location(Bukkit.getWorld("world"), 22, 57, -38.5, 180F, 0F), "§c§l§nWarps", new String[]{
                        "§c§lAdvPvP uses a personal warp system.",
                        "§c§lUse /go set \"name\" to set a warp!",
                        "§c§lUse \"/go\" to learn more!"}
                )
        );

        this.phases.add(new TutorialPhase(
                        new Location(Bukkit.getWorld("world"), 40, 54, -38.5, 180F, 0F), "§c§l§nEconomy", new String[]{
                        "§c§lIf you ever need a quick item...",
                        "§c§lTry /buy! The format used is:",
                        "§c§l/buy (item) (amount)"}
        )
        );
    }

}
