package kronex.fun.display.screens.clickgui.components.implement.autobuy.items.list;

import kronex.fun.display.screens.clickgui.components.implement.autobuy.items.AutoBuyableItem;
import kronex.fun.display.screens.clickgui.components.implement.autobuy.items.customitem.CustomItem;
import kronex.fun.display.screens.clickgui.components.implement.autobuy.items.defaultsetpricec.Defaultpricec;
import net.minecraft.item.Items;
import java.util.ArrayList;
import java.util.List;

public class SphereProvider {
    public static List<AutoBuyableItem> getSpheres(){
        List<AutoBuyableItem> r=new ArrayList<>();
        String[] names={"Ареса","Бестии","Гидры","Икара","Сатира","Титана","Хаоса","Эрида"};
        for(String n:names){String d="Сфера "+n; r.add(new CustomItem("[★] "+d,"[★] "+d,d.toLowerCase(),null,Items.PLAYER_HEAD,Defaultpricec.getPrice(d)));}
        return r;
    }
}
