package com.essde342.triggerbot;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

public class TriggerBotScreen extends Screen {
    private int tab = 0;

    private int panelLeft;
    private int panelTop;
    private int panelWidth;
    private int panelHeight;

    public TriggerBotScreen() {
        super(new LiteralText("Trigger Bot"));
    }

    @Override
    protected void init() {
        panelWidth = Math.min(330, Math.max(285, this.width - 12));
        panelHeight = Math.min(250, Math.max(210, this.height - 18));
        panelLeft = (this.width - panelWidth) / 2;
        panelTop = (this.height - panelHeight) / 2;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        // Background.
        fill(matrices, 0, 0, this.width, this.height, 0xB8000000);

        // Main window.
        fill(matrices, panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight, 0xFF101318);
        fill(matrices, panelLeft, panelTop, panelLeft + panelWidth, panelTop + 3, 0xFF57A6FF);

        // Header.
        drawString(matrices, this.textRenderer, new LiteralText("TRIGGER BOT"), panelLeft + 14, panelTop + 11, 0xFFFFFFFF);
        drawString(matrices, this.textRenderer,
                new LiteralText("Fabric 1.16.5 • Mobile Edition"),
                panelLeft + 14, panelTop + 22, 0xFF8D96A6);

        // Close button.
        int closeX = panelLeft + panelWidth - 28;
        int closeY = panelTop + 9;
        boolean closeHover = inside(mouseX, mouseY, closeX, closeY, 18, 18);
        fill(matrices, closeX, closeY, closeX + 18, closeY + 18, closeHover ? 0xFFB52E3A : 0xFF252A33);
        drawCenteredText(matrices, this.textRenderer, new LiteralText("×"), closeX + 9, closeY + 3, 0xFFFFFFFF);

        // Sidebar.
        int sideLeft = panelLeft + 8;
        int sideTop = panelTop + 45;
        int sideWidth = 78;
        int sideHeight = panelHeight - 53;
        fill(matrices, sideLeft, sideTop, sideLeft + sideWidth, sideTop + sideHeight, 0xFF171B22);

        drawSidebarTab(matrices, mouseX, mouseY, sideLeft + 6, sideTop + 8, 66, 30, "Combat", tab == 0);
        drawSidebarTab(matrices, mouseX, mouseY, sideLeft + 6, sideTop + 44, 66, 30, "Optimize", tab == 1);

        // Content.
        int contentLeft = sideLeft + sideWidth + 12;
        int contentTop = sideTop;
        int contentWidth = panelLeft + panelWidth - contentLeft - 10;

        fill(matrices, contentLeft, contentTop, contentLeft + contentWidth, contentTop + sideHeight, 0xFF141820);

        if (tab == 0) {
            renderCombat(matrices, mouseX, mouseY, contentLeft, contentTop, contentWidth);
        } else {
            renderOptimize(matrices, mouseX, mouseY, contentLeft, contentTop, contentWidth);
        }

        super.render(matrices, mouseX, mouseY, delta);
    }

    private void renderCombat(MatrixStack matrices, int mouseX, int mouseY, int x, int y, int width) {
        drawString(matrices, this.textRenderer, new LiteralText("Combat"), x + 12, y + 10, 0xFFFFFFFF);
        drawString(matrices, this.textRenderer,
                new LiteralText("Trigger automatically when a player is under your crosshair."),
                x + 12, y + 25, 0xFF8D96A6);

        drawToggle(matrices, mouseX, mouseY, x + 10, y + 47, width - 20, 30,
                "TriggerBot", TriggerBotClient.CONFIG.enabled);

        drawToggle(matrices, mouseX, mouseY, x + 10, y + 83, width - 20, 30,
                "Only crits", TriggerBotClient.CONFIG.onlyCrits);

        drawToggle(matrices, mouseX, mouseY, x + 10, y + 119, width - 20, 30,
                "Only weapon", TriggerBotClient.CONFIG.onlyWeapon);
    }

    private void renderOptimize(MatrixStack matrices, int mouseX, int mouseY, int x, int y, int width) {
        drawString(matrices, this.textRenderer, new LiteralText("Mobile optimization"), x + 12, y + 10, 0xFFFFFFFF);
        drawString(matrices, this.textRenderer,
                new LiteralText("Custom Pojav-friendly optimizer. No Sodium."),
                x + 12, y + 25, 0xFF8D96A6);

        drawToggle(matrices, mouseX, mouseY, x + 10, y + 47, width - 20, 30,
                "Optimization", TriggerBotClient.CONFIG.optimization);

        drawAction(matrices, mouseX, mouseY, x + 10, y + 83, width - 20, 30,
                "Profile: " + profileName());

        drawToggle(matrices, mouseX, mouseY, x + 10, y + 119, width - 20, 30,
                "Adaptive FPS", TriggerBotClient.CONFIG.adaptiveOptimization);

        int targetY = y + 155;
        drawString(matrices, this.textRenderer, new LiteralText("Target FPS"),
                x + 12, targetY + 8, 0xFFD9DEE7);

        drawAction(matrices, mouseX, mouseY, x + width - 92, targetY, 28, 30, "-");
        drawCenteredText(matrices, this.textRenderer,
                new LiteralText(String.valueOf(TriggerBotClient.CONFIG.targetFps)),
                x + width - 51, targetY + 8, 0xFFFFFFFF);
        drawAction(matrices, mouseX, mouseY, x + width - 42, targetY, 28, 30, "+");

        MinecraftClient client = MinecraftClient.getInstance();
        String fps = client.getCurrentFps() > 0 ? client.getCurrentFps() + " FPS" : "-- FPS";
        drawString(matrices, this.textRenderer, new LiteralText("Live: " + fps),
                x + 12, targetY + 38, 0xFF57A6FF);

        String status = TriggerBotClient.CONFIG.optimization
                ? "Optimizer active"
                : "Optimizer disabled";
        drawString(matrices, this.textRenderer, new LiteralText(status),
                x + 12, targetY + 53, TriggerBotClient.CONFIG.optimization ? 0xFF62D98B : 0xFF8D96A6);
    }

    private void drawSidebarTab(MatrixStack matrices, int mouseX, int mouseY,
                                int x, int y, int width, int height,
                                String text, boolean selected) {
        boolean hover = inside(mouseX, mouseY, x, y, width, height);
        int color = selected ? 0xFF243B57 : (hover ? 0xFF20262F : 0xFF1A1F27);
        fill(matrices, x, y, x + width, y + height, color);
        if (selected) {
            fill(matrices, x, y, x + 3, y + height, 0xFF57A6FF);
        }
        drawCenteredText(matrices, this.textRenderer, new LiteralText(text),
                x + width / 2, y + 10, selected ? 0xFFFFFFFF : 0xFFB3BAC7);
    }

    private void drawToggle(MatrixStack matrices, int mouseX, int mouseY,
                            int x, int y, int width, int height,
                            String label, boolean enabled) {
        boolean hover = inside(mouseX, mouseY, x, y, width, height);
        fill(matrices, x, y, x + width, y + height, hover ? 0xFF202631 : 0xFF1A1F27);

        drawString(matrices, this.textRenderer, new LiteralText(label),
                x + 10, y + 9, 0xFFE9EDF3);

        int switchX = x + width - 42;
        int switchColor = enabled ? 0xFF45C48A : 0xFF4B525D;
        fill(matrices, switchX, y + 7, switchX + 32, y + 23, switchColor);
        fill(matrices, enabled ? switchX + 18 : switchX + 2, y + 9,
                enabled ? switchX + 30 : switchX + 14, y + 21, 0xFFFFFFFF);
    }

    private void drawAction(MatrixStack matrices, int mouseX, int mouseY,
                            int x, int y, int width, int height, String text) {
        boolean hover = inside(mouseX, mouseY, x, y, width, height);
        fill(matrices, x, y, x + width, y + height, hover ? 0xFF2B3542 : 0xFF202631);
        drawCenteredText(matrices, this.textRenderer, new LiteralText(text),
                x + width / 2, y + 9, 0xFFFFFFFF);
    }

    private String profileName() {
        switch (TriggerBotClient.CONFIG.optimizationLevel) {
            case 0:
                return "Balanced";
            case 2:
                return "Extreme";
            default:
                return "Performance";
        }
    }

    private boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width
                && mouseY >= y && mouseY <= y + height;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        int sideLeft = panelLeft + 8;
        int sideTop = panelTop + 45;

        if (inside(mouseX, mouseY, sideLeft + 6, sideTop + 8, 66, 30)) {
            tab = 0;
            return true;
        }

        if (inside(mouseX, mouseY, sideLeft + 6, sideTop + 44, 66, 30)) {
            tab = 1;
            return true;
        }

        int closeX = panelLeft + panelWidth - 28;
        int closeY = panelTop + 9;
        if (inside(mouseX, mouseY, closeX, closeY, 18, 18)) {
            this.onClose();
            return true;
        }

        int contentLeft = sideLeft + 78 + 12;
        int contentTop = sideTop;
        int contentWidth = panelLeft + panelWidth - contentLeft - 10;

        if (tab == 0) {
            if (inside(mouseX, mouseY, contentLeft + 10, contentTop + 47, contentWidth - 20, 30)) {
                TriggerBotClient.CONFIG.enabled = !TriggerBotClient.CONFIG.enabled;
                TriggerBotClient.saveConfig();
                return true;
            }

            if (inside(mouseX, mouseY, contentLeft + 10, contentTop + 83, contentWidth - 20, 30)) {
                TriggerBotClient.CONFIG.onlyCrits = !TriggerBotClient.CONFIG.onlyCrits;
                TriggerBotClient.saveConfig();
                return true;
            }

            if (inside(mouseX, mouseY, contentLeft + 10, contentTop + 119, contentWidth - 20, 30)) {
                TriggerBotClient.CONFIG.onlyWeapon = !TriggerBotClient.CONFIG.onlyWeapon;
                TriggerBotClient.saveConfig();
                return true;
            }
        } else {
            MinecraftClient client = MinecraftClient.getInstance();

            if (inside(mouseX, mouseY, contentLeft + 10, contentTop + 47, contentWidth - 20, 30)) {
                TriggerBotOptimizer.setOptimization(client, !TriggerBotClient.CONFIG.optimization);
                TriggerBotClient.saveConfig();
                return true;
            }

            if (inside(mouseX, mouseY, contentLeft + 10, contentTop + 83, contentWidth - 20, 30)) {
                TriggerBotClient.CONFIG.optimizationLevel =
                        (TriggerBotClient.CONFIG.optimizationLevel + 1) % 3;
                if (TriggerBotClient.CONFIG.optimization) {
                    TriggerBotOptimizer.setOptimization(client, true);
                }
                TriggerBotClient.saveConfig();
                return true;
            }

            if (inside(mouseX, mouseY, contentLeft + 10, contentTop + 119, contentWidth - 20, 30)) {
                TriggerBotClient.CONFIG.adaptiveOptimization = !TriggerBotClient.CONFIG.adaptiveOptimization;
                TriggerBotClient.saveConfig();
                return true;
            }

            int targetY = contentTop + 155;
            if (inside(mouseX, mouseY, contentLeft + contentWidth - 92, targetY, 28, 30)) {
                TriggerBotClient.CONFIG.targetFps = Math.max(30, TriggerBotClient.CONFIG.targetFps - 5);
                TriggerBotClient.saveConfig();
                return true;
            }

            if (inside(mouseX, mouseY, contentLeft + contentWidth - 42, targetY, 28, 30)) {
                TriggerBotClient.CONFIG.targetFps = Math.min(60, TriggerBotClient.CONFIG.targetFps + 5);
                TriggerBotClient.saveConfig();
                return true;
            }
        }

        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
