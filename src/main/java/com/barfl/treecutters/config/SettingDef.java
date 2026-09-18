package com.barfl.treecutters.config;

import java.util.List;

public record SettingDef(String key, String icon, List<String> options, String defaultSetting) {
}
