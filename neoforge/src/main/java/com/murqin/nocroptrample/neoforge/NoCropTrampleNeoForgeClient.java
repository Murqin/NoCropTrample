package com.murqin.nocroptrample.neoforge;

import com.murqin.nocroptrample.NoCropTrample;
import com.murqin.nocroptrample.client.NoCropTrampleConfigScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Client-only NeoForge entrypoint, wiring the shared config screen into the
 * Mods menu. Never loaded on a dedicated server.
 */
@Mod(value = NoCropTrample.MOD_ID, dist = Dist.CLIENT)
public class NoCropTrampleNeoForgeClient {

    public NoCropTrampleNeoForgeClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (screenContainer, parent) -> new NoCropTrampleConfigScreen(parent));
    }
}
