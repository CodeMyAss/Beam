package me.aventium.projectbeam;

import me.aventium.projectbeam.collections.Groups;
import me.aventium.projectbeam.collections.Users;
import me.aventium.projectbeam.commands.DatabaseCommand;
import me.aventium.projectbeam.config.file.FileConfiguration;
import me.aventium.projectbeam.config.file.YamlConfiguration;
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
import org.kitteh.tag.AsyncPlayerReceiveNameTagEvent;

import java.io.File;
import java.io.IOException;
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

    public static void giveGroupPermissions(Permissible p, DBGroup group) {
        PermissionAttachment attachment = p.addAttachment(Beam.getInstance());

        HashMap<String, Boolean> toGive = new HashMap<>();

        for (String permission : group.getPermissions().keySet()) {
            toGive.put(permission, group.getPermissions().get(permission));
        }
        attachment.setPermission("beam.group." + group.getName(), true);
        for (String pa : toGive.keySet()) {
            attachment.setPermission(pa, toGive.get(pa));
        }
        p.recalculatePermissions();
    }

    public static void removeGroupPermissions(Permissible p) {
        if (p.getEffectivePermissions() == null || p.getEffectivePermissions().size() == 0) return;
        for (PermissionAttachmentInfo info : p.getEffectivePermissions()) {
            if ((info != null) && info.getPermission().contains("beam.group."))
                p.removeAttachment(info.getAttachment());
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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(final PlayerJoinEvent event) {
        Database.getExecutorService().submit(new DatabaseCommand() {
            @Override
            public void run() {
                DBUser user = Database.getCollection(Users.class).findByName(event.getPlayer().getName());
                if (user == null) {
                    event.getPlayer().sendMessage("Please relog to fix your permissions for the first time.");
                    return;
                }

                File file = new File(Beam.getInstance().getDataFolder(), "newperms.yml");
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                if(config.getString(event.getPlayer().getName()) != null) {
                    user.addGroup(Database.getCollection(Groups.class).findGroup(config.getString(event.getPlayer().getName()), Database.getServer().getFamily(), null));
                    Database.getCollection(Users.class).save(user);
                    System.out.println("Gave " + event.getPlayer().getName() + " old rank: " + config.getString(event.getPlayer().getName()));
                    try {
                        config.save(file);
                    } catch(IOException ex) {
                        ex.printStackTrace();
                    }
                }

                DBGroup network = null;
                DBGroup server = null;
                String prefix = "";

                for (DBGroup group : user.getGroups()) {
                    for(PermissionAttachmentInfo pai : event.getPlayer().getEffectivePermissions()) {
                        if(pai.getAttachment() != null && event.getPlayer().hasPermission(pai.getPermission()))
                            try {
                                event.getPlayer().removeAttachment(pai.getAttachment());
                            } catch(IllegalArgumentException ex) {}
                    }
                    giveGroupPermissions(event.getPlayer(), group);
                }

                for (DBGroup gg : user.getGroups()) {
                    if (gg.getFamily().equalsIgnoreCase("network")) network = gg;
                    else if (gg.getFamily().equalsIgnoreCase(Database.getServer().getFamily())) server = gg;
                }

                if (network != null && network != Database.getCollection(Groups.class).getDefaultGroup()) {
                    prefix = network.getPrefix() == null ? ChatColor.translateAlternateColorCodes('&', "&f") : ChatColor.translateAlternateColorCodes('&', network.getPrefix());
                }

                if (server != null && server.getPrefix() != null) {
                    prefix = server.getPrefix() == null ? (network.getPrefix() == null ? ChatColor.translateAlternateColorCodes('&', "&f") : ChatColor.translateAlternateColorCodes('&', network.getPrefix())): ChatColor.translateAlternateColorCodes('&', server.getPrefix());
                }

                String n = prefix + event.getPlayer().getName();
                event.getPlayer().sendMessage("P: " + (n.length() >= 16 ? n.substring(0, 15) : n));

                event.getPlayer().setPlayerListName(n.length() >= 16 ? n.substring(0, 15) : n);
            }
        });

    }

    @EventHandler
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
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        DBUser user = Database.getCollection(Users.class).findByName(event.getPlayer().getName());

        DBGroup hasPexGroup = null;

        File f = new File(Beam.getInstance().getDataFolder(), "newperms");
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(f);

        if(configuration.getString(user.getUsername()) != null) {
            String g = configuration.getString(user.getUsername());

            if (g != null) {
                if (Database.getCollection(Groups.class).findGroup(g, Database.getServer().getFamily(), null) != null) {
                    hasPexGroup = Database.getCollection(Groups.class).findGroup(g, Database.getServer().getFamily(), null);
                }
            }

        }

        if (hasPexGroup != null && !user.getGroups().contains(hasPexGroup)) {
            System.out.println("Giving previous rank to " + user.getUsername() + ": " + hasPexGroup.getName());
            user.addGroup(hasPexGroup);
            Database.getCollection(Users.class).save(user);
        }

        DBGroup network = null;
        DBGroup server = null;
        for (DBGroup group : user.getGroups()) {
            if (group.getFamily().equalsIgnoreCase("network")) network = group;
            else if (group.getFamily().equalsIgnoreCase(Database.getServer().getFamily())) server = group;
        }

        String prefix = null;

        if (network != null && network != Database.getCollection(Groups.class).getDefaultGroup()) {
            prefix = network.getPrefix() == null ? ChatColor.translateAlternateColorCodes('&', "&f") : ChatColor.translateAlternateColorCodes('&', network.getPrefix());
        }

        if (server != null && server.getPrefix() != null && prefix != null) {
            prefix = server.getPrefix() == null ? network.getPrefix() == null ? ChatColor.translateAlternateColorCodes('&', "&f") : ChatColor.translateAlternateColorCodes('&', network.getPrefix()) : ChatColor.translateAlternateColorCodes('&', server.getPrefix());
        }
        event.setFormat(prefix + "%1$s §7> §r%2$s");
    }


}
