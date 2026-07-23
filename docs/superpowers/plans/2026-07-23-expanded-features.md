# NoCropTrample Expanded Features Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Expand NoCropTrample mod with Feather Falling enchantment scaling, Leather Boots protection, Pet/Villager entity controls, Farmland moisture/reversion protection, and particle/sound/actionbar feedback.

**Architecture:** Extend `ModConfig` with nested/modular options, create a `FeedbackHelper` utility for client/server feedback effects with cooldowns, and enhance `FarmlandBlockMixin` to handle entity-specific logic, enchantment checks, and dehydration/decay prevention.

**Tech Stack:** Java 17 / 21, Fabric API, Minecraft 1.20+ (Mappings: Yarn/Mojmap), Mixins, Gson.

## Global Constraints

* Target Branch: `dev`
* Fabric Loader / Yarn mappings standard for Minecraft 1.20+
* Config format: JSON via Gson (`nocroptrample.json`)

---

### Task 1: Extend `ModConfig` Data Schema & Accessors

**Files:**
- Modify: `src/main/java/com/murqin/nocroptrample/config/ModConfig.java`

**Interfaces:**
- Consumes: JSON config stored in Fabric config directory (`nocroptrample.json`).
- Produces: Getters/setters for `featherFallingMode`, `protectWithLeatherBoots`, `preventPetTrampling`, `preventVillagerTrampling`, `preventDehydration`, `preventEmptyReversion`, `enableParticles`, `enableSound`, `enableActionBarMessage`, `actionBarCooldownSeconds`.

- [ ] **Step 1: Define `FeatherFallingMode` Enum & Config Fields**

```java
public enum FeatherFallingMode {
    DISABLED,
    ANY_LEVEL,
    SCALED
}
```

- [ ] **Step 2: Add Fields, Getters, Setters, and Defaults in `ModConfig`**

Add fields with default values:
- `featherFallingMode`: `FeatherFallingMode.ANY_LEVEL`
- `protectWithLeatherBoots`: `true`
- `preventPetTrampling`: `true`
- `preventVillagerTrampling`: `true`
- `preventDehydration`: `false`
- `preventEmptyReversion`: `false`
- `enableParticles`: `true`
- `enableSound`: `true`
- `enableActionBarMessage`: `false`
- `actionBarCooldownSeconds`: `3`

- [ ] **Step 3: Update `ConfigData` inner class for Gson serialization**

Ensure serialization and deserialization seamlessly preserve defaults if fields are missing in legacy JSON files.

- [ ] **Step 4: Verify build with `./gradlew build`**

Run: `./gradlew build`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Commit changes**

```bash
git add src/main/java/com/murqin/nocroptrample/config/ModConfig.java
git commit -m "feat(config): add expanded config options for feather falling and protection toggles"
```

---

### Task 2: Create `FeedbackHelper` Utility

**Files:**
- Create: `src/main/java/com/murqin/nocroptrample/util/FeedbackHelper.java`

**Interfaces:**
- Consumes: `Level`, `BlockPos`, `Entity`/`Player`, `ModConfig` settings.
- Produces: `FeedbackHelper.triggerProtectionFeedback(Level level, BlockPos pos, Entity entity)`

- [ ] **Step 1: Write `FeedbackHelper` Implementation**

```java
package com.murqin.nocroptrample.util;

import com.murqin.nocroptrample.config.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FeedbackHelper {
    private static final Map<UUID, Long> LAST_MESSAGE_TIME = new HashMap<>();

    public static void triggerProtectionFeedback(Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide()) {
            return;
        }

        // Particles
        if (ModConfig.isEnableParticles()) {
            level.addParticle(
                ParticleTypes.HAPPY_VILLAGER,
                pos.getX() + 0.5,
                pos.getY() + 1.0,
                pos.getZ() + 0.5,
                0.0, 0.05, 0.0
            );
        }

        // Sound
        if (ModConfig.isEnableSound()) {
            level.playSound(
                null,
                pos,
                SoundEvents.CROP_BREAK,
                SoundSource.BLOCKS,
                0.5f,
                1.5f
            );
        }

        // Action Bar Message
        if (ModConfig.isEnableActionBarMessage() && entity instanceof Player player) {
            long now = System.currentTimeMillis();
            long cooldownMs = ModConfig.getActionBarCooldownSeconds() * 1000L;
            UUID uuid = player.getUUID();

            if (now - LAST_MESSAGE_TIME.getOrDefault(uuid, 0L) >= cooldownMs) {
                LAST_MESSAGE_TIME.put(uuid, now);
                player.displayClientMessage(
                    Component.literal("Farmland protected!"),
                    true
                );
            }
        }
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `./gradlew build`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit changes**

```bash
git add src/main/java/com/murqin/nocroptrample/util/FeedbackHelper.java
git commit -m "feat(util): create FeedbackHelper for particle, sound, and actionbar feedback"
```

---

### Task 3: Enhance `FarmlandBlockMixin` for Entity, Enchantment & Feedback Logic

**Files:**
- Modify: `src/main/java/com/murqin/nocroptrample/mixin/FarmlandBlockMixin.java`

**Interfaces:**
- Consumes: `ModConfig`, `FeedbackHelper`, `EnchantmentHelper`, `EquipmentSlot`.
- Produces: Enhanced `onTurnToDirt` check incorporating Feather Falling, Leather Boots, Pets, Villagers, and Feedback.

- [ ] **Step 1: Add Entity Type Checks (Pet & Villager Immunity)**

Check if entity is `TamableAnimal` / `OwnableEntity` (Pet) or `Villager`. Match against `ModConfig.isPreventPetTrampling()` and `ModConfig.isPreventVillagerTrampling()`.

- [ ] **Step 2: Implement Feather Falling & Leather Boots Checks for Players**

In `onTurnToDirt`:
If entity is a `Player`:
1. Check if wearing Leather Boots (`player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof LeatherBootsItem`) and `ModConfig.isProtectWithLeatherBoots()` -> If true, cancel trampling & trigger feedback.
2. Check `ModConfig.getFeatherFallingMode()`:
   - `DISABLED`: Trample prevented based on `isPreventPlayerTrampling()`.
   - `ANY_LEVEL`: Prevent if Feather Falling level > 0.
   - `SCALED`: Feather Falling level 1 (%25), 2 (%50), 3 (%75), 4 (%100). Calculate probability using `level.random.nextFloat()`.
3. If trampling is prevented, invoke `FeedbackHelper.triggerProtectionFeedback(level, pos, entity)`.

- [ ] **Step 3: Verify build**

Run: `./gradlew build`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit changes**

```bash
git add src/main/java/com/murqin/nocroptrample/mixin/FarmlandBlockMixin.java
git commit -m "feat(mixin): implement Feather Falling, Leather Boots, and Pet/Villager protection in FarmlandBlockMixin"
```

---

### Task 4: Implement Dehydration & Empty Reversion Prevention in `FarmlandBlockMixin`

**Files:**
- Modify: `src/main/java/com/murqin/nocroptrample/mixin/FarmlandBlockMixin.java`

**Interfaces:**
- Consumes: `ModConfig.isPreventDehydration()`, `ModConfig.isPreventEmptyReversion()`.
- Produces: Intercepted moisture depletion & random tick reversion to dirt.

- [ ] **Step 1: Add Mixin Injection for Moisture / Tick**

Inject into `isNearWater` or `randomTick` / `tick` methods of `FarmlandBlock` to prevent `MOISTURE` property reduction when `preventDehydration` is true, and prevent turning to dirt when empty if `preventEmptyReversion` is true.

- [ ] **Step 2: Verify build**

Run: `./gradlew build`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit changes**

```bash
git add src/main/java/com/murqin/nocroptrample/mixin/FarmlandBlockMixin.java
git commit -m "feat(mixin): add farmland moisture dehydration and empty reversion prevention"
```

---

### Task 5: Update Mod Menu & In-Game Commands

**Files:**
- Modify: `src/main/java/com/murqin/nocroptrample/ModMenuIntegration.java`
- Modify: `src/main/java/com/murqin/nocroptrample/command/NoCropTrampleCommand.java`

**Interfaces:**
- Consumes: Extended `ModConfig` options.
- Produces: Updated GUI config screen and `/nocroptrample` commands.

- [ ] **Step 1: Update `NoCropTrampleCommand` to show and toggle new options**
- [ ] **Step 2: Update `ModMenuIntegration` screen entries**
- [ ] **Step 3: Run `./gradlew build` to verify end-to-end compilation**
- [ ] **Step 4: Commit changes**

```bash
git add src/main/java/com/murqin/nocroptrample/command/NoCropTrampleCommand.java src/main/java/com/murqin/nocroptrample/ModMenuIntegration.java
git commit -m "feat(ui): update ModMenu screen and commands for new configuration options"
```
