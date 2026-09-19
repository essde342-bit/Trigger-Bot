package com.essde342.triggerbot;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.AoMode;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.ParticlesMode;

public final class TriggerBotOptimizer {
    private static boolean active = false;
    private static boolean snapshotTaken = false;
    private static int currentLevel = 1;
    private static int timer = 0;

    private static int savedViewDistance;
    private static float savedEntityDistance;
    private static CloudRenderMode savedClouds;
    private static GraphicsMode savedGraphics;
    private static AoMode savedAo;
    private static ParticlesMode savedParticles;
    private static boolean savedEntityShadows;
    private static int savedBiomeBlend;

    private TriggerBotOptimizer() {
    }

    public static void tick(MinecraftClient client) {
        if (client == null || client.options == null) {
            return;
        }

        if (!TriggerBotClient.CONFIG.optimization) {
            if (active) {
                restore(client);
            }
            return;
        }

        if (!active) {
            enable(client);
        }

        if (++timer < 40) {
            return;
        }
        timer = 0;

        if (TriggerBotClient.CONFIG.adaptiveOptimization) {
            int fps = readFps(client);
            int target = TriggerBotClient.CONFIG.targetFps;

            if (fps > 0 && fps < target - 5 && currentLevel < 2) {
                currentLevel++;
                apply(client);
            } else if (fps >= target + 12 && currentLevel > TriggerBotClient.CONFIG.optimizationLevel) {
                currentLevel--;
                apply(client);
            }
        } else if (currentLevel != TriggerBotClient.CONFIG.optimizationLevel) {
            currentLevel = clamp(TriggerBotClient.CONFIG.optimizationLevel, 0, 2);
            apply(client);
        }

        // Extra protection when many players are close enough to be rendered.
        if (client.player != null && client.world != null) {
            int nearbyPlayers = 0;

            for (net.minecraft.entity.player.PlayerEntity other : client.world.getPlayers()) {
                if (other != client.player
                        && other.squaredDistanceTo(client.player) <= 32.0D * 32.0D) {
                    nearbyPlayers++;
                }
            }

            if (nearbyPlayers >= 8) {
                client.options.entityDistanceScaling =
                        Math.min(client.options.entityDistanceScaling, 0.60F);
            }
        }
    }

    public static void setOptimization(MinecraftClient client, boolean enabled) {
        TriggerBotClient.CONFIG.optimization = enabled;

        if (enabled) {
            enable(client);
        } else {
            restore(client);
        }
    }

    private static void enable(MinecraftClient client) {
        if (!snapshotTaken) {
            snapshot(client.options);
        }

        currentLevel = clamp(TriggerBotClient.CONFIG.optimizationLevel, 0, 2);
        active = true;
        apply(client);
    }

    private static void snapshot(GameOptions options) {
        savedViewDistance = options.viewDistance;
        savedEntityDistance = options.entityDistanceScaling;
        savedClouds = options.cloudRenderMode;
        savedGraphics = options.graphicsMode;
        savedAo = options.ao;
        savedParticles = options.particles;
        savedEntityShadows = options.entityShadows;
        savedBiomeBlend = options.biomeBlendRadius;
        snapshotTaken = true;
    }

    private static void apply(MinecraftClient client) {
        GameOptions options = client.options;

        int viewDistance = currentLevel == 2 ? 6 : (currentLevel == 1 ? 8 : 10);
        float entityDistance = currentLevel == 2 ? 0.50F : (currentLevel == 1 ? 0.70F : 0.85F);

        options.viewDistance = Math.min(options.viewDistance, viewDistance);
        options.entityDistanceScaling = Math.min(options.entityDistanceScaling, entityDistance);
        options.cloudRenderMode = CloudRenderMode.OFF;
        options.graphicsMode = GraphicsMode.FAST;
        options.ao = currentLevel == 0 ? AoMode.MIN : AoMode.OFF;
        options.particles = currentLevel == 2 ? ParticlesMode.MINIMAL : ParticlesMode.DECREASED;
        options.entityShadows = false;
        options.biomeBlendRadius = 0;
        options.write();
    }

    private static void restore(MinecraftClient client) {
        if (!snapshotTaken) {
            active = false;
            return;
        }

        GameOptions options = client.options;
        options.viewDistance = savedViewDistance;
        options.entityDistanceScaling = savedEntityDistance;
        options.cloudRenderMode = savedClouds;
        options.graphicsMode = savedGraphics;
        options.ao = savedAo;
        options.particles = savedParticles;
        options.entityShadows = savedEntityShadows;
        options.biomeBlendRadius = savedBiomeBlend;
        options.write();

        active = false;
        snapshotTaken = false;
        timer = 0;
    }

    private static int readFps(MinecraftClient client) {
        if (client.fpsDebugString == null) {
            return 0;
        }

        int value = 0;
        for (int i = 0; i < client.fpsDebugString.length(); i++) {
            char ch = client.fpsDebugString.charAt(i);
            if (Character.isDigit(ch)) {
                value = value * 10 + (ch - \'0\');
            } else if (value > 0) {
                break;
            }
        }
        return value;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
