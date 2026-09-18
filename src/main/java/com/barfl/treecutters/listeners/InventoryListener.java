package com.barfl.treecutters.listeners;

import com.barfl.treecutters.Treecutters;
import com.barfl.treecutters.data.PlayerSession;
import com.barfl.treecutters.gui.SettingsMenu;
import com.barfl.treecutters.gui.StatShopMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public final class InventoryListener implements Listener {
    private final Treecutters plugin;

    public InventoryListener(Treecutters plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        PlayerSession session = plugin.data().session(player.getUniqueId());

        if (session.openInventoryId == null) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);
        if (event.getClickedInventory() == null || event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }

        switch (session.openInventoryId) {
            case StatShopMenu.ID -> plugin.statShopMenu().handleClick(event, player);
            case SettingsMenu.ID -> plugin.settingsMenu().handleClick(event, player);
            default -> {
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        event.setCancelled(true);
    }
}
