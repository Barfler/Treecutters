package com.barfl.treecutters.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SettingsDefs {
    private SettingsDefs() {
    }

    public static final Map<String, SettingDef> ALL = new LinkedHashMap<>();

    private static void reg(SettingDef d) {
        ALL.put(d.key(), d);
    }

    static {
        reg(new SettingDef("helpfulTips", "acacia_boat", List.of("Enabled", "Disabled"), "Enabled"));
        reg(new SettingDef("stockTickerAnnouncementsGlobal", "gold_ingot", List.of("Enabled", "Disabled"), "Disabled"));
        reg(new SettingDef("logSecActionBar", "stripped_spruce_wood", List.of("Enabled", "Disabled"), "Disabled"));
        reg(new SettingDef("logEffects", "stripped_cherry_wood",
                List.of("Enabled", "Log Only", "Particles Only", "Disabled"), "Enabled"));
        reg(new SettingDef("jumpType", "feather", List.of("Look-Based", "Key-Based"), "Look-Based"));
    }
}
