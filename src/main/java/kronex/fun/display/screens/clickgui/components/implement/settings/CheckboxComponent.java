package kronex.fun.display.screens.clickgui.components.implement.settings;

import net.minecraft.client.gui.DrawContext;
import kronex.fun.features.module.setting.implement.BooleanSetting;
import kronex.fun.other.utils.display.font.Fonts;
import kronex.fun.display.screens.clickgui.components.implement.other.CheckComponent;
import static kronex.fun.other.utils.display.font.Fonts.Type.BOLD;

public class CheckboxComponent extends AbstractSettingComponent {
    private final CheckComponent checkComponent = new CheckComponent();
    private final BooleanSetting setting;

    public CheckboxComponent(BooleanSetting setting) {
        super(setting);
        this.setting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        height = 18;
        float checkboxY = centerY(y, height, 8);
        float textY = centerY(y, height, Fonts.getSize(14, BOLD).getStringHeight(setting.getName()) * 0.5f, 2f);
        Fonts.getSize(14, BOLD).drawString(context.getMatrices(), setting.getName(), x + 9, textY, 0xFFD4D6E1);

        checkComponent.position(x + width - 16, checkboxY);
        checkComponent.setRunnable(() -> setting.setValue(!setting.isValue()));
        checkComponent.setState(setting.isValue());
        checkComponent.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        checkComponent.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }
}