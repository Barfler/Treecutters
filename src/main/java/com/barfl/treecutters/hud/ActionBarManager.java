package com.barfl.treecutters.hud;

import com.barfl.treecutters.Treecutters;
import com.barfl.treecutters.data.PlayerData;
import com.barfl.treecutters.data.PlayerSession;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class ActionBarManager {
    private final Treecutters plugin;
    private final Map<java.util.UUID, BossBar> comboBars = new HashMap<>();

    public ActionBarManager(Treecutters plugin) {
        this.plugin = plugin;
    }

    private String fmt(double v) {
        return com.barfl.treecutters.util.NumFmt.format(v);
    }

    public void tick(Player player, long ticksSinceStartup) {
        PlayerData data = plugin.data().get(player.getUniqueId());
        PlayerSession session = plugin.data().session(player.getUniqueId());
        var msg = plugin.messages();

        session.combo *= 0.99;
        session.combo = Math.round(session.combo);
        if (session.combo < 0) session.combo = 0;
        if (session.combo >= 2500) session.combo = 2500;

        if (session.stat("combo_str") > 0) {
            float progress = (float) Math.max(0, Math.min(1, session.combo / 2500.0));
            Component title = msg.get("actionbar.combo-title", "combo", String.valueOf((int) session.combo));
            BossBar bar = comboBars.get(player.getUniqueId());
            if (bar == null) {
                bar = BossBar.bossBar(title, progress, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS);
                comboBars.put(player.getUniqueId(), bar);
                player.showBossBar(bar);
            } else {
                bar.name(title);
                bar.progress(progress);
            }
        }

        if (ticksSinceStartup % 20 == session.hashCode % 20) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 100_000, 0, false, false, false));
            player.setSaturation(20f);
            player.setFoodLevel(20);
            if (data.logs != session.lastDisplayedLogs) {
                updateLogDisplay(player, session, data);
                session.lastDisplayedLogs = data.logs;
            }
        }

        if (ticksSinceStartup % 20 == 19) {
            session.currentLogsSample = data.logs;
            double diff = session.currentLogsSample - session.lastLogsSample;
            session.logsPerSecondText = fmt(diff);
        }
        if (ticksSinceStartup % 20 == 0) {
            session.lastLogsSample = data.logs;
        }

        if (ticksSinceStartup % 5 == session.hashCode % 5) {
            int brokenReq = session.blocksToBreak.size();
            double totalPerc = brokenReq == 0 ? 0 :
                    Math.min(100, Math.round((session.brokenLogs.size() / (brokenReq * (0.9 - session.stat("cutoff_rate")))) * 100));

            Component bar = msg.get("actionbar.logs", "logs", fmt(data.logs));

            if (!session.logsPerSecondText.isEmpty() && "Enabled".equals(data.settings.get("logSecActionBar"))) {
                bar = bar.append(msg.get("actionbar.separator"))
                        .append(msg.get("actionbar.logs-per-second", "amount", session.logsPerSecondText));
            }
            if (data.stockCount >= 1) {
                bar = bar.append(msg.get("actionbar.separator"))
                        .append(msg.get("actionbar.stock-price", "price", fmt(plugin.globalState().state().stockValue)));
            }
            bar = bar.append(msg.get("actionbar.separator"))
                    .append(msg.get("actionbar.percent-complete", "percent", String.valueOf((long) totalPerc)));

            player.sendActionBar(bar);
        }
    }

    private void updateLogDisplay(Player player, PlayerSession session, PlayerData data) {
        double logs = data.logs;
        int basicLogs = (int) (logs % 64);
        int enchLogs = (int) (Math.floor(logs / 64) % 64);
        int hyperEnchLogs = (int) (Math.floor(logs / 64 / 64) % 64);
        int hyperEnch2Logs = (int) (Math.floor(logs / 64 / 64 / 64) % 64);
        int hyperEnch3Logs = (int) (Math.floor(logs / 64 / 64 / 64 / 64) % 64);
        int hyperEnch4Logs = (int) (Math.floor(logs / 64 / 64 / 64 / 64 / 64) % 64);

        Material[] materials = new Material[9];
        int[] counts = new int[9];
        boolean[] enchanted = new boolean[9];
        int slot = 1;
        if (hyperEnch4Logs > 1) { materials[slot] = Material.STRIPPED_CRIMSON_STEM; counts[slot] = hyperEnch4Logs; enchanted[slot] = true; slot++; }
        if (hyperEnch3Logs > 1) { materials[slot] = Material.STRIPPED_WARPED_STEM; counts[slot] = hyperEnch3Logs; enchanted[slot] = true; slot++; }
        if (hyperEnch2Logs > 1) { materials[slot] = Material.STRIPPED_CHERRY_WOOD; counts[slot] = hyperEnch2Logs; enchanted[slot] = true; slot++; }
        if (hyperEnchLogs > 1) { materials[slot] = Material.STRIPPED_SPRUCE_WOOD; counts[slot] = hyperEnchLogs; enchanted[slot] = true; slot++; }
        if (enchLogs > 1) { materials[slot] = Material.STRIPPED_OAK_WOOD; counts[slot] = enchLogs; enchanted[slot] = true; slot++; }
        if (basicLogs > 1) { materials[slot] = Material.STRIPPED_OAK_WOOD; counts[slot] = basicLogs; enchanted[slot] = false; slot++; }

        for (int s = 1; s <= 8; s++) {
            if (materials[s] == session.lastLogSlotMaterials[s] && counts[s] == session.lastLogSlotCounts[s]) continue;
            player.getInventory().setItem(s, materials[s] == null ? null : tieredLogItem(materials[s], counts[s], enchanted[s]));
            session.lastLogSlotMaterials[s] = materials[s];
            session.lastLogSlotCounts[s] = counts[s];
        }
    }

    private ItemStack tieredLogItem(Material material, int count, boolean enchanted) {
        ItemStack item = new ItemStack(material, count);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.empty());
        meta.setHideTooltip(true);
        if (enchanted) {
            meta.addEnchant(Enchantment.SHARPNESS, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        item.setItemMeta(meta);
        return item;
    }

    public void clear(Player player) {
        BossBar bar = comboBars.remove(player.getUniqueId());
        if (bar != null) player.hideBossBar(bar);
    }
}
