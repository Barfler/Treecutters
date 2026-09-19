package com.barfl.treecutters.gameplay;

import com.barfl.treecutters.Treecutters;
import com.barfl.treecutters.config.LogFamilies;
import com.barfl.treecutters.config.LogFamily;
import com.barfl.treecutters.data.PlayerData;
import com.barfl.treecutters.data.PlayerSession;
import com.barfl.treecutters.util.LocUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class SweepManager {

    private static final List<int[]> SWEEP_VECS = buildSweepVecs();

    private static List<int[]> buildSweepVecs() {
        List<int[]> vecs = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    vecs.add(new int[]{x, y, z});
                }
            }
        }
        return vecs;
    }

    private final Treecutters plugin;

    public SweepManager(Treecutters plugin) {
        this.plugin = plugin;
    }

    public List<List<Location>> getBlockOrders(Player player, double tier, Location origin, double maxBlocksIn,
                                                boolean firstStrike, double timberLevel) {
        LogFamily family = LogFamilies.forTier(tier);
        double maxBlocks = maxBlocksIn - family.toughness();
        if (maxBlocks > 8) maxBlocks = 8 + ((maxBlocks - 8) * 0.5);
        if (maxBlocks > 16) maxBlocks = 16 + ((maxBlocks - 16) * 0.5);
        if (maxBlocks > 32) maxBlocks = 32 + ((maxBlocks - 32) * 0.5);

        PlayerSession session = plugin.data().session(player.getUniqueId());

        if (LocUtil.randomInt(1, 100) <= timberLevel) {
            player.playSound(player.getLocation(), Sound.ITEM_AXE_STRIP, 1f, 1f);
            for (Location bl : new ArrayList<>(session.blocksToBreak)) {
                safeBreak(player, bl);
            }
            return List.of();
        }

        List<List<Location>> breakQueue = new ArrayList<>();
        Deque<Location> currentWave = new ArrayDeque<>();
        currentWave.add(origin);
        List<Location> nextWave = new ArrayList<>();
        List<Location> brokenSoFar = new ArrayList<>();
        int blocksMined = 0;

        while (blocksMined < maxBlocks) {
            if (currentWave.isEmpty()) {
                if (nextWave.isEmpty()) break;
                breakQueue.add(new ArrayList<>(nextWave));
                currentWave = new ArrayDeque<>(nextWave);
                nextWave = new ArrayList<>();
            }

            Location el = currentWave.pollFirst();
            if (el == null) continue;
            for (int[] offset : SWEEP_VECS) {
                Location adj = el.clone().add(offset[0], offset[1], offset[2]);
                Material mat = adj.getBlock().getType();
                if (!brokenSoFar.contains(adj) && mat != Material.AIR && plugin.items().isLogType(mat)) {
                    nextWave.add(adj);
                    brokenSoFar.add(adj);
                }
            }
            blocksMined++;
        }

        return breakQueue;
    }

    public void executeSweep(Player player, List<List<Location>> waves) {
        List<Location> flat = new ArrayList<>();
        for (List<Location> wave : waves) {
            for (Location loc : wave) {
                if (!flat.contains(loc)) flat.add(loc);
            }
        }
        executeSweepFlattened(player, flat);
    }

    public void executeSweepFlattened(Player player, List<Location> blocksIn) {
        List<Location> blocks = new ArrayList<>();
        for (Location loc : blocksIn) {
            if (!blocks.contains(loc)) blocks.add(loc);
        }
        if (blocks.isEmpty()) return;

        double oftency = blocks.size() / 10.0;
        new BukkitRunnable() {
            int index = 0;
            double acc = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                while (index < blocks.size()) {
                    safeBreak(player, blocks.get(index));
                    index++;
                    acc += 1;
                    if (acc > oftency) {
                        acc = 0;
                        return;
                    }
                }
                cancel();
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public void safeBreak(Player player, Location blockIn) {
        Location block = LocUtil.alignBlockCenter(blockIn);
        PlayerSession session = plugin.data().session(player.getUniqueId());
        if (session.treeRegenerating) return;

        PlayerData data = plugin.data().get(player.getUniqueId());
        LogFamily family = LogFamilies.forTier(session.stat("tree_type"));

        double logMul = 1;
        logMul += session.stat("fortune");
        logMul += session.stat("combo_str") * (session.combo * 0.002);

        int weatherType = plugin.weather().state().weatherType;
        if (weatherType == 1) logMul += 0.2;
        if (weatherType == 2) logMul += 0.5;
        if (weatherType == 3) logMul += 0.9;
        if (weatherType == 4) logMul += 1.5;

        double logValue = family.value() * logMul;

        Material mat = block.getBlock().getType();
        if (mat != Material.AIR && mat != Material.LIGHT && !session.brokenLogs.contains(block)) {
            session.brokenLogs.add(block);
            data.logs += logValue;
            data.trueLogs += 1;
        }

        String logEffects = data.settings.getOrDefault("logEffects", "Enabled");
        if (logEffects.equals("Enabled") || logEffects.equals("Particles Only")) {
            visualBreak(block, mat);
        } else {
            block.getBlock().setType(Material.AIR);
        }

        spawnBlockFallAnim(player, block, mat);

        session.combo += 1;

        int brokenReqCount = session.blocksToBreak.size();
        double brokenSoFarCount = session.brokenLogs.size();
        if (brokenReqCount > 0 && brokenSoFarCount / brokenReqCount >= 0.9 - session.stat("cutoff_rate")) {
            spawnTreeClearAnim(player, block);
            for (Location block2 : new ArrayList<>(session.blocksToBreak)) {
                Material mat2 = block2.getBlock().getType();
                if (mat2 != Material.AIR && !session.brokenLogs.contains(block2)) {
                    session.brokenLogs.add(block2);
                    data.logs += logValue;
                    data.trueLogs += 1;
                    session.combo += 1;
                    spawnBlockFallAnim(player, block2, mat2);
                }
                if (logEffects.equals("Enabled") || logEffects.equals("Particles Only")) {
                    visualBreak(block2, mat2);
                } else {
                    block2.getBlock().setType(Material.AIR);
                }
            }
            player.playSound(player.getLocation(), Sound.ENTITY_CREAKING_ACTIVATE, 1f, 0.5f);
            plugin.growth().regrow(player);
        }
    }

    private void visualBreak(Location block, Material mat) {
        if (mat == null || mat == Material.AIR) {
            block.getBlock().setType(Material.AIR);
            return;
        }
        block.getWorld().playSound(block, Sound.BLOCK_WOOD_BREAK, 1f, 1f);
        block.getWorld().spawnParticle(Particle.BLOCK, block, 20, 0.5, 0.5, 0.5, 0, mat.createBlockData());
        block.getBlock().setType(Material.AIR);
    }

    private void spawnTreeClearAnim(Player player, Location pos) {
        Location aligned = LocUtil.alignBlockCenter(pos);
        boolean lookingDown = player.getEyeLocation().getY() > aligned.getY();
        float pitchDegrees = lookingDown ? 90f : -90f;

        TextDisplay display = aligned.getWorld().spawn(aligned, TextDisplay.class, d -> {
            d.text(Component.text("☐"));
            d.setBillboard(Display.Billboard.FIXED);
            d.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
            d.setPersistent(false);
        });
        for (Player other : plugin.getServer().getOnlinePlayers()) {
            if (!other.equals(player)) other.hideEntity(plugin, display);
        }

        new BukkitRunnable() {
            int idx = 0;

            @Override
            public void run() {
                if (!display.isValid()) {
                    cancel();
                    return;
                }
                if (idx > 90) {
                    display.remove();
                    cancel();
                    return;
                }

                display.setInterpolationDuration(1);
                display.setInterpolationDelay(0);
                display.setTextOpacity((byte) ((100 - idx) / 2));

                float scale = idx * 2;
                display.setTransformation(new Transformation(
                        new Vector3f(-0.02f, 0f, -0.13f).mul(scale),
                        new Quaternionf(new AxisAngle4f((float) Math.toRadians(pitchDegrees), 1, 0, 0)),
                        new Vector3f(scale),
                        new Quaternionf()
                ));

                idx += 10;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void spawnBlockFallAnim(Player player, Location block, Material mat) {
        PlayerData data = plugin.data().get(player.getUniqueId());
        String logEffects = data.settings.getOrDefault("logEffects", "Enabled");
        if (logEffects.equals("Disabled") || logEffects.equals("Particles Only")) return;
        if (mat == null || mat == Material.AIR) return;

        ItemDisplay display = block.getWorld().spawn(block, ItemDisplay.class, d -> {
            d.setItemStack(new ItemStack(mat));
            d.setPersistent(false);
        });

        Location ground = block.clone();
        int searched = 0;
        while (searched < 40 && ground.getY() > block.getWorld().getMinHeight()) {
            Material groundMat = ground.getBlock().getType();
            if (groundMat == Material.WATER || groundMat == Material.GRASS_BLOCK) break;
            ground.add(0, -1, 0);
            searched++;
        }
        double distDown = Math.abs(block.getY() - ground.getY());
        int distTime = (int) Math.max(1, Math.ceil(distDown * 3));

        double xOff = LocUtil.random(-2, 2);
        double zOff = LocUtil.random(-2, 2);
        double pitchOff = LocUtil.random(-90, 90);
        double yawOff = LocUtil.random(-90, 90);
        double rollOff = LocUtil.random(-90, 90);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!display.isValid()) {
                    return;
                }
                int clampedDuration = Math.min(59, distTime);
                display.setTeleportDuration(clampedDuration);
                display.setInterpolationDuration(clampedDuration);
                display.setInterpolationDelay(0);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!display.isValid()) return;
                        Location target = block.clone().add(xOff, -distDown, zOff);
                        display.teleport(target);
                        display.setTransformation(new Transformation(
                                new Vector3f(0, 0, 0),
                                new Quaternionf().rotateY((float) Math.toRadians(yawOff))
                                        .rotateX((float) Math.toRadians(pitchOff))
                                        .rotateZ((float) Math.toRadians(rollOff)),
                                new Vector3f(1, 1, 1),
                                new Quaternionf()
                        ));

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (display.isValid()) display.remove();
                            }
                        }.runTaskLater(plugin, distTime);
                    }
                }.runTaskLater(plugin, 1L);
            }
        }.runTaskLater(plugin, 1L);
    }

}
