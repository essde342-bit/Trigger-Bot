package kronex.fun.display.screens.clickgui.components.implement.other;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import kronex.fun.other.common.discord.DiscordManager;
import kronex.fun.other.utils.display.font.Fonts;
import kronex.fun.other.utils.display.shape.ShapeProperties;
import kronex.fun.other.utils.display.color.ColorAssist;
import kronex.fun.other.utils.other.StringUtil;
import kronex.fun.other.utils.display.geometry.Render2D;
import kronex.fun.other.utils.display.scissor.ScissorAssist;
import kronex.fun.Kronex;
import kronex.fun.display.screens.clickgui.components.AbstractComponent;

@Setter
@Accessors(chain = true)
public class UserComponent extends AbstractComponent {
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrix = context.getMatrices();
        Matrix4f positionMatrix = matrix.peek().getPositionMatrix();
        DiscordManager discord = Kronex.getInstance().getDiscordManager();

        rectangle.render(ShapeProperties.create(matrix, x + 5, y - 30, 75, 25)
                .round(4).thickness(2).softness(0.5F).outlineColor(ColorAssist.getOutline()).color(ColorAssist.getGuiRectColor(0.5F)).build());

        Render2D.drawTexture(context, discord.getAvatarId(), x + 11, y - 25, 15, 7.5F, 0, 15, 21, ColorAssist.getGuiRectColor(1));

        rectangle.render(ShapeProperties.create(matrix, x + 21.5F, y - 15.5F, 5, 5)
                .round(2.5F).color(ColorAssist.getGuiRectColor(1)).build());

        rectangle.render(ShapeProperties.create(matrix, x + 22.5F, y - 14.5F, 3, 3)
                .round(1.5F).color(0xFF26c68c).build());

        ScissorAssist scissor = Kronex.getInstance().getScissorManager();
        scissor.push(positionMatrix, x + 5.5F, y - 29.5F, 74, 22);
        Fonts.getSize(12, Fonts.Type.DEFAULT).drawString(matrix, by.x.j2c.UserProfile.instance.username(), x + 30, y - 21, 0xFFD4D6E1);
        Fonts.getSize(10, Fonts.Type.DEFAULT).drawGradientString(matrix, StringUtil.getUserRole(), x + 30, y - 14.5, ColorAssist.fade(0), ColorAssist.fade(60));
        scissor.pop();
    }
}