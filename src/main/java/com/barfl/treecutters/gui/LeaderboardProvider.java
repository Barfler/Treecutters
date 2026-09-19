package com.barfl.treecutters.gui;

import com.barfl.treecutters.Treecutters;
import com.barfl.treecutters.data.PlayerData;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.ToDoubleFunction;

public final class LeaderboardProvider {
    public enum Type {
        TREE_TYPE("tree_type", d -> d.statLevel("treeType")),
        CHAT_WINS("chat_wins", d -> d.chatWins),
        TRUE_LOGS("true_logs", d -> d.trueLogs),
        WEATHER_DONATIONS("weather_donations", d -> d.donatedToWeatherMachine),
        PLAYTIME("playtime", d -> d.playtimeTicks / 20.0 / 60.0);

        final String messageKey;
        final ToDoubleFunction<PlayerData> metric;

        Type(String messageKey, ToDoubleFunction<PlayerData> metric) {
            this.messageKey = messageKey;
            this.metric = metric;
        }
    }

    private final Treecutters plugin;

    public LeaderboardProvider(Treecutters plugin) {
        this.plugin = plugin;
    }

    public Type typeFor(int leaderboardType) {
        Type[] values = Type.values();
        return values[Math.floorMod(leaderboardType, values.length)];
    }

    public List<Component> lore(int leaderboardType) {
        var msg = plugin.messages();
        Type type = typeFor(leaderboardType);

        List<PlayerData> ranked = new ArrayList<>();
        for (var player : plugin.getServer().getOnlinePlayers()) {
            ranked.add(plugin.data().get(player.getUniqueId()));
        }
        ranked.sort(Comparator.comparingDouble(type.metric).reversed());

        List<Component> lines = new ArrayList<>();
        String typeLabel = msg.rawString("leaderboard.types." + type.messageKey);
        lines.add(msg.get("leaderboard.type-line", "type", typeLabel));
        lines.add(Component.empty());

        int idx = 1;
        for (PlayerData data : ranked) {
            if (idx > 20) break;
            OfflinePlayer player = Bukkit.getOfflinePlayer(data.uuid);
            String name = player.getName() != null ? player.getName() : data.uuid.toString().substring(0, 8);
            String value = com.barfl.treecutters.util.NumFmt.format(type.metric.applyAsDouble(data));

            lines.add(msg.get("leaderboard.entry",
                    "rank", String.valueOf(idx),
                    "tag", plugin.chatTags().rawTagFor(data.statLevel("treeType")),
                    "name", name,
                    "value", value));
            idx++;
        }

        lines.add(Component.empty());
        lines.add(msg.get("leaderboard.switch-hint"));
        return lines;
    }
}
