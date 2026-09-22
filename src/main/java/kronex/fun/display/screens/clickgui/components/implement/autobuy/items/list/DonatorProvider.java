package kronex.fun.display.screens.clickgui.components.implement.autobuy.items.list;

import kronex.fun.display.screens.clickgui.components.implement.autobuy.items.AutoBuyableItem;
import kronex.fun.display.screens.clickgui.components.implement.autobuy.items.customitem.CustomItem;
import kronex.fun.display.screens.clickgui.components.implement.autobuy.items.defaultsetpricec.Defaultpricec;
import net.minecraft.item.Items;
import java.util.ArrayList;
import java.util.List;

public class DonatorProvider {
    public static List<AutoBuyableItem> getDonator(){
        List<AutoBuyableItem> r=new ArrayList<>();
        r.add(new CustomItem("[★] Элитры","[★] Элитры","Элитры",null,Items.ELYTRA,Defaultpricec.getPrice("Элитры"),null,null));
        r.add(new CustomItem("[★] Тотем бессмертия","[★] Тотем бессмертия","Тотем бессмертия",null,Items.TOTEM_OF_UNDYING,Defaultpricec.getPrice("Тотем бессмертия"),null,null));
        return r;
    }
}
