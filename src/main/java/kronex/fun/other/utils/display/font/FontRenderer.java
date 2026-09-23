package kronex.fun.other.utils.display.font;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class FontRenderer {
    private static final int RASTER_SCALE = 4;
    private static final int CACHE_LIMIT = 384;

    private static final Map<CacheKey, CachedText> CACHE =
            Collections.synchronizedMap(new LinkedHashMap<>(256, 0.75F, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<CacheKey, CachedText> eldest) {
                    if (size() <= CACHE_LIMIT) return false;
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client != null && client.getTextureManager() != null) {
                        client.getTextureManager().destroyTexture(eldest.getValue().texture);
                    }
                    eldest.getValue().image.close();
                    return true;
                }
            });

    private final float logicalSize;
    private final Fonts.Type type;

    FontRenderer(float size, Fonts.Type type) {
        this.logicalSize = Math.max(7F, size);
        this.type = Objects.requireNonNull(type);
    }

    public float getStringWidth(String text) {
        if (text == null || text.isEmpty()) return 0F;
        Graphics2D graphics = measureGraphics();
        Font font = createFont(logicalSize * RASTER_SCALE);
        graphics.setFont(font);
        FontMetrics metrics = graphics.getFontMetrics();
        float width = metrics.stringWidth(text) / (float) RASTER_SCALE;
        graphics.dispose();
        return width;
    }

    public float getStringHeight(String text) {
        Graphics2D graphics = measureGraphics();
        Font font = createFont(logicalSize * RASTER_SCALE);
        graphics.setFont(font);
        float height = graphics.getFontMetrics().getHeight() / (float) RASTER_SCALE;
        graphics.dispose();
        return height;
    }

    public void drawString(net.minecraft.client.util.math.MatrixStack matrix, String text, float x, float y, int color) {
        drawString(matrix, text, (double) x, (double) y, color);
    }

    public void drawString(net.minecraft.client.util.math.MatrixStack matrix, String text, double x, double y, int color) {
        drawText(text, x, y, color, color, false);
    }

    public void drawCenteredString(net.minecraft.client.util.math.MatrixStack matrix, String text, double centerX, double y, int color) {
        drawString(matrix, text, centerX - getStringWidth(text) / 2.0, y, color);
    }

    public void drawStringWithScroll(net.minecraft.client.util.math.MatrixStack matrix, String text, float x, float y, float maxWidth, int color) {
        if (text == null) return;
        String visible = text;
        while (!visible.isEmpty() && getStringWidth(visible) > maxWidth) {
            visible = visible.substring(0, visible.length() - 1);
        }
        drawString(matrix, visible, x, y, color);
    }

    public void drawGradientString(net.minecraft.client.util.math.MatrixStack matrix, String text, float x, float y, int color1, int color2) {
        drawText(text, x, y, color1, color2, true);
    }

    public void drawGradientString(net.minecraft.client.util.math.MatrixStack matrix, String text, double x, double y, int color1, int color2) {
        drawText(text, x, y, color1, color2, true);
    }

    private void drawText(String text, double x, double y, int color1, int color2, boolean gradient) {
        if (text == null || text.isEmpty()) return;

        DrawContext context = kronex.fun.other.utils.display.GuiRenderContext.get();
        MinecraftClient client = MinecraftClient.getInstance();
        if (context == null || client == null) return;

        CacheKey key = new CacheKey(text, Float.floatToIntBits(logicalSize), type, color1, color2, gradient);
        CachedText cached = CACHE.get(key);
        if (cached == null) {
            cached = rasterize(text, color1, color2, gradient);
            CACHE.put(key, cached);
        }

        int drawX = (int) Math.round(x);
        int drawY = (int) Math.round(y);
        context.drawTexture(
                RenderLayer::getGuiTextured,
                cached.texture,
                drawX,
                drawY,
                0F,
                0F,
                cached.drawWidth,
                cached.drawHeight,
                cached.imageWidth,
                cached.imageHeight
        );
    }

    private CachedText rasterize(String text, int color1, int color2, boolean gradient) {
        int pixelSize = Math.max(1, Math.round(logicalSize * RASTER_SCALE));

        BufferedImage measureImage = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D measure = measureImage.createGraphics();
        configure(measure);
        Font font = createFont(pixelSize);
        measure.setFont(font);
        FontMetrics metrics = measure.getFontMetrics();

        int textWidth = Math.max(1, metrics.stringWidth(text));
        int textHeight = Math.max(1, metrics.getHeight());
        int padding = Math.max(2, RASTER_SCALE);

        BufferedImage image = new BufferedImage(textWidth + padding * 2, textHeight + padding * 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        configure(graphics);
        graphics.setFont(font);
        graphics.setComposite(AlphaComposite.SrcOver);

        int argb1 = color1;
        int argb2 = gradient ? color2 : color1;
        Color first = new Color((argb1 >>> 16) & 0xFF, (argb1 >>> 8) & 0xFF, argb1 & 0xFF, (argb1 >>> 24) & 0xFF);
        Color second = new Color((argb2 >>> 16) & 0xFF, (argb2 >>> 8) & 0xFF, argb2 & 0xFF, (argb2 >>> 24) & 0xFF);

        if (gradient) {
            graphics.setPaint(new GradientPaint(0, 0, first, image.getWidth(), 0, second));
        } else {
            graphics.setColor(first);
        }

        int baseline = padding + metrics.getAscent();
        graphics.drawString(text, padding, baseline);

        graphics.dispose();
        measure.dispose();

        NativeImage nativeImage = new NativeImage(NativeImage.Format.RGBA, image.getWidth(), image.getHeight(), false);
        for (int yy = 0; yy < image.getHeight(); yy++) {
            for (int xx = 0; xx < image.getWidth(); xx++) {
                nativeImage.setColorArgb(xx, yy, image.getRGB(xx, yy));
            }
        }

        NativeImageBackedTexture texture = new NativeImageBackedTexture(nativeImage);
        Identifier textureId = MinecraftClient.getInstance().getTextureManager()
                .registerDynamicTexture("kronex_font", texture);

        return new CachedText(textureId, nativeImage, Math.max(1, image.getWidth() / RASTER_SCALE),
                Math.max(1, image.getHeight() / RASTER_SCALE), image.getWidth(), image.getHeight());
    }

    private Font createFont(float pixelSize) {
        int style = switch (type) {
            case BOLD, HUD -> Font.BOLD;
            default -> Font.PLAIN;
        };

        String family = switch (type) {
            case ICONS2 -> Font.SANS_SERIF;
            default -> Font.SANS_SERIF;
        };

        return new Font(family, style, Math.max(1, Math.round(pixelSize)));
    }

    private static Graphics2D measureGraphics() {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        configure(graphics);
        return graphics;
    }

    private static void configure(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        graphics.setStroke(new BasicStroke(1F));
    }

    private record CacheKey(String text, int sizeBits, Fonts.Type type, int color1, int color2, boolean gradient) {}

    private record CachedText(Identifier texture, NativeImage image, int drawWidth, int drawHeight,
                              int imageWidth, int imageHeight) {}
}
