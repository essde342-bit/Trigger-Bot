package kronex.fun.other.utils.display.geometry;

import net.minecraft.client.gui.DrawContext;

public final class Render2D {
    private Render2D() {}

    public static void drawTexture(
            DrawContext context,
            int textureId,
            float x,
            float y,
            float width,
            float height,
            float u,
            float v,
            float textureWidth,
            int color
    ) {
        // The archive expects an external avatar texture cache. Trigger-Bot has no
        // equivalent cache, so the reserved area is left as a normal GUI rectangle.
        context.fill(Math.round(x), Math.round(y), Math.round(x + width), Math.round(y + height), color);
    }
}