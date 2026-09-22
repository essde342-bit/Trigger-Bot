package kronex.fun.display.screens.clickgui.components.implement.window.implement.settings.theme;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import kronex.fun.Kronex;
import kronex.fun.display.screens.clickgui.MenuScreen;
import kronex.fun.display.screens.clickgui.components.AbstractComponent;
import kronex.fun.display.screens.clickgui.components.implement.window.AbstractWindow;
import kronex.fun.other.utils.display.color.ColorAssist;
import kronex.fun.other.utils.display.font.FontRenderer;
import kronex.fun.other.utils.display.font.Fonts;
import kronex.fun.other.utils.display.shape.ShapeProperties;
import kronex.fun.other.utils.math.calc.Calculate;
import kronex.fun.other.utils.theme.ThemeManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import static kronex.fun.other.utils.display.font.Fonts.Type.*;

public class ThemeEditorWindow extends AbstractWindow {
    private static final float HEADER_H = 24f;
    private static final int WINDOW_HEADER_COLOR = 0xF2282932;
    private static final int WINDOW_BODY_COLOR = 0xEE1D1F27;
    private static final int WINDOW_OUTLINE_COLOR = 0xFF353843;
    private static final int WINDOW_TITLE_COLOR = 0xFFF3F3F5;
    private static final int ITEM_COLOR = 0xE5282B34;
    private static final int ITEM_HOVER_COLOR = 0xF0303440;
    private static final int ITEM_OUTLINE_COLOR = 0x663C404C;
    private static final int ITEM_SELECTED_OUTLINE_COLOR = 0xFF6B7280;
    private static final int ITEM_TEXT_COLOR = 0xFFE6E6EA;
    private final List<AbstractComponent> presetComponents = new ArrayList<>();
    private float scroll = 0f;
    private float smoothedScroll = 0f;
    private float lastMaxScroll = 0f;

    public static void open() { open(MenuScreen.INSTANCE); }

    public static void open(MenuScreen menu) {
        ThemeEditorWindow existing = null;
        for (AbstractWindow w : windowManager.getWindows()) if (w instanceof ThemeEditorWindow te) { existing = te; break; }
        if (existing != null) { windowManager.getWindows().remove(existing); windowManager.add(existing); return; }
        float w = 116f; float h = 292f; float x = getAttachedX(menu, w); float y = getAttachedY(menu, h);
        ThemeEditorWindow win = new ThemeEditorWindow();
        win.position(x, y).size(w, h).draggable(false);
        windowManager.add(win);
    }

    public ThemeEditorWindow() { refreshPresets(); }

    public void refreshPresets() {
        presetComponents.clear();
        File[] files = ThemeManager.getThemeDir().listFiles();
        if (files != null) {
            List<File> sortedFiles = new ArrayList<>();
            for (File f : files) sortedFiles.add(f);
            sortedFiles.sort(Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));
            for (File f : sortedFiles) {
                if (f.getName().endsWith(".color")) {
                    String name = f.getName().replace(".color", "");
                    if (name.startsWith("default_")) name = name.substring("default_".length());
                    presetComponents.add(new ThemePresetComponent(name));
                }
            }
        }
    }

    @Override
    public void drawWindow(DrawContext context, int mouseX, int mouseY, float delta) {
        renderThemeList(context, mouseX, mouseY, delta);
    }

    private void renderThemeList(DrawContext context, int mouseX, int mouseY, float delta) {
        float headerH = HEADER_H;
        rectangle.render(ShapeProperties.create(context.getMatrices(), x, y, width, height)
                .round(5).thickness(1.6f).softness(1).outlineColor(WINDOW_OUTLINE_COLOR).color(WINDOW_BODY_COLOR).build());
        rectangle.render(ShapeProperties.create(context.getMatrices(), x, y, width, headerH)
                .round(5).color(WINDOW_HEADER_COLOR).build());
        Fonts.getSize(13, SEMI).drawString(context.getMatrices(), "Themes", x + 8, y + 7, WINDOW_TITLE_COLOR);

        float listTop = y + headerH + 5;
        float listBottom = y + height - 5;
        float viewH = Math.max(1f, listBottom - listTop);
        float contentH = presetComponents.size() * 28f;
        float maxScroll = Math.max(0f, contentH - viewH);
        lastMaxScroll = maxScroll;
        scroll = MathHelper.clamp(scroll, -maxScroll, 0f);
        smoothedScroll = MathHelper.lerp(0.18f, smoothedScroll, scroll);

        int index = 0;
        for (AbstractComponent component : presetComponents) {
            float cy = listTop + index * 28f + smoothedScroll;
            component.position(x + 4, cy).size(width - 8, 24);
            if (cy + 24 >= listTop && cy <= listBottom) component.render(context, mouseX, mouseY, delta);
            index++;
        }

        if (lastMaxScroll > 0) {
            float barH = Math.max(20f, viewH * viewH / Math.max(viewH, contentH));
            float barY = listTop + (-smoothedScroll / maxScroll) * (viewH - barH);
            rectangle.render(ShapeProperties.create(context.getMatrices(), x + width - 3.5f, barY, 2.5f, barH)
                    .round(1.2f).color(0xFF61656F).build());
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean inHeader = Calculate.isHovered(mouseX, mouseY, x, y, width, HEADER_H);
        if (inHeader && button == 0) { draggable(true); return super.mouseClicked(mouseX, mouseY, button); }
        for (AbstractComponent component : presetComponents) if (component.mouseClicked(mouseX, mouseY, button)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (Calculate.isHovered(mouseX, mouseY, x, y, width, height) && lastMaxScroll > 0) {
            scroll += amount * 18f;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    private static float getAttachedX(MenuScreen menu, float width) { return menu.x + menu.width + 8; }
    private static float getAttachedY(MenuScreen menu, float height) { return Math.max(6, menu.y + 10); }

    private final class ThemePresetComponent extends AbstractComponent {
        private final String name;
        ThemePresetComponent(String name) { this.name = name; }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            boolean hovered = Calculate.isHovered(mouseX, mouseY, x, y, width, height);
            rectangle.render(ShapeProperties.create(context.getMatrices(), x, y, width, height)
                    .round(4).thickness(1).outlineColor(hovered ? ITEM_SELECTED_OUTLINE_COLOR : ITEM_OUTLINE_COLOR)
                    .color(hovered ? ITEM_HOVER_COLOR : ITEM_COLOR).build());
            int iconBoxSize = 19;
            float iconBoxX = x + 4, iconBoxY = y + 2.5f;
            ThemePreview preview = readThemePreview(name);
            rectangle.render(ShapeProperties.create(context.getMatrices(), iconBoxX, iconBoxY, iconBoxSize, iconBoxSize)
                    .round(3f).color(preview.background).build());
            rectangle.render(ShapeProperties.create(context.getMatrices(), iconBoxX + 2f, iconBoxY + 2f, iconBoxSize - 4f, 3f).round(1.5f).color(preview.primary).build());
            rectangle.render(ShapeProperties.create(context.getMatrices(), iconBoxX + 2f, iconBoxY + 6f, iconBoxSize - 6f, 2f).round(1f).color(preview.secondary).build());
            rectangle.render(ShapeProperties.create(context.getMatrices(), iconBoxX + 2f, iconBoxY + 9f, iconBoxSize - 4f, 2f).round(1f).color(preview.surface).build());
            rectangle.render(ShapeProperties.create(context.getMatrices(), iconBoxX + iconBoxSize - 3.5f, iconBoxY + 1.5f, 1.5f, 1.5f).round(.75f).color(preview.text).build());
            Fonts.getSize(12, DEFAULT).drawString(context.getMatrices(), name, iconBoxX + iconBoxSize + 6f, y + 8.5f, ITEM_TEXT_COLOR);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (Calculate.isHovered(mouseX, mouseY, x, y, width, height)) { ThemeManager.loadTheme(name); return true; }
            return false;
        }
    }

    private ThemePreview readThemePreview(String themeName) {
        int fallbackPrimary=0xFF6C9AFD,fallbackSecondary=0xFF8C7FFF,fallbackBackground=0xFF111114,fallbackSurface=0xFF202023,fallbackText=0xFFFFFFFF;
        try {
            File themeFile = new File(ThemeManager.getThemeDir(), themeName + ".color");
            if (!themeFile.exists()) return new ThemePreview(fallbackPrimary,fallbackSecondary,fallbackBackground,fallbackSurface,fallbackText);
            JsonObject theme = JsonParser.parseString(Files.readString(themeFile.toPath(), StandardCharsets.UTF_8)).getAsJsonObject();
            JsonObject colors = theme.has("colors") ? theme.getAsJsonObject("colors") : null;
            if (colors == null) return new ThemePreview(fallbackPrimary,fallbackSecondary,fallbackBackground,fallbackSurface,fallbackText);
            int primary=getThemeColor(colors,fallbackPrimary,"Первый цвет","РџРµСЂРІС‹Р№ С†РІРµС‚");
            int secondary=getThemeColor(colors,fallbackSecondary,"Второй цвет","Р’С‚РѕСЂРѕР№ С†РІРµС‚");
            int background=getThemeColor(colors,fallbackBackground,"Цвет заднего фона","Р¦РІРµС‚ Р·Р°РґРЅРµРіРѕ С„РѕРЅР°");
            int surface=getThemeColor(colors,fallbackSurface,"Цвет выкл. модулей","Р¦РІРµС‚ РІС‹РєР». РјРѕРґСѓР»РµР№");
            int text=getThemeColor(colors,fallbackText,"Цвет текста","Р¦РІРµС‚ С‚РµРєСЃС‚Р°");
            return new ThemePreview(primary,secondary,background,surface,text);
        } catch (Exception ignored) { return new ThemePreview(fallbackPrimary,fallbackSecondary,fallbackBackground,fallbackSurface,fallbackText); }
    }

    private int getThemeColor(JsonObject colors,int fallback,String... keys) {
        for(String key:keys) if(colors.has(key)) try { return colors.get(key).getAsInt(); } catch(Exception ignored){}
        return fallback;
    }

    private record ThemePreview(int primary,int secondary,int background,int surface,int text) {}
}
