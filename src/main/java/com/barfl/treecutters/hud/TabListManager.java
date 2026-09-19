package com.barfl.treecutters.hud;

import com.barfl.treecutters.Treecutters;
import com.barfl.treecutters.data.PlayerData;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class TabListManager {
    private final Treecutters plugin;
    private long lastUpdate = 0;

    public TabListManager(Treecutters plugin) {
        this.plugin = plugin;
    }

    private String fmt(double v) {
        return com.barfl.treecutters.util.NumFmt.format(v);
    }

    public void tick(long nowTicks) {
        if (nowTicks - lastUpdate < 60) return;
        lastUpdate = nowTicks;
        var msg = plugin.messages();

        List<Player> online = List.copyOf(plugin.getServer().getOnlinePlayers());
        List<Player> ranked = online.stream()
                .sorted(Comparator.comparingDouble((Player p) ->
                        plugin.data().session(p.getUniqueId()).stat("tree_type")).reversed())
                .toList();

        Component header = Component.newline()
                .append(msg.get("tablist.header-title")).appendNewline().appendNewline();

        for (Player p : ranked) {
            var session = plugin.data().session(p.getUniqueId());
            PlayerData data = plugin.data().get(p.getUniqueId());
            header = header.append(msg.get("tablist.entry",
                    "tag", plugin.chatTags().rawTagFor((int) session.stat("tree_type")),
                    "player", p.getName(),
                    "logs", fmt(data.logs))).appendNewline();
        }

        header = header.appendNewline()
                .append(msg.get("tablist.players-online", "count", String.valueOf(online.size())))
                .appendNewline();

        header = header.appendNewline().append(msg.get("tablist.weather-header")).appendNewline();
        int weatherType = plugin.weather().state().weatherType;
        header = header.append(weatherLines(weatherType))
                .append(msg.get("tablist.weather-logs", "amount", fmt(plugin.weather().state().weatherMachineLogs)));

        Component finalHeader = header;
        for (Player p : online) {
            p.sendPlayerListHeader(finalHeader);
        }
    }

    private Component weatherLines(int weatherType) {
        var msg = plugin.messages();
        String key = switch (weatherType) {
            case 1 -> "tablist.weather-status.rain";
            case 2 -> "tablist.weather-status.thunderstorm";
            case 3 -> "tablist.weather-status.severe-thunderstorm";
            case 4 -> "tablist.weather-status.hurricane";
            default -> "tablist.weather-status.inactive";
        };
        return msg.getList(key).appendNewline();
    }
}
