package com.barfl.treecutters.listeners;

import com.barfl.treecutters.Treecutters;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInputEvent;

public final class InputListener implements Listener {
    private final Treecutters plugin;

    public InputListener(Treecutters plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInput(PlayerInputEvent event) {
        plugin.doubleJump().handleInput(event);
    }
}
