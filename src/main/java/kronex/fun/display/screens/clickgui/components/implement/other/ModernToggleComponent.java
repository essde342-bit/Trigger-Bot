package kronex.fun.display.screens.clickgui.components.implement.other;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import kronex.fun.other.utils.display.font.Fonts;
import kronex.fun.other.utils.display.shape.ShapeProperties;
import kronex.fun.other.utils.display.color.ColorAssist;
import kronex.fun.other.utils.math.MathUtil;
import kronex.fun.other.utils.display.other.animation.Direction;
import kronex.fun.other.utils.display.other.animation.implement.DecelerateAnimation;
import kronex.fun.display.screens.clickgui.components.AbstractComponent;
import kronex.fun.other.utils.display.interfaces.QuickImports;

import static kronex.fun.other.utils.display.font.Fonts.Type.BOLD;

@Setter
@Accessors(chain = true)
public class ModernToggleComponent extends AbstractComponent implements QuickImports {
    private String title;
    private String description;
    private boolean enabled;
    private Runnable toggleRunnable;

    private final kronex.fun.other.utils.display.other.animation.Animation hoverAnimation = new DecelerateAnimation().setMs(200).setValue(1);
    private final kronex.fun.other.utils.display.other.animation.Animation enableAnimation = new DecelerateAnimation().setMs(300).setValue(1);

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrix = context.getMatrices();
        boolean hovered = MathUtil.isHovered(mouseX, mouseY, x, y, width, height);
        hoverAnimation.setDirection(hovered ? Direction.FORWARDS : Direction.BACKWARDS);
        enableAnimation.setDirection(enabled ? Direction.FORWARDS : Direction.BACKWARDS);

        float hoverAnim = hoverAnimation.getOutput().floatValue();
        float enableAnim = enableAnimation.getOutput().floatValue();

        width = 120;
        height = 40;

        int backgroundColor = enabled
                ? ColorAssist.applyOpacity(ColorAssist.getClientColor(), (int) (enableAnim * 200))
                : ColorAssist.getGuiRectColor(1);

        rectangle.render(ShapeProperties.create(matrix, x, y, width, height).round(8).color(backgroundColor).build());

        if (hoverAnim > 0 && !enabled) {
            rectangle.render(ShapeProperties.create(matrix, x, y, width, height)
                    .round(8)
                    .color(ColorAssist.applyOpacity(ColorAssist.getClientColor(), (int) (hoverAnim * 30)))
                    .build());
        }

        rectangle.render(ShapeProperties.create(matrix, x, y, width, height)
                .round(8).thickness(1.5f).outlineColor(ColorAssist.getOutline()).build());

        drawIcon(matrix, x + 12, y + height / 2f);

        int titleColor = enabled ? 0xFFFFFFFF : 0xFFD4D6E1;
        Fonts.getSize(14, BOLD).drawString(matrix, title, x + 35, y + 13, titleColor);

        int descColor = enabled
                ? ColorAssist.applyOpacity(0xFFFFFFFF, (int) (enableAnim * 180))
                : ColorAssist.applyOpacity(0xFFD4D6E1, 180);
        Fonts.getSize(11, Fonts.Type.DEFAULT).drawString(matrix, description, x + 35, y + 26, descColor);
    }

    private void drawIcon(MatrixStack matrix, float iconX, float iconY) {
        float squareSize = 4f;
        float spacing = 2f;
        int iconColor = enabled ? 0xFFFFFFFF : ColorAssist.applyOpacity(0xFFD4D6E1, 200);

        rectangle.render(ShapeProperties.create(matrix, iconX - spacing, iconY - spacing - squareSize, squareSize, squareSize).color(iconColor).build());
        rectangle.render(ShapeProperties.create(matrix, iconX + spacing, iconY - spacing - squareSize, squareSize, squareSize).color(iconColor).build());
        rectangle.render(ShapeProperties.create(matrix, iconX + spacing + 1, iconY - spacing - squareSize + 1.5f, squareSize - 2, 1).color(0xFF1A1A1F).build());
        rectangle.render(ShapeProperties.create(matrix, iconX + spacing + 1.5f, iconY - spacing - squareSize + 1, 1, squareSize - 2).color(0xFF1A1A1F).build());
        rectangle.render(ShapeProperties.create(matrix, iconX - spacing, iconY + spacing, squareSize, squareSize).color(iconColor).build());
        rectangle.render(ShapeProperties.create(matrix, iconX + spacing, iconY + spacing, squareSize, squareSize).color(iconColor).build());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (MathUtil.isHovered(mouseX, mouseY, x, y, width, height) && button == 0) {
            enabled = !enabled;
            if (toggleRunnable != null) toggleRunnable.run();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isHover(double mouseX, double mouseY) {
        return MathUtil.isHovered(mouseX, mouseY, x, y, width, height);
    }

    public boolean isEnabled() {
        return enabled;
    }
}
