package com.murqin.nocroptrample.neoforge;

import com.murqin.nocroptrample.client.NoCropTrampleConfigScreen;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.ConfigScreenHandler;

/**
 * Client-only NeoForge wiring, wiring the shared config screen into the Mods
 * menu. NeoForge 20.4 has no {@code @Mod(dist = ...)}, so the main entrypoint
 * calls this only on the client; keeping it in its own class stops a
 * dedicated server from ever loading the client-only screen classes.
 */
final class NoCropTrampleNeoForgeClient {

    private NoCropTrampleNeoForgeClient() {
    }

    static void init(ModContainer container) {
        container.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (minecraft, parent) -> new NoCropTrampleConfigScreen(parent)));
    }
}
