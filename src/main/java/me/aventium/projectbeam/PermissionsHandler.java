package me.aventium.projectbeam;

import me.aventium.projectbeam.collections.Groups;
import me.aventium.projectbeam.collections.Users;
import me.aventium.projectbeam.commands.DatabaseCommand;
import me.aventium.projectbeam.documents.DBGroup;
import me.aventium.projectbeam.documents.DBUser;
import me.aventium.projectbeam.events.PlayerGroupChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionsHandler implements Listener {

    public static void addAttachment(Plugin plugin, Permissible p, Map<String, Boolean> perms) {
        PermissionAttachment attachment = p.addAttachment(plugin);
        for (String perm : perms.keySet()) {
            attachment.setPermission(perm, perms.get(perm));
        }
        p.recalculatePermissions();
    }

    public static void removeAttachmentByNode(Permissible p, String node) {
        List<PermissionAttachment> toRemove = new ArrayList<>();
        for (PermissionAttachmentInfo info : p.getEffectivePermissions()) {
            if (info.getPermission().equalsIgnoreCase(node)) toRemove.add(info.getAttachment());
        }
        for (PermissionAttachment pat : toRemove) {
            try {
                p.removeAttachment(pat);
            } catch (IllegalArgumentException ex) {}
        }
        p.recalculatePermissions();
    }

    public static void givePermissions(Permissible p, DBUser user) {
        PermissionAttachment attachment = p.addAttachment(Beam.getInstance());

        HashMap<String, Boolean> toGive = new HashMap<>();

        DBGroup group = user.getGroup();

        for (String permission : group.getPermissions().keySet()) {
            toGive.put(permission, group.getPermissions().get(permission));
        }
        attachment.setPermission("beam.group." + group.getName(), true);

        for(String perm : user.getPermissions().keySet()) {
            toGive.put(perm, user.getPermissions().get(perm));
        }
        for (Map.Entry<String, Boolean> entry : toGive.entrySet()) {
            attachment.setPermission(entry.getKey(), entry.getValue());
        }
        p.recalculatePermissions();
    }

    public static void givePlayerPermissions(Permissible p, DBUser user) {

    }

    public static void removeGroupPermissions(Permissible p) {
        if (p == null || p.getEffectivePermissions() == null || p.getEffectivePermissions().size() == 0) return;
        List<PermissionAttachment> toRemove = new ArrayList<>();
        for (PermissionAttachmentInfo info : p.getEffectivePermissions()) {
            if ((info != null) && info.getPermission().contains("beam.group."))
                toRemove.add(info.getAttachment());
        }

        for(PermissionAttachment pa : toRemove) {
            p.removeAttachment(pa);
        }
        p.recalculatePermissions();
    }

    public static void removeGroupPermissions(Permissible p, String groupName) {
        removeAttachmentByNode(p, "beam.group." + groupName);
    }

    public static boolean hasGroupPermissions(Permissible p, String groupName) {
        for (PermissionAttachmentInfo info : p.getEffectivePermissions()) {
            if (info.getPermission().equalsIgnoreCase("beam.group." + groupName)) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onGroupChange(PlayerGroupChangeEvent event) {
        if(Bukkit.getPlayer(event.getPlayer().getUsername()) != null) {
            Player player = Bukkit.getPlayer(event.getPlayer().getUsername());

            removeGroupPermissions(player);
            givePermissions(player, event.getPlayer());

            String prefix = (event.getNewGroup().getPrefix() == null ? ChatColor.translateAlternateColorCodes('&', "&f") : ChatColor.translateAlternateColorCodes('&', event.getNewGroup().getPrefix()));

            final String n = prefix + player.getName();

            player.setPlayerListName(n.length() >= 16 ? n.substring(0, 15) : n);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(final PlayerJoinEvent event) {
        Database.getExecutorService().submit(new DatabaseCommand() {
            @Override
            public void run() {
                DBUser user = Database.getCollection(Users.class).findOrCreateByName(event.getPlayer().getName(), event.getPlayer().getUniqueId());

                DBGroup group = user.getGroup();
                String prefix = "";

                givePermissions(event.getPlayer(), user);

                prefix = (group.getPrefix() == null ? ChatColor.translateAlternateColorCodes('&', "&f") : ChatColor.translateAlternateColorCodes('&', group.getPrefix()));

                final String n = prefix + event.getPlayer().getName();

                event.getPlayer().setPlayerListName(n.length() >= 16 ? n.substring(0, 15) : n);
            }
        });

    }

    /*@EventHandler
    public void onNT(final AsyncPlayerReceiveNameTagEvent event) {
        Database.getExecutorService().submit(new DatabaseCommand() {
            @Override
            public void run() {
                DBUser user = Database.getCollection(Users.class).findByName(event.getPlayer().getName());
                if (user != null) {
                    if (user.getGroups() == null || user.getGroups().size() == 0) {
                        user.addGroup(Database.getCollection(Groups.class).getDefaultGroup());
                        Database.getCollection(Users.class).save(user);
                    }
                    for (DBGroup group : user.getGroups()) {

                        DBGroup network = null;
                        DBGroup server = null;
                        String prefix = "";
                        for (DBGroup gg : user.getGroups()) {
                            if (gg.getFamily().equalsIgnoreCase("network")) network = gg;
                            else if (gg.getFamily().equalsIgnoreCase(Database.getServer().getFamily())) server = gg;
                        }

                        if (network != null && network != Database.getCollection(Groups.class).getDefaultGroup()) {
                            prefix = network.getPrefix() == null ? ChatColor.translateAlternateColorCodes('&', "&f") : ChatColor.translateAlternateColorCodes('&', network.getPrefix());
                        }

                        if (server != null && server.getPrefix() != null) {
                            prefix = server.getPrefix() == null ? ChatColor.translateAlternateColorCodes('&', "&f") : ChatColor.translateAlternateColorCodes('&', server.getPrefix());
                        }

                        String n = prefix + event.getPlayer().getName();

                        event.setTag(n.length() >= 16 ? n.substring(0, 16) : n);
                    }
                }
            }
        });
    }*/

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        DBUser user = Database.getCollection(Users.class).findByName(event.getPlayer().getName());

        DBGroup group = user.getGroup();

        if(group == null) group = Database.getCollection(Groups.class).getDefaultGroup();

        String prefix = null;

        prefix = (group.getPrefix() == null ? ChatColor.translateAlternateColorCodes('&', "&f") : ChatColor.translateAlternateColorCodes('&', group.getPrefix()));

        event.setFormat(prefix + "%1$s §7> §r%2$s");
    }


}
