# No Crop Trample

A lightweight, server-side mod for Fabric and NeoForge that prevents farmland from being trampled by players or mobs.

[![Platform: Fabric](https://img.shields.io/badge/Platform-Fabric-blue?style=flat-square)](#)
[![Platform: NeoForge](https://img.shields.io/badge/Platform-NeoForge-orange?style=flat-square)](#)
[![License: MIT](https://img.shields.io/badge/License-MIT-green?style=flat-square)](LICENSE)
[![Minecraft: 1.20.6–1.21.10](https://img.shields.io/badge/Minecraft-1.20.6--1.21.10-darkgreen?style=flat-square)](#)

## Demo

The same test with each setting off and on.

| | Off | On |
|---|---|---|
| **Player** | <img src="https://raw.githubusercontent.com/murqin/NoCropTrample/main/assets/player-trampling-prevention-off.gif" alt="Player trampling prevention off: farmland turns to dirt" width="320"> | <img src="https://raw.githubusercontent.com/murqin/NoCropTrample/main/assets/player-trampling-prevention-on.gif" alt="Player trampling prevention on: farmland stays intact" width="320"> |
| **Mob** | <img src="https://raw.githubusercontent.com/murqin/NoCropTrample/main/assets/mob-trampling-prevention-off.gif" alt="Mob trampling prevention off: farmland turns to dirt" width="320"> | <img src="https://raw.githubusercontent.com/murqin/NoCropTrample/main/assets/mob-trampling-prevention-on.gif" alt="Mob trampling prevention on: farmland stays intact" width="320"> |
| **Empty farmland** | <img src="https://raw.githubusercontent.com/murqin/NoCropTrample/main/assets/empty-trampling-prevention-off.gif" alt="Empty farmland trampling prevention off: farmland turns to dirt" width="320"> | <img src="https://raw.githubusercontent.com/murqin/NoCropTrample/main/assets/empty-trampling-prevention-on.gif" alt="Empty farmland trampling prevention on: farmland stays intact" width="320"> |

Empty farmland protection only affects unplanted farmland; planted farmland follows the player and mob settings.

## Features

- Stops farmland from reverting to dirt when a player or mob lands on it.
- Player and mob trampling can be toggled independently, via chat commands or the config file.
- Optional protection for empty (unplanted) farmland.
- No ticking or entity scanning on either platform (a Fabric Mixin / a native NeoForge event), negligible overhead.
- Works server-side only; client install is optional and only adds a config screen (via ModMenu on Fabric, or NeoForge's built-in mod config screen).

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) + [Fabric API](https://modrinth.com/mod/fabric-api), **or** [NeoForge](https://neoforged.net/).
2. Download the matching jar from [Modrinth](https://modrinth.com/mod/nocroptrample) — `nocroptrample-fabric-<version>.jar` or `nocroptrample-neoforge-<version>.jar`.
3. Place it in your server's `mods/` directory.

## Commands

Requires operator permission level 2.

- `/nocroptrample status` — show current settings.
- `/nocroptrample empty <on|off>` — toggle protection for empty farmland.
- `/nocroptrample player <on|off>` — toggle player trampling protection.
- `/nocroptrample mob <on|off>` — toggle mob trampling protection.
- `/nocroptrample reload` — reload the config file from disk.

## Configuration

Stored at `config/nocroptrample.json`:

```json
{
  "preventEmptyTrampling": true,
  "preventPlayerTrampling": true,
  "preventMobTrampling": true
}
```

## Building from source

Requires JDK 25.

```bash
git clone https://github.com/murqin/NoCropTrample.git
cd NoCropTrample
./gradlew build
```

`./gradlew build` builds both platforms — the jars land in `fabric/build/libs/` and `neoforge/build/libs/`. Use `./gradlew :fabric:build` or `./gradlew :neoforge:build` to build just one.

## License

MIT — see [LICENSE](LICENSE).
