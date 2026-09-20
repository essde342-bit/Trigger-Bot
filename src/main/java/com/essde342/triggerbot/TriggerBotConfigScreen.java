package com.essde342.triggerbot;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

public final class TriggerBotConfigScreen extends Screen {
    private final Screen parent;

    private TriggerBotConfigScreen(Screen parent) {
        super(new LiteralText("Trigger Bot Settings"));
        this.parent = parent;
    }

    public static Screen create(Screen parent) {
        return new TriggerBotConfigScreen(parent);
    }

    @Override
    protected void init() {
        int left = this.width / 2 - 154;
        int right = this.width / 2 + 4;
        int y = 38;

        addToggle(left, y, "TriggerBot", TriggerBotClient.CONFIG.enabled,
                value -> {
                    TriggerBotClient.CONFIG.enabled = value;
                    save();
                });
        addToggle(right, y, "Only crits", TriggerBotClient.CONFIG.onlyCrits,
                value -> {
                    TriggerBotClient.CONFIG.onlyCrits = value;
                    save();
                });

        y += 24;
        addToggle(left, y, "Smart crits", TriggerBotClient.CONFIG.smartCrits,
                value -> {
                    TriggerBotClient.CONFIG.smartCrits = value;
                    save();
                });
        addToggle(right, y, "Only weapon", TriggerBotClient.CONFIG.onlyWeapon,
                value -> {
                    TriggerBotClient.CONFIG.onlyWeapon = value;
                    save();
                });

        y += 24;
        addToggle(left, y, "Aim Assist", TriggerBotClient.CONFIG.aimAssist,
                value -> {
                    TriggerBotClient.CONFIG.aimAssist = value;
                    save();
                });
        addToggle(right, y, "Lightning ESP", TriggerBotClient.CONFIG.lightningEsp,
                value -> {
                    TriggerBotClient.CONFIG.lightningEsp = value;
                    save();
                });

        y += 24;
        addToggle(left, y, "Fullbright", TriggerBotClient.CONFIG.fullbright,
                value -> {
                    TriggerBotClient.setFullbright(MinecraftClient.getInstance(), value);
                    save();
                });
        addToggle(right, y, "No Hurt Cam", TriggerBotClient.CONFIG.noHurtCam,
                value -> {
                    TriggerBotClient.CONFIG.noHurtCam = value;
                    save();
                });

        y += 24;
        addToggle(left, y, "Optimization", TriggerBotClient.CONFIG.optimization,
                value -> {
                    TriggerBotOptimizer.setOptimization(MinecraftClient.getInstance(), value);
                    save();
                });
        addButton(new ButtonWidget(
                right, y, 150, 20,
                new LiteralText(profileText()),
                button -> {
                    TriggerBotClient.CONFIG.optimizationLevel =
                            (TriggerBotClient.CONFIG.optimizationLevel + 1) % 3;
                    if (TriggerBotClient.CONFIG.optimization) {
                        TriggerBotOptimizer.setOptimization(MinecraftClient.getInstance(), true);
                    }
                    button.setMessage(new LiteralText(profileText()));
                    save();
                }));

        y += 24;
        addButton(new ButtonWidget(
                left, y, 150, 20,
                new LiteralText(adaptiveText()),
                button -> {
                    TriggerBotClient.CONFIG.adaptiveOptimization =
                            !TriggerBotClient.CONFIG.adaptiveOptimization;
                    button.setMessage(new LiteralText(adaptiveText()));
                    save();
                }));
        addButton(new ButtonWidget(
                right, y, 150, 20,
                new LiteralText(targetFpsText()),
                button -> {
                    int fps = TriggerBotClient.CONFIG.targetFps;
                    TriggerBotClient.CONFIG.targetFps = fps == 30 ? 45 : (fps == 45 ? 60 : 30);
                    button.setMessage(new LiteralText(targetFpsText()));
                    save();
                }));

        y += 24;
        addButton(new AspectRatioSlider(
                this.width / 2 - 154, y, 304, 20,
                TriggerBotClient.CONFIG.aspectRatio));

        y += 24;
        addButton(new ButtonWidget(
                left, y, 150, 20,
                new LiteralText(gammaText()),
                button -> {
                    double gamma = TriggerBotClient.CONFIG.fullbrightGamma;
                    TriggerBotClient.CONFIG.fullbrightGamma =
                            gamma < 10.0D ? 10.0D : (gamma < 15.0D ? 15.0D : (gamma < 20.0D ? 20.0D : 5.0D));
                    if (TriggerBotClient.CONFIG.fullbright) {
                        TriggerBotClient.setFullbright(MinecraftClient.getInstance(), true);
                    }
                    button.setMessage(new LiteralText(gammaText()));
                    save();
                }));
        addButton(new ButtonWidget(
                right, y, 150, 20,
                new LiteralText("Alt Manager"),
                button -> MinecraftClient.getInstance().openScreen(AltManagerScreen.create(this))));

        addButton(new ButtonWidget(
                this.width / 2 - 75,
                this.height - 28,
                150,
                20,
                new LiteralText("Done"),
                button -> this.onClose()));
    }

    private ButtonWidget addToggle(
            int x,
            int y,
            String label,
            boolean initial,
            ToggleConsumer consumer
    ) {
        return this.addButton(new ButtonWidget(
                x,
                y,
                150,
                20,
                new LiteralText(toggleText(label, initial)),
                button -> {
                    boolean next = !isButtonEnabled(label);
                    consumer.accept(next);
                    button.setMessage(new LiteralText(toggleText(label, next)));
                }));
    }

    private boolean isButtonEnabled(String label) {
        if ("TriggerBot".equals(label)) {
            return TriggerBotClient.CONFIG.enabled;
        }
        if ("Only crits".equals(label)) {
            return TriggerBotClient.CONFIG.onlyCrits;
        }
        if ("Smart crits".equals(label)) {
            return TriggerBotClient.CONFIG.smartCrits;
        }
        if ("Only weapon".equals(label)) {
            return TriggerBotClient.CONFIG.onlyWeapon;
        }
        if ("Aim Assist".equals(label)) {
            return TriggerBotClient.CONFIG.aimAssist;
        }
        if ("Lightning ESP".equals(label)) {
            return TriggerBotClient.CONFIG.lightningEsp;
        }
        if ("Fullbright".equals(label)) {
            return TriggerBotClient.CONFIG.fullbright;
        }
        if ("No Hurt Cam".equals(label)) {
            return TriggerBotClient.CONFIG.noHurtCam;
        }
        return TriggerBotClient.CONFIG.optimization;
    }

    private static String toggleText(String label, boolean value) {
        return label + ": " + (value ? "ON" : "OFF");
    }

    private static String profileText() {
        switch (Math.max(0, Math.min(2, TriggerBotClient.CONFIG.optimizationLevel))) {
            case 2:
                return "Profile: Extreme";
            case 1:
                return "Profile: Performance";
            default:
                return "Profile: Balanced";
        }
    }

    private static String adaptiveText() {
        return "Adaptive FPS: " + (TriggerBotClient.CONFIG.adaptiveOptimization ? "ON" : "OFF");
    }

    private static String targetFpsText() {
        return "Target FPS: " + TriggerBotClient.CONFIG.targetFps;
    }

    private static String gammaText() {
        return "Gamma: " + (int) TriggerBotClient.CONFIG.fullbrightGamma;
    }

    private static final class AspectRatioSlider extends SliderWidget {
        private AspectRatioSlider(int x, int y, int width, int height, double ratio) {
            super(x, y, width, height, new LiteralText(""), (ratio - 0.50D) / 2.50D);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            double ratio = 0.50D + this.value * 2.50D;
            this.setMessage(new LiteralText("Aspect Ratio: "
                    + String.format(java.util.Locale.ROOT, "%.2f", ratio)));
        }

        @Override
        protected void applyValue() {
            TriggerBotClient.CONFIG.aspectRatio =
                    Math.max(0.50D, Math.min(3.00D, 0.50D + this.value * 2.50D));
            save();
        }
    }

    private static void save() {
        TriggerBotClient.saveConfig();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        drawCenteredText(
                matrices,
                this.textRenderer,
                new LiteralText("Trigger Bot Settings"),
                this.width / 2,
                10,
                0xFFFFFF
        );

        drawCenteredText(
                matrices,
                this.textRenderer,
                new LiteralText("Combat / Visual / Mobile optimization"),
                this.width / 2,
                23,
                0xAAAAAA
        );

        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        save();
        if (this.client != null) {
            this.client.openScreen(this.parent);
        }
    }

    @FunctionalInterface
    private interface ToggleConsumer {
        void accept(boolean value);
    }
}
