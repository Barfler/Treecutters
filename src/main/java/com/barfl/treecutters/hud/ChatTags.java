package com.barfl.treecutters.hud;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;

public final class ChatTags {
    private static final MiniMessage MM = MiniMessage.miniMessage();

    private static final List<String> GRADIENT_777_FFF = List.of("#777777", "#ffffff");
    private static final List<String> GRADIENT_BLUE_AQUA = List.of("blue", "aqua");
    private static final List<String> GRADIENT_DARKBLUE = List.of("#000099", "#0000ff");
    private static final List<String> GRADIENT_MAGENTA1 = List.of("#990099", "#ff00ff");
    private static final List<String> GRADIENT_MAGENTA2 = List.of("#660066", "#cc00cc");
    private static final List<String> GRADIENT_PERIWINKLE = List.of("#333399", "#7777ff");
    private static final List<String> GRADIENT_DARKBLUE2 = List.of("#000066", "#0000ff");
    private static final List<String> GRADIENT_PLUM = List.of("#440044", "#770077");
    private static final List<String> GRADIENT_MAGENTA3 = List.of("#770077", "#bb00bb");
    private static final List<String> GRADIENT_PERIWINKLE2 = List.of("#5555c4", "#c5d2e9");

    private static final List<String> PRIDE_PAN = List.of("#FF218C", "#FFD800", "#21B1FF");
    private static final List<String> PRIDE_TRANS = List.of("#5BCEFA", "#F5A9B8", "#FFFFFF", "#F5A9B8", "#5BCEFA");

    private static final List<String> FLAT_COLORS = List.of(
            "gray", "blue", "green", "yellow", "gold", "red", "dark_red", "dark_purple", "light_purple", "aqua"
    );
    private static final int[] FLAT_THRESHOLDS = {0, 4, 9, 14, 19, 24, 29, 34, 39, 44};

    public Component tagFor(int level) {
        return MM.deserialize(rawTagFor(level));
    }

    public String rawTagFor(int level) {
        String badge = level + "🪓";
        List<String> gradientStops = gradientFor(level);
        String middle = gradientStops != null
                ? "<gradient:" + String.join(":", gradientStops) + ">" + badge + "</gradient>"
                : "<" + flatColorFor(level) + ">" + badge;
        return "<dark_gray>[" + middle + "<dark_gray>]";
    }

    private List<String> gradientFor(int level) {
        if (level > 109) return PRIDE_TRANS;
        if (level > 104) return PRIDE_PAN;
        if (level > 99) return GRADIENT_PERIWINKLE2;
        if (level > 89) return GRADIENT_MAGENTA3;
        if (level > 84) return GRADIENT_PLUM;
        if (level > 79) return GRADIENT_DARKBLUE2;
        if (level > 74) return GRADIENT_PERIWINKLE;
        if (level > 69) return GRADIENT_MAGENTA2;
        if (level > 64) return GRADIENT_MAGENTA1;
        if (level > 59) return GRADIENT_DARKBLUE;
        if (level > 54) return GRADIENT_BLUE_AQUA;
        if (level > 49) return GRADIENT_777_FFF;
        return null;
    }

    private String flatColorFor(int level) {
        String color = FLAT_COLORS.get(0);
        for (int i = 0; i < FLAT_THRESHOLDS.length; i++) {
            if (level >= FLAT_THRESHOLDS[i]) color = FLAT_COLORS.get(i);
        }
        return color;
    }
}
