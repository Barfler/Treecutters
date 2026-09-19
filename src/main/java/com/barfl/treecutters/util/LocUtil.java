package com.barfl.treecutters.util;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class LocUtil {
    private LocUtil() {
    }

    public static double random(double min, double max) {
        if (min == max) return min;
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    public static int randomInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static double randomWhole(double min, double max) {
        return Math.round(random(min, max));
    }

    public static Location alignBlockCenter(Location loc) {
        Location out = loc.clone();
        out.setX(Math.floor(loc.getX()) + 0.5);
        out.setY(Math.floor(loc.getY()));
        out.setZ(Math.floor(loc.getZ()) + 0.5);
        out.setYaw(0);
        out.setPitch(0);
        return out;
    }

    public static Vector forward(Location loc) {
        double yawRad = Math.toRadians(loc.getYaw());
        double pitchRad = Math.toRadians(loc.getPitch());
        double x = -Math.sin(yawRad) * Math.cos(pitchRad);
        double y = -Math.sin(pitchRad);
        double z = Math.cos(yawRad) * Math.cos(pitchRad);
        return new Vector(x, y, z);
    }

    public static Location shiftInDirectionForward(Location loc, double distance) {
        Vector dir = forward(loc).normalize().multiply(distance);
        return loc.clone().add(dir);
    }

    public static List<Location> path(Location from, Location to, double step) {
        List<Location> points = new ArrayList<>();
        double distance = from.distance(to);
        if (distance < 1e-6) {
            points.add(from.clone());
            return points;
        }
        Vector dir = to.toVector().subtract(from.toVector()).normalize();
        for (double d = 0; d <= distance; d += step) {
            points.add(from.clone().add(dir.clone().multiply(d)));
        }
        return points;
    }

    public static List<Location> grid(Location corner1, Location corner2) {
        List<Location> points = new ArrayList<>();
        int minX = (int) Math.floor(Math.min(corner1.getX(), corner2.getX()));
        int maxX = (int) Math.floor(Math.max(corner1.getX(), corner2.getX()));
        int minY = (int) Math.floor(Math.min(corner1.getY(), corner2.getY()));
        int maxY = (int) Math.floor(Math.max(corner1.getY(), corner2.getY()));
        int minZ = (int) Math.floor(Math.min(corner1.getZ(), corner2.getZ()));
        int maxZ = (int) Math.floor(Math.max(corner1.getZ(), corner2.getZ()));
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    points.add(new Location(corner1.getWorld(), x, y, z));
                }
            }
        }
        return points;
    }

    public static Vector rotateAroundY(Vector v, double degrees) {
        double rad = Math.toRadians(degrees);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
        double x = v.getX() * cos + v.getZ() * sin;
        double z = -v.getX() * sin + v.getZ() * cos;
        return new Vector(x, v.getY(), z);
    }

    public static Vector rotateAroundZ(Vector v, double degrees) {
        double rad = Math.toRadians(degrees);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
        double x = v.getX() * cos - v.getY() * sin;
        double y = v.getX() * sin + v.getY() * cos;
        return new Vector(x, y, v.getZ());
    }
}
