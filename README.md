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

- **🌾 Farmland Protection:** Stops players, mobs, pets, and villagers from destroying your precious crops and reverting farmland to dirt.
- **⚙️ Granular Configuration:** Every setting can be toggled independently via interactive chat commands, the Mod Menu GUI, or the local JSON config file — no GUI-only settings, so dedicated servers have full control from the command line.
- **🧱 Prevent Empty Farmland Trampling:** Option to protect empty farmland block states from being ruined by random entities.
- **🐕 Pet & Villager Protection:** Independently protect tamed animals and villagers from triggering trampling, on top of the general mob rule.
- **🪶 Feather Falling & Leather Boots:** Choose whether players need Feather Falling (and at what level) to be protected, or exempt anyone wearing Leather Boots regardless of enchantments.
- **💧 Farmland Maintenance:** Optionally stop farmland from ever drying out, or stop unplanted farmland from reverting to dirt on its own.
- **🎇 Feedback:** Optional particles, sound, and action bar messages when a trample attempt is blocked, each independently toggleable and rate-limited.
- **📋 Presets:** Switch between curated `vanilla_plus`, `casual`, and `hardcore` profiles, or fine-tune everything as `custom`.
- **🪶 Ultra-Lightweight:** Written utilizing efficient Fabric Mixins. Features virtually zero performance overhead, with no active ticking or entity scanning.
- **🖥️ Server-Side Only Compatible:** Works purely on the server side; client installation is completely optional (though recommended for GUI configurations). Note that Mod Menu edits are client-side only and have no effect unless you are hosting the server yourself — see the "Multiplayer note" below.

---

## 📥 Installation Steps

1. Install the official [Fabric Loader](https://fabricmc.net/use/).
2. Download and drop [Fabric API](https://modrinth.com/mod/fabric-api) into your server environment.
3. Download the latest `.jar` mod build from [Modrinth](https://modrinth.com/mod/nocroptrample) or [GitHub Releases](https://github.com/Murqin/NoCropTrample/releases).
4. Save the file into your server's `mods/` directory.

---

## ⚙️ Configuration & Commands

### Interactive Admin Commands
*Requires Operator (OP) permission level 2.*

- `/nocroptrample status` - Query current active protection configuration (all settings).
- `/nocroptrample preset [vanilla_plus|casual|hardcore|custom]` - View or switch the active preset.
- `/nocroptrample empty <on|off>` - Toggle trampling protection specifically for unplanted (empty) farmland.
- `/nocroptrample player <on|off>` - Toggle trampling protection for players.
- `/nocroptrample mob <on|off>` - Toggle trampling protection for hostile and passive mobs.
- `/nocroptrample pet <on|off>` - Toggle trampling protection for tamed animals.
- `/nocroptrample villager <on|off>` - Toggle trampling protection for villagers.
- `/nocroptrample featherfalling [always|require_feather_falling|scaled_by_level]` - View or set the Feather Falling requirement for player protection.
- `/nocroptrample leatherboots <on|off>` - Toggle the Leather Boots exemption for players.
- `/nocroptrample dehydration <on|off>` - Toggle whether farmland is prevented from drying out (see the note below).
- `/nocroptrample emptyreversion <on|off>` - Toggle whether unplanted farmland is prevented from reverting to dirt on its own.
- `/nocroptrample particles <on|off>` - Toggle particle feedback on blocked trample attempts.
- `/nocroptrample sound <on|off>` - Toggle sound feedback on blocked trample attempts.
- `/nocroptrample actionbar <on|off>` - Toggle the action bar message on blocked trample attempts.
- `/nocroptrample cooldown <0-60>` - Set the feedback cooldown, in seconds, shared by particles/sound (per block) and the action bar message (per player).
- `/nocroptrample reload` - Force hot-reload configuration changes from disk.

Running any toggle command without an argument (e.g. `/nocroptrample pet`) shows its current value instead of changing it.

### Local Configuration File
Saved under `config/nocroptrample.json` in your server directory:

```json
{
  "activePreset": "CUSTOM",
  "preventPlayerTrampling": true,
  "preventMobTrampling": false,
  "preventEmptyTrampling": false,
  "featherFallingMode": "ALWAYS",
  "protectWithLeatherBoots": true,
  "preventPetTrampling": true,
  "preventVillagerTrampling": true,
  "preventDehydration": false,
  "preventEmptyReversion": false,
  "enableParticles": true,
  "enableSound": false,
  "enableActionBarMessage": false,
  "actionBarCooldownSeconds": 3
}
```

### 💧 A note on `preventDehydration`
Enabling this stops farmland from ever drying out — but since dry farmland halves crop growth chance in vanilla, this also speeds up crop growth across your whole world, not just where trampling would otherwise be a concern. It's off by default for that reason; enable it deliberately if faster growth is a tradeoff you want.

### 🖥️ Multiplayer note
The Mod Menu configuration screen changes settings in the client's local config file. On a dedicated server, this only has an effect if you are the one hosting it — connecting to someone else's server and changing settings via Mod Menu will not change how that server behaves. Use the [chat commands](#interactive-admin-commands) (with OP level 2) to change a remote server's settings instead.

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
