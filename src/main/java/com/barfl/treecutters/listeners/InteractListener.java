package com.barfl.treecutters.listeners;

import com.barfl.treecutters.Treecutters;
import com.barfl.treecutters.data.PlayerData;
import com.barfl.treecutters.data.PlayerSession;
import com.barfl.treecutters.util.LocUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public final class InteractListener implements Listener {
    private final Treecutters plugin;

    public InteractListener(Treecutters plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getClickedBlock() != null) {
            plugin.stocks().handleInteract(player, event.getClickedBlock().getLocation());
        }
        plugin.weather().donate(player);

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        event.setCancelled(true);

        if (!plugin.items().isAxeTool(player.getInventory().getItemInMainHand())) return;
        if (player.getCooldown(player.getInventory().getItemInMainHand().getType()) > 0) return;

        PlayerData data = plugin.data().get(player.getUniqueId());
        PlayerSession session = plugin.data().session(player.getUniqueId());

        Location origin = player.getEyeLocation().clone();
        origin.add(origin.getDirection().multiply(0.25));
        plugin.throwingAxe().throwAxe(player, origin);

        int cooldownTicks = (int) (10 - session.stat("ability_cooldown") + (session.stat("multishot") * 5));
        player.setCooldown(player.getInventory().getItemInMainHand().getType(), Math.max(0, cooldownTicks));

        double multishot = session.stat("multishot");
        if (multishot > 0 && !session.treeRegenerating && session.turretTimer > 0) {
            List<Location> remaining = new ArrayList<>(session.blocksToBreak);
            remaining.removeAll(session.brokenLogs);

            for (int i = 1; i <= multishot && !remaining.isEmpty(); i++) {
                Location rngBlock = remaining.get(LocUtil.randomInt(0, remaining.size() - 1));
                Location aligned = LocUtil.alignBlockCenter(rngBlock);
                aligned.add(0, -0.3, 0);
                final int delay = i * 5;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (player.isOnline()) plugin.throwingAxe().throwAxe(player, origin, aligned);
                    }
                }.runTaskLater(plugin, delay);
            }
        }
    }

    @EventHandler
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        event.setCancelled(true);
        plugin.statShopMenu().open(event.getPlayer());
    }
}
