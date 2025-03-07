package dev.nighter.shinyshop.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class ShopGUIListener implements Listener {
    private final ShopManager shopManager;

    public ShopGUIListener(ShopManager shopManager) {
        this.shopManager = shopManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (shopManager.getPlayerSessions().containsKey(player.getUniqueId())) {
            shopManager.handleClick(event);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (shopManager.getPlayerSessions().containsKey(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        // Delay session removal to check if another inventory is being opened
        Bukkit.getScheduler().runTaskLater(shopManager.getPlugin(), () -> {
            if (!player.getOpenInventory().getTopInventory().equals(event.getInventory())) {
                // Player opened another inventory, don't remove session
                return;
            }
            shopManager.getPlayerSessions().remove(player.getUniqueId());
        }, 2L); // Wait 2 ticks to check for other inventory opens
    }
}