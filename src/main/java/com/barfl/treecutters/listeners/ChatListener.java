package com.barfl.treecutters.listeners;

import com.barfl.treecutters.Treecutters;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class ChatListener implements Listener {
    private final Treecutters plugin;

    public ChatListener(Treecutters plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());
        boolean consumed = plugin.chatGames().handleChat(event.getPlayer(), message);
        if (consumed) {
            event.setCancelled(true);
            return;
        }

        int level = (int) plugin.data().session(event.getPlayer().getUniqueId()).stat("tree_type");
        Component tag = plugin.chatTags().tagFor(level);
        event.renderer((source, sourceDisplayName, msg, viewer) -> tag.appendSpace()
                .append(Component.text(source.getName()).color(TextColor.fromHexString("#dddddd")))
                .append(Component.text(": "))
                .append(msg));
    }
}
