package kronex.fun.features.module.setting.implement;

import kronex.fun.features.module.setting.Setting;

public class ButtonSetting extends Setting {
    private final Runnable runnable;
    public ButtonSetting(String name,String description,Runnable runnable){super(name,description);this.runnable=runnable;}
    public Runnable getRunnable(){return runnable;}
}
