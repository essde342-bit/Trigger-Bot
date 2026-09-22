package kronex.fun.features.module.setting;

import java.util.List;
import kronex.fun.display.screens.clickgui.components.implement.settings.AbstractSettingComponent;
import kronex.fun.display.screens.clickgui.components.implement.settings.BindComponent;
import kronex.fun.display.screens.clickgui.components.implement.settings.CheckboxComponent;
import kronex.fun.display.screens.clickgui.components.implement.settings.ColorComponent;
import kronex.fun.display.screens.clickgui.components.implement.settings.GroupComponent;
import kronex.fun.display.screens.clickgui.components.implement.settings.SButtonComponent;
import kronex.fun.display.screens.clickgui.components.implement.settings.SliderComponent;
import kronex.fun.display.screens.clickgui.components.implement.settings.TextComponent;
import kronex.fun.display.screens.clickgui.components.implement.settings.ValueComponent;
import kronex.fun.display.screens.clickgui.components.implement.settings.blockselect.BlockSelectComponent;
import kronex.fun.display.screens.clickgui.components.implement.settings.multiselect.MultiSelectComponent;
import kronex.fun.display.screens.clickgui.components.implement.settings.select.SelectComponent;
import kronex.fun.features.module.setting.implement.*;

public final class SettingComponentAdder {
    public void addSettingComponent(List<Setting> settings,List<AbstractSettingComponent> components) {
        for(Setting setting:settings){
            if(setting instanceof BooleanSetting s) components.add(new CheckboxComponent(s));
            else if(setting instanceof SliderSettings s) components.add(new SliderComponent(s));
            else if(setting instanceof ColorSetting s) components.add(new ColorComponent(s));
            else if(setting instanceof BindSetting s) components.add(new BindComponent(s));
            else if(setting instanceof GroupSetting s) components.add(new GroupComponent(s));
            else if(setting instanceof ButtonSetting s) components.add(new SButtonComponent(s));
            else if(setting instanceof SelectSetting s) components.add(new SelectComponent(s));
            else if(setting instanceof MultiSelectSetting s) components.add(new MultiSelectComponent(s));
            else if(setting instanceof BlockSelectSetting s) components.add(new BlockSelectComponent(s));
            else if(setting instanceof TextSetting s) components.add(new TextComponent(s));
        }
    }
}
