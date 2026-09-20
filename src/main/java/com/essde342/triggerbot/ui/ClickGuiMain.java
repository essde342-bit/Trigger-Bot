package com.essde342.triggerbot.ui;

import com.essde342.triggerbot.TriggerBotClient;
import com.essde342.triggerbot.ui.imple.BooleanSetting;
import com.essde342.triggerbot.ui.imple.MultiSetting;
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

public class ClickGuiMain extends Screen {
    private static final int UI_WIDTH = 600;
    private static final int UI_HEIGHT = 360;

    private static final Color BACKDROP = new Color(4, 6, 10, 220);
    private static final Color MAIN = new Color(12, 15, 22, 255);
    private static final Color PANEL = new Color(19, 23, 33, 255);
    private static final Color CARD = new Color(25, 30, 42, 255);
    private static final Color CARD_HOVER = new Color(33, 40, 55, 255);
    private static final Color BORDER = new Color(49, 57, 76, 255);
    private static final Color TEXT = new Color(245, 247, 250, 255);
    private static final Color MUTED = new Color(157, 166, 183, 255);
    private static final Color ACCENT = new Color(76, 124, 255, 255);
    private static final Color ACCENT_DARK = new Color(38, 67, 132, 255);
    private static final Color OFF = new Color(69, 76, 91, 255);

    private final MinecraftClient mc = MinecraftClient.getInstance();

    private Category selectedCategory = Category.COMBAT;
    private Module selectedModule;
    private Module waitingForBind;
    private NumberSetting draggingSlider;

    private float scale = 1.0F;
    private int originX;
    private int originY;

    public ClickGuiMain() {
        super(Text.of("Astra Client"));
        ModuleManager.moduleRegister();
        selectFirstModule();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        resizeUi();
        ClickGuiRender.rect(0, 0, width, height, BACKDROP);

        matrices.push();
        matrices.translate(originX, originY, 0.0D);
        matrices.scale(scale, scale, 1.0F);

        int mx = localX(mouseX);
        int my = localY(mouseY);

        drawBackground(matrices);
        drawSidebar(matrices, mx, my);
        drawModules(matrices, mx, my);
        drawSettings(matrices, mx, my);

        matrices.pop();
    }

    private void resizeUi() {
        scale = Math.min(
                1.0F,
                Math.min((width - 18.0F) / UI_WIDTH, (height - 18.0F) / UI_HEIGHT)
        );
        if (scale <= 0.0F) {
            scale = 1.0F;
        }

        originX = (int) ((width - UI_WIDTH * scale) / 2.0F);
        originY = (int) ((height - UI_HEIGHT * scale) / 2.0F);
    }

    private int localX(double mouseX) {
        return (int) ((mouseX - originX) / scale);
    }

    private int localY(double mouseY) {
        return (int) ((mouseY - originY) / scale);
    }

    private void drawBackground(MatrixStack matrices) {
        ClickGuiRender.shadow(0, 0, UI_WIDTH, UI_HEIGHT, 12, 12, new Color(0, 0, 0, 80));
        ClickGuiRender.panel(0, 0, UI_WIDTH, UI_HEIGHT, 12, MAIN);
        ClickGuiRender.border(0, 0, UI_WIDTH, UI_HEIGHT, 12, BORDER);

        drawText(matrices, "ASTRA CLIENT", 18, 13, TEXT.getRGB());
        drawText(matrices, "1.0.0", 126, 15, MUTED.getRGB());
        drawText(matrices, "RIGHT SHIFT", 468, 15, MUTED.getRGB());

        ClickGuiRender.rect(0, 45, UI_WIDTH, 1, BORDER);
    }

    private void drawSidebar(MatrixStack matrices, int mouseX, int mouseY) {
        int x = 12;
        int y = 57;
        int w = 118;
        int h = 290;

        ClickGuiRender.panel(x, y, w, h, 9, PANEL);
        ClickGuiRender.border(x, y, w, h, 9, BORDER);

        drawText(matrices, "CATEGORY", x + 12, y + 10, MUTED.getRGB());

        int rowY = y + 31;
        for (Category category : Category.values()) {
            boolean selected = category == selectedCategory;
            boolean hover = inside(mouseX, mouseY, x + 8, rowY, w - 16, 36);

            int color = selected ? ACCENT_DARK.getRGB()
                    : hover ? CARD_HOVER.getRGB() : PANEL.getRGB();

            ClickGuiRender.rounded(x + 8, rowY, w - 16, 36, 7, new Color(color, true));

            if (selected) {
                ClickGuiRender.rect(x + 8, rowY, 3, 36, ACCENT);
            }

            drawText(
                    matrices,
                    pretty(category),
                    x + 18,
                    rowY + 11,
                    selected ? TEXT.getRGB() : MUTED.getRGB()
            );

            rowY += 48;
        }
    }

    private void drawModules(MatrixStack matrices, int mouseX, int mouseY) {
        int x = 142;
        int y = 57;
        int w = 208;
        int h = 290;

        ClickGuiRender.panel(x, y, w, h, 9, PANEL);
        ClickGuiRender.border(x, y, w, h, 9, BORDER);

        drawText(matrices, pretty(selectedCategory), x + 12, y + 10, TEXT.getRGB());
        drawText(matrices, "MODULES", x + 12, y + 27, MUTED.getRGB());

        List<Module> modules = ModuleManager.getByCategory(selectedCategory);
        int rowY = y + 48;

        for (Module module : modules) {
            if (rowY > y + h - 54) {
                break;
            }

            boolean hover = inside(mouseX, mouseY, x + 9, rowY, w - 18, 44);
            boolean enabled = module.isEnabled();

            Color card = hover ? CARD_HOVER : CARD;
            ClickGuiRender.rounded(x + 9, rowY, w - 18, 44, 7, card);

            if (enabled) {
                ClickGuiRender.rect(x + 9, rowY, 3, 44, ACCENT);
            }

            drawText(matrices, module.getName(), x + 18, rowY + 7, TEXT.getRGB());
            drawText(matrices, trim(module.getDesc(), 27), x + 18, rowY + 24, MUTED.getRGB());

            int toggleX = x + w - 55;
            ClickGuiRender.rounded(
                    toggleX,
                    rowY + 10,
                    37,
                    24,
                    8,
                    enabled ? ACCENT : OFF
            );

            drawText(
                    matrices,
                    enabled ? "ON" : "OFF",
                    toggleX + 7,
                    rowY + 16,
                    TEXT.getRGB()
            );

            rowY += 51;
        }
    }

    private void drawSettings(MatrixStack matrices, int mouseX, int mouseY) {
        int x = 361;
        int y = 57;
        int w = 227;
        int h = 290;

        ClickGuiRender.panel(x, y, w, h, 9, PANEL);
        ClickGuiRender.border(x, y, w, h, 9, BORDER);

        if (selectedModule == null) {
            drawText(matrices, "SETTINGS", x + 12, y + 10, MUTED.getRGB());
            drawText(matrices, "Select a module", x + 12, y + 42, MUTED.getRGB());
            return;
        }

        drawText(matrices, selectedModule.getName(), x + 12, y + 10, TEXT.getRGB());
        drawText(matrices, "SETTINGS", x + 12, y + 27, MUTED.getRGB());

        int currentY = y + 47;

        for (ISetting setting : selectedModule.getSettings()) {
            if (setting instanceof BooleanSetting) {
                currentY = drawBoolean(matrices, (BooleanSetting) setting, x + 9, currentY, w - 18, mouseX, mouseY);
            } else if (setting instanceof NumberSetting) {
                currentY = drawNumber(matrices, (NumberSetting) setting, x + 9, currentY, w - 18, mouseX, mouseY);
            } else if (setting instanceof MultiSetting) {
                currentY = drawMulti(matrices, (MultiSetting) setting, x + 9, currentY, w - 18, mouseX, mouseY);
            }

            if (currentY >= y + h - 48) {
                break;
            }
        }

        if (selectedModule.getSettings().isEmpty()) {
            drawText(matrices, "No settings.", x + 12, currentY + 7, MUTED.getRGB());
        }

        int bindY = y + h - 39;
        ClickGuiRender.rounded(x + 9, bindY, w - 18, 30, 7, CARD);
        drawText(matrices, "BIND", x + 17, bindY + 9, MUTED.getRGB());

        String bind = waitingForBind == selectedModule
                ? "PRESS KEY"
                : selectedModule.getBind() < 0 ? "NONE" : keyName(selectedModule.getBind());

        int bw = mc.textRenderer.getWidth(bind);
        drawText(matrices, bind, x + w - 17 - bw, bindY + 9, TEXT.getRGB());
    }

    private int drawBoolean(
            MatrixStack matrices,
            BooleanSetting setting,
            int x,
            int y,
            int w,
            int mouseX,
            int mouseY
    ) {
        boolean hover = inside(mouseX, mouseY, x, y, w, 34);
        ClickGuiRender.rounded(x, y, w, 34, 7, hover ? CARD_HOVER : CARD);

        drawText(matrices, setting.getName(), x + 11, y + 10, TEXT.getRGB());

        int sx = x + w - 46;
        boolean enabled = setting.isEnabled();

        ClickGuiRender.rounded(sx, y + 8, 34, 18, 9, enabled ? ACCENT : OFF);
        ClickGuiRender.rounded(
                enabled ? sx + 19 : sx + 2,
                y + 10,
                14,
                14,
                7,
                new Color(248, 249, 252, 255)
        );

        return y + 42;
    }

    private int drawNumber(
            MatrixStack matrices,
            NumberSetting setting,
            int x,
            int y,
            int w,
            int mouseX,
            int mouseY
    ) {
        boolean hover = inside(mouseX, mouseY, x, y, w, 58);
        ClickGuiRender.rounded(x, y, w, 58, 7, hover ? CARD_HOVER : CARD);

        String value = formatNumber(setting);
        int vw = mc.textRenderer.getWidth(value);

        drawText(matrices, setting.getName(), x + 11, y + 7, TEXT.getRGB());
        drawText(matrices, value, x + w - 11 - vw, y + 7, MUTED.getRGB());

        int sx = x + 11;
        int sy = y + 35;
        int sw = w - 22;

        ClickGuiRender.rounded(sx, sy, sw, 6, 3, OFF);

        double p = (setting.getDoubleValue() - setting.getMin())
                / Math.max(0.000001D, setting.getMax() - setting.getMin());
        p = Math.max(0.0D, Math.min(1.0D, p));

        int fill = (int) Math.round(sw * p);
        if (fill > 0) {
            ClickGuiRender.rounded(sx, sy, fill, 6, 3, ACCENT);
        }

        int knobX = sx + fill - 6;
        ClickGuiRender.rounded(knobX, sy - 5, 18, 16, 8, new Color(235, 239, 255, 255));

        return y + 66;
    }

    private int drawMulti(
            MatrixStack matrices,
            MultiSetting setting,
            int x,
            int y,
            int w,
            int mouseX,
            int mouseY
    ) {
        drawText(matrices, setting.getName(), x + 11, y + 4, TEXT.getRGB());

        int columns = 2;
        int optionW = (w - 12) / columns;
        int oy = y + 25;

        for (int i = 0; i < setting.getOptionsCount(); i++) {
            int col = i % 2;
            int row = i / 2;
            int ox = x + col * (optionW + 6);

            boolean hover = inside(mouseX, mouseY, ox, oy + row * 31, optionW, 27);
            boolean enabled = setting.isEnabled(setting.getOptionName(i));

            ClickGuiRender.rounded(
                    ox,
                    oy + row * 31,
                    optionW,
                    27,
                    6,
                    enabled ? ACCENT_DARK : (hover ? CARD_HOVER : CARD)
            );

            drawText(
                    matrices,
                    setting.getOptionName(i),
                    ox + 9,
                    oy + row * 31 + 8,
                    enabled ? TEXT.getRGB() : MUTED.getRGB()
            );
        }

        int rows = (setting.getOptionsCount() + 1) / 2;
        return oy + rows * 31 + 4;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = localX(mouseX);
        int y = localY(mouseY);

        int categoryY = 88;
        for (Category category : Category.values()) {
            if (inside(x, y, 20, categoryY, 102, 36)) {
                selectedCategory = category;
                selectFirstModule();
                waitingForBind = null;
                return true;
            }
            categoryY += 48;
        }

        int moduleY = 105;
        for (Module module : ModuleManager.getByCategory(selectedCategory)) {
            if (inside(x, y, 151, moduleY, 190, 44)) {
                if (button == 2) {
                    waitingForBind = module;
                    selectedModule = module;
                    return true;
                }

                selectedModule = module;

                if (button == 0 && x < 295) {
                    module.toggled();
                    TriggerBotClient.saveConfig();
                }

                return true;
            }

            moduleY += 51;
        }

        if (selectedModule != null) {
            int settingsX = 370;
            int settingsW = 209;
            int currentY = 104;

            for (ISetting setting : selectedModule.getSettings()) {
                if (setting instanceof BooleanSetting) {
                    if (inside(x, y, settingsX, currentY, settingsW, 34)) {
                        ((BooleanSetting) setting).toggle();
                        TriggerBotClient.saveConfig();
                        return true;
                    }
                    currentY += 42;
                } else if (setting instanceof NumberSetting) {
                    if (inside(x, y, settingsX, currentY, settingsW, 58)) {
                        NumberSetting number = (NumberSetting) setting;
                        draggingSlider = number;
                        updateSlider(number, x, settingsX + 11, settingsW - 22);
                        TriggerBotClient.saveConfig();
                        return true;
                    }
                    currentY += 66;
                } else if (setting instanceof MultiSetting) {
                    MultiSetting multi = (MultiSetting) setting;
                    int oy = currentY + 25;
                    int optionW = (settingsW - 12) / 2;

                    for (int i = 0; i < multi.getOptionsCount(); i++) {
                        int col = i % 2;
                        int row = i / 2;
                        int ox = settingsX + col * (optionW + 6);

                        if (inside(x, y, ox, oy + row * 31, optionW, 27)) {
                            multi.toggle(multi.getOptionName(i));
                            TriggerBotClient.saveConfig();
                            return true;
                        }
                    }

                    currentY = oy + ((multi.getOptionsCount() + 1) / 2) * 31 + 4;
                }
            }

            if (inside(x, y, 370, 308, 209, 30)) {
                waitingForBind = selectedModule;
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingSlider != null) {
            int x = localX(mouseX);
            updateSlider(draggingSlider, x, 381, 187);
            TriggerBotClient.saveConfig();
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingSlider = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (waitingForBind != null) {
            waitingForBind.setBind(keyCode == GLFW.GLFW_KEY_ESCAPE ? -1 : keyCode);
            waitingForBind = null;
            TriggerBotClient.saveConfig();
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (client != null) {
                client.openScreen(null);
            }
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void updateSlider(NumberSetting setting, double mouseX, int sliderX, int sliderWidth) {
        double p = Math.max(0.0D, Math.min(1.0D, (mouseX - sliderX) / sliderWidth));
        double value = setting.getMin() + p * (setting.getMax() - setting.getMin());

        double increment = setting.getIncrement();
        if (increment > 0.0D) {
            value = Math.round(value / increment) * increment;
        }

        setting.setValue(value);
    }

    private void selectFirstModule() {
        List<Module> modules = ModuleManager.getByCategory(selectedCategory);
        selectedModule = modules.isEmpty() ? null : modules.get(0);
    }

    private void drawText(MatrixStack matrices, String text, int x, int y, int color) {
        mc.textRenderer.draw(matrices, text, x, y, color);
    }

    private boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    private String trim(String value, int max) {
        if (value == null || value.length() <= max) {
            return value == null ? "" : value;
        }
        return value.substring(0, Math.max(1, max - 3)) + "...";
    }

    private String formatNumber(NumberSetting setting) {
        double value = setting.getDoubleValue();
        double inc = setting.getIncrement();

        if (inc >= 1.0D) {
            return String.valueOf((int) Math.round(value));
        }

        if (inc >= 0.1D) {
            return String.format(java.util.Locale.ROOT, "%.1f", value);
        }

        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }

    private String keyName(int keyCode) {
        if (keyCode < 0) {
            return "NONE";
        }

        String name = GLFW.glfwGetKeyName(keyCode, 0);
        if (name != null && !name.isEmpty()) {
            return name.toUpperCase(java.util.Locale.ROOT);
        }

        return String.valueOf(keyCode);
    }

    private String pretty(Category category) {
        String value = category.name().toLowerCase(java.util.Locale.ROOT);
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
