package kronex.fun.display.screens.clickgui.components.implement.window.implement;

import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;
import kronex.fun.features.impl.render.hud.utils.HudUtil;
import kronex.fun.other.utils.display.color.HudColorutility;
import kronex.fun.other.utils.display.font.Fonts;
import kronex.fun.other.utils.display.shape.ShapeProperties;
import kronex.fun.other.utils.display.color.ColorAssist;
import kronex.fun.other.utils.math.MathUtil;
import kronex.fun.other.utils.other.StringUtil;
import kronex.fun.display.screens.clickgui.components.implement.window.AbstractWindow;
import static kronex.fun.other.utils.display.other.animation.Direction.*;

@RequiredArgsConstructor
public abstract class AbstractBindWindow extends AbstractWindow {
    private boolean binding;
    private final kronex.fun.other.utils.display.other.animation.Animation bindingOverlayAnimation =
            new kronex.fun.other.utils.display.other.animation.implement.DecelerateAnimation().setMs(180).setValue(1);

    protected abstract int getKey();
    protected abstract void setKey(int key);
    protected abstract int getType();
    protected abstract void setType(int type);

    protected String getTitle() { return "Binding module"; }

    @Override
    public void drawWindow(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrix = context.getMatrices();
        bindingOverlayAnimation.setDirection(binding ? FORWARDS : BACKWARDS);

        HudUtil.drawHudBlur(ShapeProperties.create(matrix, x, y, width, height).round(6).softness(1)
                .thickness(0).color(HudColorutility.getRectGradient(0.85F, 0.55F)).build());

        rectangle.render(ShapeProperties.create(matrix, x, y, width, height).round(6).thickness(2)
                .outlineColor(ColorAssist.getOutline(0.8F, 1)).color(ColorAssist.getGuiRectColor(0.55f)).build());

        Fonts.getSize(14, Fonts.Type.DEFAULT).drawString(matrix, getTitle(), x + 5, y + 8, -1);
        image.setTexture("textures/trash.png").render(ShapeProperties.create(matrix, x + width - 13, y + 5.3f, 8, 8).build());

        drawKeyButton(matrix);
        drawTypeButton(matrix);
        drawBindingBackdrop(context);
        drawBindingPrompt(context);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (MathUtil.isHovered(mouseX, mouseY, x + width - 57, y + 37F, 52, 13)) {
                setType(getType() != 1 ? 1 : 0);
            }
            if (MathUtil.isHovered(mouseX, mouseY, getKeyButtonX(), getKeyButtonY(), getKeyButtonWidth(), getKeyButtonHeight())) {
                binding = !binding;
            }
            if (MathUtil.isHovered(mouseX, mouseY, x + width - 13, y + 5.3f, 8, 8)) {
                startCloseAnimation();
                return true;
            }
        }
        if (binding && button > 1) {
            setKey(button);
            binding = false;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        int key = keyCode == GLFW.GLFW_KEY_DELETE ? -1 : keyCode;
        if (binding) {
            setKey(key);
            binding = false;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void drawBindingBackdrop(DrawContext context) {
        if (bindingOverlayAnimation.isFinished(BACKWARDS)) return;
        MatrixStack matrix = context.getMatrices();
        float alpha = bindingOverlayAnimation.getOutput().floatValue();

        blur.render(ShapeProperties.create(matrix, 0, 0, window.getScaledWidth(), window.getScaledHeight())
                .quality(4).round(0).softness(1)
                .color(ColorAssist.applyOpacity(0xFF000000, (int) (65 * alpha))).build());

        rectangle.render(ShapeProperties.create(matrix, 0, 0, window.getScaledWidth(), window.getScaledHeight())
                .color(ColorAssist.applyOpacity(0xFF000000, (int) (45 * alpha))).build());
    }

    private void drawBindingPrompt(DrawContext context) {
        if (bindingOverlayAnimation.isFinished(BACKWARDS)) return;

        MatrixStack matrix = context.getMatrices();
        float alpha = bindingOverlayAnimation.getOutput().floatValue();
        float centerX = window.getScaledWidth() / 2f;
        float centerY = window.getScaledHeight() / 2f;

        matrix.push();
        float scale = 0.92f + 0.08f * alpha;
        matrix.translate(centerX, centerY, 0);
        matrix.scale(scale, scale, 1f);
        matrix.translate(-centerX, -centerY, 0);

        String title = "press on button to bind";
        String subtitle = "del to cancel";
        float titleW = Fonts.getSize(18, Fonts.Type.BOLD).getStringWidth(title);
        float subtitleW = Fonts.getSize(13, Fonts.Type.DEFAULT).getStringWidth(subtitle);
        int titleColor = ColorAssist.applyOpacity(0xFFFFFFFF, (int) (255 * alpha));
        int subtitleColor = ColorAssist.applyOpacity(0xFF9EA1AD, (int) (255 * alpha));

        Fonts.getSize(18, Fonts.Type.BOLD).drawString(matrix, title, centerX - titleW / 2f, centerY - 3f, titleColor);
        Fonts.getSize(13, Fonts.Type.DEFAULT).drawString(matrix, subtitle, centerX - subtitleW / 2f, centerY + 12f, subtitleColor);
        matrix.pop();
    }

    private void drawKeyButton(MatrixStack matrix) {
        int bindingColor = binding ? 0xFF8187FF : 0xFFD4D6E1;
        String keyName = binding ? "..." : StringUtil.getBindName(getKey());
        float stringWidth = Fonts.getSize(12, Fonts.Type.BOLD).getStringWidth(keyName);

        rectangle.render(ShapeProperties.create(matrix, getKeyButtonX(), getKeyButtonY(), getKeyButtonWidth(), getKeyButtonHeight())
                .round(2).thickness(1.5f).softness(0.5f)
                .outlineColor(binding ? 0xFF8187FF : ColorAssist.getOutline(0.8F, 1))
                .color(ColorAssist.getGuiRectColor(binding ? 0.55f : 0.3f)).build());

        Fonts.getSize(12, Fonts.Type.BOLD).drawString(matrix, keyName,
                getKeyButtonX() + (getKeyButtonWidth() - stringWidth) / 2f,
                getKeyButtonY() + 5.4f, bindingColor);
        Fonts.getSize(14, Fonts.Type.DEFAULT).drawString(matrix, "Key", x + 5, (int) (y + 24.3), 0xFFD4D6E1);
    }

    private float getKeyButtonX() { return x + width - 29f; }
    private float getKeyButtonY() { return y + 16.2f; }
    private float getKeyButtonWidth() { return 20f; }
    private float getKeyButtonHeight() { return 12f; }

    private void drawTypeButton(MatrixStack matrix) {
        rectangle.render(ShapeProperties.create(matrix, x + width - 57, y + 37F, 24, 13)
                .round(2).thickness(2).softness(1).outlineColor(ColorAssist.getOutline(0.8F, 1))
                .color(ColorAssist.getOutline(0.1F, 1)).build());
        rectangle.render(ShapeProperties.create(matrix, x + width - 31, y + 37F, 26, 13)
                .round(2).thickness(2).softness(1).outlineColor(ColorAssist.getOutline(0.8F, 1))
                .color(ColorAssist.getOutline(0.1F, 1)).build());

        if (getType() == 1) {
            rectangle.render(ShapeProperties.create(matrix, x + width - 31, y + 37F, 26, 13).round(2).color(0xFF8187FF).build());
        } else {
            rectangle.render(ShapeProperties.create(matrix, x + width - 57, y + 37F, 24, 13).round(2).color(0xFF8187FF).build());
        }

        Fonts.getSize(12, Fonts.Type.DEFAULT).drawString(matrix, "HOLD", x + width - 52.5f, y + 42.1, 0xFFD4D6E1);
        Fonts.getSize(12, Fonts.Type.DEFAULT).drawString(matrix, "TOGGLE", x + width - 30.5f, y + 42.1, 0xFFD4D6E1);
        Fonts.getSize(14, Fonts.Type.DEFAULT).drawString(matrix, "Bind mode", x + 5, y + 42.3F, 0xFFD4D6E1);
    }
}