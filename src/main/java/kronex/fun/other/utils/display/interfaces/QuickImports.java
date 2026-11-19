package kronex.fun.other.utils.display.interfaces;

import kronex.fun.other.utils.display.GuiRenderContext;
import kronex.fun.other.utils.display.shape.ShapeProperties;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;

/**
 * Legacy rendering helpers retained for non-menu code.
 * The old Java window system has been removed; Astra's active menu is Kotlin.
 */
public interface QuickImports {
    MinecraftClient mc = MinecraftClient.getInstance();
    Window window = mc.getWindow();

    PrimitiveRenderer rectangle = new PrimitiveRenderer(false);
    PrimitiveRenderer blur = new PrimitiveRenderer(true);
    ImageRenderer image = new ImageRenderer();

    final class PrimitiveRenderer {
        private final boolean blur;

        public PrimitiveRenderer(boolean blur) {
            this.blur = blur;
        }

        public void render(ShapeProperties p) {
            DrawContext context = GuiRenderContext.get();
            if (context == null) return;

            int fill = p.color;
            if (blur) {
                fill = (fill & 0x00FFFFFF) | (((fill >>> 24) / 2) << 24);
            }

            int x1 = Math.round(p.x);
            int y1 = Math.round(p.y);
            int x2 = Math.round(p.x + p.width);
            int y2 = Math.round(p.y + p.height);

            if (x2 <= x1 || y2 <= y1) return;
            context.fill(x1, y1, x2, y2, fill);

            if (p.thickness > 0 && p.outlineColor != 0) {
                int t = Math.max(1, Math.round(p.thickness));
                context.fill(x1, y1, x2, Math.min(y1 + t, y2), p.outlineColor);
                context.fill(x1, Math.max(y2 - t, y1), x2, y2, p.outlineColor);
                context.fill(x1, y1, Math.min(x1 + t, x2), y2, p.outlineColor);
                context.fill(Math.max(x2 - t, x1), y1, x2, y2, p.outlineColor);
            }
        }
    }

    final class ImageRenderer {
        private String texture;

        public ImageRenderer setTexture(String texture) {
            this.texture = texture;
            return this;
        }

        public void render(ShapeProperties p) {
            // Unsupported legacy texture calls are intentionally ignored.
        }
    }

    /**
     * Source-compatibility shim. It deliberately does not render or store
     * Java windows; all live UI is owned by MenuScreen.kt.
     */
    final class GuiWindowManager {
        public void add(Object window) {}
        public java.util.List<Object> getWindows() {
            return java.util.Collections.emptyList();
        }
        public void delete(Object window) {}
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            GuiRenderContext.set(context);
        }
        public boolean mouseClicked(double mouseX, double mouseY, int button) { return false; }
        public void mouseReleased(double mouseX, double mouseY, int button) {}
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) { return false; }
        public boolean mouseScrolled(double mouseX, double mouseY, double amount) { return false; }
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) { return false; }
        public boolean charTyped(char chr, int modifiers) { return false; }
    }
}
