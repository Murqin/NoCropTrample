# No Crop Trample (Fabric)

A lightweight, server-side Fabric mod that prevents farmland from being trampled by players or mobs.

[![Platform: Fabric](https://img.shields.io/badge/Platform-Fabric-blue?style=flat-square)](#)
[![License: MIT](https://img.shields.io/badge/License-MIT-green?style=flat-square)](LICENSE)
[![Minecraft: 26.1+](https://img.shields.io/badge/Minecraft-26.1%2B-darkgreen?style=flat-square)](#)

[Demo video](https://youtu.be/ypxASh8R1tI)

## Features

- Stops farmland from reverting to dirt when a player or mob lands on it.
- Player and mob trampling can be toggled independently, via chat commands or the config file.
- Optional protection for empty (unplanted) farmland.
- Implemented with Fabric Mixins; no ticking or entity scanning, negligible overhead.
- Works server-side only; client install is optional and only adds a ModMenu config screen.

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/).
2. Install [Fabric API](https://modrinth.com/mod/fabric-api).
3. Download the mod jar from [Modrinth](https://modrinth.com/mod/nocroptrample) or [GitHub Releases](https://github.com/deimos-sh/NoCropTrample/releases).
4. Place it in your server's `mods/` directory.

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
git clone https://github.com/deimos-sh/NoCropTrample.git
cd NoCropTrample
./gradlew build
```

Output jar is written to `build/libs/`.

## License

MIT — see [LICENSE](LICENSE).
