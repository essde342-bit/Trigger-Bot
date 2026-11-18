package com.essde342.triggerbot;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GraphicsMode;

import java.util.Set;

public final class TriggerBotOptimizer {
    private static final long BALANCED_CHUNK_BUDGET = 6_000_000L;
    private static final long PERFORMANCE_CHUNK_BUDGET = 4_500_000L;
    private static final long EXTREME_CHUNK_BUDGET = 3_500_000L;

    /*
     * These mods already manage the client renderer, culling, frame pacing or
     * graphics options. Astra stays passive when any of them is installed.
     * This avoids two optimizers fighting over the same rendering state.
     */
    private static final Set<String> EXTERNAL_OPTIMIZATION_MODS = Set.of(
            "sodium",
            "sodium-extra",
            "iris",
            "immediatelyfast",
            "entityculling",
            "moreculling",
            "nvidium",
            "indium",
            "vulkanmod",
            "enhancedblockentities",
            "scalablelux",
            "modernfix"
    );

    private static boolean active = false;
    private static boolean snapshotTaken = false;
    private static int currentLevel = 1;
    private static int timer = 0;
    private static int playerScanTimer = 0;
    private static boolean crowdedServer = false;
    private static boolean adaptiveMode = false;

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

        if (client.player == null || client.world == null) {
            if (active) {
                restore(client);
            }
            return;
        }

        // Do not compete with another optimization mod that owns these settings.
        if (isExternalOptimizationPresent() || !TriggerBotClient.CONFIG.optimization) {
            if (active) {
                restore(client);
            }
            return;
        }

        if (!active) {
            enable(client);
        }

        boolean requestedAdaptive = TriggerBotClient.CONFIG.adaptiveOptimization;
        if (requestedAdaptive != adaptiveMode) {
            adaptiveMode = requestedAdaptive;
            currentLevel = adaptiveMode ? 0 : clamp(TriggerBotClient.CONFIG.optimizationLevel, 0, 2);
            apply(client);
        }

        if (++timer >= 120) {
            timer = 0;
            if (adaptiveMode) {
                int fps = readFps(client);
                int target = TriggerBotClient.CONFIG.targetFps;
                if (fps > 0 && fps < target - 5 && currentLevel < 2) {
                    currentLevel++;
                    apply(client);
                } else if (fps >= target + 12 && currentLevel > 0) {
                    currentLevel--;
                    apply(client);
                }
            } else {
                int requestedLevel = clamp(TriggerBotClient.CONFIG.optimizationLevel, 0, 2);
                if (currentLevel != requestedLevel) {
                    currentLevel = requestedLevel;
                    apply(client);
                }
            }
        }

        if (++playerScanTimer >= 120) {
            playerScanTimer = 0;
            int nearbyPlayers = 0;
            for (net.minecraft.entity.player.PlayerEntity other : client.world.getPlayers()) {
                if (other != client.player
                        && other.squaredDistanceTo(client.player) <= 32.0D * 32.0D) {
                    nearbyPlayers++;
                }
            }
            boolean nextCrowdedState = nearbyPlayers >= 8;
            if (nextCrowdedState != crowdedServer) {
                crowdedServer = nextCrowdedState;
                apply(client);
            }
        }
    }

    public static boolean isExternalOptimizationPresent() {
        FabricLoader loader = FabricLoader.getInstance();
        for (String modId : EXTERNAL_OPTIMIZATION_MODS) {
            if (loader.isModLoaded(modId)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isRendererOptimizationActive() {
        /*
         * Renderer mixins remain disabled on the 1.21.4 build. Sodium/GL4ES
         * and other renderer mods keep ownership of native render state.
         */
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

        if (enabled && !isExternalOptimizationPresent()) {
            enable(client);
        } else {
            restore(client);
        }
    }

    private static void enable(MinecraftClient client) {
        if (isExternalOptimizationPresent()) {
            active = false;
            return;
        }

        if (!snapshotTaken) {
            snapshot(client.options);
        }

        adaptiveMode = TriggerBotClient.CONFIG.adaptiveOptimization;
        currentLevel = adaptiveMode ? 0 : clamp(TriggerBotClient.CONFIG.optimizationLevel, 0, 2);
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

    private static void apply(MinecraftClient client) {
        if (isExternalOptimizationPresent()) {
            return;
        }

        GameOptions options = client.options;

        int viewDistance = currentLevel == 2 ? 4 : (currentLevel == 1 ? 6 : 8);
        double entityDistance = currentLevel == 2 ? 0.35D : (currentLevel == 1 ? 0.55D : 0.75D);

        SimpleOption<Integer> view = options.getViewDistance();
        view.setValue(Math.min(savedViewDistance, viewDistance));

        SimpleOption<Double> entities = options.getEntityDistanceScaling();
        double effectiveEntityDistance = crowdedServer ? Math.min(entityDistance, 0.35D) : entityDistance;
        entities.setValue(Math.min(savedEntityDistance, effectiveEntityDistance));

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
            timer = 0;
            playerScanTimer = 0;
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
        crowdedServer = false;
        adaptiveMode = false;
        currentLevel = clamp(TriggerBotClient.CONFIG.optimizationLevel, 0, 2);
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
