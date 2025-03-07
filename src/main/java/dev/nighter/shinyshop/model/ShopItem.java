package dev.nighter.shinyshop.model;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ShopItem {
    private final String id;
    private final Material material;
    private final String name;
    private final List<String> lore;
    private final int slot;
    private final String clickSound;
    private final String onClick;

    private double buyPrice = -1;
    private double sellPrice = -1;

    public ShopItem(String id, Material material, String name, List<String> lore, int slot, String clickSound, String onClick) {
        this.id = id;
        this.material = material;
        this.name = name;
        this.lore = lore;
        this.slot = slot;
        this.clickSound = clickSound;
        this.onClick = onClick;
    }

    public String getId() {
        return id;
    }

    public Material getMaterial() {
        return material;
    }

    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return lore;
    }

    public int getSlot() {
        return slot;
    }

    public String getClickSound() {
        return clickSound;
    }

    public String getOnClick() {
        return onClick;
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(double buyPrice) {
        this.buyPrice = buyPrice;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(double sellPrice) {
        this.sellPrice = sellPrice;
    }

    public boolean canBuy() {
        return buyPrice > 0;
    }

    public boolean canSell() {
        return sellPrice > 0;
    }

    public ItemStack createItemStack() {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta meta = itemStack.getItemMeta();

        if (meta != null) {
            if (name != null) {
                meta.setDisplayName(name);
            }

            if (lore != null && !lore.isEmpty()) {
                meta.setLore(lore);
            }

            itemStack.setItemMeta(meta);
        }

        return itemStack;
    }
}
