package com.barfl.treecutters.config;

import java.util.List;

public final class LogFamilies {
    private LogFamilies() {
    }

    public static final List<LogFamily> TABLE = List.of(
            new LogFamily(1, 0, "Oak"),
            new LogFamily(3, 6, "Darkwood"),
            new LogFamily(15, 12, "Cherry"),
            new LogFamily(30, 16, "Warped"),
            new LogFamily(60, 20, "Molten"),
            new LogFamily(100, 24, "Aerial"),
            new LogFamily(240, 28, "Lunar"),
            new LogFamily(480, 32, "Solar"),
            new LogFamily(600, 36, "Astral"),
            new LogFamily(900, 40, "Yggdrasil"),
            new LogFamily(1200, 45, "Infinitum I"),
            new LogFamily(1300, 45, "Infinitum II"),
            new LogFamily(1400, 45, "Infinitum III"),
            new LogFamily(1500, 45, "Infinitum IV"),
            new LogFamily(1600, 45, "Infinitum V"),
            new LogFamily(1700, 45, "Infinitum VI"),
            new LogFamily(1800, 45, "Infinitum VII"),
            new LogFamily(1900, 45, "Infinitum VIII"),
            new LogFamily(2000, 45, "Infinitum IX"),
            new LogFamily(2100, 45, "Infinitum X"),
            new LogFamily(2200, 45, "Infinitum XI"),
            new LogFamily(2300, 45, "Infinitum XII"),
            new LogFamily(2400, 45, "Infinitum XIII"),
            new LogFamily(2500, 45, "Infinitum XIV"),
            new LogFamily(2600, 45, "Infinitum XV"),
            new LogFamily(2700, 45, "Infinitum XVI")
    );

    public static LogFamily forTier(double treeTypeLevel) {
        int idx = (int) Math.floor(treeTypeLevel / 5.0);
        if (idx < 0) idx = 0;
        if (idx >= TABLE.size()) idx = TABLE.size() - 1;
        return TABLE.get(idx);
    }
}
