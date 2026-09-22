package kronex.fun.features.module.setting.implement;

import kronex.fun.features.module.setting.Setting;
import java.util.ArrayList;
import java.util.List;

public class GroupSetting extends Setting {
    private boolean value;
    private final List<Setting> subSettings=new ArrayList<>();
    public GroupSetting(String name,String description){super(name,description);}
    public boolean isValue(){return value;}
    public void setValue(boolean value){this.value=value;}
    public List<Setting> getSubSettings(){return subSettings;}
    public GroupSetting add(Setting setting){subSettings.add(setting);return this;}
}
