package com.essde342.triggerbot.ui;

import com.essde342.triggerbot.AltManagerScreen;
import com.essde342.triggerbot.TriggerBotClient;
import com.essde342.triggerbot.ui.imple.BooleanSetting;
import com.essde342.triggerbot.ui.imple.NumberSetting;
import com.essde342.triggerbot.ui.modules.Category;
import com.essde342.triggerbot.ui.modules.Module;
import com.essde342.triggerbot.ui.modules.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * MenuScreen ported from the provided Click GUI archive.
 *
 * The archive's 400x250 layout, sidebar, search box, two-column module cards,
 * animated-style colors, bind badge and inline settings model are retained.
 * Module data is deliberately backed by this repository's ModuleManager so
 * every visible module maps to an existing Trigger-Bot function.
 */
public final class MenuScreen extends Screen {
    private static final int WIDTH = 400;
    private static final int HEIGHT = 250;
    private static final int SIDEBAR = 85;
    private static final int COLUMN_WIDTH = 137;
    private static final int COLUMN_GAP = 10;

    private static final int OVERLAY = 0x64000000;
    private static final int BACKGROUND = 0xFF11141A;
    private static final int SIDEBAR_COLOR = 0xFF0D1015;
    private static final int CARD = 0xFF20242D;
    private static final int CARD_HOVER = 0xFF292F39;
    private static final int CARD_ENABLED = 0xFF343C55;
    private static final int TEXT = 0xFFD4D6E1;
    private static final int MUTED = 0xFF878894;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int ACCENT = 0xFF7D8CFF;
    private static final int BORDER = 0x9A3A414D;

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final List<Category> categories = new ArrayList<>();

    private Category category = Category.COMBAT;
    private String searchText = "";
    private int cursorPosition;
    private boolean typingSearch;
    private double scroll;
    private double scrollTarget;

    private Module bindingModule;
    private NumberSetting draggingSetting;
    private boolean draggingSlider;

    private int guiX;
    private int guiY;

    public MenuScreen() {
        super(Text.literal("MenuScreen"));
        categories.add(Category.COMBAT);
        categories.add(Category.VISUALS);
        categories.add(Category.OTHER);
        ModuleManager.moduleRegister();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void tick() {
        scroll += (scrollTarget - scroll) * 0.24D;
        super.tick();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        guiX = width / 2 - WIDTH / 2;
        guiY = height / 2 - HEIGHT / 2;

        context.fill(0, 0, width, height, OVERLAY);
        fill(context, guiX, guiY, WIDTH, HEIGHT, BACKGROUND);
        border(context, guiX, guiY, WIDTH, HEIGHT, BORDER);

        drawSidebar(context, mouseX, mouseY);
        drawHeader(context);
        drawProfile(context);
        drawSearch(context, mouseX, mouseY);
        drawCategoryTitle(context);
        drawModules(context, mouseX, mouseY);

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawSidebar(DrawContext context, int mouseX, int mouseY) {
        fill(context, guiX, guiY, SIDEBAR, HEIGHT, SIDEBAR_COLOR);
        context.fill(guiX + SIDEBAR - 1, guiY, guiX + SIDEBAR, guiY + HEIGHT, BORDER);

        context.drawTextWithShadow(textRenderer, Text.literal("kronex.fun"), guiX + 16, guiY + 14, ACCENT);

        for (int i = 0; i < categories.size(); i++) {
            Category current = categories.get(i);
            int y = guiY + 60 + i * 19;
            boolean selected = current == category;
            boolean hovered = inside(mouseX, mouseY, guiX + 6, y, 73, 17);

            fill(
                    context,
                    guiX + 6,
                    y,
                    73,
                    17,
                    selected ? 0xFF30374A : hovered ? 0xFF242A34 : 0xFF171B22
            );

            if (selected) {
                context.fill(guiX + 6, y, guiX + 8, y + 17, ACCENT);
            }

            context.drawTextWithShadow(
                    textRenderer,
                    Text.literal(current.getDisplayName()),
                    guiX + 20,
                    y + 4,
                    selected ? WHITE : TEXT
            );
        }
    }

    private void drawHeader(DrawContext context) {
        context.fill(guiX + SIDEBAR, guiY + 28, guiX + WIDTH, guiY + 29, BORDER);
        context.drawTextWithShadow(textRenderer, Text.literal("TRIGGER-BOT"), guiX + 95, guiY + 15, TEXT);

        String bind = "RSHIFT";
        int bindWidth = textRenderer.getWidth(bind);
        context.drawTextWithShadow(
                textRenderer,
                Text.literal(bind),
                guiX + WIDTH - bindWidth - 8,
                guiY + 15,
                MUTED
        );
    }

    private void drawProfile(DrawContext context) {
        int x = guiX + 5;
        int y = guiY + HEIGHT - 30;

        fill(context, x, y, 75, 25, CARD);
        border(context, x, y, 75, 25, BORDER);

        String username = mc.getSession() == null ? "Player" : mc.getSession().getUsername();
        context.drawTextWithShadow(textRenderer, Text.literal(fit(username, 42)), x + 30, y + 6, TEXT);
        context.drawTextWithShadow(textRenderer, Text.literal("Trigger-Bot"), x + 30, y + 15, MUTED);

        fill(context, x + 11, y + 6, 14, 14, 0xFF1A1E27);
        context.drawTextWithShadow(textRenderer, Text.literal("•"), x + 15, y + 4, ACCENT);
    }

    private void drawSearch(DrawContext context, int mouseX, int mouseY) {
        int x = guiX + 300;
        int y = guiY + 6;
        boolean hovered = inside(mouseX, mouseY, x, y, 80, 15);

        fill(context, x, y, 80, 15, typingSearch || hovered ? 0xFF252B36 : 0xFF1A1E26);
        border(context, x, y, 80, 15, BORDER);

        String value = searchText.isEmpty() && !typingSearch ? "Search" : fit(searchText, 59);
        context.drawTextWithShadow(textRenderer, Text.literal(value), x + 4, y + 4, typingSearch ? WHITE : MUTED);
        context.drawTextWithShadow(textRenderer, Text.literal("⌕"), x + 67, y + 3, MUTED);

        if (typingSearch && System.currentTimeMillis() % 1000L < 500L) {
            int caretIndex = Math.min(cursorPosition, searchText.length());
            int caretX = x + 4 + textRenderer.getWidth(fit(searchText.substring(0, caretIndex), 59));
            context.fill(caretX, y + 3, caretX + 1, y + 12, WHITE);
        }
    }

    private void drawCategoryTitle(DrawContext context) {
        context.drawTextWithShadow(
                textRenderer,
                Text.literal(category.getDisplayName()),
                guiX + 95,
                guiY + 36,
                TEXT
        );
    }

    private void drawModules(DrawContext context, int mouseX, int mouseY) {
        int panelX = guiX + SIDEBAR + 9;
        int panelY = guiY + 39;
        int panelW = WIDTH - SIDEBAR - 15;
        int panelH = HEIGHT - 47;

        fill(context, panelX, panelY, panelW, panelH, 0x5A0A0D12);
        border(context, panelX, panelY, panelW, panelH, BORDER);

        List<Module> modules = visibleModules();
        List<PlacedModule> placed = layout(modules);
        double maxScroll = maxScroll(placed, panelH);
        scrollTarget = clamp(scrollTarget, 0.0D, maxScroll);
        scroll = clamp(scroll, 0.0D, maxScroll);

        for (PlacedModule placedModule : placed) {
            float cardX = guiX + 95 + placedModule.column * (COLUMN_WIDTH + COLUMN_GAP);
            float cardY = guiY + 39 + placedModule.offset - (float) scroll;

            if (cardY + moduleHeight(placedModule.module) < panelY || cardY > panelY + panelH) {
                continue;
            }

            drawModule(context, placedModule.module, (int) cardX, (int) cardY, mouseX, mouseY);
        }

        if (maxScroll > 0.0D) {
            int barX = panelX + panelW - 4;
            int barY = panelY + 4;
            int barH = panelH - 8;
            int thumbH = Math.max(18, (int) (barH * (barH / (barH + maxScroll))));
            int thumbY = barY + (int) ((barH - thumbH) * (scroll / maxScroll));
            context.fill(barX, barY, barX + 2, barY + barH, 0x443A414D);
            context.fill(barX, thumbY, barX + 2, thumbY + thumbH, ACCENT);
        }
    }

    private void drawModule(DrawContext context, Module module, int x, int y, int mouseX, int mouseY) {
        int height = moduleHeight(module);
        boolean hovered = inside(mouseX, mouseY, x, y, COLUMN_WIDTH, 18);

        fill(context, x, y, COLUMN_WIDTH, height, 0x4A0B0E13);
        border(context, x, y, COLUMN_WIDTH, height, BORDER);
        fill(context, x, y, COLUMN_WIDTH, 18, module.isEnabled() ? CARD_ENABLED : hovered ? CARD_HOVER : CARD);

        if (module.isEnabled()) {
            context.fill(x, y, x + 2, y + 18, ACCENT);
        }

        context.drawTextWithShadow(
                textRenderer,
                Text.literal(fit(module.getName(), 92)),
                x + 9,
                y + 4,
                module.isEnabled() ? WHITE : TEXT
        );

        drawBind(context, module, x + COLUMN_WIDTH - 30, y + 4);

        int rowY = y + 24;
        for (com.essde342.triggerbot.ui.ISetting setting : module.getSettings()) {
            if (setting instanceof BooleanSetting) {
                drawBoolean(context, (BooleanSetting) setting, x, rowY, mouseX, mouseY);
                rowY += 18;
            } else if (setting instanceof NumberSetting) {
                drawNumber(context, (NumberSetting) setting, x, rowY, mouseX, mouseY);
                rowY += 28;
            }
        }
    }

    private void drawBind(DrawContext context, Module module, int x, int y) {
        String key = bindingModule == module ? "..." : module.getBind() < 0 ? "f" : keyName(module.getBind());
        int badgeW = Math.max(10, textRenderer.getWidth(key) + 6);
        int badgeX = x - badgeW + 12;

        fill(context, badgeX, y, badgeW, 11, bindingModule == module ? 0x557D8CFF : 0x55282E39);
        context.drawTextWithShadow(
                textRenderer,
                Text.literal(key),
                badgeX + 3,
                y + 2,
                bindingModule == module ? ACCENT : MUTED
        );
    }

    private void drawBoolean(DrawContext context, BooleanSetting setting, int x, int y, int mouseX, int mouseY) {
        boolean hovered = inside(mouseX, mouseY, x, y, COLUMN_WIDTH, 18);
        context.drawTextWithShadow(
                textRenderer,
                Text.literal(fit(setting.getName(), 94)),
                x + 9,
                y + 4,
                hovered ? WHITE : TEXT
        );

        int switchX = x + COLUMN_WIDTH - 31;
        int switchY = y + 2;
        fill(context, switchX, switchY, 23, 13, setting.isEnabled() ? 0xFF5968D0 : 0xFF3C424D);
        context.fill(
                setting.isEnabled() ? switchX + 14 : switchX + 3,
                switchY + 3,
                setting.isEnabled() ? switchX + 20 : switchX + 9,
                switchY + 10,
                setting.isEnabled() ? WHITE : MUTED
        );
    }

    private void drawNumber(DrawContext context, NumberSetting setting, int x, int y, int mouseX, int mouseY) {
        String value = format(setting);
        context.drawTextWithShadow(textRenderer, Text.literal(fit(setting.getName(), 82)), x + 9, y + 2, TEXT);

        int valueWidth = textRenderer.getWidth(value);
        context.drawTextWithShadow(
                textRenderer,
                Text.literal(value),
                x + COLUMN_WIDTH - valueWidth - 8,
                y + 2,
                MUTED
        );

        int sliderX = x + 9;
        int sliderY = y + 17;
        int sliderW = COLUMN_WIDTH - 18;
        double progress = (setting.getDoubleValue() - setting.getMin()) /
                Math.max(0.000001D, setting.getMax() - setting.getMin());
        progress = clamp(progress, 0.0D, 1.0D);

        context.fill(sliderX, sliderY, sliderX + sliderW, sliderY + 3, 0xFF3A404A);
        int filled = (int) Math.round(sliderW * progress);
        context.fill(sliderX, sliderY, sliderX + filled, sliderY + 3, 0xFF5968D0);
        context.fill(sliderX + filled - 2, sliderY - 2, sliderX + filled + 3, sliderY + 6, WHITE);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (bindingModule != null) {
            return true;
        }

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            for (int i = 0; i < categories.size(); i++) {
                int cy = guiY + 60 + i * 19;
                if (inside(mouseX, mouseY, guiX + 6, cy, 73, 17)) {
                    category = categories.get(i);
                    scrollTarget = 0.0D;
                    return true;
                }
            }
        }

        int searchX = guiX + 300;
        int searchY = guiY + 6;
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && inside(mouseX, mouseY, searchX, searchY, 80, 15)) {
            typingSearch = true;
            cursorPosition = searchText.length();
            return true;
        }

        if (typingSearch && !inside(mouseX, mouseY, searchX, searchY, 80, 15)) {
            typingSearch = false;
        }

        List<PlacedModule> placed = layout(visibleModules());
        for (PlacedModule placedModule : placed) {
            Module module = placedModule.module;
            float x = guiX + 95 + placedModule.column * (COLUMN_WIDTH + COLUMN_GAP);
            float y = guiY + 39 + placedModule.offset - (float) scroll;

            if (!inside(mouseX, mouseY, x, y, COLUMN_WIDTH, moduleHeight(module))) {
                continue;
            }

            int localX = (int) (mouseX - x);
            int localY = (int) (mouseY - y);

            if (localY < 18 && localX >= COLUMN_WIDTH - 35) {
                bindingModule = module;
                return true;
            }

            if (localY < 18 && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                if ("Alt Manager".equals(module.getName())) {
                    mc.setScreen(AltManagerScreen.create(this));
                } else {
                    module.toggled();
                    TriggerBotClient.saveConfig();
                }
                return true;
            }

            int rowY = 24;
            for (com.essde342.triggerbot.ui.ISetting setting : module.getSettings()) {
                if (setting instanceof BooleanSetting) {
                    if (localY >= rowY && localY < rowY + 18 && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                        ((BooleanSetting) setting).toggle();
                        TriggerBotClient.saveConfig();
                        return true;
                    }
                    rowY += 18;
                } else if (setting instanceof NumberSetting) {
                    if (localY >= rowY && localY < rowY + 28 && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                        draggingSetting = (NumberSetting) setting;
                        draggingSlider = true;
                        setNumberFromMouse(draggingSetting, mouseX, x + 9, COLUMN_WIDTH - 18);
                        return true;
                    }
                    rowY += 28;
                }
            }

            return true;
        }

        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingSlider && draggingSetting != null) {
            List<PlacedModule> placed = layout(visibleModules());
            for (PlacedModule placedModule : placed) {
                if (placedModule.module.getSettings().contains(draggingSetting)) {
                    float x = guiX + 95 + placedModule.column * (COLUMN_WIDTH + COLUMN_GAP);
                    setNumberFromMouse(draggingSetting, mouseX, x + 9, COLUMN_WIDTH - 18);
                    return true;
                }
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggingSlider && draggingSetting != null) {
            TriggerBotClient.saveConfig();
        }
        draggingSlider = false;
        draggingSetting = null;
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollTarget = clamp(scrollTarget - verticalAmount * 20.0D, 0.0D, maxScroll(visibleModules(), HEIGHT - 47));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (bindingModule != null) {
            bindingModule.setBind(keyCode == GLFW.GLFW_KEY_ESCAPE ? -1 : keyCode);
            TriggerBotClient.saveConfig();
            bindingModule = null;
            return true;
        }

        if (typingSearch) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER) {
                typingSearch = false;
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (cursorPosition > 0 && !searchText.isEmpty()) {
                    searchText = searchText.substring(0, cursorPosition - 1) + searchText.substring(cursorPosition);
                    cursorPosition--;
                }
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_LEFT) {
                cursorPosition = Math.max(0, cursorPosition - 1);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_RIGHT) {
                cursorPosition = Math.min(searchText.length(), cursorPosition + 1);
                return true;
            }
            if (Screen.hasControlDown() && keyCode == GLFW.GLFW_KEY_A) {
                cursorPosition = searchText.length();
                return true;
            }
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            typingSearch = false;
            close();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (!typingSearch || Character.isISOControl(chr) || searchText.length() >= 32) {
            return !typingSearch || super.charTyped(chr, modifiers);
        }

        searchText = searchText.substring(0, cursorPosition) + chr + searchText.substring(cursorPosition);
        cursorPosition++;
        scrollTarget = 0.0D;
        return true;
    }

    @Override
    public void close() {
        typingSearch = false;
        bindingModule = null;
        draggingSlider = false;
        draggingSetting = null;
        super.close();
    }

    private List<Module> visibleModules() {
        List<Module> result = new ArrayList<>();
        String filter = searchText.trim().toLowerCase(Locale.ROOT);

        for (Module module : ModuleManager.getByCategory(category)) {
            if (filter.isEmpty() || module.getName().toLowerCase(Locale.ROOT).contains(filter)) {
                result.add(module);
            }
        }
        return result;
    }

    private List<PlacedModule> layout(List<Module> modules) {
        float[] offsets = new float[]{0.0F, 0.0F};
        List<PlacedModule> result = new ArrayList<>();

        for (int i = modules.size() - 1; i >= 0; i--) {
            int column = (modules.size() - 1 - i) % 2;
            Module module = modules.get(i);
            result.add(new PlacedModule(module, column, offsets[column]));
            offsets[column] += moduleHeight(module) + 9.0F;
        }
        return result;
    }

    private double maxScroll(List<Module> modules, double viewport) {
        float[] offsets = new float[]{0.0F, 0.0F};
        for (int i = modules.size() - 1; i >= 0; i--) {
            int column = (modules.size() - 1 - i) % 2;
            offsets[column] += moduleHeight(modules.get(i)) + 9.0F;
        }
        return Math.max(0.0D, Math.max(offsets[0], offsets[1]) - viewport);
    }

    private int moduleHeight(Module module) {
        int height = 18 + 12;
        for (com.essde342.triggerbot.ui.ISetting setting : module.getSettings()) {
            if (setting instanceof BooleanSetting) {
                height += 18;
            } else if (setting instanceof NumberSetting) {
                height += 28;
            }
        }
        return height;
    }

    private void setNumberFromMouse(NumberSetting setting, double mouseX, double sliderX, double sliderWidth) {
        double progress = clamp((mouseX - sliderX) / sliderWidth, 0.0D, 1.0D);
        double value = setting.getMin() + progress * (setting.getMax() - setting.getMin());
        double increment = setting.getIncrement();

        if (increment > 0.0D) {
            value = Math.round(value / increment) * increment;
        }
        setting.setValue(value);
    }

    private String keyName(int keyCode) {
        String key = GLFW.glfwGetKeyName(keyCode, 0);
        return key == null || key.isEmpty() ? String.valueOf(keyCode) : key.toUpperCase(Locale.ROOT);
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
        if (name.contains("Range")) {
            return String.format(Locale.ROOT, "%.1f", value);
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

    private String fit(String value, int maxWidth) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        if (textRenderer.getWidth(value) <= maxWidth) {
            return value;
        }

        String suffix = "...";
        int suffixWidth = textRenderer.getWidth(suffix);
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < value.length(); i++) {
            String next = builder.toString() + value.charAt(i);
            if (textRenderer.getWidth(next) + suffixWidth > maxWidth) {
                break;
            }
            builder.append(value.charAt(i));
        }

        return builder + suffix;
    }

    private void fill(DrawContext context, int x, int y, int w, int h, int color) {
        context.fill(x, y, x + w, y + h, color);
    }

    private void border(DrawContext context, int x, int y, int w, int h, int color) {
        context.fill(x, y, x + w, y + 1, color);
        context.fill(x, y + h - 1, x + w, y + h, color);
        context.fill(x, y, x + 1, y + h, color);
        context.fill(x + w - 1, y, x + w, y + h, color);
    }

    private boolean inside(double mouseX, double mouseY, double x, double y, double w, double h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static final class PlacedModule {
        private final Module module;
        private final int column;
        private final float offset;

        private PlacedModule(Module module, int column, float offset) {
            this.module = module;
            this.column = column;
            this.offset = offset;
        }
    }
}
