package com.barfl.treecutters.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerData {
    public final UUID uuid;

    public double logs = 0;
    public double trueLogs = 0;
    public double chatWins = 0;
    public double donatedToWeatherMachine = 0;
    public long playtimeTicks = 0;
    public int stockCount = 0;

    public final Map<String, Integer> statShop = new HashMap<>();
    public final Map<String, String> settings = new HashMap<>();

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
    }

    public int statLevel(String key) {
        return statShop.getOrDefault(key, 0);
    }
}
