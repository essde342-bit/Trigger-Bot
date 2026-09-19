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

    @Override
    public void onInitializeClient() {
        loadConfig();

        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.triggerbot.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.triggerbot"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.wasPressed()) {
                client.openScreen(new TriggerBotScreen());
            }

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

        // Do not attack while eating or drinking.
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

        // Let vanilla attack timing control the click rate.
        if (player.getAttackCooldownProgress(0.0F) < 1.0F) {
            return;
        }

        if (CONFIG.onlyCrits && !canCriticalHit(player)) {
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

    // Matches the normal 1.16.5 critical-hit conditions used by PlayerEntity.
    private static boolean canCriticalHit(PlayerEntity player) {
        return !player.isOnGround()
                && player.fallDistance > 0.0F
                && !player.isClimbing()
                && !player.isTouchingWater()
                && !player.hasVehicle()
                && !player.hasStatusEffect(StatusEffects.BLINDNESS)
                && !player.isSprinting()
                && player.getAttackCooldownProgress(0.0F) >= 0.9F;
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
            writer.write("  \"onlyWeapon\": " + CONFIG.onlyWeapon + "\n");
            writer.write("}\n");
        } catch (IOException ignored) {
            // A broken config file must never crash the client.
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

            while ((read = reader.read(buffer)) != -1) {
                json.append(buffer, 0, read);
            }

            String text = json.toString();
            CONFIG.enabled = readBoolean(text, "enabled", CONFIG.enabled);
            CONFIG.onlyCrits = readBoolean(text, "onlyCrits", CONFIG.onlyCrits);
            CONFIG.onlyWeapon = readBoolean(text, "onlyWeapon", CONFIG.onlyWeapon);
        } catch (IOException ignored) {
            // Keep safe in-memory defaults if the config cannot be read.
        }
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
