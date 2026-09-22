package kronex.fun.display.screens.clickgui.components.implement.other;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import kronex.fun.other.utils.display.font.Fonts;
import kronex.fun.other.utils.display.shape.ShapeProperties;
import kronex.fun.other.utils.display.color.ColorAssist;
import kronex.fun.other.utils.math.MathUtil;
import kronex.fun.other.utils.display.other.animation.Direction;
import kronex.fun.other.utils.display.other.animation.implement.DecelerateAnimation;
import kronex.fun.display.screens.clickgui.MenuScreen;
import kronex.fun.display.screens.clickgui.components.AbstractComponent;

public class CosmeticsButton extends AbstractComponent {
    private final kronex.fun.other.utils.display.other.animation.Animation hoverAnimation =
            new DecelerateAnimation().setMs(200).setValue(1);

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrix = context.getMatrices();
        boolean hovered = MathUtil.isHovered(mouseX, mouseY, x, y, width, height);
        boolean active = MenuScreen.INSTANCE.isCosmeticsOpen();

        hoverAnimation.setDirection((hovered || active) ? Direction.FORWARDS : Direction.BACKWARDS);
        float anim = hoverAnimation.getOutput().floatValue();

        rectangle.render(ShapeProperties.create(matrix, x, y, width, height).round(2.25F).thickness(2)
                .outlineColor(active ? ColorAssist.getClientColor() : ColorAssist.getClientColor(anim))
                .color(active ? ColorAssist.applyOpacity(ColorAssist.getClientColor(), 40) : ColorAssist.getGuiRectColor2(anim / 2))
                .build());

        image.setTexture("textures/gui/categories/misc.png")
                .render(ShapeProperties.create(matrix, x + 7, y + 4.5F, 8, 8)
                        .color((hovered || active) ? ColorAssist.getClientColor() : ColorAssist.getText()).build());

        Fonts.getSize(14, Fonts.Type.BOLD).drawString(matrix, "Cosmetics", x + 22, y + 7,
                (hovered || active) ? ColorAssist.getClientColor() : ColorAssist.getText());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (MathUtil.isHovered(mouseX, mouseY, x, y, width, height) && button == 0) {
            MenuScreen.INSTANCE.setCosmeticsOpen(!MenuScreen.INSTANCE.isCosmeticsOpen());
            if (MenuScreen.INSTANCE.isCosmeticsOpen()) MenuScreen.INSTANCE.getCosmeticsPanel().init();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}