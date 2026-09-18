package com.barfl.treecutters;

import com.barfl.treecutters.chatgames.ChatGameManager;
import com.barfl.treecutters.commands.DiscordCommand;
import com.barfl.treecutters.commands.ProfileCommand;
import com.barfl.treecutters.commands.TtcCommand;
import com.barfl.treecutters.config.Messages;
import com.barfl.treecutters.data.GlobalStateManager;
import com.barfl.treecutters.data.PlayerDataManager;
import com.barfl.treecutters.economy.StockMarket;
import com.barfl.treecutters.gameplay.DoubleJumpManager;
import com.barfl.treecutters.gameplay.GrowthManager;
import com.barfl.treecutters.gameplay.StatsCalculator;
import com.barfl.treecutters.gameplay.SweepManager;
import com.barfl.treecutters.gameplay.ThrowingAxeManager;
import com.barfl.treecutters.gameplay.TurretAbility;
import com.barfl.treecutters.gui.LeaderboardProvider;
import com.barfl.treecutters.gui.MenuKeys;
import com.barfl.treecutters.gui.SettingsMenu;
import com.barfl.treecutters.gui.StatShopMenu;
import com.barfl.treecutters.gui.UpdateLogMenu;
import com.barfl.treecutters.hud.ActionBarManager;
import com.barfl.treecutters.hud.ChatTags;
import com.barfl.treecutters.hud.TabListManager;
import com.barfl.treecutters.hud.TipManager;
import com.barfl.treecutters.items.Items;
import com.barfl.treecutters.listeners.BlockListener;
import com.barfl.treecutters.listeners.ChatListener;
import com.barfl.treecutters.listeners.DamageListener;
import com.barfl.treecutters.listeners.InputListener;
import com.barfl.treecutters.listeners.InteractListener;
import com.barfl.treecutters.listeners.InventoryListener;
import com.barfl.treecutters.listeners.JoinQuitListener;
import com.barfl.treecutters.listeners.MovementListener;
import com.barfl.treecutters.room.RoomManager;
import com.barfl.treecutters.room.SchematicPaster;
import com.barfl.treecutters.tasks.GlobalTickTask;
import com.barfl.treecutters.tree.TreeGenerator;
import com.barfl.treecutters.weather.WeatherMachine;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class Treecutters extends JavaPlugin {
    private Messages messages;
    private PlayerDataManager playerDataManager;
    private GlobalStateManager globalStateManager;
    private RoomManager roomManager;
    private SchematicPaster schematicPaster;
    private Items items;
    private TreeGenerator treeGenerator;
    private SweepManager sweepManager;
    private GrowthManager growthManager;
    private ThrowingAxeManager throwingAxeManager;
    private TurretAbility turretAbility;
    private StatsCalculator statsCalculator;
    private DoubleJumpManager doubleJumpManager;
    private StockMarket stockMarket;
    private WeatherMachine weatherMachine;
    private ChatGameManager chatGameManager;
    private MenuKeys menuKeys;
    private StatShopMenu statShopMenu;
    private SettingsMenu settingsMenu;
    private UpdateLogMenu updateLogMenu;
    private LeaderboardProvider leaderboardProvider;
    private TabListManager tabListManager;
    private ActionBarManager actionBarManager;
    private ChatTags chatTags;
    private TipManager tipManager;
    private GlobalTickTask globalTickTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        messages = new Messages(this);

        if (getServer().getPluginManager().getPlugin("WorldEdit") == null) {
            getLogger().warning("WorldEdit is not installed - the room schematic paste feature will not work "
                    + "until it is. See https://enginehub.org/worldedit for downloads.");
        }

        World world = getServer().getWorld(getConfig().getString("world", "world"));
        if (world == null) {
            getLogger().log(Level.SEVERE, "Configured world '" + getConfig().getString("world")
                    + "' does not exist. Fix config.yml and restart.");
        }

        playerDataManager = new PlayerDataManager(this);
        globalStateManager = new GlobalStateManager(this);
        roomManager = world != null ? new RoomManager(world, getConfig()) : null;
        schematicPaster = new SchematicPaster(this);
        items = new Items(this);
        treeGenerator = new TreeGenerator();
        sweepManager = new SweepManager(this);
        growthManager = new GrowthManager(this);
        throwingAxeManager = new ThrowingAxeManager(this);
        turretAbility = new TurretAbility(this);
        statsCalculator = new StatsCalculator();
        doubleJumpManager = new DoubleJumpManager(this);
        stockMarket = new StockMarket(this);
        weatherMachine = new WeatherMachine(this);
        chatGameManager = new ChatGameManager(this);
        menuKeys = new MenuKeys(this);
        statShopMenu = new StatShopMenu(this, menuKeys);
        settingsMenu = new SettingsMenu(this, menuKeys);
        updateLogMenu = new UpdateLogMenu(this);
        leaderboardProvider = new LeaderboardProvider(this);
        tabListManager = new TabListManager(this);
        actionBarManager = new ActionBarManager(this);
        chatTags = new ChatTags();
        tipManager = new TipManager(this);

        getServer().getPluginManager().registerEvents(new JoinQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockListener(this), this);
        getServer().getPluginManager().registerEvents(new InteractListener(this), this);
        getServer().getPluginManager().registerEvents(new InputListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new DamageListener(), this);
        getServer().getPluginManager().registerEvents(new MovementListener(this), this);

        var discord = getCommand("discord");
        if (discord != null) discord.setExecutor(new DiscordCommand(this));
        var profile = getCommand("profile");
        if (profile != null) profile.setExecutor(new ProfileCommand(this));
        var ttc = getCommand("ttc");
        if (ttc != null) ttc.setExecutor(new TtcCommand(this));

        globalTickTask = new GlobalTickTask(this);
        globalTickTask.runTaskTimer(this, 1L, 1L);

        stockMarket.updateValue();

        getLogger().info("Treecutters enabled" + (roomManager != null ? " (" + roomManager.available() + " rooms available)" : ""));
    }

    @Override
    public void onDisable() {
        if (globalTickTask != null) globalTickTask.cancel();
        if (playerDataManager != null) playerDataManager.saveAllOnline();
        if (globalStateManager != null) globalStateManager.save();
    }

    public long ticksSinceStartup() {
        return globalTickTask != null ? globalTickTask.ticks() : 0;
    }

    public PlayerDataManager data() {
        return playerDataManager;
    }

    public GlobalStateManager globalState() {
        return globalStateManager;
    }

    public RoomManager rooms() {
        return roomManager;
    }

    public SchematicPaster schematics() {
        return schematicPaster;
    }

    public Items items() {
        return items;
    }

    public TreeGenerator treeGenerator() {
        return treeGenerator;
    }

    public SweepManager sweeping() {
        return sweepManager;
    }

    public GrowthManager growth() {
        return growthManager;
    }

    public ThrowingAxeManager throwingAxe() {
        return throwingAxeManager;
    }

    public TurretAbility turret() {
        return turretAbility;
    }

    public StatsCalculator statsCalculator() {
        return statsCalculator;
    }

    public DoubleJumpManager doubleJump() {
        return doubleJumpManager;
    }

    public StockMarket stocks() {
        return stockMarket;
    }

    public WeatherMachine weather() {
        return weatherMachine;
    }

    public ChatGameManager chatGames() {
        return chatGameManager;
    }

    public StatShopMenu statShopMenu() {
        return statShopMenu;
    }

    public SettingsMenu settingsMenu() {
        return settingsMenu;
    }

    public UpdateLogMenu updateLogMenu() {
        return updateLogMenu;
    }

    public LeaderboardProvider leaderboards() {
        return leaderboardProvider;
    }

    public TabListManager tabList() {
        return tabListManager;
    }

    public ActionBarManager actionBar() {
        return actionBarManager;
    }

    public ChatTags chatTags() {
        return chatTags;
    }

    public TipManager tips() {
        return tipManager;
    }

    public Messages messages() {
        return messages;
    }
}
