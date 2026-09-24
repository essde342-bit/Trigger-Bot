package com.essde342.triggerbot;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GraphicsMode;

public final class TriggerBotOptimizer {
    private static final long BALANCED_CHUNK_BUDGET = 6_000_000L;
    private static final long PERFORMANCE_CHUNK_BUDGET = 4_500_000L;
    private static final long EXTREME_CHUNK_BUDGET = 3_500_000L;
    private static boolean active = false;
    private static boolean snapshotTaken = false;
    private static int currentLevel = 1;
    private static int timer = 0;
    private static int playerScanTimer = 0;

    private static int savedViewDistance;
    private static double savedEntityDistance;
    private static CloudRenderMode savedClouds;
    private static GraphicsMode savedGraphics;
    private static boolean savedAo;
    private static Object savedParticles;
    private static boolean savedEntityShadows;
    private static int savedBiomeBlend;
    private static int savedMipmapLevels;

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

        if (++timer < 120) {
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

        if (++playerScanTimer >= 120) {
            playerScanTimer = 0;
            if (client.player != null && client.world != null) {
                int nearbyPlayers = 0;
                for (net.minecraft.entity.player.PlayerEntity other : client.world.getPlayers()) {
                    if (other != client.player
                            && other.squaredDistanceTo(client.player) <= 32.0D * 32.0D) {
                        nearbyPlayers++;
                    }
                }
                if (nearbyPlayers >= 8) {
                    client.options.getEntityDistanceScaling().setValue(
                            Math.min(client.options.getEntityDistanceScaling().getValue(), 0.35D)
                    );
                }
            }
        }
    }

    public static boolean isRendererOptimizationActive() {
        // Renderer mixins stay disabled on the 1.21.4 mobile build.
        // External render optimizers such as Sodium/ImmediatelyFast/MoreCulling
        // must remain in control of the renderer.
        return false;
    }

    public static long getChunkUpdateBudgetNanos() {
        switch (clamp(TriggerBotClient.CONFIG.optimizationLevel, 0, 2)) {
            case 2:
                return EXTREME_CHUNK_BUDGET;
            case 1:
                return PERFORMANCE_CHUNK_BUDGET;
            default:
                return BALANCED_CHUNK_BUDGET;
        }
    }

    public static double getEntityRenderDistance() {
        switch (clamp(TriggerBotClient.CONFIG.optimizationLevel, 0, 2)) {
            case 2:
                return 28.0D;
            case 1:
                return 36.0D;
            default:
                return 48.0D;
        }
    }

    public static double getBlockEntityRenderDistance() {
        switch (clamp(TriggerBotClient.CONFIG.optimizationLevel, 0, 2)) {
            case 2:
                return 20.0D;
            case 1:
                return 28.0D;
            default:
                return 40.0D;
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
        savedViewDistance = options.getViewDistance().getValue();
        savedEntityDistance = options.getEntityDistanceScaling().getValue();
        savedClouds = options.getCloudRenderMode().getValue();
        savedGraphics = options.getGraphicsMode().getValue();
        savedAo = options.getAo().getValue();
        savedParticles = options.getParticles().getValue();
        savedEntityShadows = options.getEntityShadows().getValue();
        savedBiomeBlend = options.getBiomeBlendRadius().getValue();
        savedMipmapLevels = options.getMipmapLevels().getValue();
        snapshotTaken = true;
    }

    private static boolean hasExternalRendererOptimizer() {
        FabricLoader loader = FabricLoader.getInstance();
        return loader.isModLoaded("sodium")
                || loader.isModLoaded("immediatelyfast")
                || loader.isModLoaded("moreculling")
                || loader.isModLoaded("entityculling");
    }

    private static void apply(MinecraftClient client) {
        GameOptions options = client.options;

        int viewDistance = currentLevel == 2 ? 4 : (currentLevel == 1 ? 6 : 8);
        double entityDistance = currentLevel == 2 ? 0.35D : (currentLevel == 1 ? 0.55D : 0.75D);

        SimpleOption<Integer> view = options.getViewDistance();
        view.setValue(Math.min(view.getValue(), viewDistance));

        SimpleOption<Double> entities = options.getEntityDistanceScaling();
        entities.setValue(Math.min(entities.getValue(), entityDistance));

        // Let dedicated render optimizers manage renderer-specific options.
        if (hasExternalRendererOptimizer()) {
            return;
        }

        options.getCloudRenderMode().setValue(CloudRenderMode.OFF);
        options.getGraphicsMode().setValue(GraphicsMode.FAST);
        options.getAo().setValue(currentLevel == 0);
        setMinimalParticles(options);
        options.getEntityShadows().setValue(false);
        options.getBiomeBlendRadius().setValue(0);
        options.getMipmapLevels().setValue(0);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void setMinimalParticles(GameOptions options) {
        Object current = options.getParticles().getValue();
        if (current instanceof Enum) {
            Enum<?> value = (Enum<?>) current;
            ((SimpleOption) options.getParticles()).setValue(
                    Enum.valueOf((Class) value.getDeclaringClass(), "MINIMAL")
            );
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void setParticlesValue(GameOptions options, Object value) {
        ((SimpleOption) options.getParticles()).setValue(value);
    }

    private static void restore(MinecraftClient client) {
        if (!snapshotTaken) {
            active = false;
            return;
        }

        GameOptions options = client.options;
        options.getViewDistance().setValue(savedViewDistance);
        options.getEntityDistanceScaling().setValue(savedEntityDistance);
        options.getCloudRenderMode().setValue(savedClouds);
        options.getGraphicsMode().setValue(savedGraphics);
        options.getAo().setValue(savedAo);
        setParticlesValue(options, savedParticles);
        options.getEntityShadows().setValue(savedEntityShadows);
        options.getBiomeBlendRadius().setValue(savedBiomeBlend);
        options.getMipmapLevels().setValue(savedMipmapLevels);
        options.write();

        active = false;
        snapshotTaken = false;
        timer = 0;
        playerScanTimer = 0;
    }

    private static int readFps(MinecraftClient client) {
        return client.getCurrentFps();
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
