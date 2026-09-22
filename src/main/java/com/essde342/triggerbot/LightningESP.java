package com.essde342.triggerbot;

import net.minecraft.client.MinecraftClient;

/**
 * Compatibility shim kept for old configs.
 *
 * The target glow/ESP is rendered by {@link TargetESP} now, so the legacy
 * lightning renderer is intentionally disabled. Keeping this class avoids
 * breaking existing integrations/configs while removing the obsolete 1.16
 * immediate-mode rendering code.
 */
public final class LightningESP {
    private LightningESP() {
    }

    public static void register() {
        // TargetESP owns the visual target renderer.
    }

    public static void tick(MinecraftClient client) {
        // No-op: target visuals are handled by TargetESP.
    }
}
