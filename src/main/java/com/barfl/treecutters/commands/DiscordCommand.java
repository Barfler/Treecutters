package com.barfl.treecutters.commands;

import com.barfl.treecutters.Treecutters;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class DiscordCommand implements CommandExecutor {
    private final Treecutters plugin;

    public DiscordCommand(Treecutters plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String url = plugin.messages().rawString("discord.invite-url");
        sender.sendMessage(plugin.messages().get("discord.link-message", "url", url));
        return true;
    }
}
