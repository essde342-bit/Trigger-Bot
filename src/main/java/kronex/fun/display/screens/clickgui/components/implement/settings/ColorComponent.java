package kronex.fun.display.screens.clickgui.components.implement.settings;

import net.minecraft.client.gui.DrawContext;
import kronex.fun.features.module.setting.implement.ColorSetting;
import kronex.fun.other.utils.display.font.Fonts;
import kronex.fun.other.utils.display.shape.ShapeProperties;
import kronex.fun.other.utils.display.color.ColorAssist;
import kronex.fun.other.utils.math.MathUtil;
import kronex.fun.display.screens.clickgui.components.implement.window.AbstractWindow;
import kronex.fun.display.screens.clickgui.components.implement.window.implement.settings.color.ColorWindow;

import static kronex.fun.other.utils.display.font.Fonts.Type.*;

public class ColorComponent extends AbstractSettingComponent {
    private final ColorSetting setting;
    public ColorComponent(ColorSetting setting) { super(setting); this.setting = setting; }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        String wrapped = setting.getDescription();
        height = (int) (18 + Fonts.getSize(12, Fonts.Type.DEFAULT).getStringHeight(wrapped) / 3);
        Fonts.getSize(14, BOLD).drawString(context.getMatrices(), setting.getName(), x + 9, y + 6, 0xFFD4D6E1);
        Fonts.getSize(12, Fonts.Type.DEFAULT).drawString(context.getMatrices(), wrapped, x + 9, y + 15, 0xFF878894);
        rectangle.render(ShapeProperties.create(context.getMatrices(), x + width - 14, y + 7, 7, 7).round(3.5F).color(setting.getColor()).build());
        rectangle.render(ShapeProperties.create(context.getMatrices(), x + width - 14, y + 7, 7, 7).round(3.5F).thickness(2).softness(1).outlineColor(ColorAssist.getText()).color(0x0FFFFFF).build());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (MathUtil.isHovered(mouseX, mouseY, x + width - 15, y + 6.7F, 7, 7) && button == 0) {
            AbstractWindow existingWindow = null;
            for (AbstractWindow window : windowManager.getWindows()) if (window instanceof ColorWindow) { existingWindow = window; break; }
            if (existingWindow != null) windowManager.delete(existingWindow);
            else windowManager.add(new ColorWindow(setting).position((int) (mouseX + 185), (int) (mouseY - 82)).size(150, 165).draggable(true));
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}