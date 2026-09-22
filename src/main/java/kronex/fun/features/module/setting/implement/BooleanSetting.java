package kronex.fun.features.module.setting.implement;

import kronex.fun.features.module.setting.Setting;

public final class BooleanSetting extends Setting {
    private final com.essde342.triggerbot.ui.imple.BooleanSetting backend;
    private int key=-1;
    private int type;

    public BooleanSetting(String name,com.essde342.triggerbot.ui.imple.BooleanSetting backend){super(name,"");this.backend=backend;}
    public BooleanSetting(String name,String description,com.essde342.triggerbot.ui.imple.BooleanSetting backend){super(name,description);this.backend=backend;}
    public boolean isValue(){return backend.isEnabled();}
    public void setValue(boolean value){backend.setEnabled(value);}
    public int getKey(){return key;}
    public void setKey(int key){this.key=key;}
    public int getType(){return type;}
    public void setType(int type){this.type=type;}
}
