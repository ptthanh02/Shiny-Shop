package dev.nighter.shinyshop.commands;

import dev.nighter.shinyshop.ShinyShop;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ShopCommand implements CommandExecutor, TabCompleter {
    private final ShinyShop plugin;

    public ShopCommand(ShinyShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (args.length == 0) {
            // Open main shop
            plugin.getShopManager().openShop(player, "shops");
        } else {
            // Try to open specific shop
            String shopId = "shops." + args[0];
            if (plugin.getConfigManager().getShop(shopId) != null) {
                plugin.getShopManager().openShop(player, shopId);
            } else {
                player.sendMessage("§cShop not found: " + args[0]);
                // Debug: print all available shops
                plugin.getLogger().info("Available shops: " + String.join(", ", plugin.getConfigManager().getAllShops().keySet()));
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Get all direct sub-shops of main shop
            String mainShopId = "shops";
            if (plugin.getConfigManager().getShop(mainShopId) != null) {
                plugin.getConfigManager().getShop(mainShopId).getSubShopItems().keySet()
                        .forEach(completions::add);
            }
        }

        return completions;
    }
}