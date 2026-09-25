# Changelog

All notable changes to this project will be documented in this file.

## [1.6+backport.1.20.6-1.21.10] - 2026-09-25

Backport of 1.6-26.3 to Minecraft 1.20.6–1.21.10. The previous release for the 1.21 line was 1.0.0, so this brings every change from 1.2.0 to 1.6; 1.20.6 had no earlier release.

### New Features
- **NeoForge support** — the mod now builds for both Fabric and NeoForge from one repository. NeoForge uses the native `BlockEvent.FarmlandTrampleEvent` instead of a mixin.
- **Empty farmland protection** — a new option prevents trampling of unplanted farmland. Configurable via Mod Menu or `/nocroptrample empty [on|off]`.
- **Stem block detection** — pumpkin and melon stems, including the grown `AttachedStemBlock`, are recognized as planted crops, so farmland under them is no longer treated as empty.
- **Modded crops** — farmland below a block tagged `minecraft:crops` is recognized as planted even when the block doesn't extend the vanilla crop classes.
- **Command permissions** — commands that change settings (`player`, `mob`, `empty`, `reload`) now require permission level 2 (Gamemaster). Version 1.0.0 did not restrict them.

### Bug Fixes
- **Fixed a corrupted config file crashing mod initialization** — malformed JSON in `nocroptrample.json` is now caught and the mod falls back to default settings.
- **Fixed config defaults on upgrade** — options missing from a config saved by an older version now default to enabled instead of disabled.

### Technical
- Built against Minecraft 1.21.1 and tested on Fabric and NeoForge dedicated servers running 1.20.6, 1.21, 1.21.1, 1.21.4 and 1.21.10, and played in game on the Fabric and NeoForge clients for 1.20.6, 1.21.1, 1.21.4 and 1.21.6; the versions in between share the same code but were not run individually. Minecraft 1.20.5 is not covered.
- Requires Fabric API 0.97.0+ or NeoForge 20.6–21.10, and Java 21.
- Restructured into `common`/`fabric`/`neoforge` Gradle subprojects; shared logic lives in `common`, each platform keeps only its own entrypoint and event/mixin glue.
- Wired up the mod icon in `fabric.mod.json` and `neoforge.mods.toml`.

## [1.6-26.3] - 2026-09-16

### New Features
- **NeoForge support** — the mod now builds for both Fabric and NeoForge from one repository. NeoForge uses the native `BlockEvent.FarmlandTrampleEvent` instead of a mixin, so it isn't affected by Mojang renaming `FarmlandBlock`'s internal methods.

### Bug Fixes
- **Fixed the FarmlandBlock mixin failing to apply on Minecraft 26.3** — Mojang renamed the static `turnToDirt` method to an instance method `turnToBaseBlock`; the mixin target is updated accordingly. This affected only the Fabric build.
- **Fixed a corrupted config file crashing mod initialization** — malformed JSON in `nocroptrample.json` threw an uncaught `JsonSyntaxException`; it's now caught and the mod falls back to default settings instead.
- **Fixed modded crops not recognized as planted** — farmland below a block tagged `minecraft:crops` (but not extending the vanilla `CropBlock`/`StemBlock`/`AttachedStemBlock` classes) was misclassified as empty and governed by the wrong config option.

### Technical
- Ported to Minecraft 26.3, Fabric Loader 0.19.5, Fabric API 0.160.5+26.3, Mod Menu 21.0.0-beta.1.
- Restructured into `common`/`fabric`/`neoforge` Gradle subprojects — shared logic (config, commands, the trampling decision, the config screen) lives in `common`; each platform keeps only its own entrypoint and event/mixin glue.
- Wired up the mod icon in `fabric.mod.json` and `neoforge.mods.toml` — the file existed in the jar but was never referenced by either.

## [1.5-26.2] - 2026-06-22

### Bug Fixes
- **Fixed `/nocroptrample status` displaying wrong value for "Empty" row** — The status output was showing the player-trampling flag for the empty-farmland row; it now correctly shows `preventEmptyTrampling`.
- **Fixed `/nocroptrample empty` (no argument) displaying wrong label and value** — The command was rendering "Player trampling prevention" with the player flag instead of "Empty farmland trampling prevention" with the empty flag.
- **Fixed `preventEmptyTrampling` having no effect when player and mob prevention are both disabled** — The flag now independently cancels trampling on empty farmland regardless of the player/mob settings.
- **Fixed `AttachedStemBlock` (grown pumpkin/melon stem) not recognized as a crop** — Farmland beneath a fruit-bearing stem was incorrectly treated as empty; `AttachedStemBlock` is now included in the crop check.
- **Fixed OP level check ignoring `REQUIRED_OP_LEVEL`** — Permission gating now uses `LevelBasedPermissionSet` to properly enforce level 2 (Gamemaster); previously any OP regardless of level could use admin commands.
- **Fixed potential config default mismatch on upgrade** — GSON now uses an `InstanceCreator` for `ConfigData`, ensuring field initializers (all default to `true`) run even when loading a config file that predates a field.

## [1.4-26.2] - 2026-06-22

### Bug Fixes
- **Fixed `preventEmptyTrampling` not persisting across restarts** — `save()` was missing the assignment of `preventEmptyTrampling` to `ConfigData`; the field defaulted to `true` on every save, silently discarding the user's setting.

## [1.3-26.2] - 2026-06-21

### Technical
- **Ported to Minecraft 26.2** — Bumped `minecraft_version` to `26.2`, `loader_version` to `0.19.3`, `loom_version` to `1.17-SNAPSHOT`, `fabric_api_version` to `0.152.2+26.2`.
- **Gradle wrapper upgraded to 9.5.0** — Required by Fabric Loom 1.17 which mandates `org.gradle.plugin.api-version` 9.5.0.
- **`Minecraft#setScreen` renamed to `Minecraft#setScreenAndShow`** — Updated `ModMenuIntegration#onClose` accordingly.
- **Mod Menu bumped to 20.0.0-beta.1** — `18.0.0-alpha.8` called `I18n#exists(String)` which was removed in 26.2, causing a `NoSuchMethodError` crash on the title screen.

## [1.2.1] - 2026-04-06

### Technical
- **Minecraft 26.1 Compatibility Fix** — Updated the Mod Menu config screen API to support the new `GuiGraphicsExtractor` and `extractRenderState` systems. This fixes compilation errors where `GuiGraphics` and `render` were no longer available.

## [1.2.0] - 2026-03-17

### New Features
- **Empty Farmland Protection** — Added a new option to prevent trampling on unplanted farmland. When enabled, walking over empty farmland (with no crops or stems above) won't turn it back to dirt. Configurable via Mod Menu or the `/nocroptrample empty [on|off]` command.
- **Stem Block Detection** — Pumpkin and melon stems are now recognized as planted crops, so farmland with stems is no longer considered "empty."

### Bug Fixes
- **Fixed empty trampling toggle button** — The Mod Menu button for "Empty Trampling" was incorrectly toggling the player trampling setting instead of the empty trampling setting.
- **Fixed Mod Menu button layout** — Buttons are now properly spaced and arranged in the config screen.

### Technical
- Added `/nocroptrample empty [on|off]` subcommand
- Added `StateName` enum for cleaner command state handling
- Config file now includes `preventEmptyTrampling` option (defaults to enabled)

### Credits
- Thanks to **VacantTarnished** for contributing the empty farmland trampling prevention feature!

## [1.0.0] - 2025-12-19

### Added
- Initial release for Fabric 1.21+
- Player trampling prevention (configurable)
- Mob trampling prevention (configurable)
- In-game commands: `/nocroptrample`
  - `status` - View current settings
  - `player <on|off>` - Toggle player trampling
  - `mob <on|off>` - Toggle mob trampling
  - `reload` - Reload config from file
- ModMenu integration with GUI config screen
- JSON configuration file (`config/nocroptrample.json`)