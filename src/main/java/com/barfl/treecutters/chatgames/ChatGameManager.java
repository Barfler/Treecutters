package com.barfl.treecutters.chatgames;

import com.barfl.treecutters.Treecutters;
import com.barfl.treecutters.config.LogFamilies;
import com.barfl.treecutters.data.GlobalState;
import com.barfl.treecutters.data.PlayerData;
import com.barfl.treecutters.util.LocUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ChatGameManager {
    private final Treecutters plugin;

    public ChatGameManager(Treecutters plugin) {
        this.plugin = plugin;
    }

    public void maybeStart(long ticksSinceStartup) {
        if (ticksSinceStartup % 3000 != 0) return;
        if (plugin.getServer().getOnlinePlayers().size() < 3) return;
        start(0);
    }

    public void start(int gameTypeIn) {
        var msg = plugin.messages();
        GlobalState state = plugin.globalState().state();
        state.hintType = 0;

        int gameType = gameTypeIn == 0 ? LocUtil.randomInt(1, 5) : gameTypeIn;
        Component header;
        Component prompt;

        switch (gameType) {
            case 1 -> {
                int a = LocUtil.randomInt(2, 20);
                int b = LocUtil.randomInt(2, 20);
                int c = LocUtil.randomInt(2, 20);
                int equationType = LocUtil.randomInt(1, 3);
                header = msg.get("chatgames.quick-math.header");
                String key = equationType == 1 ? "prompt-add" : equationType == 2 ? "prompt-mul" : "prompt-sub";
                if (equationType == 1) state.chatGameSolution = String.valueOf(a + b * c);
                else if (equationType == 2) state.chatGameSolution = String.valueOf(a * b * c);
                else state.chatGameSolution = String.valueOf(a * b - c);
                prompt = msg.get("chatgames.quick-math." + key,
                        "a", String.valueOf(a), "b", String.valueOf(b), "c", String.valueOf(c));
            }
            case 3 -> {
                header = msg.get("chatgames.guess-number.header");
                state.chatGameSolution = String.valueOf(LocUtil.randomInt(1, 200));
                prompt = msg.get("chatgames.guess-number.prompt");
                state.hintType = 1;
            }
            case 4 -> {
                header = msg.get("chatgames.syllogism.header");
                Map<String, String> entries = msg.rawMap("chatgames.syllogism.entries");
                List<String> keys = List.copyOf(entries.keySet());
                if (keys.isEmpty()) return;
                String key = keys.get(LocUtil.randomInt(0, keys.size() - 1));
                state.chatGameSolution = entries.get(key);
                prompt = msg.get("chatgames.syllogism.prompt", "question", key);
            }
            case 5 -> {
                header = msg.get("chatgames.riddle.header");
                Map<String, String> entries = msg.rawMap("chatgames.riddle.entries");
                List<String> keys = List.copyOf(entries.keySet());
                if (keys.isEmpty()) return;
                String key = keys.get(LocUtil.randomInt(0, keys.size() - 1));
                state.chatGameSolution = entries.get(key);
                prompt = msg.get("chatgames.riddle.prompt", "question", key);
            }
            case 2 -> {
                header = msg.get("chatgames.scramble.header");
                List<String> words = msg.rawList("chatgames.scramble.words");
                if (words.isEmpty()) return;
                String word = words.get(LocUtil.randomInt(0, words.size() - 1));
                char[] chars = word.toCharArray();
                for (int i = 0; i < 20; i++) {
                    int i1 = LocUtil.randomInt(0, chars.length - 1);
                    int i2 = LocUtil.randomInt(0, chars.length - 1);
                    char tmp = chars[i1];
                    chars[i1] = chars[i2];
                    chars[i2] = tmp;
                }
                prompt = msg.get("chatgames.scramble.prompt", "scrambled", new String(chars));
                state.chatGameSolution = word;
            }
            default -> {
                return;
            }
        }

        plugin.getServer().broadcast(header.append(Component.space()).append(prompt));
    }

    public boolean handleChat(Player player, String message) {
        var msg = plugin.messages();
        GlobalState state = plugin.globalState().state();

        if (!state.chatGameSolution.isEmpty() && message.equalsIgnoreCase(state.chatGameSolution)) {
            PlayerData data = plugin.data().get(player.getUniqueId());
            var session = plugin.data().session(player.getUniqueId());

            var family = LogFamilies.forTier(session.stat("tree_type"));
            double treeType = session.stat("tree_type");
            double logInc = family.value() * ((Math.floor(treeType % 5) + 1) * 225);
            if (treeType >= 55) logInc = family.value() * (6 * 225);
            if (treeType >= 60) logInc *= 2;
            if (treeType >= 65) logInc *= 2;
            if (treeType >= 70) logInc *= 2;
            if (treeType >= 80) logInc *= 1.5;
            if (treeType >= 85) logInc *= 2 * 1.5;
            if (treeType >= 90) logInc *= 1.5;
            if (treeType >= 100) logInc *= 1.5;

            String fmt = NumberFormat.getIntegerInstance(Locale.US).format(Math.round(logInc));
            plugin.getServer().broadcast(msg.get("chatgames.winner", "player", player.getName(), "answer", state.chatGameSolution));
            plugin.getServer().broadcast(msg.get("chatgames.earned", "amount", fmt));

            data.logs += logInc;
            data.chatWins += 1;
            state.chatGameSolution = "";
            state.hintType = 0;
            return true;
        }

        if (state.hintType == 1) {
            try {
                double real = Double.parseDouble(state.chatGameSolution);
                double guess = Double.parseDouble(message.trim());
                player.sendMessage(msg.get(guess < real ? "chatgames.guess-number.too-small" : "chatgames.guess-number.too-big",
                        "guess", message.trim()));
                return true;
            } catch (NumberFormatException ignored) {

            }
        }

        if (LocUtil.randomInt(1, 100_000_000) == 1 && message.toLowerCase(Locale.ROOT).startsWith("meow")) {
            PlayerData data = plugin.data().get(player.getUniqueId());
            data.logs += 1_000_000_000;
            plugin.getServer().broadcast(msg.get("chatgames.meow-jackpot", "player", player.getName()));
        }

        return false;
    }
}
