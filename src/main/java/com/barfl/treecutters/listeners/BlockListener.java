package com.barfl.treecutters.listeners;

import com.barfl.treecutters.Treecutters;
import com.barfl.treecutters.data.PlayerData;
import com.barfl.treecutters.data.PlayerSession;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.List;

public final class BlockListener implements Listener {
    private final Treecutters plugin;

    public BlockListener(Treecutters plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        event.setCancelled(true);

        Player player = event.getPlayer();
        if (!plugin.items().isLogType(event.getBlock().getType())) return;

        PlayerData data = plugin.data().get(player.getUniqueId());
        PlayerSession session = plugin.data().session(player.getUniqueId());

        List<List<org.bukkit.Location>> blocks = plugin.sweeping().getBlockOrders(
                player, session.stat("tree_type"), event.getBlock().getLocation(),
                session.stat("sweep"), session.firstStrike, session.stat("timber"));

        session.firstStrike = false;
        plugin.sweeping().safeBreak(player, event.getBlock().getLocation());
        plugin.sweeping().executeSweep(player, blocks);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        event.setCancelled(true);
    }
}
