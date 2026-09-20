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
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.util.List;

public class ClickGuiMain extends Screen {
    private static final int UI_WIDTH = 610;
    private static final int UI_HEIGHT = 380;

    private static final Color BG = new Color(8, 10, 16, 255);
    private static final Color PANEL = new Color(17, 20, 29, 255);
    private static final Color PANEL_2 = new Color(21, 24, 35, 255);
    private static final Color BORDER = new Color(39, 44, 60, 255);
    private static final Color TEXT = new Color(239, 242, 248, 255);
    private static final Color MUTED = new Color(145, 153, 170, 255);
    private static final Color ACCENT = new Color(65, 116, 255, 255);
    private static final Color ACCENT_2 = new Color(86, 141, 255, 255);
    private static final Color HOVER = new Color(31, 36, 50, 255);
    private static final Color DISABLED = new Color(45, 49, 61, 255);
    private static final Color ON_BG = new Color(28, 53, 105, 255);

    private final MinecraftClient mc = MinecraftClient.getInstance();

    private Category selectedCategory = Category.COMBAT;
    private Module selectedModule;
    private NumberSetting draggingSlider;
    private Module waitingForBind;

    private float scale = 1.0F;
    private int originX;
    private int originY;

    public ClickGuiMain() {
        super(UiFont.text("Astra Client"));
        ModuleManager.moduleRegister();

        List<Module> modules = ModuleManager.getByCategory(selectedCategory);
        selectedModule = modules.isEmpty() ? null : modules.get(0);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void recalculateViewport() {
        scale = Math.min(
                1.0F,
                Math.min((width - 20.0F) / UI_WIDTH, (height - 20.0F) / UI_HEIGHT)
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

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        recalculateViewport();

        ClickGuiRender.rect(0, 0, width, height, new Color(3, 5, 9, 215));

        matrices.push();
        matrices.translate(originX, originY, 0.0D);
        matrices.scale(scale, scale, 1.0F);

        int mx = localX(mouseX);
        int my = localY(mouseY);

        drawInterface(matrices, mx, my);

        matrices.pop();
    }

    private void drawInterface(MatrixStack matrices, int mouseX, int mouseY) {
        int x = 10;
        int y = 10;

        ClickGuiRender.shadow(x, y, UI_WIDTH, UI_HEIGHT, 12, 16, new Color(0, 0, 0, 90));
        ClickGuiRender.panel(x, y, UI_WIDTH, UI_HEIGHT, 12, BG);
        ClickGuiRender.border(x, y, UI_WIDTH, UI_HEIGHT, 12, BORDER);

        drawHeader(matrices, x, y);
        drawCategories(matrices, mouseX, mouseY);
        drawModules(matrices, mouseX, mouseY);
        drawSettings(matrices, mouseX, mouseY);

        drawText(matrices, "Right Shift", x + 18, y + UI_HEIGHT - 22, MUTED.getRGB());
        drawText(matrices, "open / close", x + 86, y + UI_HEIGHT - 22, MUTED.getRGB());
    }

    private void drawHeader(MatrixStack matrices, int x, int y) {
        drawText(matrices, "Astra Client", x + 18, y + 12, TEXT.getRGB());
        drawText(matrices, "1.0.0", x + 114, y + 14, MUTED.getRGB());

        ClickGuiRender.rounded(x + UI_WIDTH - 113, y + 12, 95, 24, 7, PANEL_2);
        ClickGuiRender.rounded(x + UI_WIDTH - 103, y + 19, 8, 8, 4, ACCENT);
        drawText(matrices, "READY", x + UI_WIDTH - 88, y + 15, TEXT.getRGB());
    }

    private void drawCategories(MatrixStack matrices, int mouseX, int mouseY) {
        int x = 20;
        int y = 58;
        int width = 112;
        int height = 296;

        ClickGuiRender.panel(x, y, width, height, 9, PANEL);
        ClickGuiRender.border(x, y, width, height, 9, BORDER);
        drawText(matrices, "CATEGORIES", x + 12, y + 10, MUTED.getRGB());

        int rowY = y + 38;
        for (Category category : Category.values()) {
            boolean selected = category == selectedCategory;
            boolean hovered = inside(mouseX, mouseY, x + 8, rowY, width - 16, 38);

            Color fill = selected ? ON_BG : (hovered ? HOVER : PANEL_2);
            ClickGuiRender.rounded(x + 8, rowY, width - 16, 38, 7, fill);

            if (selected) {
                ClickGuiRender.rounded(x + 8, rowY, 3, 38, 2, ACCENT);
            }

            drawText(
                    matrices,
                    pretty(category),
                    x + 19,
                    rowY + 11,
                    selected ? TEXT.getRGB() : MUTED.getRGB()
            );

            rowY += 45;
        }
    }

    private void drawModules(MatrixStack matrices, int mouseX, int mouseY) {
        int x = 142;
        int y = 58;
        int width = 210;
        int height = 296;

        ClickGuiRender.panel(x, y, width, height, 9, PANEL);
        ClickGuiRender.border(x, y, width, height, 9, BORDER);

        drawText(matrices, pretty(selectedCategory), x + 14, y + 10, TEXT.getRGB());
        drawText(matrices, "MODULES", x + 14, y + 27, MUTED.getRGB());

        List<Module> modules = ModuleManager.getByCategory(selectedCategory);
        int cardY = y + 49;

        if (modules.isEmpty()) {
            drawText(matrices, "No modules in this category.", x + 14, cardY + 12, MUTED.getRGB());
            return;
        }

        for (Module module : modules) {
            if (cardY > y + height - 55) {
                break;
            }

            boolean hovered = inside(mouseX, mouseY, x + 10, cardY, width - 20, 48);
            boolean enabled = module.isEnabled();

            Color fill = enabled ? ON_BG : (hovered ? HOVER : PANEL_2);
            ClickGuiRender.rounded(x + 10, cardY, width - 20, 48, 8, fill);

            if (enabled) {
                ClickGuiRender.rounded(x + 10, cardY, 4, 48, 3, ACCENT);
            }

            drawText(matrices, module.getName(), x + 20, cardY + 8, TEXT.getRGB());
            drawText(matrices, trim(module.getDesc(), 30), x + 20, cardY + 25, MUTED.getRGB());

            ClickGuiRender.rounded(
                    x + width - 57,
                    cardY + 11,
                    35,
                    24,
                    6,
                    enabled ? ACCENT : DISABLED
            );
            drawText(
                    matrices,
                    enabled ? "ON" : "OFF",
                    x + width - 51,
                    cardY + 16,
                    TEXT.getRGB()
            );

            drawText(matrices, ">", x + width - 19, cardY + 15, MUTED.getRGB());
            cardY += 57;
        }
    }

    private void drawSettings(MatrixStack matrices, int mouseX, int mouseY) {
        int x = 362;
        int y = 58;
        int width = 248;
        int height = 296;

        ClickGuiRender.panel(x, y, width, height, 9, PANEL);
        ClickGuiRender.border(x, y, width, height, 9, BORDER);

        if (selectedModule == null) {
            drawText(matrices, "SETTINGS", x + 14, y + 12, MUTED.getRGB());
            return;
        }

        drawText(matrices, selectedModule.getName(), x + 14, y + 10, TEXT.getRGB());
        drawText(matrices, "SETTINGS", x + 14, y + 27, MUTED.getRGB());

        int currentY = y + 48;

        for (ISetting setting : selectedModule.getSettings()) {
            if (setting instanceof BooleanSetting) {
                currentY = drawBooleanSetting(
                        matrices,
                        (BooleanSetting) setting,
                        x + 10,
                        currentY,
                        width - 20,
                        mouseX,
                        mouseY
                );
            } else if (setting instanceof NumberSetting) {
                currentY = drawNumberSetting(
                        matrices,
                        (NumberSetting) setting,
                        x + 10,
                        currentY,
                        width - 20,
                        mouseX,
                        mouseY
                );
            } else if (setting instanceof MultiSetting) {
                currentY = drawMultiSetting(
                        matrices,
                        (MultiSetting) setting,
                        x + 10,
                        currentY,
                        width - 20,
                        mouseX,
                        mouseY
                );
            }

            if (currentY > y + height - 68) {
                break;
            }
        }

        if (selectedModule.getSettings().isEmpty()) {
            drawText(matrices, "No configurable settings.", x + 14, currentY + 10, MUTED.getRGB());
        }

        ClickGuiRender.rounded(x + 10, y + height - 48, width - 20, 34, 7, PANEL_2);
        drawText(matrices, "Bind", x + 18, y + height - 38, MUTED.getRGB());

        String bind = waitingForBind == selectedModule
                ? "PRESS KEY..."
                : (selectedModule.getBind() == -1 ? "NONE" : keyName(selectedModule.getBind()));

        int bindWidth = UiFont.width(mc, bind);
        drawText(matrices, bind, x + width - 18 - bindWidth, y + height - 38, TEXT.getRGB());
    }

    private int drawBooleanSetting(
            MatrixStack matrices,
            BooleanSetting setting,
            int x,
            int y,
            int width,
            int mouseX,
            int mouseY
    ) {
        boolean hovered = inside(mouseX, mouseY, x, y, width, 36);
        ClickGuiRender.rounded(x, y, width, 36, 7, hovered ? HOVER : PANEL_2);

        drawText(matrices, setting.getName(), x + 12, y + 10, TEXT.getRGB());

        boolean enabled = setting.isEnabled();
        ClickGuiRender.rounded(x + width - 48, y + 7, 36, 22, 11, enabled ? ACCENT : DISABLED);
        ClickGuiRender.rounded(
                enabled ? x + width - 28 : x + width - 45,
                y + 10,
                16,
                16,
                8,
                Color.WHITE
        );

        return y + 44;
    }

    private int drawNumberSetting(
            MatrixStack matrices,
            NumberSetting setting,
            int x,
            int y,
            int width,
            int mouseX,
            int mouseY
    ) {
        boolean hovered = inside(mouseX, mouseY, x, y, width, 58);
        ClickGuiRender.rounded(x, y, width, 58, 7, hovered ? HOVER : PANEL_2);

        String value = formatNumber(setting);
        drawText(matrices, setting.getName(), x + 12, y + 7, TEXT.getRGB());

        int valueWidth = UiFont.width(mc, value);
        drawText(matrices, value, x + width - 12 - valueWidth, y + 7, MUTED.getRGB());

        int sliderX = x + 12;
        int sliderY = y + 34;
        int sliderW = width - 24;

        ClickGuiRender.rounded(sliderX, sliderY, sliderW, 8, 4, DISABLED);

        double progress = (setting.getDoubleValue() - setting.getMin())
                / Math.max(0.000001D, setting.getMax() - setting.getMin());
        progress = Math.max(0.0D, Math.min(1.0D, progress));

        int fillW = (int) Math.round(sliderW * progress);
        if (fillW > 0) {
            ClickGuiRender.rounded(sliderX, sliderY, fillW, 8, 4, ACCENT);
        }

        int knobX = sliderX + fillW - 5;
        ClickGuiRender.rounded(knobX, sliderY - 4, 18, 16, 8, ACCENT_2);

        return y + 66;
    }

    private int drawMultiSetting(
            MatrixStack matrices,
            MultiSetting setting,
            int x,
            int y,
            int width,
            int mouseX,
            int mouseY
    ) {
        drawText(matrices, setting.getName(), x + 12, y + 6, TEXT.getRGB());

        int optionY = y + 28;
        int half = (setting.getOptionsCount() + 1) / 2;
        int optionW = (width - 30) / 2;

        for (int i = 0; i < half; i++) {
            drawMultiOption(
                    matrices,
                    setting,
                    setting.getOptionName(i),
                    x + 10,
                    optionY,
                    optionW,
                    mouseX,
                    mouseY
            );

            int other = i + half;
            if (other < setting.getOptionsCount()) {
                drawMultiOption(
                        matrices,
                        setting,
                        setting.getOptionName(other),
                        x + 20 + optionW,
                        optionY,
                        optionW,
                        mouseX,
                        mouseY
                );
            }

            optionY += 32;
        }

        return optionY + 6;
    }

    private void drawMultiOption(
            MatrixStack matrices,
            MultiSetting setting,
            String option,
            int x,
            int y,
            int width,
            int mouseX,
            int mouseY
    ) {
        boolean hovered = inside(mouseX, mouseY, x, y, width, 28);
        boolean enabled = setting.isEnabled(option);

        ClickGuiRender.rounded(
                x,
                y,
                width,
                28,
                6,
                enabled ? ON_BG : (hovered ? HOVER : PANEL_2)
        );

        if (enabled) {
            ClickGuiRender.rounded(x, y, 3, 28, 2, ACCENT);
        }

        drawText(matrices, option, x + 10, y + 8, enabled ? TEXT.getRGB() : MUTED.getRGB());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = localX(mouseX);
        int y = localY(mouseY);

        int categoryY = 96;
        for (Category category : Category.values()) {
            if (inside(x, y, 28, categoryY, 96, 38)) {
                selectedCategory = category;
                List<Module> modules = ModuleManager.getByCategory(category);
                selectedModule = modules.isEmpty() ? null : modules.get(0);
                waitingForBind = null;
                return true;
            }
            categoryY += 45;
        }

        int moduleY = 107;
        for (Module module : ModuleManager.getByCategory(selectedCategory)) {
            if (inside(x, y, 152, moduleY, 190, 48)) {
                if (button == 2) {
                    waitingForBind = module;
                    return true;
                }

                if (button == 1 || x >= 312) {
                    selectedModule = module;
                    return true;
                }

                module.toggled();
                TriggerBotClient.saveConfig();
                selectedModule = module;
                return true;
            }

            moduleY += 57;
        }

        if (selectedModule != null) {
            int settingsX = 372;
            int settingsWidth = 228;
            int currentY = 106;

            for (ISetting setting : selectedModule.getSettings()) {
                if (setting instanceof BooleanSetting) {
                    if (inside(x, y, settingsX, currentY, settingsWidth, 36)) {
                        ((BooleanSetting) setting).toggle();
                        TriggerBotClient.saveConfig();
                        return true;
                    }
                    currentY += 44;
                } else if (setting instanceof NumberSetting) {
                    if (inside(x, y, settingsX, currentY, settingsWidth, 58)) {
                        NumberSetting numberSetting = (NumberSetting) setting;
                        draggingSlider = numberSetting;
                        updateSlider(numberSetting, x, settingsX + 12, settingsWidth - 24);
                        TriggerBotClient.saveConfig();
                        return true;
                    }
                    currentY += 66;
                } else if (setting instanceof MultiSetting) {
                    MultiSetting multiSetting = (MultiSetting) setting;
                    int optionY = currentY + 28;
                    int half = (multiSetting.getOptionsCount() + 1) / 2;
                    int optionW = (settingsWidth - 30) / 2;

                    for (int i = 0; i < half; i++) {
                        if (inside(x, y, settingsX + 10, optionY, optionW, 28)) {
                            multiSetting.toggle(multiSetting.getOptionName(i));
                            TriggerBotClient.saveConfig();
                            return true;
                        }

                        int other = i + half;
                        if (other < multiSetting.getOptionsCount()
                                && inside(x, y, settingsX + 20 + optionW, optionY, optionW, 28)) {
                            multiSetting.toggle(multiSetting.getOptionName(other));
                            TriggerBotClient.saveConfig();
                            return true;
                        }

                        optionY += 32;
                    }

                    currentY = optionY + 6;
                }
            }

            if (inside(x, y, 372, 306, 228, 34)) {
                waitingForBind = selectedModule;
                return true;
            }
        }

        if (button == 1) {
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingSlider != null) {
            int x = localX(mouseX);
            updateSlider(draggingSlider, x, 384, 204);
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
        double progress = Math.max(0.0D, Math.min(1.0D, (mouseX - sliderX) / sliderWidth));
        double value = setting.getMin() + progress * (setting.getMax() - setting.getMin());

        double increment = setting.getIncrement();
        if (increment > 0.0D) {
            value = Math.round(value / increment) * increment;
        }

        setting.setValue(value);
    }

    private void drawText(MatrixStack matrices, String text, int x, int y, int color) {
        mc.textRenderer.draw(matrices, UiFont.text(text), x, y, color);
    }

    private boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private String trim(String text, int max) {
        if (text.length() <= max) {
            return text;
        }
        return text.substring(0, Math.max(0, max - 3)) + "...";
    }

    private String formatNumber(NumberSetting setting) {
        double value = setting.getDoubleValue();
        double increment = setting.getIncrement();

        if (increment >= 1.0D) {
            return String.valueOf((int) Math.round(value));
        }

        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }

    private String keyName(int keyCode) {
        if (keyCode < 0) {
            return "NONE";
        }

        String name = GLFW.glfwGetKeyName(keyCode, 0);
        if (name != null && !name.isEmpty()) {
            return name.toUpperCase();
        }

        return String.valueOf(keyCode);
    }

    private String pretty(Category category) {
        String value = category.name().toLowerCase(java.util.Locale.ROOT);
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
