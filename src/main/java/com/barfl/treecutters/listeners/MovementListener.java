package com.barfl.treecutters.listeners;

import com.barfl.treecutters.Treecutters;
import com.barfl.treecutters.data.PlayerSession;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public final class MovementListener implements Listener {
    private final Treecutters plugin;

    public MovementListener(Treecutters plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        PlayerSession session = plugin.data().session(player.getUniqueId());
        plugin.doubleJump().refreshGroundedCharges(player);

        Location feet = player.getLocation();
        if (feet.getBlock().getType().isSolid() && feet.clone().add(0, 1, 0).getBlock().getType().isSolid()) {
            player.teleport(feet.clone().add(0, 2, 0));
            return;
        }

        Location below = feet.clone().add(0, -1, 0);
        if (below.getBlock().getType() == Material.LIGHT_BLUE_STAINED_GLASS && session.roomPos != null) {
            player.teleport(session.roomPos.clone().add(6, 6, 6));
        }
    }
}
