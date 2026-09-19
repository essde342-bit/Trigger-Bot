package com.essde342.triggerbot;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;

public final class TriggerBotConfigScreen {
    private TriggerBotConfigScreen() {
    }

    public static Screen create(Screen parent) {
        MinecraftClient client = MinecraftClient.getInstance();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(new LiteralText("Trigger Bot Settings"))
                .setSavingRunnable(TriggerBotClient::saveConfig);

        ConfigEntryBuilder entries = builder.entryBuilder();

        ConfigCategory combat = builder.getOrCreateCategory(new LiteralText("Combat"));
        combat.addEntry(entries.startBooleanToggle(
                new LiteralText("TriggerBot"), TriggerBotClient.CONFIG.enabled)
                .setDefaultValue(true)
                .setSaveConsumer(value -> TriggerBotClient.CONFIG.enabled = value)
                .build());

        combat.addEntry(entries.startBooleanToggle(
                new LiteralText("Only crits"), TriggerBotClient.CONFIG.onlyCrits)
                .setDefaultValue(false)
                .setSaveConsumer(value -> TriggerBotClient.CONFIG.onlyCrits = value)
                .build());

        combat.addEntry(entries.startBooleanToggle(
                new LiteralText("Only weapon"), TriggerBotClient.CONFIG.onlyWeapon)
                .setDefaultValue(false)
                .setSaveConsumer(value -> TriggerBotClient.CONFIG.onlyWeapon = value)
                .build());

        ConfigCategory visual = builder.getOrCreateCategory(new LiteralText("Visual"));

        visual.addEntry(entries.startBooleanToggle(
                new LiteralText("Fullbright"), TriggerBotClient.CONFIG.fullbright)
                .setDefaultValue(false)
                .setSaveConsumer(value -> TriggerBotClient.setFullbright(client, value))
                .build());

        visual.addEntry(entries.startDoubleField(
                new LiteralText("Fullbright gamma"), TriggerBotClient.CONFIG.fullbrightGamma)
                .setDefaultValue(15.0D)
                .setMin(1.0D)
                .setMax(20.0D)
                .setSaveConsumer(value -> {
                    TriggerBotClient.CONFIG.fullbrightGamma = value;
                    if (TriggerBotClient.CONFIG.fullbright) {
                        TriggerBotClient.setFullbright(client, true);
                    }
                })
                .build());

        visual.addEntry(entries.startBooleanToggle(
                new LiteralText("No Hurt Cam"), TriggerBotClient.CONFIG.noHurtCam)
                .setDefaultValue(true)
                .setSaveConsumer(value -> TriggerBotClient.CONFIG.noHurtCam = value)
                .build());

        ConfigCategory optimization = builder.getOrCreateCategory(new LiteralText("Optimization"));

        optimization.addEntry(entries.startBooleanToggle(
                new LiteralText("Custom optimization"), TriggerBotClient.CONFIG.optimization)
                .setDefaultValue(true)
                .setSaveConsumer(value -> TriggerBotOptimizer.setOptimization(client, value))
                .build());

        optimization.addEntry(entries.startIntField(
                new LiteralText("Optimization profile (0 Balanced, 1 Performance, 2 Extreme)"),
                TriggerBotClient.CONFIG.optimizationLevel)
                .setDefaultValue(2)
                .setMin(0)
                .setMax(2)
                .setSaveConsumer(value -> {
                    TriggerBotClient.CONFIG.optimizationLevel = value;
                    if (TriggerBotClient.CONFIG.optimization) {
                        TriggerBotOptimizer.setOptimization(client, true);
                    }
                })
                .build());

        optimization.addEntry(entries.startBooleanToggle(
                new LiteralText("Adaptive FPS"), TriggerBotClient.CONFIG.adaptiveOptimization)
                .setDefaultValue(false)
                .setSaveConsumer(value -> TriggerBotClient.CONFIG.adaptiveOptimization = value)
                .build());

        optimization.addEntry(entries.startIntField(
                new LiteralText("Target FPS"), TriggerBotClient.CONFIG.targetFps)
                .setDefaultValue(45)
                .setMin(30)
                .setMax(60)
                .setSaveConsumer(value -> TriggerBotClient.CONFIG.targetFps = value)
                .build());

        return builder.build();
    }
}
