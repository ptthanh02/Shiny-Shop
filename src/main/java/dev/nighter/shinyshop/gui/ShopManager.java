package dev.nighter.shinyshop.gui;

import dev.nighter.shinyshop.ShinyShop;
import dev.nighter.shinyshop.model.Shop;
import dev.nighter.shinyshop.model.ShopItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShopManager {
    private final ShinyShop plugin;
    private final Map<UUID, ShopSession> playerSessions = new HashMap<>();

    public ShopManager(ShinyShop plugin) {
        this.plugin = plugin;

        // Register events
        Bukkit.getPluginManager().registerEvents(new ShopGUIListener(this), plugin);
    }

    public void openShop(Player player, String shopId) {
        Shop shop = plugin.getConfigManager().getShop(shopId);
        if (shop == null) {
            player.sendMessage("§cShop not found: " + shopId);
            plugin.getLogger().warning("Shop not found: " + shopId);
            plugin.getLogger().warning("Available shops: " + String.join(", ", plugin.getConfigManager().getAllShops().keySet()));
            return;
        }

        // Create inventory
        int size = shop.getRows() * 9;
        Inventory inventory = Bukkit.createInventory(null, size, shop.getTitle());

        // Add decorative items (glass panes)
        fillDecorativeItems(inventory, shop);

        // Add GUI items (like close button, back button)
        for (ShopItem guiItem : shop.getGuiItems().values()) {
            if (guiItem.getSlot() >= 0 && guiItem.getSlot() < size) {
                inventory.setItem(guiItem.getSlot(), guiItem.createItemStack());
            }
        }

        // Add sub-shop items
        for (Map.Entry<String, ShopItem> entry : shop.getSubShopItems().entrySet()) {
            ShopItem subShopItem = entry.getValue();
            if (subShopItem.getSlot() >= 0 && subShopItem.getSlot() < size) {
                inventory.setItem(subShopItem.getSlot(), subShopItem.createItemStack());
            }
        }

        // Add shop items
        for (ShopItem shopItem : shop.getShopItems().values()) {
            if (shopItem.getSlot() >= 0 && shopItem.getSlot() < size) {
                inventory.setItem(shopItem.getSlot(), shopItem.createItemStack());
            }
        }

        // Store session, keep track of current shop
        playerSessions.put(player.getUniqueId(), new ShopSession(shopId));

        // Debug log
        plugin.getLogger().info("Opening shop: " + shopId + " for player: " + player.getName());

        // Open inventory
        player.openInventory(inventory);
    }

    private void fillDecorativeItems(Inventory inventory, Shop shop) {
        // Check if decorative item is set to "none" or null
        String decorativeItemStr = shop.getDecorativeItem();
        if (decorativeItemStr == null || decorativeItemStr.equalsIgnoreCase("none")) {
            // Skip decoration if set to none
            return;
        }

        // Get the material for decoration
        Material decorativeMaterial = Material.BLACK_STAINED_GLASS_PANE; // Default
        try {
            decorativeMaterial = Material.valueOf(decorativeItemStr);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid decorative item material: " + decorativeItemStr);
        }

        // Create item stack with empty name and lore
        ItemStack decorItem = new ItemStack(decorativeMaterial);
        org.bukkit.inventory.meta.ItemMeta meta = decorItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" "); // Set blank display name
            meta.setLore(null); // Clear any lore
            decorItem.setItemMeta(meta);
        }

        // Fill empty slots with decorative item
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                boolean isItemSlot = false;

                // Check if this slot is used by a GUI item
                for (ShopItem item : shop.getGuiItems().values()) {
                    if (item.getSlot() == i) {
                        isItemSlot = true;
                        break;
                    }
                }

                // Check if this slot is used by a sub-shop item
                if (!isItemSlot) {
                    for (ShopItem item : shop.getSubShopItems().values()) {
                        if (item.getSlot() == i) {
                            isItemSlot = true;
                            break;
                        }
                    }
                }

                // Check if this slot is used by a shop item
                if (!isItemSlot) {
                    for (ShopItem item : shop.getShopItems().values()) {
                        if (item.getSlot() == i) {
                            isItemSlot = true;
                            break;
                        }
                    }
                }

                if (!isItemSlot) {
                    inventory.setItem(i, decorItem);
                }
            }
        }
    }


    public void handleClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        ShopSession session = playerSessions.get(player.getUniqueId());
        if (session == null) {
            return;
        }

        Shop shop = plugin.getConfigManager().getShop(session.getCurrentShopId());
        if (shop == null) {
            return;
        }

        // Debug log
        plugin.getLogger().info("Handling click for player " + player.getName() +
                " in shop " + session.getCurrentShopId() +
                " at slot " + slot);

        // Cancel the event to prevent taking items
        event.setCancelled(true);

        // Handle GUI items (like close button)
        for (ShopItem guiItem : shop.getGuiItems().values()) {
            if (guiItem.getSlot() == slot) {
                handleItemClick(player, guiItem, shop);
                return;
            }
        }

        // Handle sub-shop items
        for (Map.Entry<String, ShopItem> entry : shop.getSubShopItems().entrySet()) {
            ShopItem subShopItem = entry.getValue();
            if (subShopItem.getSlot() == slot) {
                handleItemClick(player, subShopItem, shop);

                // Open the sub-shop if the action is "open"
                if (subShopItem.getOnClick() != null && subShopItem.getOnClick().startsWith("open")) {
                    String subShopKey = entry.getKey();
                    String subShopId = shop.getId() + "." + subShopKey;
                    // Debug log
                    plugin.getLogger().info("Attempting to open sub-shop: " + subShopId);
                    plugin.getLogger().info("Current shop ID: " + shop.getId());

                    openShop(player, subShopId);
                }
                return;
            }
        }

        // Handle shop items
        for (ShopItem shopItem : shop.getShopItems().values()) {
            if (shopItem.getSlot() == slot) {
                handleItemClick(player, shopItem, shop);

                // For now, just send a message as a placeholder for buying/selling
                if (shopItem.canBuy()) {
                    player.sendMessage("§aBuying " + shopItem.getName() + " for $" + shopItem.getBuyPrice());
                } else {
                    player.sendMessage("§cThis item cannot be purchased.");
                }
                return;
            }
        }
    }

    private void handleItemClick(Player player, ShopItem item, Shop shop) {
        // Play sound
        if (item.getClickSound() != null) {
            try {
                Sound sound = Sound.valueOf(item.getClickSound());
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            } catch (IllegalArgumentException e) {
                // Ignore invalid sound
            }
        }

        // Handle click action
        if (item.getOnClick() != null) {
            String action = item.getOnClick().toLowerCase();

            if (action.equals("close")) {
                player.closeInventory();
                playerSessions.remove(player.getUniqueId());
            } else if (action.equals("back")) {
                String parentId = shop.getParentId();
                if (parentId != null) {
                    // Debug log
                    plugin.getLogger().info("Going back to parent shop: " + parentId);
                    openShop(player, parentId);
                } else {
                    player.closeInventory();
                    playerSessions.remove(player.getUniqueId());
                }
            }
            // Note: The "open" action is handled in handleClick method
        }
    }

    public Map<UUID, ShopSession> getPlayerSessions() {
        return playerSessions;
    }

    public ShinyShop getPlugin() {
        return plugin;
    }
}