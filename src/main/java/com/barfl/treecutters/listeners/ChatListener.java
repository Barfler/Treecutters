package com.barfl.treecutters.listeners;

import com.barfl.treecutters.Treecutters;
import io.papermc.paper.event.player.AsyncChatEvent;
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
        if (consumed) event.setCancelled(true);
    }
}
