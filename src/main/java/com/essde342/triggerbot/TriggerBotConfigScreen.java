package com.essde342.triggerbot;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

/**
 * Dependency-free configuration screen.
 *
 * This intentionally uses only vanilla 1.16.5 GUI classes so PojavLauncher
 * does not have to load an external configuration UI library during startup.
 */
public final class TriggerBotConfigScreen extends Screen {
    private final Screen parent;

    private ButtonWidget triggerBotButton;
    private ButtonWidget onlyCritsButton;
    private ButtonWidget onlyWeaponButton;
    private ButtonWidget fullbrightButton;
    private ButtonWidget noHurtCamButton;
    private ButtonWidget optimizationButton;
    private ButtonWidget profileButton;
    private ButtonWidget adaptiveButton;
    private ButtonWidget targetFpsButton;
    private ButtonWidget gammaButton;

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
        int y = 42;

        triggerBotButton = addToggle(left, y, "TriggerBot", TriggerBotClient.CONFIG.enabled,
                value -> {
                    TriggerBotClient.CONFIG.enabled = value;
                    save();
                });
        onlyCritsButton = addToggle(right, y, "Only crits", TriggerBotClient.CONFIG.onlyCrits,
                value -> {
                    TriggerBotClient.CONFIG.onlyCrits = value;
                    save();
                });

        y += 26;
        onlyWeaponButton = addToggle(left, y, "Only weapon", TriggerBotClient.CONFIG.onlyWeapon,
                value -> {
                    TriggerBotClient.CONFIG.onlyWeapon = value;
                    save();
                });
        fullbrightButton = addToggle(right, y, "Fullbright", TriggerBotClient.CONFIG.fullbright,
                value -> {
                    TriggerBotClient.setFullbright(MinecraftClient.getInstance(), value);
                    save();
                });

        y += 26;
        noHurtCamButton = addToggle(left, y, "No Hurt Cam", TriggerBotClient.CONFIG.noHurtCam,
                value -> {
                    TriggerBotClient.CONFIG.noHurtCam = value;
                    save();
                });
        optimizationButton = addToggle(right, y, "Optimization", TriggerBotClient.CONFIG.optimization,
                value -> {
                    TriggerBotOptimizer.setOptimization(MinecraftClient.getInstance(), value);
                    save();
                });

        y += 26;
        profileButton = this.addButton(new ButtonWidget(
                left, y, 150, 20,
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

        adaptiveButton = this.addButton(new ButtonWidget(
                right, y, 150, 20,
                new LiteralText(adaptiveText()),
                button -> {
                    TriggerBotClient.CONFIG.adaptiveOptimization =
                            !TriggerBotClient.CONFIG.adaptiveOptimization;
                    button.setMessage(new LiteralText(adaptiveText()));
                    save();
                }));

        y += 26;
        targetFpsButton = this.addButton(new ButtonWidget(
                left, y, 150, 20,
                new LiteralText(targetFpsText()),
                button -> {
                    int fps = TriggerBotClient.CONFIG.targetFps;
                    TriggerBotClient.CONFIG.targetFps = fps == 30 ? 45 : (fps == 45 ? 60 : 30);
                    button.setMessage(new LiteralText(targetFpsText()));
                    save();
                }));

        gammaButton = this.addButton(new ButtonWidget(
                right, y, 150, 20,
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

        this.addButton(new ButtonWidget(
                this.width / 2 - 75,
                this.height - 30,
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
                    boolean next = !isButtonEnabled(button, label);
                    consumer.accept(next);
                    button.setMessage(new LiteralText(toggleText(label, next)));
                }));
    }

    private boolean isButtonEnabled(ButtonWidget button, String label) {
        if ("TriggerBot".equals(label)) {
            return TriggerBotClient.CONFIG.enabled;
        }
        if ("Only crits".equals(label)) {
            return TriggerBotClient.CONFIG.onlyCrits;
        }
        if ("Only weapon".equals(label)) {
            return TriggerBotClient.CONFIG.onlyWeapon;
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
        String profile;
        switch (Math.max(0, Math.min(2, TriggerBotClient.CONFIG.optimizationLevel))) {
            case 2:
                profile = "Extreme";
                break;
            case 1:
                profile = "Performance";
                break;
            default:
                profile = "Balanced";
                break;
        }
        return "Profile: " + profile;
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
                14,
                0xFFFFFF
        );

        drawCenteredText(
                matrices,
                this.textRenderer,
                new LiteralText("Combat / Visual / Mobile optimization"),
                this.width / 2,
                28,
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
