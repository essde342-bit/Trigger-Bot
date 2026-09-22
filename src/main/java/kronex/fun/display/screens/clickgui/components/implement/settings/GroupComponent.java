package kronex.fun.display.screens.clickgui.components.implement.settings;

import net.minecraft.client.gui.DrawContext;
import kronex.fun.features.module.setting.implement.GroupSetting;
import kronex.fun.other.utils.display.font.Fonts;
import kronex.fun.display.screens.clickgui.components.implement.other.CheckComponent;
import kronex.fun.display.screens.clickgui.components.implement.other.SettingComponent;
import kronex.fun.display.screens.clickgui.components.implement.window.AbstractWindow;
import kronex.fun.display.screens.clickgui.components.implement.window.implement.settings.group.GroupWindow;

import static kronex.fun.other.utils.display.font.Fonts.Type.BOLD;

public class GroupComponent extends AbstractSettingComponent {
    private final CheckComponent checkComponent = new CheckComponent();
    private final SettingComponent settingComponent = new SettingComponent();
    private final GroupSetting setting;
    public GroupComponent(GroupSetting setting) { super(setting); this.setting = setting; }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        String wrapped = setting.getDescription();
        height = (int) (18 + Fonts.getSize(12, Fonts.Type.DEFAULT).getStringHeight(wrapped) / 3);
        float checkboxY = centerY(y, 18, 8);
        float textY = centerY(y, 18, Fonts.getSize(14, BOLD).getStringHeight(setting.getName()) * 0.5f, 2f);
        Fonts.getSize(14, BOLD).drawString(context.getMatrices(), setting.getName(), x + 9, textY, 0xFFD4D6E1);
        Fonts.getSize(12, Fonts.Type.DEFAULT).drawString(context.getMatrices(), wrapped, x + 9, y + 15, 0xFF878894);
        ((CheckComponent) checkComponent.position(x + width - 16, checkboxY)).setRunnable(() -> setting.setValue(!setting.isValue())).setState(setting.isValue()).render(context, mouseX, mouseY, delta);
        ((SettingComponent) settingComponent.position(x + width - 28, y + 8)).setRunnable(() -> spawnWindow(mouseX, mouseY)).render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        checkComponent.mouseClicked(mouseX, mouseY, button);
        settingComponent.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void spawnWindow(int mouseX, int mouseY) {
        AbstractWindow existingWindow = null;
        for (AbstractWindow window : windowManager.getWindows()) if (window instanceof GroupWindow && ((GroupWindow) window).getSetting() == setting) { existingWindow = window; break; }
        if (existingWindow != null) windowManager.delete(existingWindow);
        else windowManager.add(new GroupWindow(setting).position(mouseX + 5, mouseY + 5).size(137, 23).draggable(false));
    }
}