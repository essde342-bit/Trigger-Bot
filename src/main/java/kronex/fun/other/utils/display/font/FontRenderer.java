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
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class FontRenderer {
    private static final int RASTER_SCALE = 4;
    private static final int ATLAS_SIZE = 1024;
    private static final int ATLAS_PADDING = 2;
    private static final int MAX_ATLAS_PAGES = 4;

    private static final String FONT_RESOURCE =
            "/assets/triggerbot/fonts/GalahadStd Regular.otf";
    private static final Font GALAHAD_FONT = loadGalahadFont();

    /*
     * GL4ES-safe design:
     * keep a small number of persistent GPU textures and put all rendered
     * strings into those atlases. The old implementation created and destroyed
     * one OpenGL texture for every cache entry, which is unsafe on GL4ES.
     */
    private static final Map<CacheKey, CachedText> CACHE = new HashMap<>();
    private static final List<AtlasPage> ATLAS_PAGES = new ArrayList<>();

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

    public void drawString(net.minecraft.client.util.math.MatrixStack matrix,
                           String text, float x, float y, int color) {
        drawString(matrix, text, (double) x, (double) y, color);
    }

    public void drawString(net.minecraft.client.util.math.MatrixStack matrix,
                           String text, double x, double y, int color) {
        drawText(text, x, y, color, color, false);
    }

    public void drawCenteredString(net.minecraft.client.util.math.MatrixStack matrix,
                                   String text, double centerX, double y, int color) {
        drawString(matrix, text, centerX - getStringWidth(text) / 2.0, y, color);
    }

    public void drawStringWithScroll(net.minecraft.client.util.math.MatrixStack matrix,
                                     String text, float x, float y,
                                     float maxWidth, int color) {
        if (text == null) return;

        String visible = text;
        while (!visible.isEmpty() && getStringWidth(visible) > maxWidth) {
            visible = visible.substring(0, visible.length() - 1);
        }

        drawString(matrix, visible, x, y, color);
    }

    public void drawGradientString(net.minecraft.client.util.math.MatrixStack matrix,
                                   String text, float x, float y,
                                   int color1, int color2) {
        drawText(text, x, y, color1, color2, true);
    }

    public void drawGradientString(net.minecraft.client.util.math.MatrixStack matrix,
                                   String text, double x, double y,
                                   int color1, int color2) {
        drawText(text, x, y, color1, color2, true);
    }

    private void drawText(String text, double x, double y,
                          int color1, int color2, boolean gradient) {
        if (text == null || text.isEmpty()) return;

        DrawContext context =
                kronex.fun.other.utils.display.GuiRenderContext.get();
        MinecraftClient client = MinecraftClient.getInstance();

        if (context == null || client == null || client.getTextureManager() == null) {
            return;
        }

        CacheKey key = new CacheKey(
                text,
                Float.floatToIntBits(logicalSize),
                type,
                color1,
                color2,
                gradient
        );

        CachedText cached = CACHE.get(key);
        if (cached == null) {
            cached = rasterize(text, color1, color2, gradient, client);
            if (cached == null) return;
            CACHE.put(key, cached);
        }

        int drawX = (int) Math.round(x);
        int drawY = (int) Math.round(y);

        // Source region is 4x larger than the logical destination.
        context.drawTexture(
                RenderLayer::getGuiTextured,
                cached.texture,
                drawX,
                drawY,
                cached.u,
                cached.v,
                cached.drawWidth,
                cached.drawHeight,
                cached.imageWidth,
                cached.imageHeight,
                ATLAS_SIZE,
                ATLAS_SIZE
        );
    }

    private CachedText rasterize(String text, int color1, int color2,
                                 boolean gradient, MinecraftClient client) {
        int pixelSize = Math.max(1, Math.round(logicalSize * RASTER_SCALE));

        BufferedImage measureImage =
                new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D measure = measureImage.createGraphics();
        configure(measure);

        Font font = createFont(pixelSize);
        measure.setFont(font);
        FontMetrics metrics = measure.getFontMetrics();

        int textWidth = Math.max(1, metrics.stringWidth(text));
        int textHeight = Math.max(1, metrics.getHeight());
        int padding = Math.max(2, RASTER_SCALE);

        BufferedImage image = new BufferedImage(
                textWidth + padding * 2,
                textHeight + padding * 2,
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D graphics = image.createGraphics();
        configure(graphics);
        graphics.setFont(font);
        graphics.setComposite(AlphaComposite.SrcOver);

        Color first = new Color(
                (color1 >>> 16) & 0xFF,
                (color1 >>> 8) & 0xFF,
                color1 & 0xFF,
                (color1 >>> 24) & 0xFF
        );

        int argb2 = gradient ? color2 : color1;
        Color second = new Color(
                (argb2 >>> 16) & 0xFF,
                (argb2 >>> 8) & 0xFF,
                argb2 & 0xFF,
                (argb2 >>> 24) & 0xFF
        );

        if (gradient) {
            graphics.setPaint(
                    new GradientPaint(
                            0, 0, first,
                            image.getWidth(), 0, second
                    )
            );
        } else {
            graphics.setColor(first);
        }

        int baseline = padding + metrics.getAscent();
        graphics.drawString(text, padding, baseline);

        graphics.dispose();
        measure.dispose();

        NativeImage nativeImage = new NativeImage(
                NativeImage.Format.RGBA,
                image.getWidth(),
                image.getHeight(),
                false
        );

        try {
            for (int yy = 0; yy < image.getHeight(); yy++) {
                for (int xx = 0; xx < image.getWidth(); xx++) {
                    nativeImage.setColorArgb(xx, yy, image.getRGB(xx, yy));
                }
            }

            AtlasPlacement placement = placeInAtlas(client, nativeImage);
            if (placement == null) return null;

            return new CachedText(
                    placement.texture,
                    placement.u,
                    placement.v,
                    Math.max(1, image.getWidth() / RASTER_SCALE),
                    Math.max(1, image.getHeight() / RASTER_SCALE),
                    image.getWidth(),
                    image.getHeight()
            );
        } finally {
            nativeImage.close();
        }
    }

    private static AtlasPlacement placeInAtlas(MinecraftClient client,
                                               NativeImage image) {
        if (image.getWidth() + ATLAS_PADDING * 2 > ATLAS_SIZE
                || image.getHeight() + ATLAS_PADDING * 2 > ATLAS_SIZE) {
            return null;
        }

        for (AtlasPage page : ATLAS_PAGES) {
            AtlasPlacement placement = page.tryAdd(image);
            if (placement != null) return placement;
        }

        if (ATLAS_PAGES.size() >= MAX_ATLAS_PAGES) {
            return null;
        }

        AtlasPage page = new AtlasPage(client, ATLAS_PAGES.size());
        ATLAS_PAGES.add(page);
        return page.tryAdd(image);
    }

    private Font createFont(float pixelSize) {
        int style = switch (type) {
            case BOLD, HUD -> Font.BOLD;
            default -> Font.PLAIN;
        };

        return GALAHAD_FONT.deriveFont(
                style,
                Math.max(1F, pixelSize)
        );
    }

    private static Font loadGalahadFont() {
        try (InputStream input =
                     FontRenderer.class.getResourceAsStream(FONT_RESOURCE)) {
            if (input == null) {
                throw new IllegalStateException(
                        "Missing bundled Galahad font: " + FONT_RESOURCE
                );
            }

            return Font.createFont(Font.TRUETYPE_FONT, input);
        } catch (FontFormatException | IOException e) {
            throw new IllegalStateException(
                    "Unable to load bundled Galahad font: " + FONT_RESOURCE,
                    e
            );
        }
    }

    private static Graphics2D measureGraphics() {
        BufferedImage image =
                new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        configure(graphics);
        return graphics;
    }

    private static void configure(Graphics2D graphics) {
        graphics.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );
        graphics.setRenderingHint(
                RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON
        );
        graphics.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY
        );
        graphics.setRenderingHint(
                RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE
        );
        graphics.setStroke(new BasicStroke(1F));
    }

    private record CacheKey(String text, int sizeBits, Fonts.Type type,
                            int color1, int color2, boolean gradient) {
    }

    private record CachedText(Identifier texture, int u, int v,
                              int drawWidth, int drawHeight,
                              int imageWidth, int imageHeight) {
    }

    private record AtlasPlacement(Identifier texture, int u, int v) {
    }

    private static final class AtlasPage {
        private final Identifier textureId;
        private final NativeImageBackedTexture texture;

        private int cursorX = ATLAS_PADDING;
        private int cursorY = ATLAS_PADDING;
        private int rowHeight;

        private AtlasPage(MinecraftClient client, int index) {
            this.textureId = Identifier.of(
                    "triggerbot",
                    "kronex_font_atlas/page_" + index
            );

            this.texture = new NativeImageBackedTexture(
                    ATLAS_SIZE,
                    ATLAS_SIZE,
                    false
            );

            // Deliberately avoid setFilter/setClamp here: those operations can
            // touch GL state, so the atlas is only created from a render pass.
            client.getTextureManager().registerTexture(textureId, texture);
        }

        private AtlasPlacement tryAdd(NativeImage image) {
            int width = image.getWidth();
            int height = image.getHeight();

            if (cursorX + width + ATLAS_PADDING > ATLAS_SIZE) {
                cursorX = ATLAS_PADDING;
                cursorY += rowHeight + ATLAS_PADDING;
                rowHeight = 0;
            }

            if (cursorY + height + ATLAS_PADDING > ATLAS_SIZE) {
                return null;
            }

            texture.bindTexture();
            image.upload(
                    0,
                    cursorX,
                    cursorY,
                    0,
                    0,
                    width,
                    height,
                    false
            );

            int u = cursorX;
            int v = cursorY;

            cursorX += width + ATLAS_PADDING;
            rowHeight = Math.max(rowHeight, height);

            return new AtlasPlacement(textureId, u, v);
        }
    }
}
