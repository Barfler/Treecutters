package com.barfl.treecutters.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumSet;
import java.util.Set;

public final class Items {
    private final JavaPlugin plugin;
    public final NamespacedKey axeKey;
    private final Set<Material> woodTypes = EnumSet.noneOf(Material.class);

    public Items(JavaPlugin plugin) {
        this.plugin = plugin;
        this.axeKey = new NamespacedKey(plugin, "axe");
        reloadWoodTypes(plugin.getConfig());
    }

    public void reloadWoodTypes(FileConfiguration config) {
        woodTypes.clear();
        for (String name : config.getStringList("woodTypes")) {
            Material m = Material.matchMaterial(name);
            if (m != null) woodTypes.add(m);
            else plugin.getLogger().warning("Unknown wood type material in config.yml: " + name);
        }
    }

    public Set<Material> woodTypes() {
        return woodTypes;
    }

    public boolean isLogType(Material material) {
        return woodTypes.contains(material);
    }

    public ItemStack buildAxeTool(Component name) {
        ItemStack tool = new ItemStack(Material.IRON_AXE);
        ItemMeta meta = tool.getItemMeta();
        meta.displayName(name.decoration(TextDecoration.ITALIC, false));
        meta.setUnbreakable(true);
        meta.getPersistentDataContainer().set(axeKey, PersistentDataType.INTEGER, 1);
        tool.setItemMeta(meta);
        return tool;
    }

    public boolean isAxeTool(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        Integer tag = item.getItemMeta().getPersistentDataContainer().get(axeKey, PersistentDataType.INTEGER);
        return tag != null && tag == 1;
    }
}
