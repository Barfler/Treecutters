package com.barfl.treecutters.data;

import org.bukkit.Location;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class PlayerSession {
    public final UUID uuid;

    public Location roomPos;
    public int hashCode;
    public boolean treeRegenerating = false;
    public boolean firstStrike = true;
    public int canDoubleJump = 0;
    public boolean jumpHeld = false;
    public double combo = 0;
    public int turretTimer = 5;
    public int leaderboardType = 0;

    public final Set<Location> brokenLogs = new HashSet<>();
    public java.util.List<Location> blocksToBreak = new java.util.ArrayList<>();

    public final Map<String, Double> stats = new HashMap<>();

    public Inventory openInventory;
    public String openInventoryId;

    public double lastLogsSample = 0;
    public double currentLogsSample = 0;
    public String logsPerSecondText = "";

    public PlayerSession(UUID uuid) {
        this.uuid = uuid;
    }

    public double stat(String key) {
        return stats.getOrDefault(key, 0.0);
    }
}
