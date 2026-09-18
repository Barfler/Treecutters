package com.barfl.treecutters.weather;

import com.barfl.treecutters.Treecutters;
import com.barfl.treecutters.data.GlobalState;
import com.barfl.treecutters.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.WeatherType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public final class WeatherMachine {
    private final Treecutters plugin;

    public WeatherMachine(Treecutters plugin) {
        this.plugin = plugin;
    }

    public GlobalState state() {
        return plugin.globalState().state();
    }

    public void tick() {
        GlobalState state = state();
        int playerCount = plugin.getServer().getOnlinePlayers().size();
        Location lightningLoc = configLoc("weatherLightning");

        if (state.weatherMachineLogs > 250_000_000) {
            state.weatherMachineLogs -= (150_000.0 * playerCount) / 20.0;
            state.weatherType = 4;
            if (plugin.ticksSinceStartup() % 100 == 1 && lightningLoc != null) {
                lightningLoc.getWorld().strikeLightningEffect(lightningLoc);
            }
            setWorldWeather(true);
        } else if (state.weatherMachineLogs > 100_000_000) {
            state.weatherMachineLogs -= (40_000.0 * playerCount) / 20.0;
            state.weatherType = 3;
            if (plugin.ticksSinceStartup() % 150 == 1 && lightningLoc != null) {
                lightningLoc.getWorld().strikeLightningEffect(lightningLoc);
            }
            setWorldWeather(true);
        } else if (state.weatherMachineLogs > 50_000_000) {
            state.weatherMachineLogs -= (15_000.0 * playerCount) / 20.0;
            state.weatherType = 2;
            if (plugin.ticksSinceStartup() % 300 == 1 && lightningLoc != null) {
                lightningLoc.getWorld().strikeLightningEffect(lightningLoc);
            }
            setWorldWeather(true);
        } else if (state.weatherMachineLogs > 10_000_000) {
            state.weatherMachineLogs -= (5_000.0 * playerCount) / 20.0;
            state.weatherType = 1;
            setWorldWeather(true);
        } else {
            state.weatherType = 0;
            setWorldWeather(false);
        }
    }

    private void setWorldWeather(boolean storm) {
        var world = plugin.getServer().getWorld(plugin.getConfig().getString("world", "world"));
        if (world != null) world.setStorm(storm);
    }

    public void donate(Player player) {
        Location machine = configLoc("weatherMachineBlock");
        if (machine == null) return;
        if (!isLookingAt(player, machine)) return;

        PlayerData data = plugin.data().get(player.getUniqueId());
        if (data.logs >= 1_000_000) {
            data.logs -= 1_000_000;
            state().weatherMachineLogs += 1_000_000;
            data.donatedToWeatherMachine += 1_000_000;
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1f);
        } else {
            player.sendMessage(plugin.messages().get("weather.insufficient-logs"));
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED, 1f, 1f);
        }
    }

    private boolean isLookingAt(Player player, Location block) {
        Location eye = player.getEyeLocation();
        Location blockCenter = block.clone().add(0.5, 0.5, 0.5);
        if (eye.getWorld() != blockCenter.getWorld()) return false;
        if (eye.distance(blockCenter) > 6) return false;
        var toBlock = blockCenter.toVector().subtract(eye.toVector()).normalize();
        return eye.getDirection().normalize().dot(toBlock) > 0.97;
    }

    private Location configLoc(String key) {
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("locations." + key);
        if (sec == null) return null;
        var world = plugin.getServer().getWorld(plugin.getConfig().getString("world", "world"));
        if (world == null) return null;
        return new Location(world, sec.getDouble("x"), sec.getDouble("y"), sec.getDouble("z"));
    }
}
