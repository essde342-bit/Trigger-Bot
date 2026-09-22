package kronex.fun.display.screens.clickgui.components.implement.other;

import net.minecraft.client.gui.DrawContext;
import kronex.fun.Kronex;
import kronex.fun.display.screens.clickgui.components.AbstractComponent;
import kronex.fun.other.utils.display.font.Fonts;
import kronex.fun.other.utils.display.shape.ShapeProperties;
import kronex.fun.other.utils.math.MathUtil;

public class StatusRender extends AbstractComponent {
    private String status = "Ready";
    private long statusUntil;

    public StatusRender() {
        width = 135;
        height = 20;
    }

    public void setStatus(String status, long durationMs) {
        this.status = status == null ? "" : status;
        this.statusUntil = System.currentTimeMillis() + Math.max(0, durationMs);
    }

    public String getStatus() {
        if (System.currentTimeMillis() > statusUntil && statusUntil != 0) return "Ready";
        return status;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        String value = getStatus();
        boolean hovered = MathUtil.isHovered(mouseX, mouseY, x, y, width, height);
        int bg = hovered ? 0xEE2B2E38 : 0xDD202229;
        rectangle.render(ShapeProperties.create(context.getMatrices(), x, y, width, height)
                .round(5).thickness(1).outlineColor(0x663D414B).color(bg).build());
        Fonts.getSize(11, Fonts.Type.DEFAULT).drawString(context.getMatrices(), value, x + 8, y + 7, 0xFFE1E2E7);
    }
}
