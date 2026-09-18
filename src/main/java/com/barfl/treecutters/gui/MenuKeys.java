package com.barfl.treecutters.gui;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class MenuKeys {
    public final NamespacedKey upgrade;
    public final NamespacedKey cost;
    public final NamespacedKey prestige;
    public final NamespacedKey warp;
    public final NamespacedKey leaderboardIcon;
    public final NamespacedKey settingsMenu;
    public final NamespacedKey updateLogMenu;
    public final NamespacedKey editSetting;
    public final NamespacedKey settingValue;

    public MenuKeys(JavaPlugin plugin) {
        upgrade = new NamespacedKey(plugin, "upgrade");
        cost = new NamespacedKey(plugin, "cost");
        prestige = new NamespacedKey(plugin, "prestige");
        warp = new NamespacedKey(plugin, "warp");
        leaderboardIcon = new NamespacedKey(plugin, "leaderboard_icon");
        settingsMenu = new NamespacedKey(plugin, "settings_menu");
        updateLogMenu = new NamespacedKey(plugin, "update_menu");
        editSetting = new NamespacedKey(plugin, "edit_setting");
        settingValue = new NamespacedKey(plugin, "setting_value");
    }
}
