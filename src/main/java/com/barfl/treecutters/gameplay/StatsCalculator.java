package com.barfl.treecutters.gameplay;

import com.barfl.treecutters.config.StatShopIcons;
import com.barfl.treecutters.config.StatUpgrade;
import com.barfl.treecutters.data.PlayerData;
import com.barfl.treecutters.data.PlayerSession;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class StatsCalculator {

    public void computeStats(Player player, PlayerData data, PlayerSession session) {
        session.stats.clear();
        session.stats.put("sweep", 0.0);
        session.stats.put("tree_growth", 1.0);

        for (StatUpgrade upgrade : StatShopIcons.ALL.values()) {
            int level = data.statLevel(upgrade.key);
            session.stats.merge(upgrade.stat, upgrade.amount * level, Double::sum);
        }

        double jumpHeight = session.stat("jump_height");
        if (jumpHeight >= 1) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST,
                    10_000_000, (int) jumpHeight - 1, false, false, false));
        }

        var speedAttr = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (speedAttr != null) speedAttr.setBaseValue(0.1 + (session.stat("walk_speed") * 0.01));

        var reachAttr = player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE);
        if (reachAttr != null) reachAttr.setBaseValue(4 + session.stat("swing_range"));
    }
}
