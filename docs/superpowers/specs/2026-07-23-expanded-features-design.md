# NoCropTrample Expanded Features Design Spec

## Overview
This specification details the expansion of the **NoCropTrample** Minecraft mod. The update introduces survival-friendly balancing via Feather Falling enchantment scaling and Leather Boots support, soil moisture and farmland reversion protection, feedback mechanisms (particles, sounds, and action bar messages), and mob/pet fine-grained trampling controls.

---

## 1. Requirements & Goals

### 1.1 Feather Falling & Footwear Mechanics
* **Feather Falling Requirement**: Trample protection can optionally require boots with the Feather Falling enchantment.
* **Modes**:
  * `DISABLED`: Trample protection is always active (no enchantment check).
  * `ANY_LEVEL`: Any level of Feather Falling (I-IV) grants 100% trample protection.
  * `SCALED`: Protection scales with enchantment level:
    * Feather Falling I: 25% protection chance
    * Feather Falling II: 50% protection chance
    * Feather Falling III: 75% protection chance
    * Feather Falling IV: 100% protection chance
* **Leather Boots Exemption**: If `protectWithLeatherBoots` is `true`, wearing Leather Boots grants 100% protection even without Feather Falling.

### 1.2 Entity Trample Protection Controls
* **Pet Immunity**: Toggleable protection against pets (cats, dogs, parrots) trampling farmland.
* **Villager Immunity**: Toggleable protection against villagers (e.g. Farmer Villagers) trampling farmland.

### 1.3 Farmland Maintenance (Moisture & Reversion)
* **Prevent Dehydration**: Toggleable setting to prevent hydrated farmland from losing moisture when far from water blocks.
* **Prevent Empty Reversion**: Toggleable setting to prevent unplanted farmland from randomly decaying into regular dirt.

### 1.4 Visual, Auditory & Message Feedback
* **Particle Effects**: Spawns green villager happy particles (`HAPPY_VILLAGER`) or crop particles when farmland trampling is prevented.
* **Sound Effects**: Plays a soft crop/feather sound (`BLOCK_CROP_BREAK` or item pickup sound with custom pitch) when protection triggers.
* **Action Bar Message**: Displays a brief message (e.g. `"Farmland protected!"` or `"Your boots saved the crops!"`) on the action bar with a configurable cooldown (e.g., 3 seconds) to prevent chat/actionbar spam.

---

## 2. Configuration Schema (`nocroptrample.json`)

```json
{
  "preventPlayerTrampling": true,
  "preventMobTrampling": true,
  "preventEmptyTrampling": true,

  "featherFalling": {
    "mode": "ANY_LEVEL",
    "protectWithLeatherBoots": true
  },

  "entityProtection": {
    "preventPetTrampling": true,
    "preventVillagerTrampling": true
  },

  "farmlandMaintenance": {
    "preventDehydration": false,
    "preventEmptyReversion": false
  },

  "effects": {
    "enableParticles": true,
    "enableSound": true,
    "enableActionBarMessage": false,
    "actionBarCooldownSeconds": 3
  }
}
```

---

## 3. Architecture & Mixin Logic

### 3.1 `FarmlandBlockMixin` Updates
1. **`onTurnToDirt` Injection**:
   * Inspect entity slot `EquipmentSlot.FEET`.
   * Check for `LeatherBootsItem` if `protectWithLeatherBoots` is enabled.
   * Query `EnchantmentHelper` for `FeatherFalling` level on feet equipment.
   * Evaluate mode (`DISABLED`, `ANY_LEVEL`, `SCALED`).
   * Perform probability check for `SCALED` mode using `level.random.nextFloat()`.
   * Trigger feedback helper (`FeedbackHelper.triggerProtectionFeedback(...)`) if cancelled.

2. **`isNearWater` / `randomTick` / `tick` Injection**:
   * Intercept farmland moisture reduction logic if `preventDehydration` is true.
   * Intercept random tick decay to dirt if `preventEmptyReversion` is true.

### 3.2 `FeedbackHelper`
* Encapsulates particle spawning, sound playing, and action bar text notification with timestamp-based cooldowns per player.

---

## 4. Git Branching Strategy
All implementation work will take place on the `dev` branch.
