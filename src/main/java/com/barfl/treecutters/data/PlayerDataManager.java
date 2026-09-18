package com.barfl.treecutters.data;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public final class PlayerDataManager {
    private final JavaPlugin plugin;
    private final File folder;
    private final Map<UUID, PlayerData> cache = new HashMap<>();
    private final Map<UUID, PlayerSession> sessions = new HashMap<>();

    public PlayerDataManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "playerdata");
        if (!folder.exists()) folder.mkdirs();
    }

    public PlayerData get(UUID uuid) {
        return cache.computeIfAbsent(uuid, this::load);
    }

    public PlayerSession session(UUID uuid) {
        return sessions.computeIfAbsent(uuid, PlayerSession::new);
    }

    public void dropSession(UUID uuid) {
        sessions.remove(uuid);
    }

    public Iterable<PlayerData> allKnown() {
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File f : files) {
                UUID id = uuidFromFileName(f.getName());
                if (id != null) cache.computeIfAbsent(id, this::load);
            }
        }
        return cache.values();
    }

    private UUID uuidFromFileName(String name) {
        try {
            return UUID.fromString(name.substring(0, name.length() - 4));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private PlayerData load(UUID uuid) {
        PlayerData data = new PlayerData(uuid);
        File file = new File(folder, uuid + ".yml");
        if (!file.exists()) return data;

        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        data.logs = yml.getDouble("logs", 0);
        data.trueLogs = yml.getDouble("trueLogs", 0);
        data.chatWins = yml.getDouble("chatWins", 0);
        data.donatedToWeatherMachine = yml.getDouble("donatedToWeatherMachine", 0);
        data.playtimeTicks = yml.getLong("playtimeTicks", 0);
        data.stockCount = yml.getInt("stockCount", 0);

        if (yml.isConfigurationSection("statShop")) {
            for (String key : yml.getConfigurationSection("statShop").getKeys(false)) {
                data.statShop.put(key, yml.getInt("statShop." + key));
            }
        }
        if (yml.isConfigurationSection("settings")) {
            for (String key : yml.getConfigurationSection("settings").getKeys(false)) {
                data.settings.put(key, yml.getString("settings." + key));
            }
        }
        return data;
    }

    public void save(PlayerData data) {
        YamlConfiguration yml = new YamlConfiguration();
        yml.set("logs", data.logs);
        yml.set("trueLogs", data.trueLogs);
        yml.set("chatWins", data.chatWins);
        yml.set("donatedToWeatherMachine", data.donatedToWeatherMachine);
        yml.set("playtimeTicks", data.playtimeTicks);
        yml.set("stockCount", data.stockCount);
        for (Map.Entry<String, Integer> e : data.statShop.entrySet()) {
            yml.set("statShop." + e.getKey(), e.getValue());
        }
        for (Map.Entry<String, String> e : data.settings.entrySet()) {
            yml.set("settings." + e.getKey(), e.getValue());
        }
        try {
            yml.save(new File(folder, data.uuid + ".yml"));
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save player data for " + data.uuid, e);
        }
    }

    public void saveAllOnline() {
        for (UUID id : sessions.keySet()) {
            PlayerData data = cache.get(id);
            if (data != null) save(data);
        }
    }
}
