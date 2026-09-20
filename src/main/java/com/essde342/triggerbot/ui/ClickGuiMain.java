package com.essde342.triggerbot.ui;

import com.essde342.triggerbot.AltManagerScreen;
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
    private static final int UI_W = 440;
    private static final int UI_H = 270;

    private static final int BACKDROP = 0xFF08090A;
    private static final int PANEL = 0xFF111316;
    private static final int PANEL_2 = 0xFF171A1E;
    private static final int CARD = 0xFF20242A;
    private static final int HOVER = 0xFF2A2F36;
    private static final int BORDER = 0xFF383F48;
    private static final int TEXT = 0xFFF0F2F4;
    private static final int MUTED = 0xFF9EA5AE;
    private static final int ACCENT = 0xFFC4C8CE;
    private static final int ACCENT_DARK = 0xFF555D67;
    private static final int OFF = 0xFF3D434B;

    private final MinecraftClient mc = MinecraftClient.getInstance();

    private Category category = Category.COMBAT;
    private Module settingsModule;
    private Module bindingModule;

    private NumberSetting draggingSlider;
    private BooleanSetting pendingBoolean;
    private boolean scrollingSettings;
    private boolean movedDuringPress;
    private double pressY;

    private double settingsScroll;
    private double settingsScrollStart;

    private float scale = 1.0F;
    private int originX;
    private int originY;

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

        // Hard reset the most important render states before drawing the opaque UI.
        DrawableHelper.fill(
                matrices,
                0,
                0,
                width,
                height,
                BACKDROP
        );

        matrices.push();
        matrices.translate(originX, originY, 0.0D);
        matrices.scale(scale, scale, 1.0F);

        int mx = localX(mouseX);
        int my = localY(mouseY);

        drawMain(matrices, mx, my);

        matrices.pop();
    }

    private void recalculateScale() {
        float fit = Math.min(
                (width - 10.0F) / UI_W,
                (height - 10.0F) / UI_H
        );

        scale = Math.min(1.0F, fit);
        if (scale < 0.45F) {
            scale = 0.45F;
        }

        originX = (int) ((width - UI_W * scale) * 0.5F);
        originY = (int) ((height - UI_H * scale) * 0.5F);
    }

    private int localX(double x) {
        return (int) ((x - originX) / scale);
    }

    private int localY(double y) {
        return (int) ((y - originY) / scale);
    }

    private void drawMain(MatrixStack matrices, int mx, int my) {
        box(matrices, 0, 0, UI_W, UI_H, PANEL);
        outline(matrices, 0, 0, UI_W, UI_H, BORDER);

        drawText(matrices, "ASTRA", 14, 11, TEXT);
        drawText(matrices, "CLIENT 1.0.0", 52, 13, MUTED);
        drawText(matrices, "RSHIFT", UI_W - 51, 13, MUTED);

        DrawableHelper.fill(
                matrices,
                0,
                37,
                UI_W,
                38,
                BORDER
        );

        if (settingsModule == null) {
            drawModuleView(matrices, mx, my);
        } else {
            drawSettingsView(matrices, mx, my);
        }
    }

    private void drawModuleView(MatrixStack matrices, int mx, int my) {
        drawCategories(matrices, mx, my);

        int listX = 12;
        int listY = 82;
        int listW = UI_W - 24;
        int listH = UI_H - 104;

        box(matrices, listX, listY, listW, listH, BACKDROP);
        outline(matrices, listX, listY, listW, listH, BORDER);

        drawText(matrices, pretty(category), listX + 10, listY + 9, TEXT);

        List<Module> modules = ModuleManager.getByCategory(category);

        int rowY = listY + 28;
        for (Module module : modules) {
            if (rowY + 36 > listY + listH - 4) {
                break;
            }

            boolean hover = inside(
                    mx,
                    my,
                    listX + 7,
                    rowY,
                    listW - 14,
                    33
            );

            boolean enabled = module.isEnabled();

            box(
                    matrices,
                    listX + 7,
                    rowY,
                    listW - 14,
                    33,
                    hover ? HOVER : CARD
            );

            if (enabled) {
                DrawableHelper.fill(
                        matrices,
                        listX + 7,
                        rowY,
                        listX + 10,
                        rowY + 33,
                        ACCENT
                );
            }

            drawText(
                    matrices,
                    fit(module.getName(), 126),
                    listX + 16,
                    rowY + 5,
                    TEXT
            );

            drawText(
                    matrices,
                    fit(module.getDesc(), 185),
                    listX + 16,
                    rowY + 19,
                    MUTED
            );

            int toggleX = listX + listW - 74;
            drawSwitch(matrices, toggleX, rowY + 7, enabled);

            int dotX = listX + listW - 20;
            drawDots(matrices, dotX, rowY + 8, settingsModule == module);

            rowY += 38;
        }

        drawText(
                matrices,
                "Tap module = ON/OFF   ••• = settings",
                14,
                UI_H - 16,
                MUTED
        );
    }

    private void drawCategories(MatrixStack matrices, int mx, int my) {
        int x = 10;
        int y = 46;
        int gap = 5;

        int count = Category.values().length;
        int w = (UI_W - 20 - gap * (count - 1)) / count;
        int h = 27;

        for (Category current : Category.values()) {
            boolean selected = current == category;
            boolean hover = inside(mx, my, x, y, w, h);

            box(
                    matrices,
                    x,
                    y,
                    w,
                    h,
                    selected ? ACCENT_DARK : (hover ? HOVER : PANEL_2)
            );

            drawCentered(
                    matrices,
                    pretty(current),
                    x + w / 2,
                    y + 9,
                    selected ? TEXT : MUTED
            );

            x += w + gap;
        }
    }

    private void drawSettingsView(MatrixStack matrices, int mx, int my) {
        int x = 10;
        int y = 46;
        int w = UI_W - 20;
        int h = UI_H - 57;

        box(matrices, x, y, w, h, BACKDROP);
        outline(matrices, x, y, w, h, BORDER);

        drawText(
                matrices,
                fit(settingsModule.getName(), w - 82),
                x + 11,
                y + 9,
                TEXT
        );

        drawText(
                matrices,
                "SETTINGS",
                x + 11,
                y + 24,
                MUTED
        );

        box(matrices, x + w - 30, y + 7, 22, 22, CARD);
        drawCentered(matrices, "X", x + w - 19, y + 14, MUTED);

        int contentX = x + 9;
        int contentW = w - 18;

        int top = y + 40;
        int bottom = y + h - 32;

        int cursor = top - (int) settingsScroll;

        for (com.essde342.triggerbot.ui.ISetting setting : settingsModule.getSettings()) {
            if (setting instanceof BooleanSetting) {
                int height = 31;

                if (cursor + height >= top && cursor <= bottom) {
                    drawBoolean(
                            matrices,
                            (BooleanSetting) setting,
                            contentX,
                            cursor,
                            contentW,
                            mx,
                            my
                    );
                }

                cursor += height + 5;
            } else if (setting instanceof NumberSetting) {
                int height = 53;

                if (cursor + height >= top && cursor <= bottom) {
                    drawNumber(
                            matrices,
                            (NumberSetting) setting,
                            contentX,
                            cursor,
                            contentW,
                            mx,
                            my
                    );
                }

                cursor += height + 7;
            }
        }

        int totalHeight = cursor - (top - (int) settingsScroll);
        int viewportHeight = bottom - top;

        if (totalHeight > viewportHeight) {
            int barX = x + w - 5;
            int barTop = top;
            int barBottom = bottom;
            int barHeight = Math.max(
                    18,
                    (int) ((viewportHeight / (double) totalHeight) * viewportHeight)
            );

            double scrollProgress = settingsScroll / getMaxSettingsScroll();
            int barY = barTop + (int) (
                    (barBottom - barTop - barHeight) * scrollProgress
            );

            box(
                    matrices,
                    barX,
                    barY,
                    3,
                    barHeight,
                    ACCENT_DARK
            );
        }

        int bindY = y + h - 25;
        box(
                matrices,
                contentX,
                bindY,
                contentW,
                20,
                PANEL_2
        );

        drawText(
                matrices,
                "BIND",
                contentX + 7,
                bindY + 6,
                MUTED
        );

        String bind = bindingModule == settingsModule
                ? "PRESS KEY"
                : settingsModule.getBind() < 0
                ? "NONE"
                : keyName(settingsModule.getBind());

        String fittedBind = fit(bind, 82);
        drawText(
                matrices,
                fittedBind,
                contentX + contentW - 7 - mc.textRenderer.getWidth(fittedBind),
                bindY + 6,
                TEXT
        );
    }

    private void drawBoolean(
            MatrixStack matrices,
            BooleanSetting setting,
            int x,
            int y,
            int w,
            int mx,
            int my
    ) {
        box(
                matrices,
                x,
                y,
                w,
                31,
                inside(mx, my, x, y, w, 31) ? HOVER : CARD
        );

        drawText(
                matrices,
                fit(setting.getName(), w - 62),
                x + 9,
                y + 9,
                TEXT
        );

        drawSwitch(
                matrices,
                x + w - 38,
                y + 7,
                setting.isEnabled()
        );
    }

    private void drawNumber(
            MatrixStack matrices,
            NumberSetting setting,
            int x,
            int y,
            int w,
            int mx,
            int my
    ) {
        box(
                matrices,
                x,
                y,
                w,
                53,
                inside(mx, my, x, y, w, 53) ? HOVER : CARD
        );

        String value = format(setting);
        drawText(
                matrices,
                fit(setting.getName(), w - 95),
                x + 9,
                y + 6,
                TEXT
        );

        drawText(
                matrices,
                value,
                x + w - 9 - mc.textRenderer.getWidth(value),
                y + 6,
                MUTED
        );

        int sliderX = x + 9;
        int sliderY = y + 34;
        int sliderW = w - 18;

        DrawableHelper.fill(
                matrices,
                sliderX,
                sliderY,
                sliderX + sliderW,
                sliderY + 5,
                OFF
        );

        double progress = (
                setting.getDoubleValue() - setting.getMin()
        ) / Math.max(
                0.000001D,
                setting.getMax() - setting.getMin()
        );

        progress = Math.max(0.0D, Math.min(1.0D, progress));

        int fill = (int) Math.round(sliderW * progress);

        if (fill > 0) {
            DrawableHelper.fill(
                    matrices,
                    sliderX,
                    sliderY,
                    sliderX + fill,
                    sliderY + 5,
                    ACCENT_DARK
            );
        }

        DrawableHelper.fill(
                matrices,
                sliderX + fill - 2,
                sliderY - 3,
                sliderX + fill + 3,
                sliderY + 8,
                ACCENT
        );
    }

    private void drawSwitch(
            MatrixStack matrices,
            int x,
            int y,
            boolean enabled
    ) {
        box(
                matrices,
                x,
                y,
                30,
                17,
                enabled ? ACCENT_DARK : OFF
        );

        DrawableHelper.fill(
                matrices,
                enabled ? x + 19 : x + 3,
                y + 4,
                enabled ? x + 26 : x + 10,
                y + 13,
                enabled ? TEXT : MUTED
        );
    }

    private void drawDots(
            MatrixStack matrices,
            int centerX,
            int y,
            boolean selected
    ) {
        box(
                matrices,
                centerX - 10,
                y - 3,
                20,
                25,
                selected ? ACCENT_DARK : PANEL_2
        );

        DrawableHelper.fill(
                matrices,
                centerX - 1,
                y + 1,
                centerX + 2,
                y + 4,
                TEXT
        );

        DrawableHelper.fill(
                matrices,
                centerX - 1,
                y + 8,
                centerX + 2,
                y + 11,
                TEXT
        );

        DrawableHelper.fill(
                matrices,
                centerX - 1,
                y + 15,
                centerX + 2,
                y + 18,
                TEXT
        );
    }

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {
        int x = localX(mouseX);
        int y = localY(mouseY);

        if (settingsModule != null) {
            if (inside(x, y, UI_W - 42, 44, 30, 31)) {
                closeSettings();
                return true;
            }

            if (handleSettingsPress(x, y)) {
                return true;
            }

            if (inside(x, y, 10, 46, UI_W - 20, UI_H - 57)) {
                scrollingSettings = true;
                pressY = y;
                settingsScrollStart = settingsScroll;
                movedDuringPress = false;
                return true;
            }

            return true;
        }

        int categoryX = 10;
        int categoryW = (UI_W - 20 - 5 * (Category.values().length - 1))
                / Category.values().length;

        for (Category current : Category.values()) {
            if (inside(x, y, categoryX, 46, categoryW, 27)) {
                category = current;
                return true;
            }

            categoryX += categoryW + 5;
        }

        int listY = 110;
        int listX = 19;
        int listW = UI_W - 38;

        for (Module module : ModuleManager.getByCategory(category)) {
            if (!inside(x, y, listX, listY, listW, 33)) {
                listY += 38;
                continue;
            }

            int dotX = listX + listW - 20;

            if (inside(x, y, dotX - 18, listY - 3, 36, 39)) {
                if (module.getName().equals("Alt Manager")) {
                    if (client != null) {
                        client.openScreen(AltManagerScreen.create(this));
                    }
                } else {
                    settingsModule = module;
                    settingsScroll = 0.0D;
                    bindingModule = null;
                }

                return true;
            }

            module.toggled();
            TriggerBotClient.saveConfig();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean handleSettingsPress(int x, int y) {
        int panelX = 10;
        int panelY = 46;
        int panelW = UI_W - 20;
        int panelH = UI_H - 57;

        int contentX = panelX + 9;
        int contentW = panelW - 18;
        int top = panelY + 40;
        int bottom = panelY + panelH - 32;

        int cursor = top - (int) settingsScroll;

        for (com.essde342.triggerbot.ui.ISetting setting : settingsModule.getSettings()) {
            if (setting instanceof BooleanSetting) {
                if (inside(x, y, contentX, cursor, contentW, 31)) {
                    pendingBoolean = (BooleanSetting) setting;
                    scrollingSettings = false;
                    movedDuringPress = false;
                    pressY = y;
                    return true;
                }

                cursor += 36;
            } else if (setting instanceof NumberSetting) {
                if (inside(x, y, contentX, cursor, contentW, 53)) {
                    draggingSlider = (NumberSetting) setting;
                    scrollingSettings = false;
                    movedDuringPress = false;
                    updateSlider(
                            draggingSlider,
                            x,
                            contentX + 9,
                            contentW - 18
                    );
                    TriggerBotClient.saveConfig();
                    return true;
                }

                cursor += 60;
            }
        }

        int bindY = panelY + panelH - 25;

        if (inside(x, y, contentX, bindY, contentW, 20)) {
            bindingModule = settingsModule;
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseDragged(
            double mouseX,
            double mouseY,
            int button,
            double deltaX,
            double deltaY
    ) {
        int x = localX(mouseX);
        int y = localY(mouseY);

        if (draggingSlider != null) {
            updateSlider(
                    draggingSlider,
                    x,
                    28,
                    404
            );
            TriggerBotClient.saveConfig();
            return true;
        }

        if (pendingBoolean != null || scrollingSettings) {
            if (Math.abs(y - pressY) > 4) {
                movedDuringPress = true;
                scrollingSettings = true;
            }

            if (scrollingSettings) {
                double next = settingsScrollStart - (y - pressY);
                settingsScroll = clamp(
                        next,
                        0.0D,
                        getMaxSettingsScroll()
                );

                return true;
            }
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(
            double mouseX,
            double mouseY,
            int button
    ) {
        if (pendingBoolean != null && !movedDuringPress) {
            pendingBoolean.toggle();
            TriggerBotClient.saveConfig();
        }

        draggingSlider = null;
        pendingBoolean = null;
        scrollingSettings = false;
        movedDuringPress = false;

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double amount
    ) {
        if (settingsModule != null) {
            settingsScroll = clamp(
                    settingsScroll - amount * 28.0D,
                    0.0D,
                    getMaxSettingsScroll()
            );
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean keyPressed(
            int keyCode,
            int scanCode,
            int modifiers
    ) {
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
            } else if (client != null) {
                client.openScreen(null);
            }

            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void closeSettings() {
        settingsModule = null;
        bindingModule = null;
        draggingSlider = null;
        pendingBoolean = null;
        scrollingSettings = false;
        movedDuringPress = false;
        settingsScroll = 0.0D;
    }

    private void updateSlider(
            NumberSetting setting,
            double mouseX,
            int sliderX,
            int sliderWidth
    ) {
        double progress = clamp(
                (mouseX - sliderX) / sliderWidth,
                0.0D,
                1.0D
        );

        double value = setting.getMin()
                + progress * (setting.getMax() - setting.getMin());

        double increment = setting.getIncrement();

        if (increment > 0.0D) {
            value = Math.round(value / increment) * increment;
        }

        setting.setValue(value);
    }

    private double getMaxSettingsScroll() {
        int total = 0;

        for (com.essde342.triggerbot.ui.ISetting setting :
                settingsModule.getSettings()) {

            if (setting instanceof BooleanSetting) {
                total += 36;
            } else if (setting instanceof NumberSetting) {
                total += 60;
            }
        }

        int viewport = (UI_H - 57) - 40 - 32;

        return Math.max(0.0D, total - viewport);
    }

    private String fit(String text, int maxWidth) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        if (mc.textRenderer.getWidth(text) <= maxWidth) {
            return text;
        }

        String dots = "...";
        int dotsWidth = mc.textRenderer.getWidth(dots);

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            String next = result.toString() + text.charAt(i);

            if (mc.textRenderer.getWidth(next) + dotsWidth > maxWidth) {
                break;
            }

            result.append(text.charAt(i));
        }

        return result + dots;
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

        if (name.contains("Range")) {
            return String.format(Locale.ROOT, "%.1f", value);
        }

        if (setting.getIncrement() >= 1.0D) {
            return String.valueOf(Math.round(value));
        }

        return String.format(Locale.ROOT, "%.1f", value);
    }

    private String keyName(int keyCode) {
        String name = GLFW.glfwGetKeyName(keyCode, 0);

        if (name == null || name.isEmpty()) {
            return String.valueOf(keyCode);
        }

        return name.toUpperCase(Locale.ROOT);
    }

    private String pretty(Category category) {
        String text = category.name().toLowerCase(Locale.ROOT);

        return Character.toUpperCase(text.charAt(0))
                + text.substring(1);
    }

    private void drawText(
            MatrixStack matrices,
            String text,
            int x,
            int y,
            int color
    ) {
        mc.textRenderer.draw(
                matrices,
                text,
                x,
                y,
                color
        );
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

    private double clamp(
            double value,
            double min,
            double max
    ) {
        return Math.max(min, Math.min(max, value));
    }

    private void box(
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

    private void outline(
            MatrixStack matrices,
            int x,
            int y,
            int width,
            int height,
            int color
    ) {
        DrawableHelper.fill(matrices, x, y, x + width, y + 1, color);
        DrawableHelper.fill(
                matrices,
                x,
                y + height - 1,
                x + width,
                y + height,
                color
        );
        DrawableHelper.fill(matrices, x, y, x + 1, y + height, color);
        DrawableHelper.fill(
                matrices,
                x + width - 1,
                y,
                x + width,
                y + height,
                color
        );
    }
}
