package me.aventium.projectbeam.listeners;

import com.sk89q.minecraft.util.commands.ChatColor;
import me.aventium.projectbeam.Beam;
import me.aventium.projectbeam.Database;
import me.aventium.projectbeam.collections.Servers;
import me.aventium.projectbeam.documents.DBServer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class LobbyListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.getPlayer().getInventory().clear();
        ItemStack lobbySelector = new ItemStack(Material.NETHER_STAR);
        ItemMeta itemMeta = lobbySelector.getItemMeta();
        itemMeta.setDisplayName("§cLobby Selector");
        lobbySelector.setItemMeta(itemMeta);

        event.getPlayer().getInventory().setItem(8, lobbySelector);

        event.getPlayer().setHealth(event.getPlayer().getMaxHealth());
        event.getPlayer().setFoodLevel(20);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if(event.getEntity() instanceof Player) event.setCancelled(true);
    }

    @EventHandler
    public void onFoodLose(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onOpen(PlayerInteractEvent event) {
        if (event.getPlayer().getItemInHand() == null || event.getPlayer().getItemInHand().getType().equals(Material.AIR))
            return;

        if (event.getPlayer().getItemInHand().getType().equals(Material.NETHER_STAR) &&
                event.getPlayer().getItemInHand().hasItemMeta() && event.getPlayer().getItemInHand().getItemMeta().hasDisplayName() &&
                event.getPlayer().getItemInHand().getItemMeta().getDisplayName().equals("§cLobby Selector")) {

            event.setCancelled(true);

            int amount = 0;

            for(DBServer server : Database.getCollection(Servers.class).findPublicServers("lobbies")) {
                amount++;
            }

            int size = 9;

            for(int i = 9; i < 54; i+=9) {
                if(amount < i && i > i-9) {
                    size = i;
                    break;
                }
            }

            Inventory inventory = Bukkit.createInventory(null, size, "§2§oLobby List");

            List<DBServer> serverList = Database.getCollection(Servers.class).findPublicServers("lobbies");
            HashMap<String, Integer> sortedList = new HashMap<>();

            if(serverList.size() > 0) {
                for(DBServer server : serverList) {
                    int position;
                    try {
                        position = Integer.parseInt(server.getBungeeName().toLowerCase().replace("lobby_", ""));
                    } catch(NumberFormatException ex) {position = serverList.size();}

                    sortedList.put(server.getName(), position);
                }

                for(String serverName : sortedList.keySet()) {
                    DBServer server = Database.getCollection(Servers.class).findPublicServer(serverName);
                    ItemStack itemStack = new ItemStack(Material.NETHER_STAR);
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    itemMeta.setDisplayName("§a" + server.getName().replaceAll("_", " "));
                    itemMeta.setLore(Arrays.asList(
                            "§fStatus: " + (server.isOnline() ? "§aOnline" : "§4Offline"),
                            "§fPlayers: " + (server.isOnline() ? server.getOnlinePlayers().size() : 0)
                    ));
                    itemStack.setItemMeta(itemMeta);
                    inventory.setItem(sortedList.get(serverName) - 1, itemStack);
                }
            }
            event.getPlayer().openInventory(inventory);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if(!(event.getWhoClicked() instanceof Player)) return;
        if(event.getInventory().getTitle().equals("§2§oLobby List")) {
            event.setCancelled(true);
            if(event.getCurrentItem() != null && event.getCurrentItem().getType() != null && !event.getCurrentItem().getType().equals(Material.AIR)) {
                ItemStack item = event.getCurrentItem();
                if(item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                    String itemName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
                    String serverName = "";
                    for(String str : itemName.split(" ")) {
                        serverName += str + "_";
                    }

                    serverName = serverName.substring(0, serverName.length() - 1);
                    DBServer server = Database.getCollection(Servers.class).findPublicServer(serverName);

                    if (server == null) {
                        ((Player) event.getWhoClicked()).sendMessage("§cServer '" + serverName + "' not found!");
                        return;
                    }

                    if(!server.isOnline()) {
                        ((Player) event.getWhoClicked()).sendMessage("§cThat server is currently offline!");
                        return;
                    }

                    ((Player) event.getWhoClicked()).sendMessage("§aTeleporting you to §l" + server.getName() + "§r§a!");
                    Beam.getInstance().getPortal().sendPlayerToServer((Player) event.getWhoClicked(), server.getBungeeName());
                }
            }
        }
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            Material type = event.getClickedBlock().getType();
            if (type.equals(Material.SIGN_POST) || type.equals(Material.WALL_SIGN)) {
                Sign state = (Sign) event.getClickedBlock().getState();
                String line1 = state.getLine(0);
                String line2 = state.getLine(1);
                String line3 = state.getLine(2);
                String line4 = state.getLine(3);

                if (line1.equals("§7[§cJoin§7]")) {
                    String serverName = ChatColor.stripColor(line2);

                    DBServer server = Database.getCollection(Servers.class).findPublicServer(serverName);

                    if (server == null) {
                        event.getPlayer().sendMessage("§cServer '" + serverName + "' not found!");
                        return;
                    }

                    if(!server.isOnline()) {
                        event.getPlayer().sendMessage("§cThat server is currently offline!");
                        return;
                    }

                    serverName = server.getName();

                    event.getPlayer().sendMessage("§aTeleporting you to §l" + server.getName() + "§r§a!");
                    Beam.getInstance().getPortal().sendPlayerToServer(event.getPlayer(), server.getBungeeName());
                }
            }
        }
    }

    @EventHandler
    public void onSignPlace(SignChangeEvent event) {
        if (event.getPlayer().isOp()) {
            String type = event.getLine(0);
            String serverName = event.getLine(1);

            if (type.equalsIgnoreCase("[Join]")) {
                DBServer server = Database.getCollection(Servers.class).findPublicServer(serverName);

                if (server == null) {
                    event.getPlayer().sendMessage("§cServer '" + serverName + "' not found!");
                    event.getBlock().breakNaturally();
                    return;
                }

                event.setLine(0, "§7[§cJoin§7]");
                event.setLine(1, "§c§l" + server.getName());
                event.setLine(3, (server.isOnline() ? "§aOnline" : "§cOffline"));
            }
        }
    }

}
