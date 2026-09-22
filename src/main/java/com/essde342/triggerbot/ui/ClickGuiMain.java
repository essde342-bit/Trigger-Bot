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

import java.util.List;
import java.util.Locale;

public final class ClickGuiMain extends Screen {
    private static final int UI_W = 440;
    private static final int UI_H = 282;

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
    private Module pendingModule;

    private boolean scrollingSettings;
    private boolean scrollingModules;
    private boolean draggingSettingsScrollBar;
    private boolean draggingModuleScrollBar;
    private boolean movedDuringPress;

    private double pressX;
    private double pressY;
    private double settingsScrollStart;
    private double moduleScrollStart;
    private double scrollGrabOffset;

    private double settingsScroll;
    private double moduleScroll;

    private float scale = 1.0F;
    private int originX;
    private int originY;

    public ClickGuiMain() {
        super(Text.of("Astra Client"));
        ModuleManager.moduleRegister();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        recalculateScale();

        context.fill( 0, 0, width, height, BACKDROP);

        context.getMatrices().push();
        context.getMatrices().translate(originX, originY, 0.0D);
        context.getMatrices().scale(scale, scale, 1.0F);

        drawMain(context, localX(mouseX), localY(mouseY));

        context.getMatrices().pop();
    }

    private void recalculateScale() {
        float fit = Math.min(
                (width - 10.0F) / UI_W,
                (height - 10.0F) / UI_H
        );

        scale = Math.min(1.0F, fit);
        if (scale < 0.50F) {
            scale = 0.50F;
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

    private void drawMain(DrawContext context, int mx, int my) {
        box(context, 0, 0, UI_W, UI_H, PANEL);
        outline(context, 0, 0, UI_W, UI_H, BORDER);

        drawText(context, "ASTRA", 14, 11, TEXT);
        drawText(context, "CLIENT 1.0.0", 52, 13, MUTED);
        drawText(context, "RSHIFT", UI_W - 51, 13, MUTED);

        context.fill( 0, 37, UI_W, 38, BORDER);

        if (settingsModule == null) {
            drawModuleView(context, mx, my);
        } else {
            drawSettingsView(context, mx, my);
        }
    }

    private void drawModuleView(DrawContext context, int mx, int my) {
        drawCategories(context, mx, my);

        int listX = 12;
        int listY = 82;
        int listW = UI_W - 24;
        int listH = UI_H - 104;

        box(context, listX, listY, listW, listH, BACKDROP);
        outline(context, listX, listY, listW, listH, BORDER);

        drawText(context, category.getDisplayName(), listX + 10, listY + 9, TEXT);

        List<Module> modules = ModuleManager.getByCategory(category);

        int rowTop = listY + 28;
        int rowBottom = listY + listH - 5;
        int rowY = rowTop - (int) moduleScroll;

        for (Module module : modules) {
            if (rowY + 33 >= rowTop && rowY <= rowBottom) {
                boolean hover = inside(mx, my, listX + 7, rowY, listW - 14, 33);
                boolean enabled = module.isEnabled();

                box(
                        context,
                        listX + 7,
                        rowY,
                        listW - 14,
                        33,
                        hover ? HOVER : CARD
                );

                if (enabled) {
                    context.fill(
                            listX + 7,
                            rowY,
                            listX + 10,
                            rowY + 33,
                            ACCENT
                    );
                }

                drawText(
                        context,
                        fit(module.getName(), 150),
                        listX + 16,
                        rowY + 5,
                        TEXT
                );

                drawText(
                        context,
                        fit(module.getDesc(), 185),
                        listX + 16,
                        rowY + 19,
                        MUTED
                );

                drawSwitch(
                        context,
                        listX + listW - 74,
                        rowY + 7,
                        enabled
                );

                drawDots(
                        context,
                        listX + listW - 20,
                        rowY + 8,
                        settingsModule == module
                );
            }

            rowY += 38;
        }

        drawModuleScrollbar(context, listX, listY, listW, rowTop, rowBottom, modules);

        drawText(
                context,
                "Drag anywhere in the list • swipe on sliders to scroll",
                14,
                UI_H - 16,
                MUTED
        );
    }

    private void drawCategories(DrawContext context, int mx, int my) {
        int x = 10;
        int y = 46;
        int gap = 5;
        int count = Category.values().length;
        int w = (UI_W - 20 - gap * (count - 1)) / count;

        for (Category current : Category.values()) {
            boolean selected = current == category;
            boolean hover = inside(mx, my, x, y, w, 27);

            box(
                    context,
                    x,
                    y,
                    w,
                    27,
                    selected ? ACCENT_DARK : (hover ? HOVER : PANEL_2)
            );

            drawCentered(
                    context,
                    current.getDisplayName(),
                    x + w / 2,
                    y + 9,
                    selected ? TEXT : MUTED
            );

            x += w + gap;
        }
    }

    private void drawModuleScrollbar(
            DrawContext context,
            int listX,
            int listY,
            int listW,
            int rowTop,
            int rowBottom,
            List<Module> modules
    ) {
        double max = getMaxModuleScroll(modules);
        if (max <= 0.0D) {
            return;
        }

        int viewport = rowBottom - rowTop;
        int total = Math.max(1, modules.size() * 38);
        int barX = listX + listW - 6;
        int barTop = rowTop;
        int barBottom = rowBottom;
        int barHeight = Math.max(24, (int) ((viewport / (double) total) * viewport));

        double progress = moduleScroll / max;
        int barY = barTop + (int) ((viewport - barHeight) * progress);

        box(context, barX, barTop, 4, viewport, PANEL_2);
        box(
                context,
                barX,
                barY,
                4,
                Math.min(barHeight, viewport),
                draggingModuleScrollBar ? ACCENT : ACCENT_DARK
        );
    }

    private void drawSettingsView(DrawContext context, int mx, int my) {
        int x = 10;
        int y = 46;
        int w = UI_W - 20;
        int h = UI_H - 57;

        box(context, x, y, w, h, BACKDROP);
        outline(context, x, y, w, h, BORDER);

        drawText(context, fit(settingsModule.getName(), w - 82), x + 11, y + 9, TEXT);
        drawText(context, "SETTINGS", x + 11, y + 24, MUTED);

        box(context, x + w - 30, y + 7, 22, 22, CARD);
        drawCentered(context, "X", x + w - 19, y + 14, MUTED);

        int contentX = x + 9;
        int contentW = w - 18;
        int top = y + 40;
        int bottom = y + h - 32;
        int cursor = top - (int) settingsScroll;

        for (com.essde342.triggerbot.ui.ISetting setting : settingsModule.getSettings()) {
            if (setting instanceof BooleanSetting) {
                int itemH = 31;
                if (cursor + itemH >= top && cursor <= bottom) {
                    drawBoolean(
                            context,
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
                int itemH = 53;
                if (cursor + itemH >= top && cursor <= bottom) {
                    drawNumber(
                            context,
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

        drawSettingsScrollbar(context, x, w, top, bottom);

        int bindY = y + h - 25;
        box(context, contentX, bindY, contentW, 20, PANEL_2);
        drawText(context, "BIND", contentX + 7, bindY + 6, MUTED);

        String bind = bindingModule == settingsModule
                ? "PRESS KEY"
                : settingsModule.getBind() < 0
                ? "NONE"
                : keyName(settingsModule.getBind());

        String fittedBind = fit(bind, 82);
        drawText(
                context,
                fittedBind,
                contentX + contentW - 7 - mc.textRenderer.getWidth(fittedBind),
                bindY + 6,
                TEXT
        );
    }

    private void drawSettingsScrollbar(
            DrawContext context,
            int panelX,
            int panelW,
            int top,
            int bottom
    ) {
        double max = getMaxSettingsScroll();
        if (max <= 0.0D) {
            return;
        }

        int viewport = bottom - top;
        int total = viewport + (int) max;
        int barX = panelX + panelW - 6;
        int barHeight = Math.max(24, (int) ((viewport / (double) total) * viewport));

        double progress = settingsScroll / max;
        int barY = top + (int) ((viewport - barHeight) * progress);

        box(context, barX, top, 4, viewport, PANEL_2);
        box(
                context,
                barX,
                barY,
                4,
                Math.min(barHeight, viewport),
                draggingSettingsScrollBar ? ACCENT : ACCENT_DARK
        );
    }

    private void drawBoolean(
            DrawContext context,
            BooleanSetting setting,
            int x,
            int y,
            int w,
            int mx,
            int my
    ) {
        box(
                context,
                x,
                y,
                w,
                31,
                inside(mx, my, x, y, w, 31) ? HOVER : CARD
        );

        drawText(context, fit(setting.getName(), w - 62), x + 9, y + 9, TEXT);
        drawSwitch(context, x + w - 38, y + 7, setting.isEnabled());
    }

    private void drawNumber(
            DrawContext context,
            NumberSetting setting,
            int x,
            int y,
            int w,
            int mx,
            int my
    ) {
        box(
                context,
                x,
                y,
                w,
                53,
                inside(mx, my, x, y, w, 53) ? HOVER : CARD
        );

        String value = format(setting);
        drawText(context, fit(setting.getName(), w - 95), x + 9, y + 6, TEXT);
        drawText(
                context,
                value,
                x + w - 9 - mc.textRenderer.getWidth(value),
                y + 6,
                MUTED
        );

        int sliderX = x + 9;
        int sliderY = y + 34;
        int sliderW = w - 18;

        context.fill(
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
            context.fill(
                    sliderX,
                    sliderY,
                    sliderX + fill,
                    sliderY + 5,
                    ACCENT_DARK
            );
        }

        context.fill(
                sliderX + fill - 2,
                sliderY - 3,
                sliderX + fill + 3,
                sliderY + 8,
                ACCENT
        );
    }

    private void drawSwitch(DrawContext context, int x, int y, boolean enabled) {
        box(context, x, y, 30, 17, enabled ? ACCENT_DARK : OFF);

        context.fill(
                enabled ? x + 19 : x + 3,
                y + 4,
                enabled ? x + 26 : x + 10,
                y + 13,
                enabled ? TEXT : MUTED
        );
    }

    private void drawDots(DrawContext context, int centerX, int y, boolean selected) {
        box(
                context,
                centerX - 10,
                y - 3,
                20,
                25,
                selected ? ACCENT_DARK : PANEL_2
        );

        for (int offset : new int[]{1, 8, 15}) {
            context.fill(
                    centerX - 1,
                    y + offset,
                    centerX + 2,
                    y + offset + 3,
                    TEXT
            );
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = localX(mouseX);
        int y = localY(mouseY);

        if (!isPointerButton(button)) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        if (settingsModule != null) {
            if (inside(x, y, UI_W - 42, 44, 30, 31)) {
                closeSettings();
                return true;
            }

            if (beginSettingsScrollbarDrag(x, y)) {
                return true;
            }

            if (handleSettingsPress(x, y)) {
                return true;
            }

            if (inside(x, y, 10, 46, UI_W - 20, UI_H - 57)) {
                beginSettingsScroll(y);
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
                moduleScroll = 0.0D;
                pendingModule = null;
                return true;
            }

            categoryX += categoryW + 5;
        }

        if (beginModuleScrollbarDrag(x, y)) {
            return true;
        }

        int listX = 19;
        int listW = UI_W - 38;
        int rowTop = 110;
        int rowY = rowTop - (int) moduleScroll;

        for (Module module : ModuleManager.getByCategory(category)) {
            if (inside(x, y, listX, rowY, listW, 33)) {
                int dotX = listX + listW - 20;

                if (inside(x, y, dotX - 18, rowY - 3, 36, 39)) {
                    if ("Alt Manager".equals(module.getName())) {
                        mc.setScreen(AltManagerScreen.create(this));
                        return true;
                    }

                    settingsModule = module;
                    settingsScroll = 0.0D;
                    bindingModule = null;
                    return true;
                }

                pendingModule = module;
                movedDuringPress = false;
                pressX = x;
                pressY = y;
                moduleScrollStart = moduleScroll;
                return true;
            }

            rowY += 38;
        }

        if (inside(x, y, 12, 82, UI_W - 24, UI_H - 104)) {
            beginModuleScroll(y);
            return true;
        }

        return true;
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
                    pressX = x;
                    pressY = y;
                    return true;
                }
                cursor += 36;
            } else if (setting instanceof NumberSetting) {
                if (inside(x, y, contentX, cursor, contentW, 53)) {
                    draggingSlider = (NumberSetting) setting;
                    scrollingSettings = false;
                    movedDuringPress = false;
                    pressX = x;
                    pressY = y;
                    updateSlider(
                            draggingSlider,
                            x,
                            contentX + 9,
                            contentW - 18
                    );
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

    private boolean beginModuleScrollbarDrag(int x, int y) {
        List<Module> modules = ModuleManager.getByCategory(category);
        double max = getMaxModuleScroll(modules);
        if (max <= 0.0D) {
            return false;
        }

        int listX = 12;
        int listY = 82;
        int listW = UI_W - 24;
        int rowTop = listY + 28;
        int rowBottom = listY + listH() - 5;
        int viewport = rowBottom - rowTop;
        int total = Math.max(1, modules.size() * 38);
        int barX = listX + listW - 6;
        int barHeight = Math.max(24, (int) ((viewport / (double) total) * viewport));
        int barY = rowTop + (int) ((viewport - barHeight) * (moduleScroll / max));

        if (!inside(x, y, barX - 6, rowTop, 16, viewport)) {
            return false;
        }

        draggingModuleScrollBar = true;

        if (y < barY || y > barY + barHeight) {
            double progress = clamp(
                    (y - rowTop - barHeight * 0.5D) /
                            Math.max(1.0D, viewport - barHeight),
                    0.0D,
                    1.0D
            );
            moduleScroll = progress * max;
            scrollGrabOffset = barHeight * 0.5D;
        } else {
            scrollGrabOffset = y - barY;
        }

        return true;
    }

    private boolean beginSettingsScrollbarDrag(int x, int y) {
        double max = getMaxSettingsScroll();
        if (max <= 0.0D) {
            return false;
        }

        int panelX = 10;
        int panelW = UI_W - 20;
        int top = 86;
        int bottom = 46 + (UI_H - 57) - 32;
        int viewport = bottom - top;
        int total = viewport + (int) max;
        int barX = panelX + panelW - 6;
        int barHeight = Math.max(24, (int) ((viewport / (double) total) * viewport));
        int barY = top + (int) ((viewport - barHeight) * (settingsScroll / max));

        if (!inside(x, y, barX - 6, top, 16, viewport)) {
            return false;
        }

        draggingSettingsScrollBar = true;

        if (y < barY || y > barY + barHeight) {
            double progress = clamp(
                    (y - top - barHeight * 0.5D) /
                            Math.max(1.0D, viewport - barHeight),
                    0.0D,
                    1.0D
            );
            settingsScroll = progress * max;
            scrollGrabOffset = barHeight * 0.5D;
        } else {
            scrollGrabOffset = y - barY;
        }

        return true;
    }

    private void beginModuleScroll(int y) {
        scrollingModules = true;
        movedDuringPress = false;
        pressY = y;
        moduleScrollStart = moduleScroll;
    }

    private void beginSettingsScroll(int y) {
        scrollingSettings = true;
        movedDuringPress = false;
        pressY = y;
        settingsScrollStart = settingsScroll;
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

        if (draggingModuleScrollBar) {
            moveModuleScrollbar(y);
            return true;
        }

        if (draggingSettingsScrollBar) {
            moveSettingsScrollbar(y);
            return true;
        }

        if (draggingSlider != null) {
            double dx = Math.abs(x - pressX);
            double dy = Math.abs(y - pressY);

            if (dy > 5.0D && dy > dx + 2.0D) {
                draggingSlider = null;
                pendingBoolean = null;
                movedDuringPress = true;
                beginSettingsScroll((int) pressY);
            } else {
                int sliderX = 28;
                int sliderWidth = 404;
                updateSlider(draggingSlider, x, sliderX, sliderWidth);
                return true;
            }
        }

        if (pendingBoolean != null || scrollingSettings) {
            if (Math.abs(y - pressY) > 4) {
                movedDuringPress = true;
                scrollingSettings = true;
            }

            if (scrollingSettings) {
                double next = settingsScrollStart - (y - pressY);
                settingsScroll = clamp(next, 0.0D, getMaxSettingsScroll());
                return true;
            }
        }

        if (pendingModule != null || scrollingModules) {
            if (Math.abs(y - pressY) > 4) {
                movedDuringPress = true;
                scrollingModules = true;
            }

            if (scrollingModules) {
                double next = moduleScrollStart - (y - pressY);
                moduleScroll = clamp(
                        next,
                        0.0D,
                        getMaxModuleScroll(ModuleManager.getByCategory(category))
                );
                return true;
            }
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    private void moveModuleScrollbar(int y) {
        List<Module> modules = ModuleManager.getByCategory(category);
        double max = getMaxModuleScroll(modules);
        if (max <= 0.0D) {
            return;
        }

        int top = 110;
        int bottom = 82 + listH() - 5;
        int viewport = bottom - top;
        int total = Math.max(1, modules.size() * 38);
        int barHeight = Math.max(24, (int) ((viewport / (double) total) * viewport));

        double progress = (y - top - scrollGrabOffset)
                / Math.max(1.0D, viewport - barHeight);

        moduleScroll = clamp(progress * max, 0.0D, max);
    }

    private void moveSettingsScrollbar(int y) {
        double max = getMaxSettingsScroll();
        if (max <= 0.0D) {
            return;
        }

        int top = 86;
        int bottom = 46 + (UI_H - 57) - 32;
        int viewport = bottom - top;
        int total = viewport + (int) max;
        int barHeight = Math.max(24, (int) ((viewport / (double) total) * viewport));

        double progress = (y - top - scrollGrabOffset)
                / Math.max(1.0D, viewport - barHeight);

        settingsScroll = clamp(progress * max, 0.0D, max);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (pendingBoolean != null && !movedDuringPress) {
            pendingBoolean.toggle();
            TriggerBotClient.saveConfig();
        }

        if (pendingModule != null && !movedDuringPress) {
            pendingModule.toggled();
            TriggerBotClient.saveConfig();
        }

        if (draggingSlider != null) {
            TriggerBotClient.saveConfig();
        }

        draggingSlider = null;
        pendingBoolean = null;
        pendingModule = null;
        scrollingSettings = false;
        scrollingModules = false;
        draggingSettingsScrollBar = false;
        draggingModuleScrollBar = false;
        movedDuringPress = false;

        return true;
    }

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double horizontalAmount,
            double verticalAmount
    ) {
        if (settingsModule != null) {
            settingsScroll = clamp(
                    settingsScroll - verticalAmount * 28.0D,
                    0.0D,
                    getMaxSettingsScroll()
            );
        } else {
            moduleScroll = clamp(
                    moduleScroll - verticalAmount * 30.0D,
                    0.0D,
                    getMaxModuleScroll(ModuleManager.getByCategory(category))
            );
        }

        return true;
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
            } else {
                mc.setScreen(null);
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
        pendingModule = null;
        scrollingSettings = false;
        movedDuringPress = false;
        settingsScroll = 0.0D;
        draggingSettingsScrollBar = false;
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

    private int listH() {
        return UI_H - 104;
    }

    private double getMaxModuleScroll(List<Module> modules) {
        if (modules == null || modules.isEmpty()) {
            return 0.0D;
        }

        int total = modules.size() * 38;
        int viewport = listH() - 32;
        return Math.max(0.0D, total - viewport);
    }

    private double getMaxSettingsScroll() {
        if (settingsModule == null) {
            return 0.0D;
        }

        int total = 0;
        for (com.essde342.triggerbot.ui.ISetting setting : settingsModule.getSettings()) {
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

    private void drawText(
            DrawContext context,
            String text,
            int x,
            int y,
            int color
    ) {
        context.drawText(mc.textRenderer, text, x, y, color, false);
    }

    private void drawCentered(
            DrawContext context,
            String text,
            int centerX,
            int y,
            int color
    ) {
        drawText(
                context,
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

    private boolean isPointerButton(int button) {
        return button == GLFW.GLFW_MOUSE_BUTTON_LEFT
                || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private void box(
            DrawContext context,
            int x,
            int y,
            int width,
            int height,
            int color
    ) {
        context.fill(
                x,
                y,
                x + width,
                y + height,
                color
        );
    }

    private void outline(
            DrawContext context,
            int x,
            int y,
            int width,
            int height,
            int color
    ) {
        context.fill( x, y, x + width, y + 1, color);
        context.fill(
                x,
                y + height - 1,
                x + width,
                y + height,
                color
        );
        context.fill( x, y, x + 1, y + height, color);
        context.fill(
                x + width - 1,
                y,
                x + width,
                y + height,
                color
        );
    }
}
