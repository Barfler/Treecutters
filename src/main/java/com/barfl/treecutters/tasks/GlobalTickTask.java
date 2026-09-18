package com.barfl.treecutters.tasks;

import com.barfl.treecutters.Treecutters;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public final class GlobalTickTask extends BukkitRunnable {
    private final Treecutters plugin;
    private long ticks = 0;

    public GlobalTickTask(Treecutters plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        ticks++;

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            plugin.data().get(player.getUniqueId()).playtimeTicks += 1;
            plugin.turret().tick(player, ticks);
            plugin.actionBar().tick(player, ticks);
        }

        plugin.tabList().tick(ticks);
        plugin.chatGames().maybeStart(ticks);
        plugin.tips().maybeBroadcast(ticks);
        plugin.stocks().tick(-1);
        plugin.weather().tick();
    }

    public long ticks() {
        return ticks;
    }
}
