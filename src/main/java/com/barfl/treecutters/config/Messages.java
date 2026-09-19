package com.barfl.treecutters.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Messages {
    private final JavaPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private YamlConfiguration yml;

    public Messages(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        yml = YamlConfiguration.loadConfiguration(file);

        try (InputStream defaultStream = plugin.getResource("messages.yml")) {
            if (defaultStream != null) {
                YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                        new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
                yml.setDefaults(defaults);
            }
        } catch (Exception ignored) {
        }
    }

    private String raw(String path, Map<String, String> placeholders) {
        String value = yml.getString(path, "<red>Missing message: " + path);
        if (placeholders != null) {
            for (Map.Entry<String, String> e : placeholders.entrySet()) {
                value = value.replace("%" + e.getKey() + "%", e.getValue());
            }
        }
        return value;
    }

    private Component deserialize(String raw) {
        return miniMessage.deserialize(raw).decoration(TextDecoration.ITALIC, false);
    }

    public Component get(String path) {
        return deserialize(raw(path, null));
    }

    public Component get(String path, Map<String, String> placeholders) {
        return deserialize(raw(path, placeholders));
    }

    public Component get(String path, String... keyValuePairs) {
        return get(path, toMap(keyValuePairs));
    }

    public Component getList(String path) {
        return getList(path, null);
    }

    public Component getList(String path, Map<String, String> placeholders) {
        List<String> lines = yml.getStringList(path);
        Component result = Component.empty();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (placeholders != null) {
                for (Map.Entry<String, String> e : placeholders.entrySet()) {
                    line = line.replace("%" + e.getKey() + "%", e.getValue());
                }
            }
            result = result.append(deserialize(line));
            if (i < lines.size() - 1) result = result.appendNewline();
        }
        return result;
    }

    public List<Component> getLoreList(String path) {
        return getLoreList(path, null);
    }

    public List<Component> getLoreList(String path, Map<String, String> placeholders) {
        List<Component> out = new ArrayList<>();
        for (String line : yml.getStringList(path)) {
            if (placeholders != null) {
                for (Map.Entry<String, String> e : placeholders.entrySet()) {
                    line = line.replace("%" + e.getKey() + "%", e.getValue());
                }
            }
            out.add(deserialize(line));
        }
        return out;
    }

    public String rawString(String path) {
        return yml.getString(path, "");
    }

    public String rawString(String path, Map<String, String> placeholders) {
        return raw(path, placeholders);
    }

    public List<String> rawList(String path) {
        return yml.getStringList(path);
    }

    public List<Map<?, ?>> rawMapList(String path) {
        return yml.getMapList(path);
    }

    public Map<String, String> rawMap(String path) {
        Map<String, String> out = new LinkedHashMap<>();
        ConfigurationSection section = yml.getConfigurationSection(path);
        if (section == null) return out;
        for (String key : section.getKeys(false)) {
            out.put(key, section.getString(key));
        }
        return out;
    }

    public ConfigurationSection section(String path) {
        return yml.getConfigurationSection(path);
    }

    private static Map<String, String> toMap(String... keyValuePairs) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i + 1 < keyValuePairs.length; i += 2) {
            map.put(keyValuePairs[i], keyValuePairs[i + 1]);
        }
        return map;
    }
}
