# No Crop Trample

🌾 **Stop players and mobs from destroying your precious crops!**

<iframe width="560" height="315" src="https://www.youtube.com/embed/ypxASh8R1tI" frameborder="0" allowfullscreen></iframe>

No Crop Trample is a lightweight utility mod that prevents farmland from being trampled and converted to dirt when entities jump on it.

---

## ✨ Features

### 🛡️ Anti-Trample Protection
Prevents farmland blocks from being destroyed when players or mobs land on them. Your crops stay safe!

### ⚙️ Granular Control
Configure player and mob trampling independently:
- Want players to trample but not mobs? Done.
- Want mobs to trample but not players? Also done.
- Want total protection? Default behavior!

### 🪶 Lightweight
Uses efficient Mixins with virtually zero performance impact. No tick handlers, no entity scanning.

### 🖥️ Server-Side Compatible
Works on servers without requiring client installation. (Client recommended for ModMenu config GUI)

---

## 📋 Commands

All commands require Operator (OP) permission level.

| Command | Description |
|---------|-------------|
| `/nocroptrample` | Show current status |
| `/nocroptrample player <on\|off>` | Toggle player trampling prevention |
| `/nocroptrample mob <on\|off>` | Toggle mob trampling prevention |
| `/nocroptrample reload` | Reload configuration |

---

## 📁 Configuration

Config file: `config/nocroptrample.json`

```json
{
  "preventPlayerTrampling": true,
  "preventMobTrampling": true
}
```

---

## 📦 Requirements

- Minecraft 1.21+
- Fabric Loader 0.16+
- Fabric API

---

## 🔗 Links

- [GitHub](https://github.com/Murqin/NoCropTrample)
- [Issues](https://github.com/Murqin/NoCropTrample/issues)
