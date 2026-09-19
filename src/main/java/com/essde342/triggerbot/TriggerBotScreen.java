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
        int buttonWidth = Math.min(190, Math.max(150, this.width - 20));
        int buttonHeight = 22;
        int spacing = 25;
        int totalHeight = spacing * 4 - 3;
        int left = (this.width - buttonWidth) / 2;
        int top = Math.max(18, (this.height - totalHeight) / 2);

        this.addButton(new ButtonWidget(
                left, top, buttonWidth, buttonHeight,
                getEnabledText(),
                button -> {
                    TriggerBotClient.CONFIG.enabled = !TriggerBotClient.CONFIG.enabled;
                    TriggerBotClient.saveConfig();
                    button.setMessage(getEnabledText());
                }
        ));

        this.addButton(new ButtonWidget(
                left, top + spacing, buttonWidth, buttonHeight,
                getCritsText(),
                button -> {
                    TriggerBotClient.CONFIG.onlyCrits = !TriggerBotClient.CONFIG.onlyCrits;
                    TriggerBotClient.saveConfig();
                    button.setMessage(getCritsText());
                }
        ));

        this.addButton(new ButtonWidget(
                left, top + spacing * 2, buttonWidth, buttonHeight,
                getWeaponText(),
                button -> {
                    TriggerBotClient.CONFIG.onlyWeapon = !TriggerBotClient.CONFIG.onlyWeapon;
                    TriggerBotClient.saveConfig();
                    button.setMessage(getWeaponText());
                }
        ));

        this.addButton(new ButtonWidget(
                left, top + spacing * 3, buttonWidth, buttonHeight,
                new LiteralText("Close"),
                button -> this.onClose()
        ));
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

        int contentHeight = 25 * 4 - 3;
        int titleY = Math.max(7, (this.height - contentHeight) / 2 - 20);

        drawCenteredText(
                matrices,
                this.textRenderer,
                this.title,
                this.width / 2,
                titleY,
                0xFFFFFF
        );

        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
