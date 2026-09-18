# Treecutters

An unofficial **Paper port** of [treecutters](https://github.com/akarahdev/treecutters), a
tree-cutting idle/tycoon minigame originally made for [DiamondFire](https://mcdiamondfire.com/).

The original game was written using [terracotta](https://github.com/Owlfroggy/terracotta),
a text-to-block-code compiler for DiamondFire's visual block-code system.

This port reimplements the game's mechanics and balance data, including prices, tiers,
thresholds, and room geometry, as a regular Java plugin using the Paper API.

## Requirements

* **Java 17+**
* **Minecraft 1.21.4+**
* **Paper** or a compatible Paper fork, such as Purpur
* **[WorldEdit](https://enginehub.org/worldedit)** - required for pasting room and city schematics

WorldEdit is a soft dependency. The plugin will still load without it, but schematics won't
be pasted and a warning will be logged.

## Building

Build the plugin with Maven:

```bash
mvn clean package
```

The compiled JAR will be located at:

```text
target/Treecutters-1.0.0.jar
```

Drop it into your server's `plugins/` directory and restart the server.

## Setup

### 1. Configure the world

Set `world` in `config.yml` to the world containing the player rooms and city:

```yaml
world: world
```

### 2. Room schematic

The original DiamondFire version copied a master room into each player's assigned slot using
`game.cloneRegion(...)`. This port uses a WorldEdit schematic instead.

A default `room.schem` is bundled with the plugin. On first startup, and on every `/ttc reload`,
it is exported to:

```text
plugins/Treecutters/schematics/room.schem
```

The file is only exported if it doesn't already exist, so your own schematic will never be
overwritten. If you want to restore the bundled version, delete `room.schem` and reload the
plugin.

To use your own room:

1. Build the room or arena you want each player to receive.

2. Save it as a WorldEdit `.schem` file:

   ```text
   //schematic save room
   ```

3. Place it at:

   ```text
   plugins/Treecutters/schematics/room.schem
   ```

You can also change `schematic.file` in `config.yml` to use a different filename. The bundled
schematic is always exported as `room.schem`, so custom filenames need to be provided manually.

The `schematic.offsetX`, `schematic.offsetY`, and `schematic.offsetZ` settings control where
the schematic's origin is placed relative to the assigned room position.

The defaults (`-25, -3, -25`) match the offsets used by the original game. Adjust them if
needed to match your schematic's origin.

### 3. Room grid

The `roomGrid` section in `config.yml` controls how many room slots are available and how far
apart they are.

Make sure the spacing is large enough for your room schematic so neighboring rooms don't
overlap.

Configure this **before assigning permanent player rooms**. Changing the grid afterward can
change where existing room slots are expected to be.

### 4. City schematic and locations

The original game uses fixed world locations for things like:

* Stock market signs
* Weather machine
* City warp point

A default `city.schem` is bundled with the plugin and works the same way as `room.schem`. On
startup and `/ttc reload`, it is exported to:

```text
plugins/Treecutters/schematics/city.schem
```

The file is only exported if it doesn't already exist.

Unlike player rooms, the city isn't pasted automatically. It's meant to be placed once at a
fixed location. Set `citySchematic.x`, `citySchematic.y`, and `citySchematic.z` in `config.yml`,
then run:

```text
/ttc pastecity
```

If you're using your own city schematic, set `citySchematic.file` before running the command.

After pasting the city, update the `locations` section in `config.yml` to match the locations
of its signs, weather machine, and warp point. The stock market buy, sell, value, and chart
locations need to point to actual signs.

### 5. Discord invite

Set `discord.invite-url` in `messages.yml` to your server's Discord invite URL.

## Customizing text (`messages.yml`)

Player-facing text lives in `plugins/Treecutters/messages.yml` rather than being hardcoded in
the Java source. This includes chat messages, GUI titles and lore, the tab list, action bar,
chat minigames, stock ticker text, the Discord invite, "Did You Know?" tips, and menu names
and descriptions.

Values use [MiniMessage](https://docs.advntr.dev/minimessage/format.html), so formatting such
as `<green>`, `<bold>`, and `<gradient:c1:c2>` is supported.

Placeholders use `%name%`, for example:

```text
%player%
%amount%
%cost%
```

Available placeholders are documented alongside their respective keys in `messages.yml`.

Balance and structural values aren't stored in `messages.yml`. Things like prices, stat
scaling, level requirements, material icons, and internal setting values such as `"Enabled"`
and `"Look-Based"` still live in `config.yml` or the `com.barfl.treecutters.config` classes.

Run `/ttc reload` after editing either file to apply your changes without restarting the
server. This reloads `config.yml` and `messages.yml`, refreshes the wood type list, and reloads
the room schematic.

The world and room grid are only read at startup. They aren't reloaded because changing them
while players already have assigned rooms can cause problems.

## Commands

* `/discord` - shows the configured Discord invite
* `/profile <player>` - shows a player's stat shop levels and current tree tier
* `/ttc reload` - reloads `config.yml` and `messages.yml`
* `/ttc grant_logs <player> <amount>` - gives a player logs
* `/ttc setstatshop <player> <stat> <level>` - sets a player's upgrade level
* `/ttc stocktime <1-12|-1>` - forces the outcome of the next stock market tick
* `/ttc chat_game <1-5>` - starts a specific chat minigame
* `/ttc didyouknow` - broadcasts a random tip
* `/ttc analyzetree <player>` - shows a player's broken and total tree block counts
* `/ttc killall` - removes all non-player entities in the command sender's world

All commands above except `/ttc reload` require the `treecutters.admin` permission, which
defaults to op.

## License & Attribution

This project is licensed under the [MIT License](./LICENSE).

Treecutters is an unofficial, from-scratch Java reimplementation of
[akarahdev/treecutters](https://github.com/akarahdev/treecutters). It recreates the original
game's mechanics and balance data using the Paper API without including or redistributing the
original Terracotta source code.

The original `treecutters` repository does not currently include a license. The MIT License
in this repository only applies to the code in this reimplementation, not the original
project or its source code.

### Credits

* **akarahdev** - creator of the original [treecutters](https://github.com/akarahdev/treecutters)
* **Owlfroggy** - creator of [terracotta](https://github.com/Owlfroggy/terracotta), which was
  used to build the original DiamondFire version

This project is not affiliated with or endorsed by the original authors.
