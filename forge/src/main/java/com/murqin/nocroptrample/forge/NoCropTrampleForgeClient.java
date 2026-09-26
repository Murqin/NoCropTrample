package com.murqin.nocroptrample.forge;

import com.murqin.nocroptrample.client.NoCropTrampleConfigScreen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;

/**
 * Client-only Forge wiring, putting the shared config screen into the Mods
 * menu. Kept in its own class and called only on the client, so a dedicated
 * server never loads the client-only screen classes.
 */
final class NoCropTrampleForgeClient {

    private NoCropTrampleForgeClient() {
    }

    static void init() {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (minecraft, parent) -> new NoCropTrampleConfigScreen(parent)));
    }
}
