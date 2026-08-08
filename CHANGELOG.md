# Changelog

All notable changes to this project will be documented in this file.

## [1.6-26.2] - 2026-08-08

### Features
- **Feather Falling & Leather Boots protection** — Players can now be exempted from trampling by wearing Leather Boots (`protectWithLeatherBoots`, on by default) or based on a configurable Feather Falling mode (`featherFallingMode`): `ALWAYS` (ignore Feather Falling, always protect — the default), `REQUIRE_FEATHER_FALLING` (any level required), or `SCALED_BY_LEVEL` (protection chance scales with enchantment level, 25% per level).
- **Pet & Villager protection** — `preventPetTrampling` and `preventVillagerTrampling` independently protect farmland from tamed animals and villagers; when disabled, they fall back to the general mob rule instead of being exempt outright.
- **Farmland moisture control** — `preventDehydration` keeps farmland from ever drying out (also speeds up crop growth, since dry farmland halves growth chance — documented in the GUI tooltip and README), and `preventEmptyReversion` stops unplanted farmland from reverting to dirt on its own.
- **Particle, sound, and action bar feedback** — Protected trampling attempts can now trigger particles, a sound, and an action bar message, each independently toggleable. Particle/sound triggers are throttled per farmland block (not per player) to avoid spam from mobs/pets standing on protected land; the action bar message has its own per-player cooldown. Both share the same configurable `actionBarCooldownSeconds` (0–60, clamped).
- **Preset profiles** — `/nocroptrample preset [vanilla_plus|casual|hardcore|custom]` and a GUI button cycle between curated presets; editing any individual setting automatically flips the active preset to `custom`.
- **Expanded commands** — Every setting (`featherfalling`, `leatherboots`, `pet`, `villager`, `dehydration`, `emptyreversion`, `particles`, `sound`, `actionbar`, `cooldown`, plus the existing `empty`/`player`/`mob`) is now readable and settable from the command line, not just Mod Menu — needed for dedicated servers, where Mod Menu isn't available.
- **Redesigned Mod Menu screen** — Rebuilt on a scrollable, category-grouped layout (`HeaderAndFooterLayout` + `ScrollableLayout` + `GridLayout`) so it stays usable at any window size or GUI scale, and shows a warning when connected to a remote server (client-side settings don't affect a server you don't host).
- **Localization** — All user-facing GUI/command text moved to `lang/en_us.json`, with a Turkish translation (`lang/tr_tr.json`) included.

### Bug Fixes
- **Fixed config migration silently discarding pre-1.6 settings** — `load()` previously branched on `activePreset` before reading the individual fields, so a config file without that field (or edited by hand) had `preventMobTrampling`/`preventEmptyTrampling`/etc. silently reset to preset defaults. Fields are now always read first; the active preset is derived afterward via `checkPresetMatch()`.
- **Fixed the mod not preventing trampling out of the box** — The Feather Falling default previously required players to be wearing an enchanted boot before protection kicked in. The default mode is now `ALWAYS`.
- **Fixed pets/villagers bypassing the general mob rule** — Disabling `preventPetTrampling`/`preventVillagerTrampling` used to exempt them entirely instead of falling back to `preventMobTrampling`.
- **Fixed unbounded growth of the action-bar cooldown map** — Player entries are now removed on disconnect (`ServerPlayConnectionEvents.DISCONNECT`).

### Technical
- Renamed `FeatherFallingMode` enum values (`DISABLED`/`ANY_LEVEL`/`SCALED` → `ALWAYS`/`REQUIRE_FEATHER_FALLING`/`SCALED_BY_LEVEL`) to describe what they do instead of what they don't.
- Preset definitions (`applyPreset`/`matchesPreset`) now share a single `PresetValues` record so updating a preset can't update one method and miss the other.
- CI now builds the `dev` branch in addition to `main`/`master`.
- Removed the now-unused `StateName` enum in favor of a generic `buildBooleanCommand` command builder shared by all boolean toggles.

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