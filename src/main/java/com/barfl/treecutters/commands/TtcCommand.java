package com.barfl.treecutters.commands;

import com.barfl.treecutters.Treecutters;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public final class TtcCommand implements CommandExecutor {
    private final Treecutters plugin;

    public TtcCommand(Treecutters plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        var msg = plugin.messages();
        if (args.length < 1) {
            sender.sendMessage(msg.get("commands.usage", "usage", command.getUsage()));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "grant_logs" -> {
                if (args.length < 3) return usage(sender);
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                double amount = Double.parseDouble(args[2].replace("_", ""));
                plugin.data().get(target.getUniqueId()).logs += amount;
                sender.sendMessage(msg.get("commands.grant-logs-confirm", "player", args[1], "amount", String.valueOf(amount)));
            }
            case "setstatshop" -> {
                if (args.length < 4) return usage(sender);
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                plugin.data().get(target.getUniqueId()).statShop.put(args[2], Integer.parseInt(args[3]));
            }
            case "stocktime" -> {
                if (args.length < 2) return usage(sender);
                int fc = Integer.parseInt(args[1]);
                if (fc == 0) fc = -1;
                plugin.globalState().state().lotteryTime = 0;
                plugin.stocks().tick(fc);
            }
            case "didyouknow" -> plugin.tips().broadcastNow();
            case "pastecity" -> {
                if (plugin.schematics() == null || !plugin.schematics().isCityReady()) {
                    sender.sendMessage(msg.get("commands.paste-city-not-ready"));
                    return true;
                }
                plugin.schematics().pasteCity();
                sender.sendMessage(msg.get("commands.paste-city-confirm"));
            }
            case "reload" -> {
                plugin.reloadConfig();
                plugin.messages().reload();
                plugin.items().reloadWoodTypes(plugin.getConfig());
                if (plugin.schematics() != null) plugin.schematics().reload();
                sender.sendMessage(msg.get("commands.reload-confirm"));
            }
            case "chat_game" -> {
                if (args.length < 2) return usage(sender);
                plugin.chatGames().start(Integer.parseInt(args[1]));
            }
            case "analyzetree" -> {
                if (args.length < 2) return usage(sender);
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                var session = plugin.data().session(target.getUniqueId());
                sender.sendMessage(msg.get("commands.analyze-tree-result",
                        "broken", String.valueOf(session.brokenLogs.size()), "total", String.valueOf(session.blocksToBreak.size())));
            }
            case "killall" -> {
                if (sender instanceof Player player) {
                    for (Entity e : player.getWorld().getEntities()) {
                        if (!(e instanceof Player)) e.remove();
                    }
                }
            }
            default -> {
                return usage(sender);
            }
        }
        return true;
    }

    private boolean usage(CommandSender sender) {
        sender.sendMessage(plugin.messages().get("commands.unknown-subcommand"));
        return true;
    }
}
