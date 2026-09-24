package kronex.fun.other.utils.display.font;

import kronex.fun.other.utils.display.GuiRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Objects;

public final class FontRenderer {
    /*
     * The source asset is the user's GalahadStd Regular OTF.
     * The build converts it to a TrueType glyf font because Minecraft 1.21.x
     * loads GUI font providers through its TrueType pipeline.
     */
    private static final Identifier GALAHAD_FONT =
            Identifier.of("triggerbot", "galahad");

    private static final float BASE_FONT_SIZE = 32.0F;

    private final float logicalSize;
    private final Fonts.Type type;

    FontRenderer(float size, Fonts.Type type) {
        this.logicalSize = Math.max(7F, size);
        this.type = Objects.requireNonNull(type);
    }

    public float getStringWidth(String text) {
        if (text == null || text.isEmpty()) {
            return 0.0F;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.textRenderer == null) {
            return text.length() * logicalSize * 0.5F;
        }

        float scale = getScale();
        Text styled = styled(text, 0xFFFFFFFF);
        return client.textRenderer.getWidth(styled) * scale;
    }

    public float getStringHeight(String text) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.textRenderer == null) {
            return logicalSize;
        }

        return client.textRenderer.fontHeight * getScale();
    }

    public void drawString(
            net.minecraft.client.util.math.MatrixStack matrix,
            String text,
            float x,
            float y,
            int color
    ) {
        drawString(matrix, text, (double) x, (double) y, color);
    }

    public void drawString(
            net.minecraft.client.util.math.MatrixStack matrix,
            String text,
            double x,
            double y,
            int color
    ) {
        drawText(matrix, text, x, y, color, color, false);
    }

    public void drawCenteredString(
            net.minecraft.client.util.math.MatrixStack matrix,
            String text,
            double centerX,
            double y,
            int color
    ) {
        drawString(
                matrix,
                text,
                centerX - getStringWidth(text) / 2.0D,
                y,
                color
        );
    }

    public void drawStringWithScroll(
            net.minecraft.client.util.math.MatrixStack matrix,
            String text,
            float x,
            float y,
            float maxWidth,
            int color
    ) {
        if (text == null || text.isEmpty()) {
            return;
        }

        String visible = text;
        while (!visible.isEmpty() && getStringWidth(visible) > maxWidth) {
            int end = visible.offsetByCodePoints(visible.length(), -1);
            visible = visible.substring(0, end);
        }

        drawString(matrix, visible, x, y, color);
    }

    public void drawGradientString(
            net.minecraft.client.util.math.MatrixStack matrix,
            String text,
            float x,
            float y,
            int color1,
            int color2
    ) {
        drawText(matrix, text, x, y, color1, color2);
    }

    public void drawGradientString(
            net.minecraft.client.util.math.MatrixStack matrix,
            String text,
            double x,
            double y,
            int color1,
            int color2
    ) {
        drawText(matrix, text, x, y, color1, color2);
    }

    private void drawText(
            net.minecraft.client.util.math.MatrixStack matrix,
            String text,
            double x,
            double y,
            int color1,
            int color2
    ) {
        drawText(matrix, text, x, y, color1, color2, true);
    }

    private void drawText(
            net.minecraft.client.util.math.MatrixStack matrix,
            String text,
            double x,
            double y,
            int color1,
            int color2,
            boolean gradient
    ) {
        if (text == null || text.isEmpty()) {
            return;
        }

        DrawContext context = GuiRenderContext.get();
        MinecraftClient client = MinecraftClient.getInstance();

        if (context == null || client == null || client.textRenderer == null) {
            return;
        }

        float scale = getScale();
        MutableText styled = gradient
                ? gradientText(text, color1, color2)
                : styled(text, color1);

        net.minecraft.client.util.math.MatrixStack matrices =
                matrix != null ? matrix : context.getMatrices();

        matrices.push();
        try {
            matrices.scale(scale, scale, 1.0F);

            int drawX = (int) Math.round(x / scale);
            int drawY = (int) Math.round(y / scale);

            // The text uses Style.withFont(Galahad), so this is not Minecraft's
            // default font as long as the bundled Galahad provider is valid.
            context.drawText(
                    client.textRenderer,
                    styled,
                    drawX,
                    drawY,
                    gradient ? 0xFFFFFFFF : color1,
                    false
            );
        } finally {
            matrices.pop();
        }
    }

    private MutableText styled(String text, int color) {
        Style style = baseStyle();

        if ((color & 0x00FFFFFF) != 0x00FFFFFF) {
            style = style.withColor(color & 0x00FFFFFF);
        }

        return Text.literal(text).setStyle(style);
    }

    private MutableText gradientText(String text, int color1, int color2) {
        MutableText result = Text.empty();

        int[] codePoints = text.codePoints().toArray();
        int count = Math.max(1, codePoints.length);

        for (int i = 0; i < codePoints.length; i++) {
            float t = count == 1 ? 0.0F : (float) i / (float) (count - 1);

            int r = lerpChannel((color1 >>> 16) & 0xFF, (color2 >>> 16) & 0xFF, t);
            int g = lerpChannel((color1 >>> 8) & 0xFF, (color2 >>> 8) & 0xFF, t);
            int b = lerpChannel(color1 & 0xFF, color2 & 0xFF, t);

            int rgb = (r << 16) | (g << 8) | b;
            String part = new String(Character.toChars(codePoints[i]));

            result.append(
                    Text.literal(part)
                            .setStyle(baseStyle().withColor(rgb))
            );
        }

        return result;
    }

    private Style baseStyle() {
        Style style = Style.EMPTY.withFont(GALAHAD_FONT);

        if (type == Fonts.Type.BOLD || type == Fonts.Type.HUD) {
            style = style.withBold(true);
        }

        return style;
    }

    private float getScale() {
        return logicalSize / BASE_FONT_SIZE;
    }

    private static int lerpChannel(int first, int second, float t) {
        return Math.round(first + (second - first) * t);
    }
}
