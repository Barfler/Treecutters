package com.barfl.treecutters.room;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayDeque;
import java.util.Deque;

public final class RoomManager {
    private final Deque<Location> pool = new ArrayDeque<>();

    public RoomManager(World world, FileConfiguration config) {
        int startX = config.getInt("roomGrid.startX");
        int endX = config.getInt("roomGrid.endX");
        int stepX = config.getInt("roomGrid.stepX");
        int startZ = config.getInt("roomGrid.startZ");
        int endZ = config.getInt("roomGrid.endZ");
        int stepZ = config.getInt("roomGrid.stepZ");
        int startY = config.getInt("roomGrid.startY");
        int endY = config.getInt("roomGrid.endY");
        int stepY = config.getInt("roomGrid.stepY");

        for (int x = startX; x <= endX; x += stepX) {
            for (int z = startZ; z <= endZ; z += stepZ) {
                for (int y = startY; y <= endY; y += stepY) {
                    pool.addLast(new Location(world, x, y, z));
                }
            }
        }
    }

    public synchronized Location assign() {
        return pool.pollFirst();
    }

    public synchronized void release(Location roomPos) {
        if (roomPos != null) pool.addLast(roomPos);
    }

    public synchronized int available() {
        return pool.size();
    }
}
