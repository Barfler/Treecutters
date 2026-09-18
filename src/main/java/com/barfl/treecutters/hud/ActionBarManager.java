package com.barfl.treecutters.hud;

import com.barfl.treecutters.Treecutters;
import com.barfl.treecutters.data.PlayerData;
import com.barfl.treecutters.data.PlayerSession;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

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
        return NumberFormat.getIntegerInstance(Locale.US).format(Math.round(v));
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

    public void clear(Player player) {
        BossBar bar = comboBars.remove(player.getUniqueId());
        if (bar != null) player.hideBossBar(bar);
    }
}
