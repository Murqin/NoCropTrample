# No Crop Trample (Fabric) 🌾

> **A lightweight, server-side utility mod for Minecraft Fabric 26.1+ that prevents farmland and crops from being trampled by players or mobs.**

[![Platform: Fabric](https://img.shields.io/badge/Platform-Fabric-blue?style=flat-square&logo=minecraft&logoColor=white)](#)
[![License: MIT](https://img.shields.io/badge/License-MIT-green?style=flat-square)](LICENSE)
[![Minecraft: 26.1+](https://img.shields.io/badge/Minecraft-26.1%2B-darkgreen?style=flat-square)](#)
[![Java: 25](https://img.shields.io/badge/Java-25-orange?style=flat-square)](#)
[![Demo: YouTube](https://img.shields.io/badge/Demo-YouTube-red?style=flat-square&logo=youtube&logoColor=white)](https://youtu.be/ypxASh8R1tI)

No Crop Trample provides robust and efficient farmland protection. Say goodbye to ruined crops caused by players jumping, running, or mobs wandering over your fields.

---

## ✨ Features

- **🌾 Farmland Protection:** Stops players and mobs from destroying your precious crops and reverting farmland to dirt.
- **⚙️ Granular Configuration:** Toggle player and mob trampling independently via interactive chat commands or local JSON config files.
- **🧱 Prevent Empty Farmland Trampling:** Option to protect empty farmland block states from being ruined by random entities.
- **🪶 Ultra-Lightweight:** Written utilizing efficient Fabric Mixins. Features virtually zero performance overhead, with no active ticking or entity scanning.
- **🖥️ Server-Side Only Compatible:** Works purely on the server side; client installation is completely optional (though recommended for GUI configurations).

---

## 📥 Installation Steps

1. Install the official [Fabric Loader](https://fabricmc.net/use/).
2. Download and drop [Fabric API](https://modrinth.com/mod/fabric-api) into your server environment.
3. Download the latest `.jar` mod build from [Modrinth](https://modrinth.com/mod/nocroptrample) or [GitHub Releases](https://github.com/Murqin/NoCropTrample/releases).
4. Save the file into your server's `mods/` directory.

---

## ⚙️ Configuration & Commands

### Interactive Admin Commands
*Requires Operator (OP) permission level 4.*

- `/nocroptrample status` - Query current active protection configurations.
- `/nocroptrample empty <on|off>` - Toggle trampling protection specifically for unplanted (empty) farmland.
- `/nocroptrample player <on|off>` - Toggle trampling protection for players.
- `/nocroptrample mob <on|off>` - Toggle trampling protection for hostile and passive mobs.
- `/nocroptrample reload` - Force hot-reload configuration changes from disk.

### Local Configuration File
Saved under `config/nocroptrample.json` in your server directory:

```json
{
  "preventEmptyTrampling": true,
  "preventPlayerTrampling": true,
  "preventMobTrampling": true
}
```

---

## 🛠️ Compilation from Source

Requirements: **Java JDK 25**

```bash
git clone https://github.com/Murqin/NoCropTrample.git
cd NoCropTrample
./gradlew build
```
Your compiled mod library will be situated in `build/libs/`.

---

## 📄 License

Licensed under the terms of the MIT License. See [LICENSE](LICENSE) for more details.
