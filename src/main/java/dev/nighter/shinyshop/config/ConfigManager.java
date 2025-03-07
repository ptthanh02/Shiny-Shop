package dev.nighter.shinyshop.config;

import dev.nighter.shinyshop.ShinyShop;
import dev.nighter.shinyshop.model.Shop;
import dev.nighter.shinyshop.model.ShopItem;
import dev.nighter.shinyshop.utils.ColorUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class ConfigManager {
    private final ShinyShop plugin;
    private final Map<String, Shop> shops = new HashMap<>();
    private final Map<String, ItemWorth> itemWorths = new HashMap<>();

    public ConfigManager(ShinyShop plugin) {
        this.plugin = plugin;
    }

    public void loadConfigurations() {
        // Create shops directory if it doesn't exist
        File shopsDir = new File(plugin.getDataFolder(), "shops");
        if (!shopsDir.exists()) {
            shopsDir.mkdirs();
            // Save default shops
            plugin.saveResource("shops/shops.yml", false);
            plugin.saveResource("shops/food/food.yml", false);
        }

        // Load worth.yml
        File worthFile = new File(plugin.getDataFolder(), "worth.yml");
        if (!worthFile.exists()) {
            plugin.saveResource("worth.yml", false);
        }

        loadWorthConfig(worthFile);

        // Load main shop and all subshops
        // Main shop ID is just "shops"
        loadShops(shopsDir, "shops");

        plugin.getLogger().info("Loaded " + shops.size() + " shops!");

        // Debug: print all loaded shops
        for (String shopId : shops.keySet()) {
            plugin.getLogger().info("Loaded shop ID: " + shopId);
        }
    }

    private void loadWorthConfig(File worthFile) {
        FileConfiguration worthConfig = YamlConfiguration.loadConfiguration(worthFile);

        for (String itemId : worthConfig.getKeys(false)) {
            ConfigurationSection section = worthConfig.getConfigurationSection(itemId);
            if (section == null) continue;

            double sellPrice = section.getDouble("sell", -1);
            double buyPrice = section.getDouble("buy", -1);

            itemWorths.put(itemId, new ItemWorth(sellPrice, buyPrice));
        }

        plugin.getLogger().info("Loaded worth data for " + itemWorths.size() + " items!");
    }

    private void loadShops(File directory, String shopId) {
        File[] files = directory.listFiles();
        if (files == null) return;

        // First load the main shop file in this directory
        for (File file : files) {
            if (file.isFile() && file.getName().equals(directory.getName() + ".yml")) {
                // This file name matches the directory name, so it's the main config for this shop
                Shop shop = loadShopFromFile(file, shopId);
                if (shop != null) {
                    shops.put(shopId, shop);
                }
                break;
            }
        }

        // Then process subdirectories (which may be sub-shops)
        for (File file : files) {
            if (file.isDirectory()) {
                // For each subdirectory, construct the shop ID
                String subShopId = shopId + "." + file.getName();
                loadShops(file, subShopId);
            }
        }
    }

    private Shop loadShopFromFile(File file, String shopId) {
        try {
            String shopName = file.getName().replace(".yml", "");

            FileConfiguration config = YamlConfiguration.loadConfiguration(file);

            String title = ColorUtils.colorize(config.getString("gui-title", "&8Shop"));
            int rows = config.getInt("rows", 3);

            // Load settings
            ConfigurationSection settingsSection = config.getConfigurationSection("settings");
            String defaultClickSound = settingsSection != null ?
                    settingsSection.getString("default-click-sound") : null;
            String decorativeItem = settingsSection != null ?
                    settingsSection.getString("decorative-item", "BLACK_STAINED_GLASS_PANE") : "BLACK_STAINED_GLASS_PANE";

            Shop shop = new Shop(shopId, shopName, title, rows, defaultClickSound, decorativeItem);

            // Load GUI items
            ConfigurationSection guiItemsSection = config.getConfigurationSection("gui-items");
            if (guiItemsSection != null) {
                for (String itemKey : guiItemsSection.getKeys(false)) {
                    ConfigurationSection itemSection = guiItemsSection.getConfigurationSection(itemKey);
                    if (itemSection == null) continue;

                    ShopItem item = loadShopItem(itemKey, itemSection, shop);
                    shop.addGuiItem(item);
                }
            }

            // Load sub-shops
            ConfigurationSection subShopsSection = config.getConfigurationSection("sub-shops");
            if (subShopsSection != null) {
                for (String subShopKey : subShopsSection.getKeys(false)) {
                    ConfigurationSection subShopSection = subShopsSection.getConfigurationSection(subShopKey);
                    if (subShopSection == null) continue;

                    ShopItem subShopItem = loadShopItem(subShopKey, subShopSection, shop);
                    shop.addSubShopItem(subShopKey, subShopItem);
                }
            }

            // Load shop items
            ConfigurationSection itemsSection = config.getConfigurationSection("items");
            if (itemsSection != null) {
                for (String itemKey : itemsSection.getKeys(false)) {
                    ConfigurationSection itemSection = itemsSection.getConfigurationSection(itemKey);
                    if (itemSection == null) continue;

                    ShopItem item = loadShopItem(itemKey, itemSection, shop);

                    // Attach worth data if available
                    ItemWorth worth = itemWorths.get(itemKey);
                    if (worth != null) {
                        item.setBuyPrice(worth.getBuyPrice());
                        item.setSellPrice(worth.getSellPrice());
                    }

                    shop.addShopItem(item);
                }
            }

            plugin.getLogger().info("Loaded shop: " + shopId);
            return shop;

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load shop from file: " + file.getName(), e);
            return null;
        }
    }

    private ShopItem loadShopItem(String itemKey, ConfigurationSection section, Shop shop) {
        String materialName = section.getString("material", "STONE");
        Material material = Material.getMaterial(materialName.toUpperCase());
        if (material == null) {
            plugin.getLogger().warning("Invalid material for item " + itemKey + ": " + materialName);
            material = Material.STONE;
        }

        String name = section.getString("name");
        if (name != null) {
            name = ColorUtils.colorize(name);
        }

        List<String> lore = section.getStringList("lore");
        List<String> colorizedLore = new ArrayList<>();
        for (String loreLine : lore) {
            colorizedLore.add(ColorUtils.colorize(loreLine));
        }

        int slot = section.getInt("slot", -1);
        String clickSound = section.getString("click-sound", shop.getDefaultClickSound());
        String onClick = section.getString("on-click", "");

        return new ShopItem(itemKey, material, name, colorizedLore, slot, clickSound, onClick);
    }

    public Shop getShop(String shopId) {
        return shops.get(shopId);
    }

    public Map<String, Shop> getAllShops() {
        return shops;
    }

    public static class ItemWorth {
        private final double sellPrice;
        private final double buyPrice;

        public ItemWorth(double sellPrice, double buyPrice) {
            this.sellPrice = sellPrice;
            this.buyPrice = buyPrice;
        }

        public double getSellPrice() {
            return sellPrice;
        }

        public double getBuyPrice() {
            return buyPrice;
        }
    }
}