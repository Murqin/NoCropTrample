# No Crop Trample

A lightweight Fabric mod that prevents farmland from being trampled and reverted to dirt when players or mobs land on it.

<iframe width="560" height="315" src="https://www.youtube.com/embed/ypxASh8R1tI" frameborder="0" allowfullscreen></iframe>

## Features

- Prevents farmland blocks from reverting to dirt when entities land on them.
- Player and mob trampling can be toggled independently.
- Optional protection for empty (unplanted) farmland.
- Implemented with Fabric Mixins — no tick handlers, no entity scanning.
- Works server-side only; client install is optional (adds a ModMenu config screen).

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

- Minecraft 26.1+
- Fabric Loader 0.16+
- Fabric API
- Java 25+

## Links

- [GitHub](https://github.com/deimos-sh/NoCropTrample)
- [Issues](https://github.com/deimos-sh/NoCropTrample/issues)
