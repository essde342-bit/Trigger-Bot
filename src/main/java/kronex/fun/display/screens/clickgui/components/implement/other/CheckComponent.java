package kronex.fun.display.screens.clickgui.components.implement.other;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import kronex.fun.other.utils.display.other.animation.implement.DecelerateAnimation;
import kronex.fun.other.utils.display.shape.ShapeProperties;
import kronex.fun.other.utils.display.color.ColorAssist;
import kronex.fun.other.utils.math.MathUtil;
import kronex.fun.display.screens.clickgui.components.AbstractComponent;


@Setter
@Accessors(chain = true)
public class CheckComponent extends AbstractComponent {
    private boolean state;
    private Runnable runnable;

    private final kronex.fun.other.utils.display.other.animation.Animation alphaAnimation =
            new DecelerateAnimation().setMs(300).setValue(255);

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrix = context.getMatrices();
        alphaAnimation.setDirection(state ? FORWARDS : BACKWARDS);
        int stateColor = state ? ColorAssist.getClientColor() : ColorAssist.getGuiRectColor(1);
        int outlineStateColor = state ? ColorAssist.getClientColor() : ColorAssist.getOutline();
        int opacity = alphaAnimation.getOutput().intValue();

        rectangle.render(ShapeProperties.create(matrix, x, y, 8, 8)
                .round(1.5F).thickness(2).softness(0.5F)
                .outlineColor(outlineStateColor)
                .color(ColorAssist.applyOpacity(stateColor, opacity)).build());

        if (state) {
            rectangle.render(
                    ShapeProperties.create(matrix, x + 2, y + 2, 4, 4)
                            .round(1).color(0xFFFFFFFF).build()
            );
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (MathUtil.isHovered(mouseX, mouseY, x, y, 8, 8) && button == 0 && runnable != null) {
            runnable.run();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}