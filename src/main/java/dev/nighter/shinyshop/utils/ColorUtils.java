package dev.nighter.shinyshop.utils;

import org.bukkit.ChatColor;

public class ColorUtils {
    /**
     * Translate color codes in a string
     *
     * @param text Text to colorize
     * @return Colorized text
     */
    public static String colorize(String text) {
        if (text == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}