package com.barfl.treecutters.gameplay;

import com.barfl.treecutters.Treecutters;
import com.barfl.treecutters.data.PlayerSession;
import com.barfl.treecutters.util.LocUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public final class ThrowingAxeManager {
    private final Treecutters plugin;

    public ThrowingAxeManager(Treecutters plugin) {
        this.plugin = plugin;
    }

    public void throwAxe(Player player, Location origin) {
        throwAxe(player, origin, null);
    }

    public void throwAxe(Player player, Location origin, Location target) {
        player.getWorld().playSound(origin, Sound.ITEM_TRIDENT_THROW, 1f, 1f);
        player.getWorld().playSound(origin, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1f, 1f);
        player.getWorld().playSound(origin, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 1f);

        ItemDisplay display = origin.getWorld().spawn(origin, ItemDisplay.class, d -> {
            d.setItemStack(new ItemStack(Material.IRON_AXE));
            d.setPersistent(false);
        });

        Location pos = origin.clone();
        Vector direction;
        if (target != null) {
            direction = target.toVector().subtract(origin.toVector()).normalize();
            pos.setDirection(direction);
        } else {
            direction = origin.getDirection();
        }

        PlayerSession session = plugin.data().session(player.getUniqueId());
        double speed = (20 + session.stat("throwing_axe_speed")) / 20.0;

        new BukkitRunnable() {
            int dt = 0;
            float spin = 0;

            @Override
            public void run() {
                dt++;
                if (dt > 30 || !display.isValid()) {
                    if (display.isValid()) display.remove();
                    cancel();
                    return;
                }

                pos.add(direction.clone().multiply(speed));

                if (dt % 2 == 1) {
                    Location hitLoc = findWoodHit(pos);
                    if (hitLoc != null) {
                        display.remove();
                        execBreak(player, hitLoc);
                        cancel();
                        return;
                    }
                }

                spin += 28f;
                display.setTransformation(new Transformation(
                        new Vector3f(0, 0, 0),
                        new Quaternionf(new AxisAngle4f((float) Math.toRadians(spin), 0, 1, 0)),
                        new Vector3f(1, 1, 1),
                        new Quaternionf()
                ));
                display.teleport(pos);
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private Location findWoodHit(Location pos) {
        Location base = LocUtil.alignBlockCenter(pos);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    Location adj = base.clone().add(dx, dy, dz);
                    if (plugin.items().isLogType(adj.getBlock().getType())) {
                        return LocUtil.alignBlockCenter(adj);
                    }
                }
            }
        }
        return null;
    }

    private void execBreak(Player player, Location pos) {
        PlayerSession session = plugin.data().session(player.getUniqueId());

        List<List<Location>> blocks = plugin.sweeping().getBlockOrders(
                player, session.stat("tree_type"), pos, session.stat("sweep"), session.firstStrike, session.stat("timber"));

        session.firstStrike = false;
        plugin.sweeping().safeBreak(player, pos);
        plugin.sweeping().executeSweep(player, blocks);
    }
}
