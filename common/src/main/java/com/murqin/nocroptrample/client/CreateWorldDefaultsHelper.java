package com.murqin.nocroptrample.client;

import com.murqin.nocroptrample.ModGameRules;
import com.murqin.nocroptrample.config.ModConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Seeds newly created worlds opened from CreateWorldScreen with the latest
 * in-memory {@link ModConfig} defaults, so changes made from the Title Screen
 * take effect immediately without restarting Minecraft.
 */
public final class CreateWorldDefaultsHelper {

    private static final Set<WorldCreationUiState> SEEDED_STATES =
            Collections.newSetFromMap(new WeakHashMap<>());

    private CreateWorldDefaultsHelper() {
    }

    public static void applyIfCreateWorldScreen(Screen screen) {
        if (!(screen instanceof CreateWorldScreen createWorldScreen) || !ModGameRules.isRegistered()) {
            return;
        }
        WorldCreationUiState uiState = createWorldScreen.getUiState();
        if (uiState != null && SEEDED_STATES.add(uiState)) {
            ModGameRules.applySnapshotToRules(uiState.getGameRules(), ModConfig.snapshot());
            uiState.onChanged();
        }
    }
}
