package kronex.fun.display.screens.clickgui.components.implement.settings.multiselect;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import kronex.fun.features.module.setting.implement.MultiSelectSetting;
import kronex.fun.other.utils.display.other.animation.Direction;
import kronex.fun.other.utils.display.other.animation.implement.DecelerateAnimation;
import kronex.fun.other.utils.display.font.Fonts;
import kronex.fun.other.utils.display.shape.ShapeProperties;
import kronex.fun.other.utils.display.color.ColorAssist;
import kronex.fun.other.utils.math.MathUtil;
import kronex.fun.display.screens.clickgui.components.AbstractComponent;
import kronex.fun.display.screens.clickgui.components.implement.settings.select.SelectedButton;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import static kronex.fun.other.utils.display.font.Fonts.Type.BOLD;

public class MultiSelectedButton extends AbstractComponent {
    private final MultiSelectSetting setting;
    private final String text;
    @Setter @Accessors(chain = true) private float alpha;
    private final kronex.fun.other.utils.display.other.animation.Animation alphaAnimation =
            new DecelerateAnimation().setMs(300).setValue(0.5F);

    public MultiSelectedButton(MultiSelectSetting setting, String text) {
        this.setting = setting; this.text = text;
        alphaAnimation.setDirection(Direction.BACKWARDS);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrix = context.getMatrices();
        alphaAnimation.setDirection(setting.getSelected().contains(text) ? Direction.FORWARDS : Direction.BACKWARDS);
        float opacity = alphaAnimation.getOutput().floatValue();
        int selectedOpacity = ColorAssist.multAlpha(ColorAssist.getClientColor(), opacity * alpha);
        if (!alphaAnimation.isFinished(Direction.BACKWARDS)) {
            rectangle.render(ShapeProperties.create(matrix, x, y, width, height + 0.15F)
                    .round(SelectedButton.getRound(setting.getList(), text)).color(selectedOpacity).build());
        }
        Fonts.getSize(12, BOLD).drawString(matrix, text, x + 4, y + 5, ColorAssist.multAlpha(0xFFD4D6E1, alpha));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (MathUtil.isHovered(mouseX, mouseY, x, y, width, height) && button == 0) {
            List<String> selected = new ArrayList<>(setting.getSelected());
            if (selected.contains(text)) selected.remove(text);
            else { selected.add(text); selected.sort(Comparator.comparingInt(setting.getList()::indexOf)); }
            setting.setSelected(selected);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
