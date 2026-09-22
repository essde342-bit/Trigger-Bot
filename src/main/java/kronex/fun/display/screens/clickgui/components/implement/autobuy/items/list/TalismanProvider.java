package kronex.fun.display.screens.clickgui.components.implement.autobuy.items.list;

import kronex.fun.display.screens.clickgui.components.implement.autobuy.items.AutoBuyableItem;
import kronex.fun.display.screens.clickgui.components.implement.autobuy.items.customitem.CustomItem;
import kronex.fun.display.screens.clickgui.components.implement.autobuy.items.defaultsetpricec.Defaultpricec;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.List;

public class TalismanProvider {
    public static List<AutoBuyableItem> getTalismans(){
        List<AutoBuyableItem> r=new ArrayList<>();
        String[] names={"Вихря","Демона","Карателя","Крушителя","Мрака","Раздора","Тирана","Ярости"};
        for(String n:russian(names)){
            String parse="Талисман "+n.toLowerCase();
            r.add(new CustomItem("[★] Талисман "+n,"[★] Талисман "+n,parse,null,Items.TOTEM_OF_UNDYING,Defaultpricec.getPrice(parse),null,List.of(Text.literal("Талисман "+n))));
        }
        return r;
    }
    private static String[] russian(String[] a){return a;}
}
