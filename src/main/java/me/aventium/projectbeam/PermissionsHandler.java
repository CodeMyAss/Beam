package me.aventium.projectbeam;

import me.aventium.projectbeam.collections.Groups;
import me.aventium.projectbeam.collections.Users;
import me.aventium.projectbeam.commands.DatabaseCommand;
import me.aventium.projectbeam.documents.DBGroup;
import me.aventium.projectbeam.documents.DBUser;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import java.util.Map;

public class PermissionsHandler implements Listener {

    public static void addAttachment(Plugin plugin, Permissible p, Map<String, Boolean> perms) {
        PermissionAttachment attachment = p.addAttachment(plugin);
        for(String perm : perms.keySet()) {
            attachment.setPermission(perm, perms.get(perm));
        }
        p.recalculatePermissions();
    }

    public static void removeAttachmentByNode(Permissible p, String node) {
        for(PermissionAttachmentInfo info : p.getEffectivePermissions()) {
            if(info.getPermission().equalsIgnoreCase(node)) p.removeAttachment(info.getAttachment());
        }
        p.recalculatePermissions();
    }

    public static void giveGroupPermissions(Permissible p, DBGroup group) {
        PermissionAttachment attachment = p.addAttachment(Beam.getInstance());
        attachment.setPermission("group." + group.getName(), true);

        for(String permission : group.getPermissions().keySet()) {
            attachment.setPermission(permission, group.getPermissions().get(permission));
        }
        p.recalculatePermissions();
    }

    public static void removeGroupPermissions(Permissible p) {
        if(p.getEffectivePermissions() == null || p.getEffectivePermissions().size() == 0) return;
        for(PermissionAttachmentInfo info : p.getEffectivePermissions()) {
            if((info != null) && info.getPermission().contains("group.")) p.removeAttachment(info.getAttachment());
        }
        p.recalculatePermissions();
    }

    public static void removeGroupPermissions(Permissible p, String groupName) {
        removeAttachmentByNode(p, "group." + groupName);
    }

    public static boolean hasGroupPermissions(Permissible p, String groupName) {
        for(PermissionAttachmentInfo info : p.getEffectivePermissions()) {
            if(info.getPermission().equalsIgnoreCase("group." + groupName)) {
                return true;
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(final PlayerJoinEvent event) {
        Database.getExecutorService().submit(new DatabaseCommand() {
            @Override
            public void run() {
                DBUser user = Database.getCollection(Users.class).findByName(event.getPlayer().getName());
                if(user != null) {
                    if(user.getGroup() == null) {
                        user.setGroup(Database.getCollection(Groups.class).getDefaultGroup());
                        Database.getCollection(Users.class).save(user);
                    }
                    removeGroupPermissions(event.getPlayer(), user.getGroup().getName());
                    giveGroupPermissions(event.getPlayer(), user.getGroup());
                }
            }
        });

    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        DBUser user = Database.getCollection(Users.class).findByName(event.getPlayer().getName());
        String prefix = (user.getGroup() == null || user.getGroup().getPrefix() == null ? ChatColor.translateAlternateColorCodes('&', "&f") : ChatColor.translateAlternateColorCodes('&', user.getGroup().getPrefix()));
        event.setFormat(prefix + "%1$s§7> §r%2$s");
    }


}
