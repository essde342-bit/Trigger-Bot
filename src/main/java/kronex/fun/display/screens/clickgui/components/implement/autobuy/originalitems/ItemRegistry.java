package kronex.fun.display.screens.clickgui.components.implement.autobuy.originalitems;

import kronex.fun.display.screens.clickgui.components.implement.autobuy.items.AutoBuyableItem;
import kronex.fun.display.screens.clickgui.components.implement.autobuy.items.list.*;
import kronex.fun.display.screens.clickgui.components.implement.autobuy.settings.AutoBuySettingsManager;
import kronex.fun.display.screens.clickgui.components.implement.autobuy.util.krushprovider.KrushProvider;
import java.util.ArrayList;
import java.util.List;

public class ItemRegistry {
    private static List<AutoBuyableItem> allItems=null;
    private static boolean settingsLoaded=false;
    public static void ensureSettingsLoaded(){if(!settingsLoaded)settingsLoaded=true;}
    public static List<AutoBuyableItem> getAllItems(){if(allItems==null){allItems=new ArrayList<>();allItems.addAll(getKrush());allItems.addAll(getTalismans());allItems.addAll(getSpheres());allItems.addAll(getMisc());allItems.addAll(getDonator());allItems.addAll(getPotions());allItems.addAll(getBuffs());for(AutoBuyableItem item:allItems)AutoBuySettingsManager.getInstance().loadSettings(item.getDisplayName(),item.getSettings());}return allItems;}
    public static void reloadSettings(){if(allItems!=null)for(AutoBuyableItem item:allItems)AutoBuySettingsManager.getInstance().loadSettings(item.getDisplayName(),item.getSettings());}
    public static List<AutoBuyableItem> getKrush(){return KrushProvider.getKrush();}
    public static List<AutoBuyableItem> getTalismans(){return TalismanProvider.getTalismans();}
    public static List<AutoBuyableItem> getSpheres(){return SphereProvider.getSpheres();}
    public static List<AutoBuyableItem> getMisc(){return MiscProvider.getMisc();}
    public static List<AutoBuyableItem> getDonator(){return DonatorProvider.getDonator();}
    public static List<AutoBuyableItem> getPotions(){return PotionProvider.getPotions();}
    public static List<AutoBuyableItem> getBuffs(){return BuffProvider.getBuffs();}
}
