package kronex.fun.display.screens.clickgui.components.implement.settings.multiselect;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import kronex.fun.other.utils.display.other.animation.Direction;
import kronex.fun.other.utils.display.other.animation.implement.DecelerateAnimation;
import kronex.fun.other.utils.display.font.FontRenderer;
import kronex.fun.other.utils.display.color.ColorAssist;
import kronex.fun.other.utils.display.scissor.ScissorAssist;
import kronex.fun.features.module.setting.implement.MultiSelectSetting;
import kronex.fun.other.utils.display.font.Fonts;
import kronex.fun.other.utils.display.shape.ShapeProperties;
import kronex.fun.other.utils.math.MathUtil;
import kronex.fun.Kronex;
import kronex.fun.display.screens.clickgui.components.implement.settings.AbstractSettingComponent;
import java.util.ArrayList;
import java.util.List;
import static kronex.fun.other.utils.display.font.Fonts.Type.BOLD;

public class MultiSelectComponent extends AbstractSettingComponent {
    private final List<MultiSelectedButton> multiSelectedButtons = new ArrayList<>();
    private final MultiSelectSetting setting;
    private boolean open;
    private float dropdownListX, dropDownListY, dropDownListWidth, dropDownListHeight;
    private final kronex.fun.other.utils.display.other.animation.Animation alphaAnimation =
            new DecelerateAnimation().setMs(300).setValue(1);

    public MultiSelectComponent(MultiSelectSetting setting) {
        super(setting); this.setting = setting;
        alphaAnimation.setDirection(Direction.BACKWARDS);
        for (String s : setting.getList()) multiSelectedButtons.add(new MultiSelectedButton(setting, s));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrices = context.getMatrices();
        String wrapped = setting.getDescription();
        height = (int) (18 + Fonts.getSize(12, Fonts.Type.DEFAULT).getStringHeight(wrapped) / 3);
        this.dropdownListX = x + width - 75;
        this.dropDownListY = y + 20;
        this.dropDownListWidth = 66;
        this.dropDownListHeight = setting.getList().size() * 12;
        alphaAnimation.setDirection(open ? Direction.FORWARDS : Direction.BACKWARDS);
        renderSelected(matrices);
        Fonts.getSize(14, BOLD).drawString(matrices, setting.getName(), x + 9, y + 6, 0xFFD4D6E1);
        Fonts.getSize(12, Fonts.Type.DEFAULT).drawString(matrices, wrapped, x + 9, y + 15, 0xFF878894);
    }

    @Override
    public void renderOverlay(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!alphaAnimation.isFinished(Direction.BACKWARDS)) renderSelectList(context, mouseX, mouseY, delta);
    }

    public boolean isOpen() { return open; }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (MathUtil.isHovered(mouseX, mouseY, x + width - 75, y + 4, 66, 14)) open = !open;
            else if (open && !isHoveredList(mouseX, mouseY)) open = false;
            if (open) multiSelectedButtons.forEach(b -> b.mouseClicked(mouseX, mouseY, button));
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isHover(double mouseX, double mouseY) { return open && isHoveredList(mouseX, mouseY); }

    private void renderSelected(MatrixStack matrix) {
        FontRenderer font = Fonts.getSize(12, Fonts.Type.DEFAULT);
        int x1 = (int) (x + width - 72);
        rectangle.render(ShapeProperties.create(matrix, x1 - 3, y + 4, 66, 14)
                .round(2).thickness(2).softness(0.5F).outlineColor(ColorAssist.getOutline()).color(ColorAssist.getGuiRectColor(0.5F)).build());
        String selectedName = String.join(", ", setting.getSelected());
        ScissorAssist scissor = Kronex.getInstance().getScissorManager();
        scissor.push(matrix.peek().getPositionMatrix(), x1 - 2, (float) window.getScaledHeight() / 2 - 96, 64, 220);
        font.drawStringWithScroll(matrix, selectedName, x1, y + 10, 64, ColorAssist.getText());
        scissor.pop();
    }

    private void renderSelectList(DrawContext context, int mouseX, int mouseY, float delta) {
        float opacity = alphaAnimation.getOutput().floatValue();
        rectangle.render(ShapeProperties.create(context.getMatrices(), dropdownListX, dropDownListY, dropDownListWidth, dropDownListHeight)
                .round(4).thickness(2).outlineColor(ColorAssist.getOutline(opacity, 1)).color(ColorAssist.getGuiRectColor(opacity)).build());
        float offset = dropDownListY;
        for (MultiSelectedButton button : multiSelectedButtons) {
            button.x = dropdownListX; button.y = offset; button.width = dropDownListWidth; button.height = 12;
            button.setAlpha(opacity); button.render(context, mouseX, mouseY, delta); offset += 12;
        }
    }

    private boolean isHoveredList(double mouseX, double mouseY) {
        return MathUtil.isHovered(mouseX, mouseY, dropdownListX, dropDownListY - 16, dropDownListWidth, dropDownListHeight + 16);
    }
}
