package com.barfl.treecutters.listeners;

import com.barfl.treecutters.Treecutters;
import com.barfl.treecutters.data.PlayerData;
import com.barfl.treecutters.data.PlayerSession;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public final class JoinQuitListener implements Listener {
    private final Treecutters plugin;

    public JoinQuitListener(Treecutters plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        PlayerData data = plugin.data().get(player.getUniqueId());
        PlayerSession session = plugin.data().session(player.getUniqueId());

        plugin.settingsMenu().ensureDefaults(data);

        for (var online : plugin.getServer().getOnlinePlayers()) {
            plugin.statsCalculator().computeStats(online, plugin.data().get(online.getUniqueId()),
                    plugin.data().session(online.getUniqueId()));
        }

        player.setGameMode(GameMode.SURVIVAL);
        boolean hasAxe = false;
        for (var item : player.getInventory().getContents()) {
            if (plugin.items().isAxeTool(item)) {
                hasAxe = true;
                break;
            }
        }
        if (!hasAxe) {
            player.getInventory().addItem(plugin.items().buildAxeTool(plugin.messages().get("items.axe-name")));
        }

        Location roomPos = plugin.rooms().assign();
        if (roomPos == null) {
            player.kick(plugin.messages().get("join.no-rooms-kick"));
            return;
        }
        session.roomPos = roomPos;
        session.hashCode = (int) (Math.random() * 2_147_000_000);

        Location spawn = roomPos.clone().add(-6, 3, -6);
        spawn.setWorld(roomPos.getWorld());
        player.teleport(spawn);

        if (plugin.schematics() != null) plugin.schematics().pasteAt(roomPos);
        plugin.growth().regrow(player);

        plugin.statsCalculator().computeStats(player, data, session);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;
                player.sendMessage(plugin.messages().getList("join.welcome"));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
            }
        }.runTaskLater(plugin, 60L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        var player = event.getPlayer();
        PlayerSession session = plugin.data().session(player.getUniqueId());

        if (session.roomPos != null) {
            plugin.rooms().release(session.roomPos);
        }

        PlayerData data = plugin.data().get(player.getUniqueId());
        plugin.data().save(data);
        plugin.data().dropSession(player.getUniqueId());
    }
}
