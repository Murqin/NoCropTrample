# No Crop Trample

A lightweight mod for Fabric and NeoForge that prevents farmland from being trampled and reverted to dirt when players or mobs land on it.

## Demo

The same test with each setting off and on.

| | Off | On |
|---|---|---|
| **Player** | <img src="https://raw.githubusercontent.com/murqin/NoCropTrample/main/assets/player-trampling-prevention-off.gif" alt="Player trampling prevention off: farmland turns to dirt" width="320"> | <img src="https://raw.githubusercontent.com/murqin/NoCropTrample/main/assets/player-trampling-prevention-on.gif" alt="Player trampling prevention on: farmland stays intact" width="320"> |
| **Mob** | <img src="https://raw.githubusercontent.com/murqin/NoCropTrample/main/assets/mob-trampling-prevention-off.gif" alt="Mob trampling prevention off: farmland turns to dirt" width="320"> | <img src="https://raw.githubusercontent.com/murqin/NoCropTrample/main/assets/mob-trampling-prevention-on.gif" alt="Mob trampling prevention on: farmland stays intact" width="320"> |
| **Empty farmland** | <img src="https://raw.githubusercontent.com/murqin/NoCropTrample/main/assets/empty-trampling-prevention-off.gif" alt="Empty farmland trampling prevention off: farmland turns to dirt" width="320"> | <img src="https://raw.githubusercontent.com/murqin/NoCropTrample/main/assets/empty-trampling-prevention-on.gif" alt="Empty farmland trampling prevention on: farmland stays intact" width="320"> |

Empty farmland protection only affects unplanted farmland; planted farmland follows the player and mob settings.

## Features

- Prevents farmland blocks from reverting to dirt when entities land on them.
- Player and mob trampling can be toggled independently.
- Optional protection for empty (unplanted) farmland.
- No tick handlers, no entity scanning — a Fabric Mixin on Fabric, a native event listener on NeoForge.
- Works server-side only; client install is optional (adds a config screen via ModMenu on Fabric, or NeoForge's built-in mod config screen).

## Commands

Requires operator permission level 2.

| Command | Description |
|---------|-------------|
| `/nocroptrample status` | Show current settings. |
| `/nocroptrample empty <on\|off>` | Toggle protection for empty farmland. |
| `/nocroptrample player <on\|off>` | Toggle player trampling protection. |
| `/nocroptrample mob <on\|off>` | Toggle mob trampling protection. |
| `/nocroptrample reload` | Reload the config file from disk. |

## Configuration

Config file: `config/nocroptrample.json`

```json
{
  "preventEmptyTrampling": true,
  "preventPlayerTrampling": true,
  "preventMobTrampling": true
}
```

## Requirements

- Minecraft 1.20.6–1.21.10
- Fabric Loader 0.16+ and Fabric API, **or** NeoForge 20.6–21.10
- Java 21+

## Links

- [GitHub](https://github.com/murqin/NoCropTrample)
- [Issues](https://github.com/murqin/NoCropTrample/issues)
