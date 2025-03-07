package dev.nighter.shinyshop.gui;

public class ShopSession {
    private final String currentShopId;
    private final String previousShopId;

    public ShopSession(String currentShopId) {
        this(currentShopId, null);
    }

    public ShopSession(String currentShopId, String previousShopId) {
        this.currentShopId = currentShopId;
        this.previousShopId = previousShopId;
    }

    public String getCurrentShopId() {
        return currentShopId;
    }

    public String getPreviousShopId() {
        return previousShopId;
    }

    @Override
    public String toString() {
        return "ShopSession{currentShopId=" + currentShopId + ", previousShopId=" + previousShopId + "}";
    }
}