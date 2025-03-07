package dev.nighter.shinyshop.model;

import java.util.HashMap;
import java.util.Map;

public class Shop {
    private final String id;
    private final String name;
    private final String title;
    private final int rows;
    private final String defaultClickSound;
    private final String decorativeItem;

    private final Map<String, ShopItem> shopItems = new HashMap<>();
    private final Map<String, ShopItem> guiItems = new HashMap<>();
    private final Map<String, ShopItem> subShopItems = new HashMap<>();

    public Shop(String id, String name, String title, int rows, String defaultClickSound, String decorativeItem) {
        this.id = id;
        this.name = name;
        this.title = title;
        this.rows = rows;
        this.defaultClickSound = defaultClickSound;
        this.decorativeItem = decorativeItem;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public int getRows() {
        return rows;
    }

    public String getDefaultClickSound() {
        return defaultClickSound;
    }

    public String getDecorativeItem() {
        return decorativeItem;
    }

    public void addShopItem(ShopItem item) {
        shopItems.put(item.getId(), item);
    }

    public void addGuiItem(ShopItem item) {
        guiItems.put(item.getId(), item);
    }

    public void addSubShopItem(String subShopId, ShopItem item) {
        subShopItems.put(subShopId, item);
    }

    public Map<String, ShopItem> getShopItems() {
        return shopItems;
    }

    public Map<String, ShopItem> getGuiItems() {
        return guiItems;
    }

    public Map<String, ShopItem> getSubShopItems() {
        return subShopItems;
    }

    public String getParentId() {
        int lastDotIndex = id.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return null; // This is the main shop with no parent
        }
        return id.substring(0, lastDotIndex);
    }
}

