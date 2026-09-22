package kronex.fun.other.utils.display.interfaces;

import com.mojang.blaze3d.systems.RenderSystem;
import kronex.fun.display.screens.clickgui.components.implement.window.AbstractWindow;
import kronex.fun.display.screens.clickgui.components.implement.window.WindowManager;
import kronex.fun.other.utils.display.GuiRenderContext;
import kronex.fun.other.utils.display.shape.ShapeProperties;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;

public interface QuickImports {
    MinecraftClient mc = MinecraftClient.getInstance();
    Window window = mc.getWindow();
    GuiWindowManager windowManager = new GuiWindowManager();

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
            // The archive's texture calls are retained; Trigger-Bot has no matching
            // Kronex texture pack, so unsupported textures are intentionally skipped.
        }
    }

    final class GuiWindowManager {
        private WindowManager delegate;

        private WindowManager delegate() {
            if (delegate == null) delegate = new WindowManager();
            return delegate;
        }

        public void add(AbstractWindow window) { delegate().add(window); }
        public java.util.List<AbstractWindow> getWindows() { return delegate().getWindows(); }
        public void delete(AbstractWindow window) { delegate().delete(window); }

        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            GuiRenderContext.set(context);
            delegate().render(context, mouseX, mouseY, delta);
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return delegate().mouseClicked(mouseX, mouseY, button);
        }

        public void mouseReleased(double mouseX, double mouseY, int button) {
            delegate().mouseReleased(mouseX, mouseY, button);
        }

        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            return delegate().mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }

        public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
            return delegate().mouseScrolled(mouseX, mouseY, amount);
        }

        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return delegate().keyPressed(keyCode, scanCode, modifiers);
        }

        public boolean charTyped(char chr, int modifiers) {
            return delegate().charTyped(chr, modifiers);
        }
    }
}