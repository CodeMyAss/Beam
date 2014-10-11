package me.aventium.projectbeam.tutorial;

import me.aventium.projectbeam.Beam;
import me.aventium.projectbeam.Database;
import me.aventium.projectbeam.collections.Users;
import me.aventium.projectbeam.documents.DBUser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TutorialTick implements Runnable {

    TutorialManager manager;

    public TutorialTick(TutorialManager manager) {
        this.manager = manager;
        Bukkit.getScheduler().runTaskTimer(Beam.getInstance(), this, 1L, 1L);
    }

    @Override
    public void run() {
        for(Tutorial tutorial : manager.tutorials) {
            for(Player player : tutorial.inTutorial.keySet()) {
                TutorialInfo info = tutorial.inTutorial.get(player);

                for(Player plr : Bukkit.getOnlinePlayers()) {
                    if(plr != player) player.hidePlayer(plr);
                }

                if(info.update()) {
                    if(info.phaseStep < tutorial.getPhases().size()) {
                        info.setNextPhase(tutorial.getPhases().get(info.phaseStep));
                    } else {

                        tutorial.inTutorial.remove(player);

                        DBUser user = Database.getCollection(Users.class).findByName(player.getName());

                        for(Player plr : Bukkit.getOnlinePlayers()) {
                            player.showPlayer(plr);
                        }

                        if(user.getTutorialsCompleted().contains(tutorial.getName())) {
                            player.sendMessage("§cYou have already completed this tutorial, so no reward this time!");
                            player.teleport(new Location(Bukkit.getWorld("world"), 0.5, 77, 0.5));
                            return;
                        }

                        user.completeTutorial(tutorial.getName());
                        Database.getCollection(Users.class).save(user);

                        player.sendMessage("§6You have completed " + tutorial.getName() + "! You have been awarded $" + tutorial.getReward() + "!");
                        player.teleport(new Location(Bukkit.getWorld("world"), 0.5, 77, 0.5));

                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + player.getName() + " " + tutorial.getReward());
                    }
                }
            }
        }
    }
}
