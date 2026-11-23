package com.murqin.nocroptrample.client;

import com.murqin.nocroptrample.config.ConfigSnapshot;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Holds the read-only server configuration received over {@code nocroptrample:sync_config}.
 * Intentionally free of {@code net.minecraft.client.*} imports so shared networking glue
 * stays safe on dedicated servers.
 */
public final class ClientConfigState {

    private static final AtomicLong SESSION_GENERATION = new AtomicLong(0);

    private static volatile ConfigSnapshot serverConfig;
    private static volatile Runnable onUpdateListener;

    private ClientConfigState() {
    }

    public static long currentGeneration() {
        return SESSION_GENERATION.get();
    }

    public static void setServerConfigIfGeneration(ConfigSnapshot snapshot, long expectedGeneration) {
        if (SESSION_GENERATION.get() != expectedGeneration) {
            return;
        }
        setServerConfig(snapshot);
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
        SESSION_GENERATION.incrementAndGet();
        serverConfig = null;
    }

    public static void setOnUpdateListener(Runnable listener) {
        onUpdateListener = listener;
    }

    public static void clearOnUpdateListener(Runnable expected) {
        if (expected != null && onUpdateListener == expected) {
            onUpdateListener = null;
        }
    }

    public static void clearOnUpdateListener() {
        onUpdateListener = null;
    }
}
