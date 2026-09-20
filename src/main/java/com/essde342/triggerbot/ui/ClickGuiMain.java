package com.essde342.triggerbot.ui;

import com.essde342.triggerbot.TriggerBotClient;
import com.essde342.triggerbot.ui.imple.BooleanSetting;
import com.essde342.triggerbot.ui.imple.NumberSetting;
import com.essde342.triggerbot.ui.modules.Category;
import com.essde342.triggerbot.ui.modules.Module;
import com.essde342.triggerbot.ui.modules.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Locale;

public final class ClickGuiMain extends Screen {
    private static final int BASE_W = 460;
    private static final int BASE_H = 270;

    private static final int BLACK = 0xFF090A0C;
    private static final int PANEL = 0xFF121417;
    private static final int PANEL_2 = 0xFF191C20;
    private static final int CARD = 0xFF20242A;
    private static final int HOVER = 0xFF292E35;
    private static final int BORDER = 0xFF353B43;
    private static final int TEXT = 0xFFF1F3F5;
    private static final int MUTED = 0xFF9CA3AD;
    private static final int ACCENT = 0xFFB8BDC5;
    private static final int ACCENT_DARK = 0xFF515963;
    private static final int OFF = 0xFF3C424A;

    private final MinecraftClient mc = MinecraftClient.getInstance();

    private Category category = Category.COMBAT;
    private Module settingsModule;
    private Module bindingModule;
    private NumberSetting dragging;
    private double settingsScroll = 0.0D;

    private float scale = 1.0F;
    private int ox;
    private int oy;

    public ClickGuiMain() {
        super(Text.of("Astra Client"));
        ModuleManager.moduleRegister();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        recalculateScale();

        // Full opaque background. Never call vanilla renderBackground here.
        DrawableHelper.fill(matrices, 0, 0, width, height, BLACK);

        matrices.push();
        matrices.translate(ox, oy, 0.0D);
        matrices.scale(scale, scale, 1.0F);

        int mx = localX(mouseX);
        int my = localY(mouseY);

        drawFrame(matrices);

        if (settingsModule == null) {
            drawModuleScreen(matrices, mx, my);
        } else {
            drawSettingsScreen(matrices, mx, my);
        }

        matrices.pop();
    }

    private void recalculateScale() {
        scale = Math.min(
                1.0F,
                Math.min(
                        (width - 12.0F) / (float) BASE_W,
                        (height - 12.0F) / (float) BASE_H
                )
        );

        if (scale < 0.55F) {
            scale = 0.55F;
        }

        ox = (int) ((width - BASE_W * scale) * 0.5F);
        oy = (int) ((height - BASE_H * scale) * 0.5F);
    }

    private int localX(double mouseX) {
        return (int) ((mouseX - ox) / scale);
    }

    private int localY(double mouseY) {
        return (int) ((mouseY - oy) / scale);
    }

    private void drawFrame(MatrixStack matrices) {
        panel(matrices, 0, 0, BASE_W, BASE_H, PANEL);
        border(matrices, 0, 0, BASE_W, BASE_H, BORDER);

        drawText(matrices, "ASTRA", 16, 12, TEXT);
        drawText(matrices, "CLIENT 1.0.0", 55, 14, MUTED);
        drawText(matrices, "RSHIFT", BASE_W - 48, 14, MUTED);

        DrawableHelper.fill(matrices, 0, 39, BASE_W, 40, BORDER);
    }

    private void drawModuleScreen(MatrixStack matrices, int mx, int my) {
        drawCategoryBar(matrices, mx, my);
        drawModules(matrices, mx, my);

        drawText(matrices, "Tap ••• for settings", 16, BASE_H - 16, MUTED);
        drawText(matrices, "ESC", BASE_W - 27, BASE_H - 16, MUTED);
    }

    private void drawCategoryBar(MatrixStack matrices, int mx, int my) {
        int x = 12;
        int y = 47;
        int w = 76;
        int h = 28;
        int gap = 5;

        for (Category current : Category.values()) {
            boolean selected = current == category;
            boolean hovered = inside(mx, my, x, y, w, h);

            panel(
                    matrices,
                    x,
                    y,
                    w,
                    h,
                    selected ? ACCENT_DARK : (hovered ? HOVER : PANEL_2)
            );

            drawCentered(
                    matrices,
                    pretty(current),
                    x + w / 2,
                    y + 10,
                    selected ? TEXT : MUTED
            );

            x += w + gap;
        }
    }

    private void drawModules(MatrixStack matrices, int mx, int my) {
        int x = 12;
        int y = 81;
        int w = BASE_W - 24;
        int h = BASE_H - 102;

        panel(matrices, x, y, w, h, BLACK);
        border(matrices, x, y, w, h, BORDER);

        drawText(matrices, pretty(category), x + 10, y + 9, TEXT);

        List<Module> modules = ModuleManager.getByCategory(category);
        int rowY = y + 28;

        for (Module module : modules) {
            if (rowY + 38 > y + h - 5) {
                break;
            }

            boolean hovered = inside(mx, my, x + 7, rowY, w - 14, 35);
            boolean enabled = module.isEnabled();

            panel(
                    matrices,
                    x + 7,
                    rowY,
                    w - 14,
                    35,
                    hovered ? HOVER : CARD
            );

            if (enabled) {
                DrawableHelper.fill(
                        matrices,
                        x + 7,
                        rowY,
                        x + 10,
                        rowY + 35,
                        ACCENT
                );
            }

            drawText(
                    matrices,
                    fit(module.getName(), 125),
                    x + 16,
                    rowY + 6,
                    TEXT
            );

            drawText(
                    matrices,
                    fit(module.getDesc(), 210),
                    x + 16,
                    rowY + 21,
                    MUTED
            );

            int toggleX = x + w - 82;
            drawSwitch(matrices, toggleX, rowY + 8, enabled);

            int dotsX = x + w - 20;
            drawDots(matrices, dotsX, rowY + 10, settingsModule == module);

            rowY += 40;
        }
    }

    private void drawSettingsScreen(MatrixStack matrices, int mx, int my) {
        int x = 12;
        int y = 47;
        int w = BASE_W - 24;
        int h = BASE_H - 66;

        panel(matrices, x, y, w, h, BLACK);
        border(matrices, x, y, w, h, BORDER);

        drawText(matrices, settingsModule.getName(), x + 12, y + 10, TEXT);
        drawText(matrices, "SETTINGS", x + 12, y + 25, MUTED);

        panel(matrices, x + w - 30, y + 7, 22, 22, CARD);
        drawCentered(matrices, "X", x + w - 19, y + 14, MUTED);

        int contentX = x + 10;
        int contentW = w - 20;
        int top = y + 45;
        int bottom = y + h - 35;

        int cursor = top - (int) settingsScroll;

        for (ISetting setting : settingsModule.getSettings()) {
            if (setting instanceof BooleanSetting) {
                if (cursor + 30 >= top && cursor <= bottom) {
                    drawBooleanSetting(
                            matrices,
                            (BooleanSetting) setting,
                            contentX,
                            cursor,
                            contentW,
                            mx,
                            my
                    );
                }
                cursor += 36;
            } else if (setting instanceof NumberSetting) {
                if (cursor + 54 >= top && cursor <= bottom) {
                    drawNumberSetting(
                            matrices,
                            (NumberSetting) setting,
                            contentX,
                            cursor,
                            contentW,
                            mx,
                            my
                    );
                }
                cursor += 60;
            }
        }

        int bindY = y + h - 28;
        panel(matrices, contentX, bindY, contentW, 22, PANEL_2);

        drawText(matrices, "BIND", contentX + 8, bindY + 7, MUTED);

        String bind = bindingModule == settingsModule
                ? "PRESS KEY"
                : settingsModule.getBind() < 0
                ? "NONE"
                : keyName(settingsModule.getBind());

        int bindWidth = mc.textRenderer.getWidth(bind);
        drawText(
                matrices,
                fit(bind, 75),
                contentX + contentW - 8 - bindWidth,
                bindY + 7,
                TEXT
        );

        drawText(matrices, "ESC  BACK", x + 10, y + h - 9, MUTED);
    }

    private void drawBooleanSetting(
            MatrixStack matrices,
            BooleanSetting setting,
            int x,
            int y,
            int w,
            int mx,
            int my
    ) {
        if (y < 85 || y + 30 > BASE_H - 45) {
            return;
        }

        boolean hovered = inside(mx, my, x, y, w, 30);
        panel(matrices, x, y, w, 30, hovered ? HOVER : CARD);

        drawText(matrices, fit(setting.getName(), w - 70), x + 9, y + 9, TEXT);

        drawSwitch(matrices, x + w - 44, y + 6, setting.isEnabled());
    }

    private void drawNumberSetting(
            MatrixStack matrices,
            NumberSetting setting,
            int x,
            int y,
            int w,
            int mx,
            int my
    ) {
        if (y < 85 || y + 54 > BASE_H - 45) {
            return;
        }

        boolean hovered = inside(mx, my, x, y, w, 54);
        panel(matrices, x, y, w, 54, hovered ? HOVER : CARD);

        String value = format(setting);
        drawText(matrices, fit(setting.getName(), w - 90), x + 9, y + 7, TEXT);

        int valueWidth = mc.textRenderer.getWidth(value);
        drawText(matrices, value, x + w - 9 - valueWidth, y + 7, MUTED);

        int sliderX = x + 9;
        int sliderY = y + 34;
        int sliderW = w - 18;

        DrawableHelper.fill(matrices, sliderX, sliderY, sliderX + sliderW, sliderY + 5, OFF);

        double progress = (setting.getDoubleValue() - setting.getMin())
                / Math.max(0.000001D, setting.getMax() - setting.getMin());

        progress = Math.max(0.0D, Math.min(1.0D, progress));

        int filled = (int) Math.round(sliderW * progress);

        if (filled > 0) {
            DrawableHelper.fill(
                    matrices,
                    sliderX,
                    sliderY,
                    sliderX + filled,
                    sliderY + 5,
                    ACCENT_DARK
            );
        }

        int knob = sliderX + filled;
        DrawableHelper.fill(matrices, knob - 2, sliderY - 3, knob + 3, sliderY + 8, ACCENT);
    }

    private void drawSwitch(MatrixStack matrices, int x, int y, boolean enabled) {
        panel(matrices, x, y, 30, 18, enabled ? ACCENT_DARK : OFF);

        DrawableHelper.fill(
                matrices,
                enabled ? x + 18 : x + 3,
                y + 4,
                enabled ? x + 26 : x + 11,
                y + 14,
                enabled ? TEXT : MUTED
        );
    }

    private void drawDots(MatrixStack matrices, int x, int y, boolean selected) {
        panel(matrices, x - 7, y - 4, 14, 22, selected ? ACCENT_DARK : PANEL_2);

        DrawableHelper.fill(matrices, x - 1, y, x + 2, y + 3, TEXT);
        DrawableHelper.fill(matrices, x - 1, y + 6, x + 2, y + 9, TEXT);
        DrawableHelper.fill(matrices, x - 1, y + 12, x + 2, y + 15, TEXT);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = localX(mouseX);
        int y = localY(mouseY);

        if (settingsModule != null) {
            if (inside(x, y, BASE_W - 54, 51, 28, 28)) {
                closeSettings();
                return true;
            }

            int contentX = 22;
            int contentW = BASE_W - 44;
            int cursor = 92 - (int) settingsScroll;

            for (ISetting setting : settingsModule.getSettings()) {
                if (setting instanceof BooleanSetting) {
                    if (inside(x, y, contentX, cursor, contentW, 30)) {
                        ((BooleanSetting) setting).toggle();
                        TriggerBotClient.saveConfig();
                        return true;
                    }
                    cursor += 36;
                } else if (setting instanceof NumberSetting) {
                    if (inside(x, y, contentX, cursor, contentW, 54)) {
                        dragging = (NumberSetting) setting;
                        updateSlider(dragging, x, contentX + 9, contentW - 18);
                        TriggerBotClient.saveConfig();
                        return true;
                    }
                    cursor += 60;
                }
            }

            int bindY = BASE_H - 47;
            if (inside(x, y, 22, bindY, contentW, 22)) {
                bindingModule = settingsModule;
                return true;
            }

            return true;
        }

        int categoryX = 12;
        for (Category current : Category.values()) {
            if (inside(x, y, categoryX, 47, 76, 28)) {
                category = current;
                return true;
            }
            categoryX += 81;
        }

        int rowY = 109;
        int cardX = 19;
        int cardW = BASE_W - 38;

        for (Module module : ModuleManager.getByCategory(category)) {
            if (inside(x, y, cardX, rowY, cardW, 35)) {
                int dotsX = cardX + cardW - 20;

                if (inside(x, y, dotsX - 10, rowY - 2, 20, 39)) {
                    settingsModule = module;
                    settingsScroll = 0.0D;
                    bindingModule = null;
                    dragging = null;
                    return true;
                }

                module.toggled();
                TriggerBotClient.saveConfig();
                return true;
            }

            rowY += 40;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(
            double mouseX,
            double mouseY,
            int button,
            double deltaX,
            double deltaY
    ) {
        if (dragging != null) {
            int x = localX(mouseX);
            updateSlider(dragging, x, 31, BASE_W - 56);
            TriggerBotClient.saveConfig();
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (settingsModule != null) {
            settingsScroll -= amount * 24.0D;

            int total = 0;
            for (ISetting setting : settingsModule.getSettings()) {
                total += setting instanceof NumberSetting ? 60 : 36;
            }

            double maxScroll = Math.max(
                    0.0D,
                    total - (BASE_H - 110)
            );

            settingsScroll = Math.max(
                    0.0D,
                    Math.min(maxScroll, settingsScroll)
            );

            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (bindingModule != null) {
            bindingModule.setBind(
                    keyCode == GLFW.GLFW_KEY_ESCAPE ? -1 : keyCode
            );
            TriggerBotClient.saveConfig();
            bindingModule = null;
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (settingsModule != null) {
                closeSettings();
                return true;
            }

            if (client != null) {
                client.openScreen(null);
            }

            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void closeSettings() {
        settingsModule = null;
        bindingModule = null;
        dragging = null;
        settingsScroll = 0.0D;
    }

    private void updateSlider(NumberSetting setting, double mouseX, int sliderX, int sliderWidth) {
        double progress = Math.max(
                0.0D,
                Math.min(1.0D, (mouseX - sliderX) / sliderWidth)
        );

        double value = setting.getMin()
                + progress * (setting.getMax() - setting.getMin());

        double increment = setting.getIncrement();

        if (increment > 0.0D) {
            value = Math.round(value / increment) * increment;
        }

        setting.setValue(value);
    }

    private String fit(String value, int maxWidth) {
        if (value == null || value.isEmpty()) {
            return "";
        }

        if (mc.textRenderer.getWidth(value) <= maxWidth) {
            return value;
        }

        String dots = "...";
        int dotsWidth = mc.textRenderer.getWidth(dots);

        if (dotsWidth >= maxWidth) {
            return "";
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < value.length(); i++) {
            String next = builder.toString() + value.charAt(i);

            if (mc.textRenderer.getWidth(next) + dotsWidth > maxWidth) {
                break;
            }

            builder.append(value.charAt(i));
        }

        return builder + dots;
    }

    private String format(NumberSetting setting) {
        double value = setting.getDoubleValue();
        String name = setting.getName();

        if (name.contains("Time") || name.contains("Delay")) {
            return Math.round(value) + " ms";
        }

        if (name.contains("FPS")) {
            return Math.round(value) + " FPS";
        }

        if (name.contains("Ratio")) {
            return String.format(Locale.ROOT, "%.2f", value);
        }

        if (name.contains("Gamma")) {
            return String.format(Locale.ROOT, "%.0f", value);
        }

        if (setting.getIncrement() >= 1.0D) {
            return String.valueOf(Math.round(value));
        }

        return String.format(Locale.ROOT, "%.1f", value);
    }

    private String keyName(int keyCode) {
        if (keyCode < 0) {
            return "NONE";
        }

        String name = GLFW.glfwGetKeyName(keyCode, 0);
        return name == null
                ? String.valueOf(keyCode)
                : name.toUpperCase(Locale.ROOT);
    }

    private String pretty(Category value) {
        String text = value.name().toLowerCase(Locale.ROOT);
        return Character.toUpperCase(text.charAt(0))
                + text.substring(1);
    }

    private void drawText(MatrixStack matrices, String text, int x, int y, int color) {
        mc.textRenderer.draw(matrices, text, x, y, color);
    }

    private void drawCentered(
            MatrixStack matrices,
            String text,
            int centerX,
            int y,
            int color
    ) {
        drawText(
                matrices,
                text,
                centerX - mc.textRenderer.getWidth(text) / 2,
                y,
                color
        );
    }

    private boolean inside(
            double mouseX,
            double mouseY,
            int x,
            int y,
            int width,
            int height
    ) {
        return mouseX >= x
                && mouseX <= x + width
                && mouseY >= y
                && mouseY <= y + height;
    }

    private void panel(
            MatrixStack matrices,
            int x,
            int y,
            int width,
            int height,
            int color
    ) {
        DrawableHelper.fill(
                matrices,
                x,
                y,
                x + width,
                y + height,
                color
        );
    }

    private void border(
            MatrixStack matrices,
            int x,
            int y,
            int width,
            int height,
            int color
    ) {
        DrawableHelper.fill(matrices, x, y, x + width, y + 1, color);
        DrawableHelper.fill(matrices, x, y + height - 1, x + width, y + height, color);
        DrawableHelper.fill(matrices, x, y, x + 1, y + height, color);
        DrawableHelper.fill(matrices, x + width - 1, y, x + width, y + height, color);
    }
}
