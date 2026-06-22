# Changelog

All notable changes to this project will be documented in this file.

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