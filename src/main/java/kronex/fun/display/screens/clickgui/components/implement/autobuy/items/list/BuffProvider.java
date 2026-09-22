package kronex.fun.display.screens.clickgui.components.implement.autobuy.items.list;

import kronex.fun.display.screens.clickgui.components.implement.autobuy.items.AutoBuyableItem;
import kronex.fun.display.screens.clickgui.components.implement.autobuy.items.customitem.CustomItem;
import kronex.fun.display.screens.clickgui.components.implement.autobuy.items.defaultsetpricec.Defaultpricec;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.List;

public class BuffProvider {
    public static List<AutoBuyableItem> getBuffs() {
        List<AutoBuyableItem> buffs=new ArrayList<>();
        buffs.add(create("Зелье Ассасина",0x000000,Defaultpricec.getPrice("Зелье Ассасина"),List.of(Text.literal("Темная сила ассасина"))));
        buffs.add(create("Зелье Палладина",0x0000FF,Defaultpricec.getPrice("Зелье Палладина"),List.of(Text.literal("Святая сила паладина"))));
        buffs.add(create("Зелье Гнева",0xFF0000,Defaultpricec.getPrice("Зелье Гнева"),List.of(Text.literal("Ярость берсерка"))));
        buffs.add(create("Зелье Радиации",0x00FF00,Defaultpricec.getPrice("Зелье Радиации"),List.of(Text.literal("Токсичная радиация"))));
        return buffs;
    }
    private static AutoBuyableItem create(String name,int ignoredColor,int price,List<Text> lore){
        return new CustomItem(name,"[★] "+name,name,null,Items.SPLASH_POTION,price,null,lore);
    }
}
