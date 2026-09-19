package com.essde342.triggerbot;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TridentItem;
import net.minecraft.util.Hand;
import net.minecraft.util.UseAction;
import net.minecraft.util.hit.EntityHitResult;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class TriggerBotClient implements ClientModInitializer {
    public static final TriggerBotConfig CONFIG = new TriggerBotConfig();

    private static KeyBinding openMenuKey;
    private static KeyBinding fullbrightKey;
    private static KeyBinding noHurtCamKey;
    private static int optimizationTick = 0;

    private static boolean fullbrightSnapshotTaken = false;
    private static double savedGamma = 1.0D;

    @Override
    public void onInitializeClient() {
        loadConfig();
        AltManager.load();
        optimizationTick = 0;

        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.triggerbot.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.triggerbot"
        ));

        fullbrightKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.triggerbot.fullbright",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category.triggerbot"
        ));

        noHurtCamKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.triggerbot.no_hurt_cam",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "category.triggerbot"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.openScreen(TriggerBotConfigScreen.create(null));
                }
            }

            while (fullbrightKey.wasPressed()) {
                toggleFullbright(client);
            }

            while (noHurtCamKey.wasPressed()) {
                toggleNoHurtCam();
            }

            applyVisualFeatures(client);

            if (client.player != null && client.world != null) {
                TriggerBotOptimizer.tick(client);
            }

            // Never throttle TriggerBot because of the mobile optimizer.
            // Vanilla attack cooldown remains the only attack-rate limiter.
            if (client.player != null && client.world != null && CONFIG.enabled) {
                tickTriggerBot(client);
            }
        });
    }

    private static void tickTriggerBot(MinecraftClient client) {
        PlayerEntity player = client.player;

        if (!player.isAlive() || client.currentScreen != null || client.interactionManager == null) {
            return;
        }

        if (player.isUsingItem()) {
            ItemStack active = player.getActiveItem();
            if (active != null) {
                UseAction action = active.getUseAction();
                if (action == UseAction.EAT || action == UseAction.DRINK) {
                    return;
                }
            }
        }

        if (CONFIG.onlyWeapon && !isWeapon(player.getMainHandStack())) {
            return;
        }

        if (!(client.crosshairTarget instanceof EntityHitResult)) {
            return;
        }

        Entity target = ((EntityHitResult) client.crosshairTarget).getEntity();

        if (!(target instanceof PlayerEntity)
                || target == player
                || !target.isAlive()
                || target.isSpectator()) {
            return;
        }

        // No artificial delay: attack on the first tick where vanilla cooldown is ready.
        if (player.getAttackCooldownProgress(0.0F) < 1.0F) {
            return;
        }

        if (CONFIG.onlyCrits && !canCriticalHit(player, CONFIG.smartCrits)) {
            return;
        }

        client.interactionManager.attackEntity(player, target);
        player.swingHand(Hand.MAIN_HAND);
    }

    private static boolean isWeapon(ItemStack stack) {
        return stack.getItem() instanceof SwordItem
                || stack.getItem() instanceof AxeItem
                || stack.getItem() instanceof TridentItem;
    }

    /**
     * Smart critical detection.
     *
     * It deliberately waits for the falling part of the jump instead of
     * attacking on the way up. With Smart Crits enabled there is no mod-side
     * cooldown: every subsequent vanilla-ready attack during a real crit
     * window may be sent.
     */
    private static boolean canCriticalHit(PlayerEntity player, boolean smart) {
        if (player.isOnGround()
                || player.fallDistance <= 0.0F
                || player.isClimbing()
                || player.isTouchingWater()
                || player.hasVehicle()
                || player.hasStatusEffect(StatusEffects.BLINDNESS)
                || player.isSprinting()) {
            return false;
        }

        if (player.getAttackCooldownProgress(0.0F) < 0.9F) {
            return false;
        }

        if (smart && player.getVelocity().y > 0.0D) {
            return false;
        }

        return true;
    }

    /*
     * CUSTOM FULLBRIGHT
     */
    public static void toggleFullbright(MinecraftClient client) {
        setFullbright(client, !CONFIG.fullbright);
        saveConfig();
    }

    public static void setFullbright(MinecraftClient client, boolean enabled) {
        CONFIG.fullbright = enabled;

        if (client == null || client.options == null) {
            return;
        }

        if (enabled) {
            if (!fullbrightSnapshotTaken) {
                savedGamma = client.options.gamma;
                fullbrightSnapshotTaken = true;
            }

            client.options.gamma = clampDouble(CONFIG.fullbrightGamma, 1.0D, 20.0D);
        } else {
            restoreOriginalGamma(client);
        }
    }

    private static void restoreOriginalGamma(MinecraftClient client) {
        if (client == null || client.options == null) {
            return;
        }

        if (fullbrightSnapshotTaken) {
            client.options.gamma = savedGamma;
            fullbrightSnapshotTaken = false;
        }
    }

    public static void toggleNoHurtCam() {
        CONFIG.noHurtCam = !CONFIG.noHurtCam;
        saveConfig();
    }

    public static boolean isNoHurtCamEnabled() {
        return CONFIG.noHurtCam;
    }

    private static void applyVisualFeatures(MinecraftClient client) {
        if (client == null || client.options == null) {
            return;
        }

        if (CONFIG.fullbright) {
            if (!fullbrightSnapshotTaken) {
                savedGamma = client.options.gamma;
                fullbrightSnapshotTaken = true;
            }

            client.options.gamma = clampDouble(CONFIG.fullbrightGamma, 1.0D, 20.0D);
        }
    }

    public static void saveConfig() {
        File file = getConfigFile();
        File parent = file.getParentFile();

        if (!parent.exists() && !parent.mkdirs()) {
            return;
        }

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("{\n");
            writer.write("  \"enabled\": " + CONFIG.enabled + ",\n");
            writer.write("  \"onlyCrits\": " + CONFIG.onlyCrits + ",\n");
            writer.write("  \"smartCrits\": " + CONFIG.smartCrits + ",\n");
            writer.write("  \"onlyWeapon\": " + CONFIG.onlyWeapon + ",\n");
            writer.write("  \"optimization\": " + CONFIG.optimization + ",\n");
            writer.write("  \"optimizationLevel\": " + CONFIG.optimizationLevel + ",\n");
            writer.write("  \"adaptiveOptimization\": " + CONFIG.adaptiveOptimization + ",\n");
            writer.write("  \"targetFps\": " + CONFIG.targetFps + ",\n");
            writer.write("  \"fullbright\": " + CONFIG.fullbright + ",\n");
            writer.write("  \"noHurtCam\": " + CONFIG.noHurtCam + ",\n");
            writer.write("  \"fullbrightGamma\": " + CONFIG.fullbrightGamma + "\n");
            writer.write("}\n");
        } catch (IOException ignored) {
        }

        AltManager.save();
    }

    public static void loadConfig() {
        File file = getConfigFile();

        if (!file.exists()) {
            saveConfig();
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            StringBuilder json = new StringBuilder();
            char[] buffer = new char[512];
            int read;

            while ((read = reader.read(buffer)) != -1) {
                json.append(buffer, 0, read);
            }

            String text = json.toString();
            CONFIG.enabled = readBoolean(text, "enabled", CONFIG.enabled);
            CONFIG.onlyCrits = readBoolean(text, "onlyCrits", CONFIG.onlyCrits);
            CONFIG.smartCrits = readBoolean(text, "smartCrits", CONFIG.smartCrits);
            CONFIG.onlyWeapon = readBoolean(text, "onlyWeapon", CONFIG.onlyWeapon);
            CONFIG.optimization = readBoolean(text, "optimization", CONFIG.optimization);
            CONFIG.optimizationLevel = clampInt(readInt(text, "optimizationLevel", CONFIG.optimizationLevel), 0, 2);
            CONFIG.adaptiveOptimization = readBoolean(text, "adaptiveOptimization", CONFIG.adaptiveOptimization);
            CONFIG.targetFps = clampInt(readInt(text, "targetFps", CONFIG.targetFps), 30, 60);
            CONFIG.fullbright = readBoolean(text, "fullbright", CONFIG.fullbright);
            CONFIG.noHurtCam = readBoolean(text, "noHurtCam", CONFIG.noHurtCam);
            CONFIG.fullbrightGamma = clampDouble(
                    readDouble(text, "fullbrightGamma", CONFIG.fullbrightGamma),
                    1.0D,
                    20.0D
            );
        } catch (IOException ignored) {
        }
    }

    private static int readInt(String json, String key, int fallback) {
        String needle = "\"" + key + "\"";
        int start = json.indexOf(needle);
        if (start < 0) {
            return fallback;
        }

        int colon = json.indexOf(':', start + needle.length());
        if (colon < 0) {
            return fallback;
        }

        int end = colon + 1;
        while (end < json.length() && Character.isWhitespace(json.charAt(end))) {
            end++;
        }

        int stop = end;
        while (stop < json.length()
                && (Character.isDigit(json.charAt(stop)) || json.charAt(stop) == '-')) {
            stop++;
        }

        try {
            return Integer.parseInt(json.substring(end, stop));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static double readDouble(String json, String key, double fallback) {
        String needle = "\"" + key + "\"";
        int start = json.indexOf(needle);
        if (start < 0) {
            return fallback;
        }

        int colon = json.indexOf(':', start + needle.length());
        if (colon < 0) {
            return fallback;
        }

        int end = colon + 1;
        while (end < json.length() && Character.isWhitespace(json.charAt(end))) {
            end++;
        }

        int stop = end;
        while (stop < json.length()
                && "0123456789.-".indexOf(json.charAt(stop)) >= 0) {
            stop++;
        }

        try {
            return Double.parseDouble(json.substring(end, stop));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clampDouble(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static boolean readBoolean(String json, String key, boolean fallback) {
        String needle = "\"" + key + "\"";
        int start = json.indexOf(needle);

        if (start < 0) {
            return fallback;
        }

        int colon = json.indexOf(':', start + needle.length());

        if (colon < 0) {
            return fallback;
        }

        String value = json.substring(colon + 1).trim();

        if (value.startsWith("true")) {
            return true;
        }

        if (value.startsWith("false")) {
            return false;
        }

        return fallback;
    }

    private static File getConfigFile() {
        return new File(MinecraftClient.getInstance().runDirectory, "config/triggerbot.json");
    }
}
