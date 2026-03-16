# Changelog

All notable changes to this project will be documented in this file.

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