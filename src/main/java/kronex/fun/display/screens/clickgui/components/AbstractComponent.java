package kronex.fun.display.screens.clickgui.components;

import kronex.fun.other.utils.display.interfaces.QuickImports;
import kronex.fun.other.utils.client.interfaces.ResizableMovable;
import kronex.fun.other.utils.math.MathUtil;

public abstract class AbstractComponent implements Component, QuickImports, ResizableMovable {
    public float x, y, width, height;
    public double scroll = 0;
    public double smoothedScroll = 0;

    public static float centerY(float rowY, float rowHeight, float elemHeight, float offset) {
        return rowY + (rowHeight - elemHeight) / 2f + offset;
    }

    public static float centerY(float rowY, float rowHeight, float elemHeight) {
        return centerY(rowY, rowHeight, elemHeight, 0);
    }

    @Override
    public ResizableMovable position(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    @Override
    public ResizableMovable size(float width, float height) {
        this.width = width;
        this.height = height;
        return this;
    }

    @Override
    public void tick() {
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) { return false; }
    @Override public boolean mouseReleased(double mouseX, double mouseY, int button) { return false; }
    @Override public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) { return false; }
    @Override public boolean mouseScrolled(double mouseX, double mouseY, double amount) { return false; }
    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) { return false; }
    @Override public boolean charTyped(char chr, int modifiers) { return false; }

    @Override
    public boolean isHover(double mouseX, double mouseY) {
        return MathUtil.isHovered(mouseX, mouseY, x, y, width, height);
    }
}