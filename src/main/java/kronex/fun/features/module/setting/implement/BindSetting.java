package kronex.fun.features.module.setting.implement;

import kronex.fun.features.module.setting.Setting;

public class BindSetting extends Setting {
    private int key=-1;
    private int type;
    public BindSetting(String name,String description){super(name,description);}
    public BindSetting(String name){this(name,"");}
    public int getKey(){return key;}
    public void setKey(int key){this.key=key;}
    public int getType(){return type;}
    public void setType(int type){this.type=type;}
}
