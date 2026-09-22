package kronex.fun.other.utils.display;

import net.minecraft.client.gui.DrawContext;

public final class GuiRenderContext {
    private static DrawContext current;

    private GuiRenderContext() {}

    public static void set(DrawContext context) {
        current = context;
    }

    public static DrawContext get() {
        return current;
    }
}