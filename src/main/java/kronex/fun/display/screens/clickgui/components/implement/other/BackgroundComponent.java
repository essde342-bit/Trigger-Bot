package kronex.fun.display.screens.clickgui.components.implement.other;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Util;
import kronex.fun.display.screens.clickgui.MenuScreen;
import kronex.fun.display.screens.clickgui.components.AbstractComponent;
import kronex.fun.other.utils.display.color.ColorAssist;
import kronex.fun.other.utils.display.font.Fonts;
import kronex.fun.other.utils.display.interfaces.QuickImports;
import kronex.fun.other.utils.display.other.animation.Animation;
import kronex.fun.other.utils.display.other.animation.Direction;
import kronex.fun.other.utils.display.other.animation.implement.DecelerateAnimation;
import kronex.fun.other.utils.display.shape.ShapeProperties;

@Setter
@Accessors(chain = true)
public class BackgroundComponent extends AbstractComponent implements QuickImports {
    private static final String KRONEX_URL = "https://kronex.fun";
    private static final String TELEGRAM_URL = "https://t.me/KronexDLC";
    private static final String DISCORD_URL = "https://discord.gg/WueJATMhpC";

    private float textX, textY, textWidth, textHeight;
    private float aX, aY, aWidth, aHeight;
    private float bX, bY, bWidth, bHeight;
    private final Animation hoverAnimation = new DecelerateAnimation().setMs(200).setValue(1);

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrix = context.getMatrices();

        rectangle.render(ShapeProperties.create(matrix, x, y, width, height)
                .round(6F).color(ColorAssist.BACKGROUND).build());

        rectangle.render(ShapeProperties.create(matrix, x + 85, y, width - 85, height)
                .round(4F).color(ColorAssist.HEADER).build());

        rectangle.render(ShapeProperties.create(matrix, x + 85, y + 28, width - 85, height - 28)
                .round(4F).color(ColorAssist.PANEL).build());

        rectangle.render(ShapeProperties.create(context.getMatrices(), x + 85, y, 0.5F, height)
                .color(ColorAssist.applyOpacity(ColorAssist.getOutline(), 90)).build());
        rectangle.render(ShapeProperties.create(context.getMatrices(), x + 85, y + 28, width - 85, 0.5F)
                .color(ColorAssist.applyOpacity(ColorAssist.getOutline(), 70)).build());

        renderBrand(context, mouseX, mouseY);
        renderSocialButtons(context, mouseX, mouseY);

        String title = MenuScreen.INSTANCE.isCosmeticsOpen()
                ? "Cosmetics"
                : MenuScreen.INSTANCE.getCategory().getReadableName();
        Fonts.getSize(20, Fonts.Type.BOLD).drawString(matrix, title, x + 95, y + 16.5f, 0xFFD4D6E1);
    }

    private void renderBrand(DrawContext context, int mouseX, int mouseY) {
        MatrixStack matrix = context.getMatrices();
        String kronexFunText = "kronex.fun";

        textX = x + 16;
        textY = y + 14;
        textWidth = Fonts.getSize(18, Fonts.Type.BOLD).getStringWidth(kronexFunText);
        textHeight = 18;

        boolean isHoverFun = isHoverFun(mouseX, mouseY);
        hoverAnimation.setDirection(isHoverFun ? Direction.FORWARDS : Direction.BACKWARDS);
        float scale = 1.0f + (hoverAnimation.getOutput().floatValue() * 0.1f);

        matrix.push();
        matrix.translate(textX + textWidth / 2f, textY + textHeight / 2f, 0);
        matrix.scale(scale, scale, 1);
        matrix.translate(-(textX + textWidth / 2f), -(textY + textHeight / 2f), 0);
        Fonts.getSize(18, Fonts.Type.BOLD).drawString(matrix, kronexFunText, textX, textY, ColorAssist.getClientColor());
        matrix.pop();
    }

    private void renderSocialButtons(DrawContext context, int mouseX, int mouseY) {
        float iconY = textY + 11f;
        float iconSize = 16f;
        float gap = 7f;

        aX = textX + 3f;
        aY = iconY;
        aWidth = iconSize;
        aHeight = iconSize;
        bX = aX + iconSize + gap;
        bY = iconY;
        bWidth = iconSize;
        bHeight = iconSize;

        renderSocialButton(context, "◉", aX, aY, aWidth, aHeight, isHoverA(mouseX, mouseY));
        renderSocialButton(context, "➤", bX, bY, bWidth, bHeight, isHoverB(mouseX, mouseY));
    }

    private void renderSocialButton(DrawContext context, String label, float buttonX, float buttonY, float buttonW, float buttonH, boolean hovered) {
        MatrixStack matrix = context.getMatrices();

        float textW = Fonts.getSize(15, Fonts.Type.ICONS2).getStringWidth(label);
        Fonts.getSize(15, Fonts.Type.ICONS2).drawString(matrix, label,
                buttonX + (buttonW - textW) / 2f,
                buttonY + 10f,
                hovered ? ColorAssist.getClientColor() : ColorAssist.getText());
    }

    @Override public void tick() {}

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
        if (isHoverFun(mouseX, mouseY)) { openLink(KRONEX_URL); return true; }
        if (isHoverA(mouseX, mouseY)) { openLink(DISCORD_URL); return true; }
        if (isHoverB(mouseX, mouseY)) { openLink(TELEGRAM_URL); return true; }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isHover(double mouseX, double mouseY) {
        return super.isHover(mouseX, mouseY) || isHoverFun(mouseX, mouseY)
                || isHoverA(mouseX, mouseY) || isHoverB(mouseX, mouseY);
    }

    private boolean isHoverFun(double mouseX, double mouseY) {
        return mouseX >= textX && mouseX <= textX + textWidth && mouseY >= textY && mouseY <= textY + textHeight;
    }

    private boolean isHoverA(double mouseX, double mouseY) {
        return mouseX >= aX && mouseX <= aX + aWidth && mouseY >= aY && mouseY <= aY + aHeight;
    }

    private boolean isHoverB(double mouseX, double mouseY) {
        return mouseX >= bX && mouseX <= bX + bWidth && mouseY >= bY && mouseY <= bY + bHeight;
    }

    private void openLink(String url) {
        try {
            Util.getOperatingSystem().open(url);
        } catch (Exception ignored) {
        }
    }
}