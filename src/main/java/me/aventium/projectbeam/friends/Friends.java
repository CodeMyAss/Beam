package me.aventium.projectbeam.friends;

import com.sk89q.bukkit.util.CommandsManagerRegistration;
import me.aventium.projectbeam.tasks.PollingTaskManager;

/**
 * Experimental cross-server friends server, allowing you to see when your friends were
 * last online, be notified when a friend logs in to the network, and more.
 * @author Aventium
 */
public class Friends {

    public static void init(CommandsManagerRegistration cmdRegister, PollingTaskManager taskManager) {
        cmdRegister.register(BaseFriendCommands.class);
        //taskManager.start(new FriendLoginTask());
    }

}
