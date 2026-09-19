package com.barfl.treecutters.gui;

import com.barfl.treecutters.Treecutters;
import com.barfl.treecutters.config.SettingDef;
import com.barfl.treecutters.config.SettingsDefs;
import com.barfl.treecutters.data.PlayerData;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public final class SettingsMenu {
    public static final String ID = "settings";

    private final Treecutters plugin;
    private final MenuKeys keys;

    public SettingsMenu(Treecutters plugin, MenuKeys keys) {
        this.plugin = plugin;
        this.keys = keys;
    }

    public void ensureDefaults(PlayerData data) {
        for (SettingDef def : SettingsDefs.ALL.values()) {
            data.settings.putIfAbsent(def.key(), def.defaultSetting());
        }
    }

    public void open(Player player) {
        PlayerData data = plugin.data().get(player.getUniqueId());
        ensureDefaults(data);
        var msg = plugin.messages();

        Inventory inv = plugin.getServer().createInventory(player, 27, msg.get("gui.settings.title"));
        ItemStack tile = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta tileMeta = tile.getItemMeta();
        tileMeta.displayName(Component.empty());
        tileMeta.setHideTooltip(true);
        tile.setItemMeta(tileMeta);
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, tile.clone());

        int slot = 0;
        for (SettingDef def : SettingsDefs.ALL.values()) {
            Material mat = Material.matchMaterial(def.icon());
            ItemStack icon = new ItemStack(mat != null ? mat : Material.PAPER);
            ItemMeta meta = icon.getItemMeta();
            meta.displayName(msg.get("settings." + def.key() + ".name"));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.addAll(msg.getLoreList("settings." + def.key() + ".description"));
            lore.add(Component.empty());

            String current = data.settings.getOrDefault(def.key(), def.defaultSetting());
            String next = def.options().get(0);
            for (String option : def.options()) {
                if (option.equals(current)) {
                    lore.add(msg.get("gui.settings.selected-option", "option", option));
                    int idx = def.options().indexOf(option);
                    next = def.options().get((idx + 1) % def.options().size());
                } else {
                    lore.add(msg.get("gui.settings.unselected-option", "option", option));
                }
            }
            meta.lore(lore);
            meta.getPersistentDataContainer().set(keys.editSetting, PersistentDataType.STRING, def.key());
            meta.getPersistentDataContainer().set(keys.settingValue, PersistentDataType.STRING, next);
            icon.setItemMeta(meta);

            inv.setItem(slot, icon);
            slot++;
        }

        plugin.data().session(player.getUniqueId()).openInventoryId = ID;
        player.openInventory(inv);
    }

    public void handleClick(InventoryClickEvent event, Player player) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        var pdc = clicked.getItemMeta().getPersistentDataContainer();

        String settingKey = pdc.get(keys.editSetting, PersistentDataType.STRING);
        if (settingKey == null || !SettingsDefs.ALL.containsKey(settingKey)) return;

        String nextValue = pdc.get(keys.settingValue, PersistentDataType.STRING);
        PlayerData data = plugin.data().get(player.getUniqueId());
        data.settings.put(settingKey, nextValue);
        open(player);
    }
}
