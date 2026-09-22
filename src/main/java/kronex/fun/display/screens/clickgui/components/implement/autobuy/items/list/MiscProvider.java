package kronex.fun.display.screens.clickgui.components.implement.autobuy.items.list;

import kronex.fun.display.screens.clickgui.components.implement.autobuy.items.AutoBuyableItem;
import kronex.fun.display.screens.clickgui.components.implement.autobuy.items.customitem.CustomItem;
import kronex.fun.display.screens.clickgui.components.implement.autobuy.items.defaultsetpricec.Defaultpricec;
import net.minecraft.item.Items;
import java.util.ArrayList;
import java.util.List;

public class MiscProvider {
    public static List<AutoBuyableItem> getMisc(){
        List<AutoBuyableItem> r=new ArrayList<>();
        String[][] values={{"Алмаз","Алмаз","Алмаз"},{"Золотое яблоко","Золотое яблоко","Золотое яблоко"},{"Незеритовый слиток","Незеритовый слиток","Незеритовый слиток"},{"Тотем бессмертия","Тотем бессмертия","Тотем бессмертия"}};
        for(String[] v:values)r.add(new CustomItem(v[0],v[1],v[2],null,switch(v[0]){case "Алмаз"->Items.DIAMOND;case "Золотое яблоко"->Items.ENCHANTED_GOLDEN_APPLE;case "Незеритовый слиток"->Items.NETHERITE_INGOT;default->Items.TOTEM_OF_UNDYING;},Defaultpricec.getPrice(v[2])));
        return r;
    }
}
