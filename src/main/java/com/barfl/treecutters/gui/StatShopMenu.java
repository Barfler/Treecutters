package com.barfl.treecutters.gui;

import com.barfl.treecutters.Treecutters;
import com.barfl.treecutters.config.StatShopIcons;
import com.barfl.treecutters.config.StatUpgrade;
import com.barfl.treecutters.data.PlayerData;
import com.barfl.treecutters.data.PlayerSession;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class StatShopMenu {
    public static final String ID = "statshop";

    private final Treecutters plugin;
    private final MenuKeys keys;

    public StatShopMenu(Treecutters plugin, MenuKeys keys) {
        this.plugin = plugin;
        this.keys = keys;
    }

    private String fmt(double v) {
        return NumberFormat.getIntegerInstance(Locale.US).format(Math.round(v));
    }

    public void open(Player player) {
        PlayerData data = plugin.data().get(player.getUniqueId());
        PlayerSession session = plugin.data().session(player.getUniqueId());
        var msg = plugin.messages();

        Inventory inv = plugin.getServer().createInventory(player, 36, msg.get("gui.upgrades.title"));
        fillBlank(inv);

        double treeType = session.stat("tree_type");
        if (treeType >= 25) {
            Location city = configLoc("citySpawn");
            boolean atCity = city != null && player.getLocation().getY() >= 33;
            ItemStack icon = new ItemStack(atCity ? Material.GOLD_BLOCK : Material.GRASS_BLOCK);
            ItemMeta meta = icon.getItemMeta();
            meta.displayName(msg.get(atCity ? "gui.upgrades.city-icon-name" : "gui.upgrades.home-icon-name"));
            meta.lore(msg.getLoreList(atCity ? "gui.upgrades.city-icon-lore" : "gui.upgrades.home-icon-lore"));
            meta.getPersistentDataContainer().set(keys.warp, PersistentDataType.STRING, atCity ? "city" : "home");
            icon.setItemMeta(meta);
            inv.setItem(35, icon);
        }

        ItemStack settingsIcon = new ItemStack(Material.IRON_INGOT);
        ItemMeta settingsMeta = settingsIcon.getItemMeta();
        settingsMeta.displayName(msg.get("gui.upgrades.settings-icon-name"));
        settingsMeta.getPersistentDataContainer().set(keys.settingsMenu, PersistentDataType.INTEGER, 1);
        settingsIcon.setItemMeta(settingsMeta);
        inv.setItem(33, settingsIcon);

        ItemStack updateIcon = new ItemStack(Material.LECTERN);
        ItemMeta updateMeta = updateIcon.getItemMeta();
        updateMeta.displayName(msg.get("gui.upgrades.update-icon-name"));
        updateMeta.getPersistentDataContainer().set(keys.updateLogMenu, PersistentDataType.INTEGER, 1);
        updateIcon.setItemMeta(updateMeta);
        inv.setItem(32, updateIcon);

        ItemStack lbIcon = new ItemStack(Material.CHERRY_HANGING_SIGN);
        ItemMeta lbMeta = lbIcon.getItemMeta();
        lbMeta.displayName(msg.get("gui.upgrades.leaderboard-icon-name"));
        lbMeta.lore(plugin.leaderboards().lore(session.leaderboardType));
        lbMeta.getPersistentDataContainer().set(keys.leaderboardIcon, PersistentDataType.INTEGER, 1);
        lbIcon.setItemMeta(lbMeta);
        inv.setItem(34, lbIcon);

        int slot = 0;
        for (StatUpgrade upgrade : StatShopIcons.ALL.values()) {
            if (treeType < upgrade.levelReq) continue;

            int level = data.statLevel(upgrade.key);

            double cost = upgrade.basePrice * Math.pow(upgrade.priceScaling, level);
            if (!upgrade.priceBrackets.isEmpty()) {
                cost = level < upgrade.priceBrackets.size() ? upgrade.priceBrackets.get(level) : -1;
            }
            if (!upgrade.polynomialPrice.isEmpty()) {
                cost = 0;
                int exponent = 0;
                List<Double> reversed = new ArrayList<>(upgrade.polynomialPrice);
                java.util.Collections.reverse(reversed);
                for (double poly : reversed) {
                    cost += poly * Math.pow(level + 1, exponent);
                    exponent++;
                }
            }

            double maxLevel = upgrade.priceBrackets.isEmpty() ? upgrade.maxLevel : upgrade.priceBrackets.size();
            if (level >= maxLevel) cost = -1;

            Material mat = Material.matchMaterial(upgrade.material);
            ItemStack icon = new ItemStack(mat != null ? mat : Material.PAPER);
            ItemMeta meta = icon.getItemMeta();
            meta.displayName(msg.get("statshop." + upgrade.key + ".name"));

            List<Component> lore = new ArrayList<>();
            lore.add(msg.get("gui.upgrades.upgrade-level-line", "level", String.valueOf(level), "max", String.valueOf((int) maxLevel)));
            List<Component> description = msg.getLoreList("statshop." + upgrade.key + ".description");
            if (!description.isEmpty()) {
                lore.add(Component.empty());
                lore.addAll(description);
            }
            boolean isPrestige = upgrade.prestigeEvery != -1 && cost != -1 && ((level + 1) % upgrade.prestigeEvery == 0) && level <= 50;
            if (isPrestige) {
                lore.add(Component.empty());
                lore.add(msg.get("gui.upgrades.prestige-title"));
                lore.add(msg.get("gui.upgrades.prestige-line1"));
                lore.add(msg.get("gui.upgrades.prestige-line2"));
            }
            if (cost != -1) {
                lore.add(Component.empty());
                lore.add(msg.get("gui.upgrades.cost-line", "cost", fmt(cost)));
                lore.add(Component.empty());
                lore.add(msg.get("gui.upgrades.click-to-upgrade"));
            } else {
                lore.add(Component.empty());
                lore.add(msg.get("gui.upgrades.maxed"));
            }
            meta.lore(lore);
            meta.getPersistentDataContainer().set(keys.upgrade, PersistentDataType.STRING, upgrade.key);
            meta.getPersistentDataContainer().set(keys.cost, PersistentDataType.DOUBLE, cost);
            if (isPrestige) meta.getPersistentDataContainer().set(keys.prestige, PersistentDataType.INTEGER, 1);
            icon.setItemMeta(meta);

            inv.setItem(slot, icon);
            slot++;
            if (slot >= 32) break;
        }

        session.openInventoryId = ID;
        player.openInventory(inv);
    }

    private void fillBlank(Inventory inv) {
        ItemStack tile = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = tile.getItemMeta();
        meta.displayName(Component.empty());
        tile.setItemMeta(meta);
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, tile.clone());
    }

    public void handleClick(InventoryClickEvent event, Player player) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        var pdc = clicked.getItemMeta().getPersistentDataContainer();

        String warpTarget = pdc.get(keys.warp, PersistentDataType.STRING);
        if (warpTarget != null) {
            Location target = "home".equals(warpTarget)
                    ? plugin.data().session(player.getUniqueId()).roomPos.clone().add(-6, 3, -6)
                    : configLoc("citySpawn");
            if (target != null) player.teleport(target);
            player.closeInventory();
            return;
        }

        if (pdc.has(keys.settingsMenu, PersistentDataType.INTEGER)) {
            plugin.settingsMenu().open(player);
            return;
        }

        if (pdc.has(keys.updateLogMenu, PersistentDataType.INTEGER)) {
            plugin.updateLogMenu().open(player);
            return;
        }

        if (pdc.has(keys.leaderboardIcon, PersistentDataType.INTEGER)) {
            PlayerSession session = plugin.data().session(player.getUniqueId());
            session.leaderboardType = (session.leaderboardType + 1) % 5;
            open(player);
            return;
        }

        String iconKey = pdc.get(keys.upgrade, PersistentDataType.STRING);
        if (iconKey == null || !StatShopIcons.ALL.containsKey(iconKey)) return;

        Double cost = pdc.get(keys.cost, PersistentDataType.DOUBLE);
        if (cost == null || cost == -1) return;

        PlayerData data = plugin.data().get(player.getUniqueId());
        if (data.logs >= Math.floor(cost)) {
            data.logs -= cost;
            data.statShop.merge(iconKey, 1, Integer::sum);

            if (iconKey.equals("treeType")) {
                PlayerSession session = plugin.data().session(player.getUniqueId());
                if (!session.treeRegenerating) {
                    plugin.growth().regrow(player);
                }
            }
            if (pdc.has(keys.prestige, PersistentDataType.INTEGER)) {
                data.logs = 0;
            }
        }

        plugin.statsCalculator().computeStats(player, data, plugin.data().session(player.getUniqueId()));
        open(player);
    }

    private Location configLoc(String key) {
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("locations." + key);
        if (sec == null) return null;
        var world = plugin.getServer().getWorld(plugin.getConfig().getString("world", "world"));
        if (world == null) return null;
        Location loc = new Location(world, sec.getDouble("x"), sec.getDouble("y"), sec.getDouble("z"));
        loc.setYaw((float) sec.getDouble("yaw", 0));
        loc.setPitch((float) sec.getDouble("pitch", 0));
        return loc;
    }
}
