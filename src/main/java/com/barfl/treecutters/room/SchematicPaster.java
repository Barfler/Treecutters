package com.barfl.treecutters.room;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;

public final class SchematicPaster {
    private final JavaPlugin plugin;

    private Clipboard roomClipboard;
    private int roomOffsetX, roomOffsetY, roomOffsetZ;
    private boolean roomIgnoreAir;

    private Clipboard cityClipboard;
    private boolean cityIgnoreAir;

    public SchematicPaster(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        FileConfiguration cfg = plugin.getConfig();

        roomOffsetX = cfg.getInt("schematic.offsetX", -25);
        roomOffsetY = cfg.getInt("schematic.offsetY", -3);
        roomOffsetZ = cfg.getInt("schematic.offsetZ", -25);
        roomIgnoreAir = cfg.getBoolean("schematic.ignoreAirBlocks", false);
        roomClipboard = loadClipboard(cfg.getString("schematic.file", "room.schem"));

        cityIgnoreAir = cfg.getBoolean("citySchematic.ignoreAirBlocks", false);
        cityClipboard = loadClipboard(cfg.getString("citySchematic.file", "city.schem"));
    }

    private Clipboard loadClipboard(String fileName) {
        File schemFolder = new File(plugin.getDataFolder(), "schematics");
        if (!schemFolder.exists()) schemFolder.mkdirs();
        File schemFile = new File(schemFolder, fileName);

        exportDefaultIfMissing(schemFile, fileName);

        if (!schemFile.exists()) {
            plugin.getLogger().warning("Schematic file not found: " + schemFile.getPath());
            return null;
        }

        ClipboardFormat format = ClipboardFormats.findByFile(schemFile);
        if (format == null) {
            plugin.getLogger().warning("Unrecognized schematic format for " + schemFile.getPath());
            return null;
        }

        try (FileInputStream fis = new FileInputStream(schemFile);
             ClipboardReader reader = format.getReader(fis)) {
            return reader.read();
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load schematic " + schemFile.getPath(), e);
            return null;
        }
    }

    private void exportDefaultIfMissing(File schemFile, String fileName) {
        if (schemFile.exists()) return;

        String resourcePath = "schematics/" + fileName;
        if (plugin.getResource(resourcePath) == null) return;

        plugin.saveResource(resourcePath, false);
        plugin.getLogger().info("No schematic found at " + schemFile.getPath() + " - exported the bundled default.");
    }

    public boolean isReady() {
        return roomClipboard != null;
    }

    public boolean isCityReady() {
        return cityClipboard != null;
    }

    public void pasteAt(Location roomPos) {
        if (roomClipboard == null) {
            plugin.getLogger().warning("Cannot paste room schematic: none loaded (see config.yml schematic.file).");
            return;
        }

        BlockVector3 to = BlockVector3.at(
                roomPos.getBlockX() + roomOffsetX,
                roomPos.getBlockY() + roomOffsetY,
                roomPos.getBlockZ() + roomOffsetZ
        );
        paste(roomClipboard, roomPos.getWorld(), to, roomIgnoreAir, "room");
    }

    public void pasteCity() {
        if (cityClipboard == null) {
            plugin.getLogger().warning("Cannot paste city schematic: none loaded (see config.yml citySchematic.file).");
            return;
        }

        FileConfiguration cfg = plugin.getConfig();
        World world = plugin.getServer().getWorld(cfg.getString("world", "world"));
        if (world == null) {
            plugin.getLogger().warning("Cannot paste city schematic: configured world does not exist.");
            return;
        }

        BlockVector3 to = BlockVector3.at(
                cfg.getInt("citySchematic.x", 0),
                cfg.getInt("citySchematic.y", 4),
                cfg.getInt("citySchematic.z", 0)
        );
        paste(cityClipboard, world, to, cityIgnoreAir, "city");
    }

    private void paste(Clipboard clipboard, World world, BlockVector3 to, boolean ignoreAir, String label) {
        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(to)
                    .ignoreAirBlocks(ignoreAir)
                    .build();
            Operations.complete(operation);
        } catch (WorldEditException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to paste " + label + " schematic", e);
        }
    }
}
