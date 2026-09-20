package com.essde342.triggerbot.ui;

import com.essde342.triggerbot.TriggerBotClient;
import com.essde342.triggerbot.ui.imple.BooleanSetting;
import com.essde342.triggerbot.ui.imple.NumberSetting;
import com.essde342.triggerbot.ui.modules.Category;
import com.essde342.triggerbot.ui.modules.Module;
import com.essde342.triggerbot.ui.modules.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.util.List;
import java.util.Locale;

public final class ClickGuiMain extends Screen {
    private static final int W = 760;
    private static final int H = 440;

    private static final Color BLACK = new Color(10, 10, 11, 255);
    private static final Color GRAPHITE = new Color(23, 23, 24, 255);
    private static final Color GRAPHITE_2 = new Color(30, 30, 31, 255);
    private static final Color GRAPHITE_3 = new Color(38, 38, 39, 255);
    private static final Color HOVER = new Color(47, 47, 48, 255);
    private static final Color LINE = new Color(60, 60, 61, 255);
    private static final Color TEXT = new Color(238, 238, 239, 255);
    private static final Color MUTED = new Color(150, 150, 153, 255);
    private static final Color ACCENT = new Color(205, 205, 208, 255);
    private static final Color ACCENT_DARK = new Color(88, 88, 91, 255);
    private static final Color OFF = new Color(71, 71, 73, 255);

    private final MinecraftClient mc = MinecraftClient.getInstance();

    private Category category = Category.COMBAT;
    private Module settingsModule;
    private Module bindingModule;
    private NumberSetting dragging;

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
        resize();

        // Entire UI is opaque. There is deliberately no translucent vanilla screen behind it.
        ClickGuiRender.rect(0, 0, width, height, BLACK);

        matrices.push();
        matrices.translate(originX, originY, 0.0D);
        matrices.scale(scale, scale, 1.0F);

        int mx = localX(mouseX);
        int my = localY(mouseY);

        drawMain(matrices, mx, my);

        if (settingsModule != null) {
            drawSettings(matrices, mx, my);
        }

        matrices.pop();
    }

    private void resize() {
        scale = Math.min(
                1.0F,
                Math.min((width - 12.0F) / W, (height - 12.0F) / H)
        );

        if (scale < 0.55F) {
            scale = 0.55F;
        }

        originX = (int) ((width - W * scale) / 2.0F);
        originY = (int) ((height - H * scale) / 2.0F);
    }

    private int localX(double x) {
        return (int) ((x - originX) / scale);
    }

    private int localY(double y) {
        return (int) ((y - originY) / scale);
    }

    private void drawMain(MatrixStack matrices, int mx, int my) {
        ClickGuiRender.rounded(0, 0, W, H, 12, GRAPHITE);
        ClickGuiRender.border(0, 0, W, H, 12, LINE);

        drawText(matrices, "ASTRA CLIENT", 20, 16, TEXT.getRGB());
        drawText(matrices, "1.0.0", 122, 18, MUTED.getRGB());

        drawText(matrices, "RIGHT SHIFT", W - 104, 18, MUTED.getRGB());
        ClickGuiRender.rect(0, 52, W, 1, LINE);

        drawSidebar(matrices, mx, my);
        drawModuleList(matrices, mx, my);

        drawText(matrices, "ESC  CLOSE", 20, H - 20, MUTED.getRGB());
    }

    private void drawSidebar(MatrixStack matrices, int mx, int my) {
        int x = 14;
        int y = 67;
        int w = 128;
        int h = 341;

        ClickGuiRender.rounded(x, y, w, h, 9, BLACK);
        ClickGuiRender.border(x, y, w, h, 9, LINE);

        drawText(matrices, "CATEGORY", x + 12, y + 11, MUTED.getRGB());

        int rowY = y + 34;
        for (Category current : Category.values()) {
            boolean selected = current == category;
            boolean hovered = inside(mx, my, x + 8, rowY, w - 16, 40);

            ClickGuiRender.rounded(
                    x + 8,
                    rowY,
                    w - 16,
                    40,
                    7,
                    selected ? ACCENT_DARK : hovered ? HOVER : GRAPHITE
            );

            if (selected) {
                ClickGuiRender.rect(x + 8, rowY, 4, 40, ACCENT);
            }

            drawText(
                    matrices,
                    pretty(current),
                    x + 20,
                    rowY + 13,
                    selected ? TEXT.getRGB() : MUTED.getRGB()
            );

            rowY += 48;
        }
    }

    private void drawModuleList(MatrixStack matrices, int mx, int my) {
        int x = 154;
        int y = 67;
        int w = 592;
        int h = 341;

        ClickGuiRender.rounded(x, y, w, h, 9, BLACK);
        ClickGuiRender.border(x, y, w, h, 9, LINE);

        drawText(matrices, pretty(category), x + 14, y + 11, TEXT.getRGB());
        drawText(matrices, "MODULES", x + 14, y + 28, MUTED.getRGB());

        List<Module> modules = ModuleManager.getByCategory(category);
        int cardY = y + 48;

        if (modules.isEmpty()) {
            drawText(matrices, "No modules", x + 14, cardY + 12, MUTED.getRGB());
            return;
        }

        for (Module module : modules) {
            if (cardY + 54 > y + h - 6) {
                break;
            }

            boolean hovered = inside(mx, my, x + 10, cardY, w - 20, 48);
            boolean enabled = module.isEnabled();

            ClickGuiRender.rounded(
                    x + 10,
                    cardY,
                    w - 20,
                    48,
                    7,
                    hovered ? HOVER : GRAPHITE_2
            );

            if (enabled) {
                ClickGuiRender.rect(x + 10, cardY, 4, 48, ACCENT);
            }

            drawText(
                    matrices,
                    fit(module.getName(), 210),
                    x + 22,
                    cardY + 8,
                    TEXT.getRGB()
            );

            drawText(
                    matrices,
                    fit(module.getDesc(), 320),
                    x + 22,
                    cardY + 26,
                    MUTED.getRGB()
            );

            int switchX = x + w - 114;
            ClickGuiRender.rounded(
                    switchX,
                    cardY + 12,
                    42,
                    24,
                    12,
                    enabled ? ACCENT_DARK : OFF
            );

            ClickGuiRender.circle(
                    enabled ? switchX + 32 : switchX + 10,
                    cardY + 24,
                    7,
                    enabled ? TEXT : new Color(155, 155, 158, 255)
            );

            int menuX = x + w - 44;
            ClickGuiRender.rounded(
                    menuX - 18,
                    cardY + 7,
                    36,
                    34,
                    7,
                    settingsModule == module ? ACCENT_DARK : GRAPHITE_3
            );

            ClickGuiRender.circle(menuX, cardY + 16, 2, TEXT);
            ClickGuiRender.circle(menuX, cardY + 24, 2, TEXT);
            ClickGuiRender.circle(menuX, cardY + 32, 2, TEXT);

            cardY += 57;
        }
    }

    private void drawSettings(MatrixStack matrices, int mx, int my) {
        int x = 190;
        int y = 34;
        int w = 536;
        int h = 372;

        ClickGuiRender.rect(0, 0, W, H, BLACK);

        ClickGuiRender.rounded(x, y, w, h, 11, GRAPHITE);
        ClickGuiRender.border(x, y, w, h, 11, LINE);

        drawText(matrices, fit(settingsModule.getName(), 360), x + 18, y + 15, TEXT.getRGB());
        drawText(matrices, "MODULE SETTINGS", x + 18, y + 32, MUTED.getRGB());

        ClickGuiRender.rounded(x + w - 44, y + 10, 30, 30, 8, GRAPHITE_3);
        drawCentered(matrices, "X", x + w - 29, y + 18, MUTED.getRGB());

        int contentX = x + 16;
        int contentW = w - 32;
        int cy = y + 54;
        int bottom = y + h - 54;

        for (ISetting setting : settingsModule.getSettings()) {
            if (setting instanceof BooleanSetting) {
                if (cy + 42 > bottom) break;
                cy = drawBoolean(matrices, (BooleanSetting) setting, contentX, cy, contentW, mx, my);
            } else if (setting instanceof NumberSetting) {
                if (cy + 66 > bottom) break;
                cy = drawNumber(matrices, (NumberSetting) setting, contentX, cy, contentW, mx, my);
            }
        }

        int bindY = y + h - 43;
        ClickGuiRender.rounded(contentX, bindY, contentW, 31, 7, GRAPHITE_2);
        drawText(matrices, "BIND", contentX + 12, bindY + 9, MUTED.getRGB());

        String bind = bindingModule == settingsModule
                ? "PRESS KEY"
                : settingsModule.getBind() < 0
                ? "NONE"
                : keyName(settingsModule.getBind());

        int bw = mc.textRenderer.getWidth(bind);
        drawText(
                matrices,
                bind,
                contentX + contentW - 12 - bw,
                bindY + 9,
                TEXT.getRGB()
        );

        drawText(
                matrices,
                "Click the three dots beside a module to open these settings.",
                contentX,
                y + h - 61,
                MUTED.getRGB()
        );
    }

    private int drawBoolean(
            MatrixStack matrices,
            BooleanSetting setting,
            int x,
            int y,
            int w,
            int mx,
            int my
    ) {
        boolean hovered = inside(mx, my, x, y, w, 38);

        ClickGuiRender.rounded(
                x,
                y,
                w,
                38,
                7,
                hovered ? HOVER : GRAPHITE_2
        );

        drawText(
                matrices,
                fit(setting.getName(), w - 105),
                x + 12,
                y + 11,
                TEXT.getRGB()
        );

        int sx = x + w - 52;
        boolean enabled = setting.isEnabled();

        ClickGuiRender.rounded(
                sx,
                y + 10,
                38,
                18,
                9,
                enabled ? ACCENT_DARK : OFF
        );

        ClickGuiRender.circle(
                enabled ? sx + 29 : sx + 9,
                y + 19,
                6,
                enabled ? TEXT : new Color(155, 155, 158, 255)
        );

        return y + 46;
    }

    private int drawNumber(
            MatrixStack matrices,
            NumberSetting setting,
            int x,
            int y,
            int w,
            int mx,
            int my
    ) {
        boolean hovered = inside(mx, my, x, y, w, 62);

        ClickGuiRender.rounded(
                x,
                y,
                w,
                62,
                7,
                hovered ? HOVER : GRAPHITE_2
        );

        String name = fit(setting.getName(), w - 120);
        String value = format(setting);

        drawText(matrices, name, x + 12, y + 8, TEXT.getRGB());

        int valueW = mc.textRenderer.getWidth(value);
        drawText(
                matrices,
                value,
                x + w - 12 - valueW,
                y + 8,
                MUTED.getRGB()
        );

        int sliderX = x + 12;
        int sliderY = y + 39;
        int sliderW = w - 24;

        ClickGuiRender.rounded(sliderX, sliderY, sliderW, 6, 3, OFF);

        double progress = (
                setting.getDoubleValue() - setting.getMin()
        ) / Math.max(0.000001D, setting.getMax() - setting.getMin());

        progress = Math.max(0.0D, Math.min(1.0D, progress));

        int fill = (int) Math.round(sliderW * progress);

        if (fill > 0) {
            ClickGuiRender.rounded(sliderX, sliderY, fill, 6, 3, ACCENT_DARK);
        }

        ClickGuiRender.circle(sliderX + fill, sliderY + 3, 7, ACCENT);

        return y + 70;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = localX(mouseX);
        int y = localY(mouseY);

        if (settingsModule != null) {
            int panelX = 190;
            int panelY = 34;
            int panelW = 536;
            int panelH = 372;

            if (inside(x, y, panelX + panelW - 50, panelY + 7, 40, 38)) {
                closeSettings();
                return true;
            }

            int contentX = panelX + 16;
            int contentW = panelW - 32;
            int cy = panelY + 54;
            int bottom = panelY + panelH - 54;

            for (ISetting setting : settingsModule.getSettings()) {
                if (setting instanceof BooleanSetting) {
                    if (cy + 42 > bottom) break;

                    if (inside(x, y, contentX, cy, contentW, 38)) {
                        ((BooleanSetting) setting).toggle();
                        TriggerBotClient.saveConfig();
                        return true;
                    }

                    cy += 46;
                } else if (setting instanceof NumberSetting) {
                    if (cy + 66 > bottom) break;

                    if (inside(x, y, contentX, cy, contentW, 62)) {
                        dragging = (NumberSetting) setting;
                        updateSlider(dragging, x, contentX + 12, contentW - 24);
                        TriggerBotClient.saveConfig();
                        return true;
                    }

                    cy += 70;
                }
            }

            if (inside(x, y, contentX, panelY + panelH - 43, contentW, 31)) {
                bindingModule = settingsModule;
                return true;
            }

            return true;
        }

        int sidebarX = 22;
        int sidebarY = 101;
        for (Category current : Category.values()) {
            if (inside(x, y, sidebarX, sidebarY, 112, 40)) {
                category = current;
                return true;
            }
            sidebarY += 48;
        }

        int listX = 164;
        int listY = 115;
        int listW = 572;

        for (Module module : ModuleManager.getByCategory(category)) {
            if (!inside(x, y, listX, listY, listW, 48)) {
                listY += 57;
                continue;
            }

            int toggleX = listX + listW - 114;
            int dotsX = listX + listW - 62;

            if (inside(x, y, dotsX - 18, listY + 6, 36, 36)) {
                settingsModule = module;
                bindingModule = null;
                dragging = null;
                return true;
            }

            if (button == 0 && inside(x, y, toggleX - 4, listY + 4, 52, 40)) {
                module.toggled();
                TriggerBotClient.saveConfig();
                return true;
            }

            if (button == 0) {
                module.toggled();
                TriggerBotClient.saveConfig();
                return true;
            }

            listY += 57;
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
            updateSlider(dragging, x, 206, 492);
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
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (bindingModule != null) {
            bindingModule.setBind(keyCode == GLFW.GLFW_KEY_ESCAPE ? -1 : keyCode);
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
    }

    private void updateSlider(NumberSetting setting, double mouseX, int sliderX, int sliderW) {
        double progress = Math.max(
                0.0D,
                Math.min(1.0D, (mouseX - sliderX) / sliderW)
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
        if (value == null || value.isEmpty()) return "";
        if (mc.textRenderer.getWidth(value) <= maxWidth) return value;

        String suffix = "...";
        int suffixWidth = mc.textRenderer.getWidth(suffix);

        if (suffixWidth >= maxWidth) return "";

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < value.length(); i++) {
            String next = result.toString() + value.charAt(i);
            if (mc.textRenderer.getWidth(next) + suffixWidth > maxWidth) break;
            result.append(value.charAt(i));
        }

        return result + suffix;
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

        return setting.getIncrement() >= 1.0D
                ? String.valueOf(Math.round(value))
                : String.format(Locale.ROOT, "%.1f", value);
    }

    private String keyName(int keyCode) {
        if (keyCode < 0) return "NONE";

        String name = GLFW.glfwGetKeyName(keyCode, 0);
        return name == null
                ? String.valueOf(keyCode)
                : name.toUpperCase(Locale.ROOT);
    }

    private String pretty(Category value) {
        String text = value.name().toLowerCase(Locale.ROOT);
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    private void drawText(MatrixStack matrices, String text, int x, int y, int color) {
        mc.textRenderer.draw(matrices, text, x, y, color);
    }

    private void drawCentered(MatrixStack matrices, String text, int centerX, int y, int color) {
        drawText(
                matrices,
                text,
                centerX - mc.textRenderer.getWidth(text) / 2,
                y,
                color
        );
    }

    private boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x
                && mouseX <= x + w
                && mouseY >= y
                && mouseY <= y + h;
    }
}
