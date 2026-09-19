package com.barfl.treecutters.economy;

import com.barfl.treecutters.Treecutters;
import com.barfl.treecutters.data.GlobalState;
import com.barfl.treecutters.data.PlayerData;
import com.barfl.treecutters.util.LocUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public final class StockMarket {
    private final Treecutters plugin;

    public StockMarket(Treecutters plugin) {
        this.plugin = plugin;
    }

    private GlobalState state() {
        return plugin.globalState().state();
    }

    private String fmt(double value) {
        return com.barfl.treecutters.util.NumFmt.format(value);
    }

    public void tick(int forceChance) {
        var msg = plugin.messages();
        GlobalState state = state();
        if (plugin.getServer().getOnlinePlayers().size() < 3) return;

        if (state.lotteryTime == 0) {
            state.lotteryTime = LocUtil.randomInt(20 * 45, 20 * 90);
            int depression = LocUtil.randomInt(1, 400);
            if (forceChance == 400) depression = 5;

            if (depression == 5) {
                state.stockValue = 100_000;
                broadcastToTicker(msg.get("stocks.great-depression"));
                return;
            }

            int rng = LocUtil.randomInt(1, 12);
            if (forceChance != -1) rng = forceChance;

            if (rng >= 1 && rng <= 5) {
                double scale = LocUtil.random(0.05, 0.1);
                boom(scale);
                broadcastToTicker(msg.get("stocks.ticker-up", "percent", fmt(scale * 100)));
            }
            if (rng >= 6 && rng <= 10) {
                double scale = LocUtil.random(0.05, 0.1);
                fall(scale);
                broadcastToTicker(msg.get("stocks.ticker-down", "percent", fmt(scale * 100)));
            }
            if (rng == 11) {
                double scale = LocUtil.random(0.4, 0.6);
                boom(scale);
                broadcastToTicker(msg.get("stocks.ticker-event-up", "event", randomEvent("stocks.boom-events"), "percent", fmt(scale * 100)));
            }
            if (rng == 12) {
                double scale = LocUtil.random(0.4, 0.6);
                fall(scale);
                broadcastToTicker(msg.get("stocks.ticker-event-down", "event", randomEvent("stocks.fall-events"), "percent", fmt(scale * 100)));
            }
            updateValue();
        }
        state.lotteryTime -= 1;
    }

    private String randomEvent(String listPath) {
        List<String> events = plugin.messages().rawList(listPath);
        if (events.isEmpty()) return "";
        String event = events.get(LocUtil.randomInt(0, events.size() - 1));

        List<String> companies = plugin.messages().rawList("stocks.event-companies");
        if (!companies.isEmpty()) {
            String company = companies.get(LocUtil.randomInt(0, companies.size() - 1));
            event = event.replace("{company}", company);
        }
        return event;
    }

    private void broadcastToTicker(Component message) {
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            PlayerData data = plugin.data().get(p.getUniqueId());
            if ("Enabled".equals(data.settings.get("stockTickerAnnouncementsGlobal"))) {
                p.sendMessage(message);
            }
        }
    }

    public void boom(double scale) {
        GlobalState state = state();
        state.historicalStocks.add(state.stockValue);
        state.stockValue *= 1 + scale;
        if (state.stockValue >= 3_000_000) state.stockValue = 3_000_000;
        trimHistory();
        updateValue();
    }

    public void fall(double scale) {
        GlobalState state = state();
        state.historicalStocks.add(state.stockValue);
        state.stockValue *= 1 / (1 + scale);
        if (state.stockValue <= 100_000) state.stockValue = 100_000;
        trimHistory();
        updateValue();
    }

    private void trimHistory() {
        GlobalState state = state();
        while (state.historicalStocks.size() > 5000) state.historicalStocks.remove(0);
    }

    public void buy(Player player) {
        PlayerData data = plugin.data().get(player.getUniqueId());
        GlobalState state = state();
        if (data.logs >= state.stockValue) {
            data.stockCount += 1;
            data.logs -= state.stockValue;
            state.stockValue += LocUtil.random(5000, 7000);
            updateValue();
        }
    }

    public void sell(Player player) {
        PlayerData data = plugin.data().get(player.getUniqueId());
        GlobalState state = state();
        if (data.stockCount >= 1) {
            data.stockCount -= 1;
            data.logs += state.stockValue;
            state.stockValue -= LocUtil.random(5000, 7000);
            updateValue();
        }
    }

    public void refreshIfNearby(Player player) {
        Location buy = configLoc("stockBuySign");
        if (buy != null && player.getLocation().getWorld() == buy.getWorld() && player.getLocation().distance(buy) <= 12) {
            updateValue();
        }
    }

    public void handleInteract(Player player, Location blockLocation) {
        Location buy = configLoc("stockBuySign");
        Location sell = configLoc("stockSellSign");
        Location chart = configLoc("stockChart");

        if (chart != null && blockLocation.getWorld() == chart.getWorld() && blockLocation.distance(chart) <= 1
                && player.getCooldown(Material.ACACIA_HANGING_SIGN) == 0) {
            player.setCooldown(Material.ACACIA_HANGING_SIGN, 20);
            showChart(player, chart);
        }
        if (buy != null && blockLocation.getWorld() == buy.getWorld() && blockLocation.distance(buy) <= 1) {
            buy(player);
        }
        if (sell != null && blockLocation.getWorld() == sell.getWorld() && blockLocation.distance(sell) <= 1) {
            sell(player);
        }
    }

    private void showChart(Player player, Location chartOrigin) {
        GlobalState state = state();
        if (state.historicalStocks.isEmpty()) return;
        List<Double> history = state.historicalStocks;

        new org.bukkit.scheduler.BukkitRunnable() {
            int x = 0;
            final Location pos = chartOrigin.clone();

            @Override
            public void run() {
                if (x >= history.size() || !player.isOnline()) {
                    cancel();
                    return;
                }
                pos.add(0, 0, 0.05);
                pos.setY(4 + (history.get(x) / 500000.0));
                player.spawnParticle(Particle.HAPPY_VILLAGER, pos, 1);
                x += 25;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public void updateValue() {
        var msg = plugin.messages();
        Location valueSign = configLoc("stockValueSign");
        if (valueSign != null) {
            setSignLine(valueSign, 2, msg.get("stocks.sign-value-label", "value", fmt(state().stockValue)));
        }

        Location buySign = configLoc("stockBuySign");
        Location sellSign = configLoc("stockSellSign");
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            PlayerData data = plugin.data().get(player.getUniqueId());
            Component holding = msg.get("stocks.sign-stocks-label", "count", fmt(data.stockCount));
            if (buySign != null) setSignLineFor(buySign, msg.get("stocks.sign-buy-title"), NamedTextColor.GREEN, holding);
            if (sellSign != null) setSignLineFor(sellSign, msg.get("stocks.sign-sell-title"), NamedTextColor.RED, holding);
        }
    }

    private void setSignLine(Location loc, int line, Component text) {
        if (loc.getBlock().getState() instanceof Sign sign) {
            sign.getSide(Side.FRONT).line(line, text);
            sign.update();
        }
    }

    private void setSignLineFor(Location loc, Component title, NamedTextColor color, Component subLine) {
        if (loc.getBlock().getState() instanceof Sign sign) {
            sign.getSide(Side.FRONT).line(0, Component.empty());
            sign.getSide(Side.FRONT).line(1, title.color(color).decorate(TextDecoration.BOLD));
            sign.getSide(Side.FRONT).line(2, Component.empty());
            sign.getSide(Side.FRONT).line(3, subLine);
            sign.update();
        }
    }

    private Location configLoc(String key) {
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("locations." + key);
        if (sec == null) return null;
        var world = plugin.getServer().getWorld(plugin.getConfig().getString("world", "world"));
        if (world == null) return null;
        return new Location(world, sec.getDouble("x"), sec.getDouble("y"), sec.getDouble("z"));
    }
}
