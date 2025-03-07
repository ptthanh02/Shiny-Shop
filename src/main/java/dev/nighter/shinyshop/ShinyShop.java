package dev.nighter.shinyshop;

import dev.nighter.shinyshop.commands.ShopCommand;
import dev.nighter.shinyshop.config.ConfigManager;
import dev.nighter.shinyshop.gui.ShopManager;
import dev.nighter.shinyshop.utils.ColorUtils;
import org.bukkit.plugin.java.JavaPlugin;

public final class ShinyShop extends JavaPlugin {
    private ConfigManager configManager;
    private ShopManager shopManager;

    @Override
    public void onEnable() {
        // Save default configuration files
        saveDefaultConfig();

        // Initialize managers
        this.configManager = new ConfigManager(this);
        this.shopManager = new ShopManager(this);

        // Load configurations
        configManager.loadConfigurations();

        // Register commands
        getCommand("shop").setExecutor(new ShopCommand(this));

        getLogger().info(ColorUtils.colorize("&aShinyShop has been enabled!"));
    }

    @Override
    public void onDisable() {
        getLogger().info(ColorUtils.colorize("&cShinyShop has been disabled!"));
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }
}