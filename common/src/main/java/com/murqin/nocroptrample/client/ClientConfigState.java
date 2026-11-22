package com.murqin.nocroptrample.client;

import com.murqin.nocroptrample.config.ConfigSnapshot;

/**
 * Holds the read-only server configuration received over {@code nocroptrample:sync_config}.
 * Intentionally free of {@code net.minecraft.client.*} imports so shared networking glue
 * stays safe on dedicated servers.
 */
public final class ClientConfigState {

    private static volatile ConfigSnapshot serverConfig;
    private static volatile Runnable onUpdateListener;

    private ClientConfigState() {
    }

    public static void setServerConfig(ConfigSnapshot snapshot) {
        serverConfig = snapshot;
        Runnable listener = onUpdateListener;
        if (listener != null) {
            listener.run();
        }
    }

    public static ConfigSnapshot getServerConfig() {
        return serverConfig;
    }

    public static void clear() {
        serverConfig = null;
    }

    public static void setOnUpdateListener(Runnable listener) {
        onUpdateListener = listener;
    }

    public static void clearOnUpdateListener() {
        onUpdateListener = null;
    }
}
