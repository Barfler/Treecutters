package com.barfl.treecutters.hud;

import com.barfl.treecutters.Treecutters;
import com.barfl.treecutters.data.PlayerData;
import com.barfl.treecutters.util.LocUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.List;

public final class TipManager {
    private final Treecutters plugin;

    public TipManager(Treecutters plugin) {
        this.plugin = plugin;
    }

    public void maybeBroadcast(long ticksSinceStartup) {
        if (ticksSinceStartup % 1200 != 0) return;
        broadcastNow();
    }

    public void broadcastNow() {
        List<String> tips = plugin.messages().rawList("tips.list");
        if (tips.isEmpty()) return;
        String tip = tips.get(LocUtil.randomInt(0, tips.size() - 1));
        Component message = plugin.messages().get("tips.header", "tip", tip);

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            PlayerData data = plugin.data().get(player.getUniqueId());
            if ("Enabled".equals(data.settings.getOrDefault("helpfulTips", "Enabled"))) {
                player.sendMessage(message);
            }
        }
    }
}
