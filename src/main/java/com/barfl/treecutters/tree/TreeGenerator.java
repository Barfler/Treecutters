package com.barfl.treecutters.tree;

import com.barfl.treecutters.util.LocUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class TreeGenerator {

    private static final List<TreeType> DEFAULT_UNIQUE_TYPES = List.of(
            TreeType.OAK, TreeType.DARK, TreeType.CHERRY, TreeType.WARPED, TreeType.MOLTEN,
            TreeType.AERIAL, TreeType.LUNAR, TreeType.SOLAR, TreeType.ASTRAL, TreeType.YGGDRASIL
    );

    public TreeSize sizeFor(int idx) {
        TreeSize[] sizes = {TreeSize.TINY, TreeSize.SMALL, TreeSize.MEDIUM, TreeSize.BIG, TreeSize.MEGA};
        TreeSize size = sizes[Math.floorMod(idx, 5)];
        return idx >= 55 ? TreeSize.MEGA : size;
    }

    private TreeType defaultTypeFor(int idx) {
        int typeIndex = Math.floorDiv(idx, 5);
        if (typeIndex < DEFAULT_UNIQUE_TYPES.size()) {
            return DEFAULT_UNIQUE_TYPES.get(typeIndex);
        }
        return TreeType.INFINITE;
    }

    public List<TreePlacement> growArbitraryTree(World world, Location roomPos, int idx) {
        return growArbitraryTree(world, roomPos, idx, defaultTypeFor(idx));
    }

    public List<TreePlacement> growArbitraryTree(World world, Location roomPos, int idx, TreeType treeType) {
        TreeSize size = sizeFor(idx);

        Location pos = roomPos.clone().add(LocUtil.randomInt(-3, 3), -1, LocUtil.randomInt(-3, 3));

        return switch (treeType) {
            case OAK -> buildOakTree(world, pos, size);
            case DARK -> buildDarkTree(world, pos, size);
            case CHERRY -> buildCherryTree(world, pos, size);
            case WARPED -> buildWarpedTree(world, pos, size);
            case MOLTEN -> buildMoltenTree(world, pos, size);
            case AERIAL -> buildAerialTree(world, pos, size);
            case LUNAR -> buildLunarTree(world, pos, size);
            case SOLAR -> buildSolarTree(world, pos, size);
            case ASTRAL -> buildAstralTree(world, pos, size);
            case YGGDRASIL -> buildYggdrasilTree(world, pos, size);
            case METEORIC -> buildMeteoricTree(world, pos, size);
            case TALL_METEORIC -> buildTallMeteoricTree(world, pos, size);
            case INFINITE -> {
                List<TreeType> sets = new ArrayList<>(List.of(
                        TreeType.OAK, TreeType.DARK, TreeType.CHERRY, TreeType.WARPED,
                        TreeType.AERIAL, TreeType.SOLAR, TreeType.ASTRAL, TreeType.YGGDRASIL
                ));
                List<TreeType> removalOrder = List.of(TreeType.OAK, TreeType.DARK, TreeType.CHERRY, TreeType.AERIAL);
                int tidx = idx;
                int tmp = 0;
                while (tidx >= 55 && tmp < removalOrder.size()) {
                    sets.remove(removalOrder.get(tmp));
                    tidx -= 5;
                    tmp++;
                }
                if (idx >= 55) sets.add(TreeType.METEORIC);
                if (idx >= 60) sets.add(TreeType.TALL_METEORIC);
                TreeType chosen = sets.get(LocUtil.randomInt(0, sets.size() - 1));
                yield growArbitraryTree(world, roomPos, idx, chosen);
            }
        };
    }

    private TreeSizeVariables sizeVariables(TreeSize size) {
        List<Vector> logPositions;
        int height;
        int branchReqs;
        int meteorSize = 0;
        switch (size) {
            case TINY -> {
                logPositions = List.of(new Vector(0, 0, 0));
                height = LocUtil.randomInt(4, 5);
                branchReqs = 2;
            }
            case SMALL -> {
                logPositions = List.of(
                        new Vector(0, 0, 0), new Vector(0, 0, 1), new Vector(1, 0, 0), new Vector(1, 0, 1));
                height = LocUtil.randomInt(6, 9);
                branchReqs = 3;
            }
            case MEDIUM -> {
                logPositions = ninePack();
                height = LocUtil.randomInt(7, 10);
                branchReqs = 3;
            }
            case BIG -> {
                logPositions = ninePack();
                height = LocUtil.randomInt(10, 13);
                branchReqs = 4;
            }
            case MEGA -> {
                List<Vector> pts = new ArrayList<>(ninePack());
                pts.add(new Vector(2, 0, 0));
                pts.add(new Vector(-2, 0, 0));
                pts.add(new Vector(0, 0, 2));
                pts.add(new Vector(0, 0, -2));
                logPositions = pts;
                height = LocUtil.randomInt(12, 14);
                branchReqs = 6;
                meteorSize = 4;
            }
            default -> throw new IllegalStateException();
        }
        return new TreeSizeVariables(logPositions, height, branchReqs, meteorSize);
    }

    private List<Vector> ninePack() {
        return List.of(
                new Vector(0, 0, 0), new Vector(0, 0, 1), new Vector(1, 0, 0), new Vector(1, 0, 1),
                new Vector(1, 0, -1), new Vector(-1, 0, 1), new Vector(0, 0, -1), new Vector(-1, 0, 0),
                new Vector(-1, 0, -1)
        );
    }

    private void reserveIfFree(World world, Set<Location> reserved, List<TreePlacement> out, Location loc, Material mat) {
        Location aligned = LocUtil.alignBlockCenter(loc);
        if (world.getBlockAt(aligned).getType() != Material.AIR) return;
        if (!reserved.add(aligned)) return;
        out.add(new TreePlacement(aligned, mat));
    }

    private void buildTrunk(World world, Set<Location> reserved, List<TreePlacement> out, Location root, int height,
                             List<Vector> logPositions, Material mat) {
        Location r = root.clone();
        for (int i = 0; i <= height; i++) {
            for (Vector offset : logPositions) {
                reserveIfFree(world, reserved, out, r.clone().add(offset), mat);
            }
            r.add(0, 1, 0);
        }
    }

    private List<Location> buildBranches(World world, Set<Location> reserved, List<TreePlacement> out, Location root,
                                          double length, int branches, double targetYOff, double centerDistance, Material mat) {
        double yawOffset = LocUtil.random(0, 360);
        double yawDiff = 360.0 / branches;
        List<Location> endcaps = new ArrayList<>();

        for (int idx = 1; idx <= branches; idx++) {
            double yaw = yawOffset + (yawDiff * idx);
            Location rootCopy = root.clone();
            rootCopy.setYaw((float) (rootCopy.getYaw() + yaw));
            rootCopy = LocUtil.shiftInDirectionForward(rootCopy, centerDistance);

            Location targetLoc = LocUtil.shiftInDirectionForward(rootCopy, length);
            targetLoc.add(0, targetYOff, 0);

            List<Location> path = LocUtil.path(rootCopy, targetLoc, 0.25);
            Location last = rootCopy;
            for (Location raw : path) {
                Location aligned = LocUtil.alignBlockCenter(raw);
                last = aligned;
                if (world.getBlockAt(aligned).getType() == Material.AIR && reserved.add(aligned)) {
                    out.add(new TreePlacement(aligned, mat));
                }
            }
            endcaps.add(last);
        }
        return endcaps;
    }

    private void buildMeteor(World world, Set<Location> reserved, List<TreePlacement> out, Location root, int size, Material mat) {
        for (Location gr : LocUtil.grid(root.clone().add(-size, -size, -size), root.clone().add(size, size, size))) {
            Location aligned = LocUtil.alignBlockCenter(gr);
            if (world.getBlockAt(aligned).getType() != Material.AIR) continue;
            if (aligned.distance(root) <= size && reserved.add(aligned)) {
                out.add(new TreePlacement(aligned, mat));
            }
        }
    }

    private List<TreePlacement> buildOakTree(World world, Location root, TreeSize size) {
        TreeSizeVariables v = sizeVariables(size);
        Set<Location> reserved = new HashSet<>();
        List<TreePlacement> out = new ArrayList<>();
        Location trunkRoot = root.clone();

        buildTrunk(world, reserved, out, trunkRoot, v.height(), v.logPositions(), Material.STRIPPED_OAK_WOOD);
        trunkRoot.add(0, v.height(), 0);
        buildBranches(world, reserved, out, trunkRoot, v.height() / 2.0, v.branchReqs(), LocUtil.random(-2, 2), 0,
                Material.STRIPPED_OAK_WOOD);

        return out;
    }

    private List<TreePlacement> buildDarkTree(World world, Location root, TreeSize size) {
        TreeSizeVariables v = sizeVariables(size);
        Set<Location> reserved = new HashSet<>();
        List<TreePlacement> out = new ArrayList<>();
        Location r = root.clone();

        r.add(0, 5, 0);
        buildBranches(world, reserved, out, r, 6, v.branchReqs(), -5, 0, Material.STRIPPED_SPRUCE_WOOD);
        r.add(0, -5, 0);

        buildTrunk(world, reserved, out, r, v.height(), v.logPositions(), Material.STRIPPED_SPRUCE_WOOD);
        r.add(0, v.height(), 0);
        buildBranches(world, reserved, out, r, v.height() * 0.75, v.branchReqs(), LocUtil.random(-3, 3), 0,
                Material.STRIPPED_SPRUCE_WOOD);

        return out;
    }

    private List<TreePlacement> buildCherryTree(World world, Location root, TreeSize size) {
        TreeSizeVariables v = sizeVariables(size);
        Set<Location> reserved = new HashSet<>();
        List<TreePlacement> out = new ArrayList<>();
        Location r = root.clone();
        double dx = LocUtil.random(-1, 1);
        double dz = LocUtil.random(-1, 1);

        for (int part = 1; part <= 4; part++) {
            buildTrunk(world, reserved, out, r, (int) Math.round(v.height() / 4.0), v.logPositions(), Material.STRIPPED_CHERRY_WOOD);
            r.add(dx, v.height() / 4.0, dz);
            r = LocUtil.alignBlockCenter(r);
        }

        buildBranches(world, reserved, out, r, v.height() * 0.8, v.branchReqs(), LocUtil.random(-3, 3), 0,
                Material.STRIPPED_CHERRY_WOOD);

        return out;
    }

    private List<TreePlacement> buildWarpedTree(World world, Location root, TreeSize size) {
        TreeSizeVariables v = sizeVariables(size);
        Set<Location> reserved = new HashSet<>();
        List<TreePlacement> out = new ArrayList<>();
        Location r = root.clone();

        buildTrunk(world, reserved, out, r, (int) Math.round(v.height() / 2.0), v.logPositions(), Material.STRIPPED_WARPED_STEM);
        r.add(0, v.height() / 2.0, 0);

        List<Location> endcaps = buildBranches(world, reserved, out, r, v.height() * 0.6, v.branchReqs(),
                LocUtil.random(2, 4), 0, Material.STRIPPED_WARPED_STEM);

        for (Location endcap : endcaps) {
            Location e = endcap.clone();
            buildTrunk(world, reserved, out, e, (int) Math.round(v.height() / 2.0), v.logPositions(), Material.STRIPPED_WARPED_STEM);
            e.add(0, v.height() / 2.0, 0);
            buildBranches(world, reserved, out, e, v.height() * 0.1, v.branchReqs(), LocUtil.random(2, 4), 0,
                    Material.STRIPPED_WARPED_STEM);
        }

        return out;
    }

    private List<TreePlacement> buildMoltenTree(World world, Location root, TreeSize size) {
        TreeSizeVariables v = sizeVariables(size);
        Set<Location> reserved = new HashSet<>();
        List<TreePlacement> out = new ArrayList<>();
        Location r = root.clone();

        buildTrunk(world, reserved, out, r, v.height(), v.logPositions(), Material.STRIPPED_CRIMSON_STEM);

        Location root2 = root.clone();
        for (int x = 0; x < v.height(); x += 3) {
            root2.add(0, 3, 0);
            buildBranches(world, reserved, out, root2, v.height() * 0.7, v.branchReqs(), LocUtil.random(-3, -1), 0,
                    Material.STRIPPED_CRIMSON_STEM);
        }

        return out;
    }

    private List<TreePlacement> buildAerialTree(World world, Location root, TreeSize size) {
        TreeSizeVariables v = sizeVariables(size);
        Set<Location> reserved = new HashSet<>();
        List<TreePlacement> out = new ArrayList<>();
        Location r = root.clone();

        buildTrunk(world, reserved, out, r, v.height(), v.logPositions(), Material.STRIPPED_PALE_OAK_WOOD);
        r.add(0, v.height(), 0);

        List<Location> endcaps = buildBranches(world, reserved, out, r, v.height() * 0.7, v.branchReqs(),
                LocUtil.random(-3, -1), 0, Material.STRIPPED_PALE_OAK_WOOD);

        for (Location endcap : endcaps) {
            buildBranches(world, reserved, out, endcap, LocUtil.random(2, 3), 1,
                    -LocUtil.random(v.height() * 0.6, v.height() * 0.8), 0, Material.STRIPPED_PALE_OAK_WOOD);
        }

        return out;
    }

    private List<TreePlacement> buildLunarTree(World world, Location root, TreeSize size) {
        TreeSizeVariables v = sizeVariables(size);
        Set<Location> reserved = new HashSet<>();
        List<TreePlacement> out = new ArrayList<>();
        Location r = root.clone();

        buildTrunk(world, reserved, out, r, v.height(), v.logPositions(), Material.PALE_OAK_WOOD);
        r.add(0, v.height(), 0);

        List<Location> endcaps = buildBranches(world, reserved, out, r, v.height() * 0.6, v.branchReqs(),
                LocUtil.random(2, 4), 0, Material.PALE_OAK_WOOD);

        for (Location endcap : endcaps) {
            buildBranches(world, reserved, out, endcap, v.height() * 0.5, v.branchReqs(), LocUtil.random(2, 4), 0,
                    Material.PALE_OAK_WOOD);
        }

        return out;
    }

    private List<TreePlacement> buildSolarTree(World world, Location root, TreeSize size) {
        TreeSizeVariables v = sizeVariables(size);
        Set<Location> reserved = new HashSet<>();
        List<TreePlacement> out = new ArrayList<>();

        double dt = LocUtil.random(0, 3.14);
        Location lastRoot = root.clone();
        for (double t = 0; t <= v.height(); t += 0.4) {
            double dx = Math.cos(t + dt) * 4;
            double dy = Math.sin(t + dt) * 4;
            lastRoot = root.clone().add(dx, t, dy);
            buildTrunk(world, reserved, out, lastRoot, 1, v.logPositions(), Material.STRIPPED_BIRCH_WOOD);
        }

        buildBranches(world, reserved, out, lastRoot, v.height() * 0.6, v.branchReqs(), LocUtil.random(2, 4), 0,
                Material.STRIPPED_BIRCH_WOOD);

        return out;
    }

    private List<TreePlacement> buildAstralTree(World world, Location root, TreeSize size) {
        TreeSizeVariables v = sizeVariables(size);
        Set<Location> reserved = new HashSet<>();
        List<TreePlacement> out = new ArrayList<>();

        double dt = LocUtil.random(0, 3.14);
        Location lastRoot = root.clone();
        double limit = v.height() * 1.2;
        for (double t = 0; t <= limit; t += 0.4) {
            double dx = Math.cos(t + dt) * 2;
            double dy = Math.sin(t + dt) * 2;
            lastRoot = root.clone().add(dx, t, dy);
            buildTrunk(world, reserved, out, lastRoot, 1, v.logPositions(), Material.STRIPPED_DARK_OAK_WOOD);
            if (t % 4 == 1.2) {
                buildBranches(world, reserved, out, lastRoot, Math.min(v.height() * 0.7, 6), v.branchReqs(),
                        LocUtil.random(1, 2), 0, Material.STRIPPED_DARK_OAK_WOOD);
            }
        }

        buildBranches(world, reserved, out, lastRoot, v.height() * 0.7, v.branchReqs(), LocUtil.random(2, 4), 0,
                Material.STRIPPED_DARK_OAK_WOOD);

        return out;
    }

    private List<TreePlacement> buildYggdrasilTree(World world, Location root, TreeSize size) {
        TreeSizeVariables v = sizeVariables(size);
        Set<Location> reserved = new HashSet<>();
        List<TreePlacement> out = new ArrayList<>();

        double dt = LocUtil.random(0, 3.14);
        Location lastRoot = root.clone();
        double limit = v.height() * 1.2;
        for (double t = 0; t <= limit; t += 0.4) {
            double dx = Math.cos(t + dt) * 2;
            double dy = Math.sin(t + dt) * 2;
            lastRoot = root.clone().add(dx, t, dy);
            buildTrunk(world, reserved, out, lastRoot, 1, v.logPositions(), Material.OAK_WOOD);

            Location midRoot = root.clone().add(0, t, 0);
            buildTrunk(world, reserved, out, midRoot, 1, v.logPositions(), Material.OAK_WOOD);

            if (t % 4 == 1.2) {
                buildBranches(world, reserved, out, lastRoot, Math.min(v.height() * 0.7, 6), v.branchReqs(),
                        LocUtil.random(1, 2), 0, Material.OAK_WOOD);
            }
        }

        buildBranches(world, reserved, out, lastRoot, v.height() * 0.7, v.branchReqs(), LocUtil.random(2, 4), 0,
                Material.OAK_WOOD);

        return out;
    }

    private List<TreePlacement> buildMeteoricTree(World world, Location root, TreeSize size) {
        TreeSizeVariables v = sizeVariables(size);
        Set<Location> reserved = new HashSet<>();
        List<TreePlacement> out = new ArrayList<>();
        Location r = root.clone();

        buildTrunk(world, reserved, out, r, v.height(), v.logPositions(), Material.STRIPPED_CRIMSON_STEM);
        r.add(0, v.height(), 0);

        List<Location> endcaps = buildBranches(world, reserved, out, r, v.height() * 0.8, v.branchReqs(),
                LocUtil.random(-3, -1), 0, Material.STRIPPED_CRIMSON_STEM);

        for (Location endcap : endcaps) {
            buildMeteor(world, reserved, out, endcap, 4, Material.STRIPPED_CRIMSON_STEM);
        }

        return out;
    }

    private List<TreePlacement> buildTallMeteoricTree(World world, Location root, TreeSize size) {
        TreeSizeVariables v = sizeVariables(size);
        Set<Location> reserved = new HashSet<>();
        List<TreePlacement> out = new ArrayList<>();
        Location r = root.clone();

        for (int idx = 0; idx < v.height(); idx += 3) {
            if (idx + 2 >= v.height()) break;
            r.add(0, 3, 0);
            buildTrunk(world, reserved, out, r, v.height(), v.logPositions(), Material.STRIPPED_ACACIA_WOOD);
            List<Location> endcaps = buildBranches(world, reserved, out, r, v.height() * 0.8, 1, LocUtil.random(-1, 1), 0,
                    Material.STRIPPED_ACACIA_WOOD);
            for (Location endcap : endcaps) {
                buildMeteor(world, reserved, out, endcap, 4, Material.STRIPPED_ACACIA_WOOD);
            }
        }

        return out;
    }
}
