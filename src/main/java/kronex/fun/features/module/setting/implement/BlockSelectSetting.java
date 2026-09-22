package kronex.fun.features.module.setting.implement;

import kronex.fun.features.module.setting.Setting;

public class BlockSelectSetting extends Setting {
    private String selectedBlockName="None";
    private Runnable selector;
    public BlockSelectSetting(String name,String description){super(name,description);}
    public String getSelectedBlockName(){return selectedBlockName;}
    public void setSelectedBlockName(String name){selectedBlockName=name==null?"None":name;}
    public void setSelector(Runnable selector){this.selector=selector;}
    public void openSelector(){if(selector!=null)selector.run();}
}
