package com.barfl.treecutters.gameplay;

import com.barfl.treecutters.Treecutters;
import com.barfl.treecutters.data.PlayerSession;
import com.barfl.treecutters.tree.TreePlacement;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public final class GrowthManager {
    private final Treecutters plugin;

    public GrowthManager(Treecutters plugin) {
        this.plugin = plugin;
    }

    public void regrow(Player player) {
        PlayerSession session = plugin.data().session(player.getUniqueId());
        if (session.treeRegenerating) return;
        session.treeRegenerating = true;

        long delay = session.stat("tree_type") < 50 ? 10L : 1L;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || session.roomPos == null) {
                    session.treeRegenerating = false;
                    return;
                }
                session.brokenLogs.clear();

                List<TreePlacement> plan = plugin.treeGenerator().growArbitraryTree(
                        session.roomPos.getWorld(), session.roomPos, (int) Math.round(session.stat("tree_type")));

                int attempts = 0;
                while (plan.isEmpty() && attempts < 20) {
                    plan = plugin.treeGenerator().growArbitraryTree(
                            session.roomPos.getWorld(), session.roomPos, (int) Math.round(session.stat("tree_type")));
                    attempts++;
                }

                animatePlacement(player, session, plan);
            }
        }.runTaskLater(plugin, delay);
    }

    private void animatePlacement(Player player, PlayerSession session, List<TreePlacement> plan) {
        List<Location> blocksToBreak = new ArrayList<>(plan.size());
        for (TreePlacement p : plan) blocksToBreak.add(p.location());
        session.blocksToBreak = blocksToBreak;

        if (plan.isEmpty()) {
            finishGrowth(player, session);
            return;
        }

        new BukkitRunnable() {
            int index = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    session.treeRegenerating = false;
                    return;
                }

                int blocksPerTick = Math.max(1, (int) Math.round(session.stat("tree_growth")));
                for (int c = 0; c < blocksPerTick && index < plan.size(); c++, index++) {
                    TreePlacement placement = plan.get(index);
                    placement.location().getBlock().setType(placement.material());
                    placement.location().getWorld().playSound(placement.location(), Sound.BLOCK_WOOD_PLACE, 1f, 1f);
                }

                if (index >= plan.size()) {
                    cancel();
                    finishGrowth(player, session);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void finishGrowth(Player player, PlayerSession session) {
        if (player.isOnline()) {
            player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_STEP, 1f, 0.6f);
        }
        session.treeRegenerating = false;
        session.firstStrike = true;
    }
}
