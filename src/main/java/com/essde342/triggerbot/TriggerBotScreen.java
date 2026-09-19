package com.essde342.triggerbot;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

public class TriggerBotScreen extends Screen {
    public TriggerBotScreen() {
        super(new LiteralText("Trigger Bot"));
    }

    @Override
    protected void init() {
        int width = 180, height = 20;
        int left = (this.width - width) / 2;
        int top = Math.max(25, (this.height - 100) / 2);

        this.addButton(new ButtonWidget(left, top, width, height, getEnabledText(), button -> {
            TriggerBotClient.CONFIG.enabled = !TriggerBotClient.CONFIG.enabled;
            TriggerBotClient.saveConfig();
            button.setMessage(getEnabledText());
        }));
        this.addButton(new ButtonWidget(left, top + 25, width, height, getCritsText(), button -> {
            TriggerBotClient.CONFIG.onlyCrits = !TriggerBotClient.CONFIG.onlyCrits;
            TriggerBotClient.saveConfig();
            button.setMessage(getCritsText());
        }));
        this.addButton(new ButtonWidget(left, top + 50, width, height, getWeaponText(), button -> {
            TriggerBotClient.CONFIG.onlyWeapon = !TriggerBotClient.CONFIG.onlyWeapon;
            TriggerBotClient.saveConfig();
            button.setMessage(getWeaponText());
        }));
        this.addButton(new ButtonWidget(left, top + 75, width, height,
                new LiteralText("Close"), button -> this.client.openScreen(null)));
    }

    private LiteralText getEnabledText() {
        return new LiteralText("TriggerBot: " + (TriggerBotClient.CONFIG.enabled ? "ON" : "OFF"));
    }
    private LiteralText getCritsText() {
        return new LiteralText("Only crits: " + (TriggerBotClient.CONFIG.onlyCrits ? "ON" : "OFF"));
    }
    private LiteralText getWeaponText() {
        return new LiteralText("Only weapon: " + (TriggerBotClient.CONFIG.onlyWeapon ? "ON" : "OFF"));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        int titleY = Math.max(8, (this.height - 100) / 2 - 22);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, titleY, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() { return false; }
}
