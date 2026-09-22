package kronex.fun.display.screens.clickgui.components.implement.other;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import kronex.fun.other.utils.display.other.animation.implement.DecelerateAnimation;
import kronex.fun.other.utils.display.shape.ShapeProperties;
import kronex.fun.other.utils.display.color.ColorAssist;
import kronex.fun.other.utils.math.MathUtil;
import kronex.fun.other.utils.display.scissor.ScissorAssist;
import kronex.fun.Kronex;
import kronex.fun.display.screens.clickgui.components.AbstractComponent;

import static kronex.fun.other.utils.display.other.animation.Direction.BACKWARDS;
import static kronex.fun.other.utils.display.other.animation.Direction.FORWARDS;

@Setter
@Accessors(chain = true)
public class CheckComponent extends AbstractComponent {
    private boolean state;
    private Runnable runnable;

    private final kronex.fun.other.utils.display.other.animation.Animation alphaAnimation =
            new DecelerateAnimation().setMs(300).setValue(255);
    private final kronex.fun.other.utils.display.other.animation.Animation stencilAnimation =
            new DecelerateAnimation().setMs(200).setValue(8);

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrix = context.getMatrices();
        alphaAnimation.setDirection(state ? FORWARDS : BACKWARDS);
        stencilAnimation.setDirection(state ? FORWARDS : BACKWARDS);

        int stateColor = state ? ColorAssist.getClientColor() : ColorAssist.getGuiRectColor(1);
        int outlineStateColor = state ? ColorAssist.getClientColor() : ColorAssist.getOutline();
        int opacity = alphaAnimation.getOutput().intValue();

        rectangle.render(ShapeProperties.create(matrix, x, y, 8, 8)
                .round(1.5F).thickness(2).softness(0.5F)
                .outlineColor(outlineStateColor)
                .color(ColorAssist.applyOpacity(stateColor, opacity)).build());

        ScissorAssist scissor = Kronex.getInstance().getScissorManager();
        scissor.push(matrix.peek().getPositionMatrix(), x, (float) window.getScaledHeight() / 2 - 96,
                stencilAnimation.getOutput().intValue(), 220);

        image.setTexture("textures/check.png")
                .render(ShapeProperties.create(matrix, x + 2, y + 2.5f, 4, 3)
                        .color(ColorAssist.applyOpacity(0xFFFFFFFF, opacity)).build());
        scissor.pop();
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