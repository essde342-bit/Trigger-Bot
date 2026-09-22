package com.essde342.triggerbot;

import kronex.fun.display.screens.clickgui.MenuScreen;
import com.essde342.triggerbot.ui.modules.ModuleManager;

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
import net.minecraft.util.math.MathHelper;
import net.minecraft.item.consume.UseAction;
import net.minecraft.util.hit.EntityHitResult;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class TriggerBotClient implements ClientModInitializer {
    public static final TriggerBotConfig CONFIG = new TriggerBotConfig();

    private static KeyBinding openMenuKey;
    private static int optimizationTick = 0;
    private static boolean fullbrightSnapshotTaken = false;
    private static double savedGamma = 1.0D;
    private static PlayerEntity aimTarget;
    private static PlayerEntity triggerTarget;
    private static long lastTriggerAttackTime;

    @Override
    public void onInitializeClient() {
        loadConfig();
        ModuleManager.moduleRegister();
        AltManager.load();
        optimizationTick = 0;

        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.triggerbot.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.triggerbot"
        ));

        TargetESP.register();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.wasPressed()) {
                if (client.currentScreen == null) {
                    MenuScreen.INSTANCE.openGui();
                } else if (client.currentScreen instanceof MenuScreen) {
                    client.currentScreen.keyPressed(GLFW.GLFW_KEY_ESCAPE, 0, 0);
                }
            }

            ModuleManager.handleKeyBinds(client);
            applyVisualFeatures(client);

            if (client.player != null && client.world != null) {
                TriggerBotOptimizer.tick(client);
            }

            if (client.player != null && client.world != null) {
                if (CONFIG.aimAssist) {
                    tickAimAssist(client);
                } else {
                    aimTarget = null;
                }

                if (CONFIG.enabled) {
                    tickTriggerBot(client);
                } else {
                    triggerTarget = null;
                }
            }
        });
    }

    private static void tickAimAssist(MinecraftClient client) {
        PlayerEntity player = client.player;
        if (player == null || !player.isAlive() || client.currentScreen != null) {
            aimTarget = null;
            return;
        }

        PlayerEntity target = null;
        double bestDistance = CONFIG.aimAssistRange * CONFIG.aimAssistRange;

        for (PlayerEntity candidate : client.world.getPlayers()) {
            if (candidate == player || !candidate.isAlive() || candidate.isSpectator()) continue;
            double distance = player.squaredDistanceTo(candidate);
            if (distance >= bestDistance || !player.canSee(candidate)) continue;
            bestDistance = distance;
            target = candidate;
        }

        if (target == null) {
            aimTarget = null;
            return;
        }

        aimTarget = target;
        double dx = target.getX() - player.getX();
        double dz = target.getZ() - player.getZ();
        double dy = target.getY() + target.getStandingEyeHeight() * 0.85D - (player.getY() + player.getStandingEyeHeight());
        double horizontal = Math.sqrt(dx * dx + dz * dz);

        float targetYaw = (float) (Math.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
        float targetPitch = (float) -(Math.atan2(dy, horizontal) * 180.0D / Math.PI);
        double responseMs = Math.max(100.0D, CONFIG.aimAssistDurationMs);
        float smoothing = (float) MathHelper.clamp(1.0D - Math.exp(-50.0D / responseMs), 0.04D, 0.38D);
        float yawDelta = MathHelper.wrapDegrees(targetYaw - player.getYaw());
        float pitchDelta = targetPitch - player.getPitch();
        float yawStep = MathHelper.clamp(yawDelta * smoothing, -10.0F, 10.0F);
        float pitchStep = MathHelper.clamp(pitchDelta * smoothing, -7.0F, 7.0F);

        player.setYaw(player.getYaw() + yawStep);
        player.setPitch(MathHelper.clamp(player.getPitch() + pitchStep, -90.0F, 90.0F));
    }

    private static void tickTriggerBot(MinecraftClient client) {
        PlayerEntity player = client.player;
        triggerTarget = null;

        if (!player.isAlive() || client.currentScreen != null || client.interactionManager == null) return;

        if (player.isUsingItem()) {
            ItemStack active = player.getActiveItem();
            if (active != null) {
                UseAction action = active.getUseAction();
                if (action == UseAction.EAT || action == UseAction.DRINK) return;
            }
        }

        if (CONFIG.onlyWeapon && !isWeapon(player.getMainHandStack())) return;
        if (!(client.crosshairTarget instanceof EntityHitResult)) return;

        Entity target = ((EntityHitResult) client.crosshairTarget).getEntity();
        if (!(target instanceof PlayerEntity)
                || target == player
                || !target.isAlive()
                || target.isSpectator()) return;

        if (player.squaredDistanceTo(target) > CONFIG.triggerRange * CONFIG.triggerRange) return;
        triggerTarget = (PlayerEntity) target;

        if (player.getAttackCooldownProgress(0.0F) < 1.0F) return;
        if (CONFIG.onlyCrits && !canCriticalHit(player, CONFIG.smartCrits)) return;

        long now = System.currentTimeMillis();
        if (now - lastTriggerAttackTime < CONFIG.triggerDelayMs) return;

        client.interactionManager.attackEntity(player, target);
        player.swingHand(Hand.MAIN_HAND);
        lastTriggerAttackTime = now;
    }

    private static boolean isWeapon(ItemStack stack) {
        return stack.getItem() instanceof SwordItem || stack.getItem() instanceof AxeItem || stack.getItem() instanceof TridentItem;
    }

    private static boolean canCriticalHit(PlayerEntity player, boolean smart) {
        if (player.isOnGround()
                || player.fallDistance <= 0.0F
                || player.isClimbing()
                || player.isTouchingWater()
                || player.hasVehicle()
                || player.hasStatusEffect(StatusEffects.BLINDNESS)
                || player.isSprinting()) return false;

        if (player.getAttackCooldownProgress(0.0F) < 0.9F) return false;
        if (smart && player.getVelocity().y > 0.0D) return false;
        return true;
    }

    public static void toggleFullbright(MinecraftClient client) {
        setFullbright(client, !CONFIG.fullbright);
        saveConfig();
    }

    public static void setFullbright(MinecraftClient client, boolean enabled) {
        CONFIG.fullbright = enabled;
        if (client == null || client.options == null) return;

        if (enabled) {
            if (!fullbrightSnapshotTaken) {
                savedGamma = client.options.getGamma().getValue();
                fullbrightSnapshotTaken = true;
            }
            client.options.getGamma().setValue(clampDouble(CONFIG.fullbrightGamma, 0.0D, 1.0D));
        } else {
            restoreOriginalGamma(client);
        }
    }

    private static void restoreOriginalGamma(MinecraftClient client) {
        if (client == null || client.options == null) return;
        if (fullbrightSnapshotTaken) {
            client.options.getGamma().setValue(savedGamma);
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
        if (client == null || client.options == null) return;
        if (CONFIG.fullbright) {
            if (!fullbrightSnapshotTaken) {
                savedGamma = client.options.getGamma().getValue();
                fullbrightSnapshotTaken = true;
            }
            client.options.getGamma().setValue(clampDouble(CONFIG.fullbrightGamma, 1.0D, 20.0D));
        } else if (fullbrightSnapshotTaken) {
            restoreOriginalGamma(client);
        }
    }

    public static PlayerEntity getCurrentCombatTarget() {
        if (aimTarget != null && aimTarget.isAlive()) return aimTarget;
        if (triggerTarget != null && triggerTarget.isAlive()) return triggerTarget;
        return null;
    }

    public static void saveConfig() {
        if (ModuleManager.isInitialized()) ModuleManager.syncBindsToConfig();

        File file = getConfigFile();
        File parent = file.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) return;

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("{\n");
            writer.write("  \"enabled\": " + CONFIG.enabled + ",\n");
            writer.write("  \"onlyCrits\": " + CONFIG.onlyCrits + ",\n");
            writer.write("  \"smartCrits\": " + CONFIG.smartCrits + ",\n");
            writer.write("  \"onlyWeapon\": " + CONFIG.onlyWeapon + ",\n");
            writer.write("  \"triggerDelayMs\": " + CONFIG.triggerDelayMs + ",\n");
            writer.write("  \"triggerRange\": " + CONFIG.triggerRange + ",\n");
            writer.write("  \"aimAssist\": " + CONFIG.aimAssist + ",\n");
            writer.write("  \"aimAssistDurationMs\": " + CONFIG.aimAssistDurationMs + ",\n");
            writer.write("  \"aimAssistRange\": " + CONFIG.aimAssistRange + ",\n");
            writer.write("  \"lightningEsp\": " + CONFIG.lightningEsp + ",\n");
            writer.write("  \"targetEsp\": " + CONFIG.targetEsp + ",\n");
            writer.write("  \"jumpCircle\": " + CONFIG.jumpCircle + ",\n");
            writer.write("  \"aspectRatioEnabled\": " + CONFIG.aspectRatioEnabled + ",\n");
            writer.write("  \"aspectRatio\": " + CONFIG.aspectRatio + ",\n");
            writer.write("  \"optimization\": " + CONFIG.optimization + ",\n");
            writer.write("  \"optimizationLevel\": " + CONFIG.optimizationLevel + ",\n");
            writer.write("  \"adaptiveOptimization\": " + CONFIG.adaptiveOptimization + ",\n");
            writer.write("  \"targetFps\": " + CONFIG.targetFps + ",\n");
            writer.write("  \"fullbright\": " + CONFIG.fullbright + ",\n");
            writer.write("  \"noHurtCam\": " + CONFIG.noHurtCam + ",\n");
            writer.write("  \"fullbrightGamma\": " + CONFIG.fullbrightGamma + ",\n");
            writer.write("  \"bindTriggerBot\": " + CONFIG.bindTriggerBot + ",\n");
            writer.write("  \"bindAimAssist\": " + CONFIG.bindAimAssist + ",\n");
            writer.write("  \"bindLightningEsp\": " + CONFIG.bindLightningEsp + ",\n");
            writer.write("  \"bindTargetEsp\": " + CONFIG.bindTargetEsp + ",\n");
            writer.write("  \"bindJumpCircle\": " + CONFIG.bindJumpCircle + ",\n");
            writer.write("  \"bindFullbright\": " + CONFIG.bindFullbright + ",\n");
            writer.write("  \"bindNoHurtCam\": " + CONFIG.bindNoHurtCam + ",\n");
            writer.write("  \"bindAspectRatio\": " + CONFIG.bindAspectRatio + ",\n");
            writer.write("  \"bindOptimization\": " + CONFIG.bindOptimization + ",\n");
            writer.write("  \"bindAdaptiveOptimization\": " + CONFIG.bindAdaptiveOptimization + "\n");
            writer.write("}\n");
        } catch (IOException ignored) {
        }
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
            while ((read = reader.read(buffer)) != -1) json.append(buffer, 0, read);

            String text = json.toString();
            CONFIG.enabled = readBoolean(text, "enabled", CONFIG.enabled);
            CONFIG.onlyCrits = readBoolean(text, "onlyCrits", CONFIG.onlyCrits);
            CONFIG.smartCrits = readBoolean(text, "smartCrits", CONFIG.smartCrits);
            CONFIG.onlyWeapon = readBoolean(text, "onlyWeapon", CONFIG.onlyWeapon);
            CONFIG.triggerDelayMs = clampInt(readInt(text, "triggerDelayMs", CONFIG.triggerDelayMs), 0, 500);
            CONFIG.triggerRange = clampDouble(readDouble(text, "triggerRange", CONFIG.triggerRange), 2.0D, 6.0D);
            CONFIG.aimAssist = readBoolean(text, "aimAssist", CONFIG.aimAssist);
            CONFIG.aimAssistDurationMs = clampInt(readInt(text, "aimAssistDurationMs", CONFIG.aimAssistDurationMs), 100, 1500);
            CONFIG.aimAssistRange = clampDouble(readDouble(text, "aimAssistRange", CONFIG.aimAssistRange), 2.0D, 6.0D);
            CONFIG.lightningEsp = readBoolean(text, "lightningEsp", CONFIG.lightningEsp);
            CONFIG.targetEsp = readBoolean(text, "targetEsp", CONFIG.targetEsp);
            CONFIG.jumpCircle = readBoolean(text, "jumpCircle", CONFIG.jumpCircle);
            CONFIG.aspectRatioEnabled = readBoolean(text, "aspectRatioEnabled", CONFIG.aspectRatioEnabled);
            CONFIG.aspectRatio = clampDouble(readDouble(text, "aspectRatio", CONFIG.aspectRatio), 0.50D, 3.00D);
            CONFIG.optimization = readBoolean(text, "optimization", CONFIG.optimization);
            CONFIG.optimizationLevel = clampInt(readInt(text, "optimizationLevel", CONFIG.optimizationLevel), 0, 2);
            CONFIG.adaptiveOptimization = readBoolean(text, "adaptiveOptimization", CONFIG.adaptiveOptimization);
            CONFIG.targetFps = clampInt(readInt(text, "targetFps", CONFIG.targetFps), 30, 60);
            CONFIG.fullbright = readBoolean(text, "fullbright", CONFIG.fullbright);
            CONFIG.noHurtCam = readBoolean(text, "noHurtCam", CONFIG.noHurtCam);
            CONFIG.fullbrightGamma = clampDouble(readDouble(text, "fullbrightGamma", CONFIG.fullbrightGamma), 0.0D, 1.0D);
            CONFIG.bindTriggerBot = readInt(text, "bindTriggerBot", CONFIG.bindTriggerBot);
            CONFIG.bindAimAssist = readInt(text, "bindAimAssist", CONFIG.bindAimAssist);
            CONFIG.bindLightningEsp = readInt(text, "bindLightningEsp", CONFIG.bindLightningEsp);
            CONFIG.bindTargetEsp = readInt(text, "bindTargetEsp", CONFIG.bindTargetEsp);
            CONFIG.bindJumpCircle = readInt(text, "bindJumpCircle", CONFIG.bindJumpCircle);
            CONFIG.bindFullbright = readInt(text, "bindFullbright", CONFIG.bindFullbright);
            CONFIG.bindNoHurtCam = readInt(text, "bindNoHurtCam", CONFIG.bindNoHurtCam);
            CONFIG.bindAspectRatio = readInt(text, "bindAspectRatio", CONFIG.bindAspectRatio);
            CONFIG.bindOptimization = readInt(text, "bindOptimization", CONFIG.bindOptimization);
            CONFIG.bindAdaptiveOptimization = readInt(text, "bindAdaptiveOptimization", CONFIG.bindAdaptiveOptimization);
        } catch (IOException ignored) {
        }
    }

    private static int readInt(String json, String key, int fallback) {
        String needle = "\"" + key + "\"";
        int start = json.indexOf(needle);
        if (start < 0) return fallback;
        int colon = json.indexOf(':', start + needle.length());
        if (colon < 0) return fallback;
        int end = colon + 1;
        while (end < json.length() && Character.isWhitespace(json.charAt(end))) end++;
        int stop = end;
        while (stop < json.length() && (Character.isDigit(json.charAt(stop)) || json.charAt(stop) == '-')) stop++;
        try { return Integer.parseInt(json.substring(end, stop)); } catch (NumberFormatException ignored) { return fallback; }
    }

    private static double readDouble(String json, String key, double fallback) {
        String needle = "\"" + key + "\"";
        int start = json.indexOf(needle);
        if (start < 0) return fallback;
        int colon = json.indexOf(':', start + needle.length());
        if (colon < 0) return fallback;
        int end = colon + 1;
        while (end < json.length() && Character.isWhitespace(json.charAt(end))) end++;
        int stop = end;
        while (stop < json.length() && "0123456789.-".indexOf(json.charAt(stop)) >= 0) stop++;
        try { return Double.parseDouble(json.substring(end, stop)); } catch (NumberFormatException ignored) { return fallback; }
    }

    private static int clampInt(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
    private static double clampDouble(double value, double min, double max) { return Math.max(min, Math.min(max, value)); }

    private static boolean readBoolean(String json, String key, boolean fallback) {
        String needle = "\"" + key + "\"";
        int start = json.indexOf(needle);
        if (start < 0) return fallback;
        int colon = json.indexOf(':', start + needle.length());
        if (colon < 0) return fallback;
        String value = json.substring(colon + 1).trim();
        if (value.startsWith("true")) return true;
        if (value.startsWith("false")) return false;
        return fallback;
    }

    private static File getConfigFile() {
        return new File(MinecraftClient.getInstance().runDirectory, "config/triggerbot.json");
    }
}