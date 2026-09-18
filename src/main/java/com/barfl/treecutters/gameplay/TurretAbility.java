package com.barfl.treecutters.gameplay;

import com.barfl.treecutters.Treecutters;
import com.barfl.treecutters.data.PlayerSession;
import com.barfl.treecutters.util.LocUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class TurretAbility {
    private final Treecutters plugin;

    public TurretAbility(Treecutters plugin) {
        this.plugin = plugin;
    }

    public void tick(Player player, long ticksSinceStartup) {
        PlayerSession session = plugin.data().session(player.getUniqueId());
        double turretLevel = session.stat("turret");
        if (turretLevel <= 0 || session.turretTimer <= 0) return;
        if (ticksSinceStartup % Math.max(1, (80 - (long) turretLevel)) != 0) return;

        session.turretTimer -= 1;

        List<Location> remaining = new ArrayList<>(session.blocksToBreak);
        remaining.removeAll(session.brokenLogs);
        if (remaining.isEmpty()) return;

        Location target = remaining.get(LocUtil.randomInt(0, remaining.size() - 1));

        List<List<Location>> blocks = plugin.sweeping().getBlockOrders(
                player, session.stat("tree_type"), target, session.stat("sweep"), session.firstStrike, session.stat("timber"));
        session.firstStrike = false;

        List<Location> flat = new ArrayList<>();
        for (List<Location> wave : blocks) flat.addAll(wave);

        plugin.sweeping().safeBreak(player, target);
        plugin.sweeping().executeSweepFlattened(player, flat);
    }
}
