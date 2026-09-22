package kronex.fun.features.module.setting;

import java.util.List;

import kronex.fun.display.screens.clickgui.components.implement.settings.AbstractSettingComponent;
import kronex.fun.display.screens.clickgui.components.implement.settings.CheckboxComponent;
import kronex.fun.display.screens.clickgui.components.implement.settings.SliderComponent;
import kronex.fun.features.module.setting.implement.BooleanSetting;
import kronex.fun.features.module.setting.implement.SliderSettings;

public final class SettingComponentAdder {
    public void addSettingComponent(
            List<Setting> settings,
            List<AbstractSettingComponent> components
    ) {
        for (Setting setting : settings) {
            if (setting instanceof BooleanSetting booleanSetting) {
                components.add(new CheckboxComponent(booleanSetting));
            } else if (setting instanceof SliderSettings sliderSettings) {
                components.add(new SliderComponent(sliderSettings));
            }
        }
    }
}