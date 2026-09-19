package com.barfl.treecutters.data;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public final class GlobalStateManager {
    private final JavaPlugin plugin;
    private final File file;
    private final GlobalState state;

    public GlobalStateManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "global.yml");
        this.state = load();
    }

    public GlobalState state() {
        return state;
    }

    private GlobalState load() {
        GlobalState s = new GlobalState();
        if (!file.exists()) return s;
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        s.stockValue = yml.getDouble("stockValue", 600_000);
        s.weatherMachineLogs = yml.getDouble("weatherMachineLogs", 0);
        s.cityPasted = yml.getBoolean("cityPasted", false);
        for (Double d : yml.getDoubleList("historicalStocks")) {
            s.historicalStocks.add(d);
        }
        return s;
    }

    public void save() {
        YamlConfiguration yml = new YamlConfiguration();
        yml.set("stockValue", state.stockValue);
        yml.set("weatherMachineLogs", state.weatherMachineLogs);
        yml.set("cityPasted", state.cityPasted);
        yml.set("historicalStocks", state.historicalStocks);
        try {
            yml.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save global state", e);
        }
    }
}
