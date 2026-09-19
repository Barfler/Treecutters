package com.barfl.treecutters.gameplay;

import com.barfl.treecutters.Treecutters;
import com.barfl.treecutters.data.PlayerSession;
import com.barfl.treecutters.util.LocUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public final class ThrowingAxeManager {
    private final Treecutters plugin;

    private static final Vector F0 = LocUtil.rotateAroundY(new Vector(0, 0, 1), -90);
    private static final Vector S0 = LocUtil.rotateAroundY(new Vector(1, 0, 0), -90);
    private static final Vector3f S0F = new Vector3f((float) S0.getX(), (float) S0.getY(), (float) S0.getZ());

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

        Vector movementDir;
        Vector rotationDir;
        if (target != null) {
            rotationDir = target.toVector().subtract(player.getLocation().toVector()).normalize();
            movementDir = rotationDir;
            pos.setDirection(movementDir);
        } else {
            movementDir = origin.getDirection();
            rotationDir = new Vector(0, 0, 1);
        }

        PlayerSession session = plugin.data().session(player.getUniqueId());
        double speed = (20 + session.stat("throwing_axe_speed")) / 20.0;

        Vector crossFD = F0.clone().crossProduct(rotationDir);
        Vector rotAxis = crossFD.lengthSquared() < 1e-9 ? new Vector(1, 0, 0) : crossFD.clone().normalize();
        double rotAngle = Math.atan2(crossFD.length(), F0.dot(rotationDir));
        Quaternionf leftRotation = new Quaternionf(new AxisAngle4f((float) rotAngle,
                (float) rotAxis.getX(), (float) rotAxis.getY(), (float) rotAxis.getZ()));

        Location[] oldPos = {pos.clone()};

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

                pos.add(movementDir.clone().multiply(speed));

                if (dt % 2 == 1) {
                    Location hitLoc = findWoodHit(pos);
                    if (hitLoc != null) {
                        display.remove();
                        execBreak(player, hitLoc);
                        cancel();
                        return;
                    }
                    if (LocUtil.alignBlockCenter(pos).getBlock().getType() != Material.AIR) {
                        dt = 30;
                    }
                }

                if (dt % 3 == 1) {
                    spawnTemporaryBeam(oldPos[0].clone().add(0, -0.25, 0), pos.clone().add(0, -0.25, 0));
                    oldPos[0] = pos.clone();
                }

                spin += 28f;
                display.setTeleportDuration(1);
                display.setInterpolationDuration(1);
                display.setInterpolationDelay(0);
                display.setTransformation(new Transformation(
                        new Vector3f(0, 0, 0),
                        leftRotation,
                        new Vector3f(1, 1, 1),
                        new Quaternionf(new AxisAngle4f((float) Math.toRadians(spin), S0F.x, S0F.y, S0F.z))
                ));
                display.teleport(pos);
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void spawnTemporaryBeam(Location source, Location target) {
        double distance = source.distance(target);
        if (distance < 1e-4) return;
        double dist = distance * 5;

        Vector direction = target.toVector().subtract(source.toVector()).normalize();
        Location spawnLoc = source.clone();
        spawnLoc.setDirection(direction);

        Vector translation = LocUtil.rotateAroundY(new Vector(-0.13, 0.01, 0.0), 90);

        TextDisplay display = source.getWorld().spawn(spawnLoc, TextDisplay.class, d -> {
            d.text(Component.text("█", TextColor.color(0xcccccc)));
            d.setBillboard(Display.Billboard.FIXED);
            d.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
            d.setPersistent(false);
            d.setTextOpacity((byte) 50);
            d.setTransformation(new Transformation(
                    new Vector3f((float) translation.getX(), (float) translation.getY(), (float) translation.getZ()),
                    new Quaternionf(new AxisAngle4f((float) Math.toRadians(90), 1, 0, 0)),
                    new Vector3f(1f, (float) dist, 1f),
                    new Quaternionf()
            ));
        });

        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (!display.isValid() || t > 24) {
                    if (display.isValid()) display.remove();
                    cancel();
                    return;
                }
                display.setTextOpacity((byte) (50 - 2 * t));
                t++;
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
