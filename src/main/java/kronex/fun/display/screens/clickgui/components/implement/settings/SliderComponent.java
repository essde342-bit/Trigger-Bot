package kronex.fun.display.screens.clickgui.components.implement.settings;

import kronex.fun.other.utils.display.color.ColorAssist;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import kronex.fun.features.module.setting.implement.SliderSettings;
import kronex.fun.other.utils.display.font.Fonts;
import kronex.fun.other.utils.display.shape.ShapeProperties;
import kronex.fun.other.utils.math.calc.Calculate;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import static kronex.fun.other.utils.display.font.Fonts.Type.*;
import static kronex.fun.other.common.localization.Localization.get;

public class SliderComponent extends AbstractSettingComponent {
    public static final int SLIDER_WIDTH = 65;
    private final SliderSettings setting;
    private boolean dragging;
    private double animation;

    public SliderComponent(SliderSettings setting) {
        super(setting);
        this.setting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrix = context.getMatrices();

        height = 28;
        String value = setting.isInteger() ? String.valueOf((int) setting.getValue()) : String.valueOf(setting.getValue());
        float valueW = Fonts.getSize(14, BOLD).getStringWidth(value);
        float nameX = x + 8;
        float nameY = y + 9f;
        float maxNameW = Math.max(0, (x + width - 9f - valueW - 6f) - nameX);

        float nameWidth = Fonts.getSize(14, DEFAULT).getStringWidth(setting.getName());
        if (nameWidth > maxNameW) {
            Fonts.getSize(14, DEFAULT).drawStringWithScroll(
                    matrix, get(setting.getName()), nameX, nameY, maxNameW, Color.WHITE.getRGB()
            );
        } else {
            Fonts.getSize(14, DEFAULT).drawString(matrix, get(setting.getName()), nameX, nameY, Color.WHITE.getRGB());
        }

        Fonts.getSize(14, BOLD).drawString(matrix, value, x + width - 9 - valueW, nameY, Color.WHITE.getRGB());

        float sliderX = x + 8;
        float sliderY = y + 22f;
        float sliderW = Math.max(40f, width - 17f);
        float diff = getDifference(mouseX, matrix, sliderX, sliderY, sliderW);
        changeValue(diff, sliderW);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float sliderX = x + 8;
        float sliderY = y + 22f;
        float sliderW = Math.max(40f, width - 17f);
        dragging = Calculate.isHovered(mouseX, mouseY, sliderX, sliderY - 3.5f, sliderW, 8f) && button == 0;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private float getDifference(int mouseX, MatrixStack matrix, float sliderX, float sliderY, float sliderW) {
        float percentValue = (float) (sliderW * (setting.getValue() - setting.getMin()) / (setting.getMax() - setting.getMin()));
        float difference = MathHelper.clamp(mouseX - sliderX, 0, sliderW);

        animation = Calculate.interpolate((float) animation, percentValue);

        int clientColor = kronex.fun.features.impl.render.Interface.getInstance().colorSetting.getColor() | 0xFF000000;
        int sliderFillColor = ColorAssist.multAlpha(clientColor, 0.7f);

        rectangle.render(ShapeProperties.create(matrix, sliderX, sliderY, sliderW, 3).round(1).color(0x2D2E414D).build());
        rectangle.render(ShapeProperties.create(matrix, sliderX, sliderY, (float) animation, 3).round(1).color(sliderFillColor).build());

        float v = MathHelper.clamp((float) (sliderX + animation), sliderX, sliderX + sliderW);
        blur.render(ShapeProperties.create(matrix, v - 3F, sliderY - 1F, 5, 5)
                .round(3).softness(0).color(new Color(255, 255, 255, 220).getRGB()).build());
        return difference;
    }

    private void changeValue(float difference, float sliderW) {
        BigDecimal bd = BigDecimal.valueOf((difference / sliderW) * (setting.getMax() - setting.getMin()) + setting.getMin()).setScale(2, RoundingMode.HALF_UP);
        if (dragging) {
            float value = difference == 0 ? (float) setting.getMin() : bd.floatValue();
            setting.setValue(snapValue(value));
        }
    }

    private float snapValue(float value) {
        float step = setting.isInteger() ? 1.0f : Math.max(0.01f, (float) setting.getStep());
        float snapped = Math.round(value / step) * step;
        snapped = MathHelper.clamp(snapped, (float) setting.getMin(), (float) setting.getMax());
        return setting.isInteger() ? (int) snapped : snapped;
    }
}