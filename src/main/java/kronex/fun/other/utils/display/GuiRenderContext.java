package kronex.fun.other.utils.display;

import net.minecraft.client.gui.DrawContext;

/**
 * Keeps the current DrawContext only during an active GUI render pass.
 */
public final class GuiRenderContext {
    private static final ThreadLocal<DrawContext> CURRENT = new ThreadLocal<>();

    private GuiRenderContext() {
    }

    public static void set(DrawContext context) {
        CURRENT.set(context);
    }

    public static DrawContext get() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }
}
