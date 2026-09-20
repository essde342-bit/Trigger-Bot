package com.essde342.triggerbot;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.text.LiteralText;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Glass-style Click GUI ported from the supplied Relake/Platon menu source.
 * The original GUI depended on Relake-specific module/settings/render classes,
 * so the visual system is kept while controls are bound directly to TriggerBot.
 */
public final class TriggerBotConfigScreen extends Screen {
    private static final int PANEL_WIDTH = 900;
    private static final int PANEL_HEIGHT = 600;
    private static final int SIDEBAR_WIDTH = 250;
    private static final int ACCENT = 0xFF8B6CFF;

    private final Screen parent;
    private final List<HitBox> hitBoxes = new ArrayList<HitBox>();

    private Category category = Category.COMBAT;
    private float scroll = 0.0F;
    private String search = "";
    private boolean searchFocused = false;
    private int activeSlider = 0;
    private long openedAt = System.currentTimeMillis();

    private int panelX;
    private int panelY;

    private TriggerBotConfigScreen(Screen parent) {
        super(new LiteralText("Trigger Bot"));
        this.parent = parent;
    }

    public static Screen create(Screen parent) {
        return new TriggerBotConfigScreen(parent);
    }

    private enum Category {
        COMBAT("COMBAT", "C"),
        RENDER("RENDER", "R"),
        PERFORMANCE("PERFORMANCE", "P"),
        PLAYER("PLAYER", "U");

        private final String title;
        private final String icon;

        Category(String title, String icon) {
            this.title = title;
            this.icon = icon;
        }
    }

    private static final class HitBox {
        final float x;
        final float y;
        final float width;
        final float height;
        final Runnable action;

        HitBox(float x, float y, float width, float height, Runnable action) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.action = action;
        }

        boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + width
                    && mouseY >= y && mouseY <= y + height;
        }
    }

    @Override
    protected void init() {
        openedAt = System.currentTimeMillis();
        scroll = 0.0F;
        activeSlider = 0;
        searchFocused = false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        float open = MathHelper.clamp(
                (System.currentTimeMillis() - openedAt) / 220.0F,
                0.0F,
                1.0F
        );
        open = 1.0F - (1.0F - open) * (1.0F - open) * (1.0F - open);

        hitBoxes.clear();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        fill(matrices, 0, 0, width, height, 0xA8000000);

        int baseX = width / 2 - PANEL_WIDTH / 2;
        int baseY = height / 2 - PANEL_HEIGHT / 2;
        panelX = baseX;
        panelY = baseY + Math.round((1.0F - open) * 36.0F);

        matrices.push();
        matrices.translate(width / 2.0F, height / 2.0F, 0.0F);
        float scale = 0.92F + 0.08F * open;
        matrices.scale(scale, scale, 1.0F);
        matrices.translate(-width / 2.0F, -height / 2.0F, 0.0F);

        drawShadow(matrices, panelX - 8, panelY - 8, PANEL_WIDTH + 16, PANEL_HEIGHT + 16, 0x90000000, 18);
        drawPanel(matrices, panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, 0xE81E1E24);
        drawSidebar(matrices, mouseX, mouseY);
        drawMain(matrices, mouseX, mouseY);

        matrices.pop();
        RenderSystem.disableBlend();
    }

    private void drawSidebar(MatrixStack matrices, int mouseX, int mouseY) {
        int x = panelX;
        int y = panelY;

        drawPanel(matrices, x, y, SIDEBAR_WIDTH, PANEL_HEIGHT, 0xE82A2A31);
        drawOutline(matrices, x, y, SIDEBAR_WIDTH, PANEL_HEIGHT, 0x904F4F5A);

        text(matrices, "T", x + 20, y + 18, 0xFFFFFFFF, 30);
        text(matrices, "TRIGGER BOT", x + 62, y + 24, 0xFFFFFFFF, 18);
        text(matrices, "CLICK GUI", x + 63, y + 43, 0xFF9696A8, 9);

        drawSearch(matrices, x + 15, y + 72, SIDEBAR_WIDTH - 30, 34, mouseX, mouseY);

        int cy = y + 118;
        for (final Category value : Category.values()) {
            boolean selected = category == value;
            boolean hovered = inside(mouseX, mouseY, x + 10, cy, SIDEBAR_WIDTH - 20, 45);

            drawPanel(matrices, x + 10, cy, SIDEBAR_WIDTH - 20, 45,
                    selected ? 0xC04C3A66 : (hovered ? 0x7843434D : 0x5A35353D));
            drawOutline(matrices, x + 10, cy, SIDEBAR_WIDTH - 20, 45,
                    selected ? 0xC08B6CFF : 0x704D4D57);

            text(matrices, value.icon, x + 22, cy + 13,
                    selected ? 0xFFFFFFFF : 0xFFBEBECA, 18);
            text(matrices, value.title, x + 54, cy + 14,
                    selected ? 0xFFFFFFFF : 0xFFD8D8E0, 10);

            final Category target = value;
            addHitBox(x + 10, cy, SIDEBAR_WIDTH - 20, 45, new Runnable() {
                @Override
                public void run() {
                    category = target;
                    scroll = 0.0F;
                }
            });
            cy += 52;
        }

        int userY = y + PANEL_HEIGHT - 66;
        drawPanel(matrices, x + 10, userY, SIDEBAR_WIDTH - 20, 50, 0x7A45454D);
        drawOutline(matrices, x + 10, userY, SIDEBAR_WIDTH - 20, 50, 0x685B5B66);
        drawPanel(matrices, x + 20, userY + 10, 30, 30, 0x8A60606C);

        String username = currentPlayerName();
        text(matrices, "U", x + 29, userY + 14, 0xFFFFFFFF, 15);
        text(matrices, trim(username, 15), x + 60, userY + 11, 0xFFFFFFFF, 10);
        text(matrices, "Online", x + 60, userY + 29, 0xFF76D58A, 9);
    }

    private void drawSearch(MatrixStack matrices, int x, int y, int w, int h, int mouseX, int mouseY) {
        boolean hovered = inside(mouseX, mouseY, x, y, w, h);
        drawPanel(matrices, x, y, w, h, hovered || searchFocused ? 0xA63A3A44 : 0x8A33333B);
        drawOutline(matrices, x, y, w, h, searchFocused ? 0xC08B6CFF : 0x68585863);

        String value = search.isEmpty() ? "Search..." : search;
        int color = search.isEmpty() ? 0xFF8D8D9A : 0xFFFFFFFF;
        text(matrices, "⌕", x + 10, y + 8, 0xFFAAAAFF, 14);
        text(matrices, trim(value, 28), x + 30, y + 10, color, 9);

        addHitBox(x, y, w, h, new Runnable() {
            @Override
            public void run() {
                searchFocused = true;
            }
        });
    }

    private void drawMain(MatrixStack matrices, int mouseX, int mouseY) {
        int x = panelX + SIDEBAR_WIDTH + 10;
        int y = panelY;
        int w = PANEL_WIDTH - SIDEBAR_WIDTH - 10;
        int h = PANEL_HEIGHT;

        drawPanel(matrices, x, y, w, h, 0xC91F1F25);
        drawOutline(matrices, x, y, w, h, 0x744B4B55);

        text(matrices, category.title, x + 20, y + 20, ACCENT, 18);
        text(matrices, "Trigger-Bot configuration", x + 20, y + 46, 0xFF9292A2, 9);
        drawPanel(matrices, x + 20, y + 63, 180, 1, 0x908B6CFF);

        int contentTop = y + 84;
        int contentBottom = y + h - 18;
        drawPanel(matrices, x + 10, contentTop - 8, w - 20, contentBottom - contentTop + 16, 0x1AFFFFFF);

        int contentHeight = drawCategoryContent(
                matrices, mouseX, mouseY, x + 20, contentTop, w - 40
        );
        int maxScroll = Math.max(0, contentHeight - (contentBottom - contentTop));
        scroll = MathHelper.clamp(scroll, -maxScroll, 0.0F);

        if (maxScroll > 0) {
            float viewHeight = contentBottom - contentTop;
            float ratio = viewHeight / (float) contentHeight;
            float barHeight = Math.max(30.0F, viewHeight * ratio);
            float barY = contentTop + (-scroll / maxScroll) * (viewHeight - barHeight);
            drawPanel(matrices, x + w - 8, Math.round(barY), 3, Math.round(barHeight), 0x888B6CFF);
        }
    }

    private int drawCategoryContent(MatrixStack matrices, int mouseX, int mouseY, int x, int top, int width) {
        String query = search.toLowerCase(Locale.ROOT).replace(" ", "");

        if (!query.isEmpty() && !categoryHasMatch(query)) {
            text(matrices, "No matching modules", x + 12, top + 20, 0xFF8D8D9A, 10);
            return 50;
        }

        int gap = 15;
        int columnWidth = (width - gap) / 2;

        if (category == Category.COMBAT) {
            int left = drawCombatModule(matrices, x, top + Math.round(scroll), columnWidth, mouseX, mouseY);
            int right = drawAssistModule(matrices, x + columnWidth + gap, top + Math.round(scroll), columnWidth, mouseX, mouseY);
            return Math.max(left, right) - top + Math.round(-scroll);
        }

        if (category == Category.RENDER) {
            int left = drawVisualModule(matrices, x, top + Math.round(scroll), columnWidth, mouseX, mouseY);
            int right = drawAspectModule(matrices, x + columnWidth + gap, top + Math.round(scroll), columnWidth, mouseX, mouseY);
            return Math.max(left, right) - top + Math.round(-scroll);
        }

        if (category == Category.PERFORMANCE) {
            int left = drawOptimizationModule(matrices, x, top + Math.round(scroll), columnWidth, mouseX, mouseY);
            int right = drawFpsModule(matrices, x + columnWidth + gap, top + Math.round(scroll), columnWidth, mouseX, mouseY);
            return Math.max(left, right) - top + Math.round(-scroll);
        }

        return drawPlayerModule(matrices, x, top + Math.round(scroll), width, mouseX, mouseY)
                - top + Math.round(-scroll);
    }

    private boolean categoryHasMatch(String q) {
        if (category == Category.COMBAT) {
            return contains("TriggerBot", q) || contains("Only Crits", q)
                    || contains("Smart Crits", q) || contains("Only Weapon", q)
                    || contains("Aim Assist", q) || contains("Lightning ESP", q);
        }
        if (category == Category.RENDER) {
            return contains("Fullbright", q) || contains("No Hurt Cam", q)
                    || contains("Aspect Ratio", q) || contains("Gamma", q);
        }
        if (category == Category.PERFORMANCE) {
            return contains("Optimization", q) || contains("Adaptive FPS", q)
                    || contains("Target FPS", q) || contains("Profile", q);
        }
        return contains("Alt Manager", q);
    }

    private boolean contains(String value, String q) {
        return value.toLowerCase(Locale.ROOT).replace(" ", "").contains(q);
    }

    private int drawCombatModule(MatrixStack matrices, int x, int y, int w, int mouseX, int mouseY) {
        int h = 52 + 4 * 30;
        drawModuleCard(matrices, x, y, w, h, "TriggerBot",
                "Automatic attack on crosshair target", TriggerBotClient.CONFIG.enabled, mouseX, mouseY,
                new Runnable() {
                    @Override
                    public void run() {
                        TriggerBotClient.CONFIG.enabled = !TriggerBotClient.CONFIG.enabled;
                        save();
                    }
                });

        int sy = y + 55;
        sy = drawToggleRow(matrices, x + 12, sy, w - 24, "Only Crits",
                TriggerBotClient.CONFIG.onlyCrits, new BooleanAction() {
                    @Override public void toggle() {
                        TriggerBotClient.CONFIG.onlyCrits = !TriggerBotClient.CONFIG.onlyCrits;
                        save();
                    }
                });
        sy = drawToggleRow(matrices, x + 12, sy, w - 24, "Smart Crits",
                TriggerBotClient.CONFIG.smartCrits, new BooleanAction() {
                    @Override public void toggle() {
                        TriggerBotClient.CONFIG.smartCrits = !TriggerBotClient.CONFIG.smartCrits;
                        save();
                    }
                });
        sy = drawToggleRow(matrices, x + 12, sy, w - 24, "Only Weapon",
                TriggerBotClient.CONFIG.onlyWeapon, new BooleanAction() {
                    @Override public void toggle() {
                        TriggerBotClient.CONFIG.onlyWeapon = !TriggerBotClient.CONFIG.onlyWeapon;
                        save();
                    }
                });
        drawToggleRow(matrices, x + 12, sy, w - 24, "Aim Assist",
                TriggerBotClient.CONFIG.aimAssist, new BooleanAction() {
                    @Override public void toggle() {
                        TriggerBotClient.CONFIG.aimAssist = !TriggerBotClient.CONFIG.aimAssist;
                        save();
                    }
                });
        return y + h + 15;
    }

    private int drawAssistModule(MatrixStack matrices, int x, int y, int w, int mouseX, int mouseY) {
        int h = 82;
        drawModuleCard(matrices, x, y, w, h, "Lightning ESP",
                "Procedural target lightning overlay", TriggerBotClient.CONFIG.lightningEsp, mouseX, mouseY,
                new Runnable() {
                    @Override
                    public void run() {
                        TriggerBotClient.CONFIG.lightningEsp = !TriggerBotClient.CONFIG.lightningEsp;
                        save();
                    }
                });
        text(matrices, "Shows through walls", x + 14, y + 54, 0xFF8D8D9A, 9);
        text(matrices, "Target range: 6 blocks", x + 14, y + 68, 0xFF8D8D9A, 9);
        return y + h + 15;
    }

    private int drawVisualModule(MatrixStack matrices, int x, int y, int w, int mouseX, int mouseY) {
        int h = TriggerBotClient.CONFIG.fullbright ? 112 : 82;

        drawModuleCard(matrices, x, y, w, h, "Fullbright",
                "Keep caves and dark areas visible", TriggerBotClient.CONFIG.fullbright, mouseX, mouseY,
                new Runnable() {
                    @Override
                    public void run() {
                        TriggerBotClient.setFullbright(
                                MinecraftClient.getInstance(),
                                !TriggerBotClient.CONFIG.fullbright
                        );
                        save();
                    }
                });

        text(matrices, "No Hurt Cam", x + 14, y + 54, 0xFFFFFFFF, 9);
        drawSwitch(matrices, x + w - 56, y + 50, TriggerBotClient.CONFIG.noHurtCam);
        addHitBox(x + 10, y + 46, w - 20, 24, new Runnable() {
            @Override
            public void run() {
                TriggerBotClient.CONFIG.noHurtCam = !TriggerBotClient.CONFIG.noHurtCam;
                save();
            }
        });

        if (TriggerBotClient.CONFIG.fullbright) {
            drawSlider(matrices, x + 14, y + 79, w - 28,
                    "Gamma", TriggerBotClient.CONFIG.fullbrightGamma, 1.0D, 20.0D, 1);
        }

        return y + h + 15;
    }

    private int drawAspectModule(MatrixStack matrices, int x, int y, int w, int mouseX, int mouseY) {
        int h = 112;
        drawModuleCard(matrices, x, y, w, h, "Aspect Ratio",
                "Projection width / height", false, mouseX, mouseY, null);

        double ratio = TriggerBotClient.CONFIG.aspectRatio;
        text(matrices, String.format(Locale.ROOT, "%.2f", ratio), x + 14, y + 55, 0xFFFFFFFF, 15);
        text(matrices, "0.50", x + 14, y + 81, 0xFF7F7F8A, 8);
        text(matrices, "3.00", x + w - 42, y + 81, 0xFF7F7F8A, 8);
        drawSliderBar(matrices, x + 14, y + 98, w - 28, (ratio - 0.5D) / 2.5D);

        addHitBox(x + 10, y + 86, w - 20, 22, new Runnable() {
            @Override
            public void run() {
                activeSlider = 2;
            }
        });

        return y + h + 15;
    }

    private int drawOptimizationModule(MatrixStack matrices, int x, int y, int w, int mouseX, int mouseY) {
        int h = 82;
        drawModuleCard(matrices, x, y, w, h, "Optimization",
                "Mobile performance profiles", TriggerBotClient.CONFIG.optimization, mouseX, mouseY,
                new Runnable() {
                    @Override
                    public void run() {
                        TriggerBotOptimizer.setOptimization(
                                MinecraftClient.getInstance(),
                                !TriggerBotClient.CONFIG.optimization
                        );
                        save();
                    }
                });

        text(matrices, profileName(), x + 14, y + 56, 0xFFFFFFFF, 9);
        addHitBox(x + 10, y + 44, w - 20, 28, new Runnable() {
            @Override
            public void run() {
                TriggerBotClient.CONFIG.optimizationLevel =
                        (TriggerBotClient.CONFIG.optimizationLevel + 1) % 3;
                if (TriggerBotClient.CONFIG.optimization) {
                    TriggerBotOptimizer.setOptimization(MinecraftClient.getInstance(), true);
                }
                save();
            }
        });

        return y + h + 15;
    }

    private int drawFpsModule(MatrixStack matrices, int x, int y, int w, int mouseX, int mouseY) {
        int h = 112;
        drawModuleCard(matrices, x, y, w, h, "Adaptive FPS",
                "Automatic mobile target control", TriggerBotClient.CONFIG.adaptiveOptimization, mouseX, mouseY,
                new Runnable() {
                    @Override
                    public void run() {
                        TriggerBotClient.CONFIG.adaptiveOptimization =
                                !TriggerBotClient.CONFIG.adaptiveOptimization;
                        save();
                    }
                });

        text(matrices, "Target FPS", x + 14, y + 54, 0xFFFFFFFF, 9);
        text(matrices, String.valueOf(TriggerBotClient.CONFIG.targetFps), x + w - 48, y + 54, ACCENT, 10);
        addHitBox(x + 10, y + 44, w - 20, 24, new Runnable() {
            @Override
            public void run() {
                int fps = TriggerBotClient.CONFIG.targetFps;
                TriggerBotClient.CONFIG.targetFps = fps == 30 ? 45 : (fps == 45 ? 60 : 30);
                save();
            }
        });

        text(matrices, "30", x + 14, y + 86, 0xFF7F7F8A, 8);
        text(matrices, "60", x + w - 28, y + 86, 0xFF7F7F8A, 8);
        drawSliderBar(matrices, x + 14, y + 100, w - 28,
                (TriggerBotClient.CONFIG.targetFps - 30.0D) / 30.0D);

        return y + h + 15;
    }

    private int drawPlayerModule(MatrixStack matrices, int x, int y, int w, int mouseX, int mouseY) {
        int h = 112;
        drawModuleCard(matrices, x, y, w, h, "Alt Manager",
                "Local nickname/session helper", false, mouseX, mouseY, null);

        text(matrices, "Current nickname:", x + 14, y + 54, 0xFF8D8D9A, 9);
        text(matrices, trim(currentPlayerName(), 18), x + 14, y + 71, 0xFFFFFFFF, 11);

        drawPanel(matrices, x + w - 118, y + 54, 102, 30, 0x8A4B3F66);
        drawOutline(matrices, x + w - 118, y + 54, 102, 30, 0xB08B6CFF);
        text(matrices, "OPEN", x + w - 85, y + 63, 0xFFFFFFFF, 9);

        addHitBox(x + w - 118, y + 54, 102, 30, new Runnable() {
            @Override
            public void run() {
                MinecraftClient.getInstance().openScreen(
                        AltManagerScreen.create(TriggerBotConfigScreen.this)
                );
            }
        });

        return y + h + 15;
    }

    private void drawModuleCard(
            MatrixStack matrices,
            int x,
            int y,
            int w,
            int h,
            String title,
            String description,
            boolean enabled,
            int mouseX,
            int mouseY,
            Runnable action
    ) {
        boolean hovered = inside(mouseX, mouseY, x, y, w, 40);

        drawShadow(matrices, x - 3, y - 3, w + 6, h + 6, 0x50000000, 8);
        drawPanel(matrices, x, y, w, h, hovered ? 0x9D363640 : 0x8F2A2A32);
        drawOutline(matrices, x, y, w, h, enabled ? 0xD08B6CFF : 0x704F4F59);

        text(matrices, title, x + 12, y + 10, 0xFFFFFFFF, 10);
        text(matrices, trim(description, 42), x + 12, y + 26, 0xFF8F8F9B, 8);

        if (action != null) {
            drawSwitch(matrices, x + w - 52, y + 13, enabled);
            addHitBox(x, y, w, 40, action);
        }
    }

    private int drawToggleRow(
            MatrixStack matrices,
            int x,
            int y,
            int w,
            String name,
            boolean enabled,
            final BooleanAction action
    ) {
        text(matrices, name, x + 2, y + 8, 0xFFD0D0D9, 9);
        drawSwitch(matrices, x + w - 34, y + 3, enabled);
        addHitBox(x, y, w, 24, new Runnable() {
            @Override
            public void run() {
                action.toggle();
            }
        });
        return y + 30;
    }

    private void drawSlider(
            MatrixStack matrices,
            int x,
            int y,
            int w,
            String label,
            double value,
            double min,
            double max,
            final int sliderId
    ) {
        text(matrices, label, x, y - 1, 0xFFCFCFD8, 8);
        text(matrices, String.format(Locale.ROOT, "%.0f", value),
                x + w - 25, y - 1, ACCENT, 9);

        drawSliderBar(matrices, x, y + 14, w, (value - min) / (max - min));
        addHitBox(x, y + 8, w, 24, new Runnable() {
            @Override
            public void run() {
                activeSlider = sliderId;
            }
        });
    }

    private void drawSliderBar(MatrixStack matrices, int x, int y, int w, double progress) {
        float p = MathHelper.clamp((float) progress, 0.0F, 1.0F);
        drawPanel(matrices, x, y, w, 4, 0x88464650);
        drawPanel(matrices, x, y, Math.max(2, Math.round(w * p)), 4, ACCENT);
        drawPanel(matrices, x + Math.round(w * p) - 4, y - 3, 8, 10, 0xFFF0E9FF);
    }

    private void drawSwitch(MatrixStack matrices, int x, int y, boolean enabled) {
        drawPanel(matrices, x, y, 34, 16, enabled ? 0xCC8B6CFF : 0x80595964);
        drawPanel(matrices, enabled ? x + 20 : x + 2, y + 2, 12, 12, 0xFFF5F2FF);
    }

    private void handleSlider(double mouseX) {
        if (activeSlider == 1) {
            int mainX = panelX + SIDEBAR_WIDTH + 10;
            int contentX = mainX + 20;
            int gap = 15;
            int columnWidth = (PANEL_WIDTH - SIDEBAR_WIDTH - 10 - 40 - gap) / 2;
            int sliderX = contentX + 14;
            int sliderW = columnWidth - 28;

            double p = MathHelper.clamp((mouseX - sliderX) / (double) sliderW, 0.0D, 1.0D);
            TriggerBotClient.CONFIG.fullbrightGamma = 1.0D + p * 19.0D;
            TriggerBotClient.setFullbright(
                    MinecraftClient.getInstance(),
                    TriggerBotClient.CONFIG.fullbright
            );
            save();
        } else if (activeSlider == 2) {
            int mainX = panelX + SIDEBAR_WIDTH + 10;
            int contentX = mainX + 20;
            int gap = 15;
            int columnWidth = (PANEL_WIDTH - SIDEBAR_WIDTH - 10 - 40 - gap) / 2;
            int sliderX = contentX + columnWidth + gap + 14;
            int sliderW = columnWidth - 28;

            double p = MathHelper.clamp((mouseX - sliderX) / (double) sliderW, 0.0D, 1.0D);
            TriggerBotClient.CONFIG.aspectRatio = 0.50D + p * 2.50D;
            save();
        }
    }

    private void addHitBox(float x, float y, float width, float height, Runnable action) {
        if (width > 0 && height > 0) {
            hitBoxes.add(new HitBox(x, y, width, height, action));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            searchFocused = inside(
                    mouseX, mouseY,
                    panelX + 15, panelY + 72,
                    SIDEBAR_WIDTH - 30, 34
            );

            for (int i = hitBoxes.size() - 1; i >= 0; i--) {
                HitBox hitBox = hitBoxes.get(i);
                if (hitBox.contains(mouseX, mouseY)) {
                    hitBox.action.run();
                    if (activeSlider != 0) {
                        handleSlider(mouseX);
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0 && activeSlider != 0) {
            handleSlider(mouseX);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            activeSlider = 0;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int mainX = panelX + SIDEBAR_WIDTH + 10;
        if (inside(mouseX, mouseY, mainX + 10, panelY + 70,
                PANEL_WIDTH - SIDEBAR_WIDTH - 20, PANEL_HEIGHT - 70)) {
            scroll += (float) (delta * 45.0D);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchFocused) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!search.isEmpty()) {
                    search = search.substring(0, search.length() - 1);
                }
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                if (!search.isEmpty()) {
                    search = "";
                } else {
                    searchFocused = false;
                }
                return true;
            }
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (searchFocused && isAllowedSearchChar(codePoint) && search.length() < 24) {
            search += Character.toString(codePoint);
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    private boolean isAllowedSearchChar(char c) {
        return Character.isLetterOrDigit(c) || Character.isSpaceChar(c) || c == '_' || c == '-';
    }

    @Override
    public void onClose() {
        save();
        activeSlider = 0;
        if (this.client != null) {
            this.client.openScreen(parent);
        }
    }

    private static void save() {
        TriggerBotClient.saveConfig();
    }

    private static String profileName() {
        switch (Math.max(0, Math.min(2, TriggerBotClient.CONFIG.optimizationLevel))) {
            case 2:
                return "Extreme profile";
            case 1:
                return "Performance profile";
            default:
                return "Balanced profile";
        }
    }

    private String currentPlayerName() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.player == null ? "Player" : client.player.getName().getString();
    }

    private static String trim(String text, int max) {
        if (text == null) return "";
        return text.length() <= max
                ? text
                : text.substring(0, Math.max(0, max - 1)) + "...";
    }

    private void drawPanel(MatrixStack matrices, int x, int y, int width, int height, int color) {
        fill(matrices, x, y, x + width, y + height, color);
    }

    private void drawOutline(MatrixStack matrices, int x, int y, int width, int height, int color) {
        fill(matrices, x, y, x + width, y + 1, color);
        fill(matrices, x, y + height - 1, x + width, y + height, color);
        fill(matrices, x, y, x + 1, y + height, color);
        fill(matrices, x + width - 1, y, x + width, y + height, color);
    }

    private void drawShadow(MatrixStack matrices, int x, int y, int width, int height, int color, int spread) {
        for (int i = spread; i >= 1; i -= 2) {
            int alpha = (color >>> 24) * (spread - i + 1) / spread / 2;
            int c = (color & 0x00FFFFFF) | (alpha << 24);
            fill(matrices, x - i, y - i, x + width + i, y + height + i, c);
        }
    }

    private void text(MatrixStack matrices, String value, float x, float y, int color, int size) {
        if (size >= 16) {
            matrices.push();
            matrices.translate(x, y, 0.0F);
            float scale = size / 9.0F;
            matrices.scale(scale, scale, 1.0F);
            this.textRenderer.draw(matrices, value, 0, 0, color);
            matrices.pop();
        } else {
            this.textRenderer.draw(matrices, value, x, y, color);
        }
    }

    private static boolean inside(double mouseX, double mouseY, float x, float y, float w, float h) {
        return mouseX >= x && mouseX <= x + w
                && mouseY >= y && mouseY <= y + h;
    }

    private interface BooleanAction {
        void toggle();
    }
}
