package kronex.fun.features.module.setting.implement;

import kronex.fun.features.module.setting.Setting;
import java.util.ArrayList;
import java.util.List;

public class MultiSelectSetting extends Setting {
    private final List<String> list=new ArrayList<>();
    private final List<String> selected=new ArrayList<>();
    public MultiSelectSetting(String name,String description,List<String> values){super(name,description);list.addAll(values);}
    public List<String> getList(){return list;}
    public List<String> getSelected(){return selected;}
    public void setSelected(List<String> values){selected.clear();for(String v:values)if(list.contains(v))selected.add(v);}
}
