package me.aventium.projectbeam.tutorial;

import io.netty.util.internal.ConcurrentSet;
import me.aventium.projectbeam.Database;
import me.aventium.projectbeam.tutorial.tutorials.AdvPvPWelcome;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.plugin.Plugin;

public class TutorialManager implements Listener {

    protected final ConcurrentSet<Tutorial> tutorials;
    protected final TutorialTick tick;

    public TutorialManager(Plugin plugin) {
        this.tutorials = new ConcurrentSet<>();
        this.tick = new TutorialTick(this);

        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        if(Database.getServer().getFamily().equalsIgnoreCase("AdvancedPvP")) tutorials.add(new AdvPvPWelcome());
    }

    public boolean inTutorial(Player player) {
        for(Tutorial tutorial : this.tutorials) {
            if(tutorial.inTutorial.containsKey(player.getName())) return true;
        }
        return false;
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        if(inTutorial(event.getPlayer())) return;

        if(!(event.getRightClicked() instanceof LivingEntity)) return;

        LivingEntity entity = (LivingEntity) event.getRightClicked();

        String name = entity.getCustomName();
        if(name == null) return;

        for(Tutorial tutorial : this.tutorials) {
            if(name.contains(tutorial.getName())) {

                event.setCancelled(true);
                event.getPlayer().closeInventory();

                event.getPlayer().sendMessage("§6You have started the " + tutorial.getName() + " tutorial!");
                tutorial.begin(event.getPlayer());
                return;
            }
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        for(Tutorial tutorial : this.tutorials) {
            tutorial.end(event.getPlayer());
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        for(Tutorial tutorial : this.tutorials) {
            for(Player player : tutorial.inTutorial.keySet()) {
                event.getRecipients().remove(player);
            }
        }
    }

    @EventHandler
    public void cancelInteract(PlayerInteractEvent event) {
        if(inTutorial(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void cancelMove(PlayerMoveEvent event) {
        if(inTutorial(event.getPlayer())) {
            event.setCancelled(true);
        }
    }



}
