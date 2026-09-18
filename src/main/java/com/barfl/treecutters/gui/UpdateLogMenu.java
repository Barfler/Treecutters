package com.barfl.treecutters.gui;

import com.barfl.treecutters.Treecutters;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class UpdateLogMenu {
    public static final String ID = "updatelog";

    private final Treecutters plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public UpdateLogMenu(Treecutters plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = plugin.getServer().createInventory(player, 36, plugin.messages().get("gui.updatelog.title"));

        ItemStack tile = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta tileMeta = tile.getItemMeta();
        tileMeta.displayName(Component.empty());
        tile.setItemMeta(tileMeta);
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, tile.clone());

        List<Map<?, ?>> entries = plugin.messages().rawMapList("updatelog.entries");

        int slot = 0;
        for (Map<?, ?> entry : entries) {
            String date = String.valueOf(entry.get("date"));
            @SuppressWarnings("unchecked")
            List<String> changes = (List<String>) entry.get("changes");

            ItemStack icon = new ItemStack(Material.OAK_BOAT);
            ItemMeta meta = icon.getItemMeta();
            meta.displayName(Component.text(date, NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));

            List<Component> lore = new ArrayList<>();
            if (changes != null) {
                for (String line : changes) {
                    lore.add(miniMessage.deserialize("<gray>" + line).decoration(TextDecoration.ITALIC, false));
                }
            }
            meta.lore(lore);
            icon.setItemMeta(meta);

            inv.setItem(slot, icon);
            slot++;
            if (slot >= inv.getSize()) break;
        }

        plugin.data().session(player.getUniqueId()).openInventoryId = ID;
        player.openInventory(inv);
    }
}
