package com.barfl.treecutters.commands;

import com.barfl.treecutters.Treecutters;
import com.barfl.treecutters.config.LogFamilies;
import com.barfl.treecutters.config.StatShopIcons;
import com.barfl.treecutters.config.StatUpgrade;
import com.barfl.treecutters.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

public final class ProfileCommand implements CommandExecutor {
    private final Treecutters plugin;

    public ProfileCommand(Treecutters plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        var msg = plugin.messages();
        if (args.length < 1) {
            sender.sendMessage(msg.get("commands.usage", "usage", "/profile <player>"));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(msg.get("commands.player-not-found"));
            return true;
        }

        PlayerData data = plugin.data().get(target.getUniqueId());
        sender.sendMessage(msg.get("commands.profile-header", "player", target.getName()));

        for (Map.Entry<String, StatUpgrade> entry : StatShopIcons.ALL.entrySet()) {
            int level = data.statLevel(entry.getKey());
            String name = msg.rawString("statshop." + entry.getKey() + ".name");
            sender.sendMessage(msg.get("commands.profile-stat-line", "name", name, "level", String.valueOf(level)));

            if (entry.getKey().equals("treeType")) {
                var family = LogFamilies.forTier(level);
                String fmt = com.barfl.treecutters.util.NumFmt.format(family.value());
                sender.sendMessage(msg.get("commands.profile-tree-line", "family", family.name(), "value", fmt));
            }
        }
        return true;
    }
}
