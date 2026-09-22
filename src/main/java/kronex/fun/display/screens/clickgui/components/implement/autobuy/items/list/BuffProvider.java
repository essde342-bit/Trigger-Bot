package kronex.fun.display.screens.clickgui.components.implement.autobuy.items.list;

import kronex.fun.display.screens.clickgui.components.implement.autobuy.items.AutoBuyableItem;
import kronex.fun.display.screens.clickgui.components.implement.autobuy.items.customitem.CustomItem;
import kronex.fun.display.screens.clickgui.components.implement.autobuy.items.defaultsetpricec.Defaultpricec;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BuffProvider {
    public static List<AutoBuyableItem> getBuffs() {
        List<AutoBuyableItem> buffs=new ArrayList<>();
        buffs.add(createSplashPotion("Зелье Ассасина","[★] Зелье Ассасина","Зелье Ассасина",0x000000,Defaultpricec.getPrice("Зелье Ассасина"),List.of(Text.literal("Темная сила ассасина"),Text.literal("Скрывает в тенях"),Text.literal("Усиливает удар"))));
        buffs.add(createSplashPotion("Зелье Палладина","[★] Зелье Палладина","Зелье Палладина",0x0000FF,Defaultpricec.getPrice("Зелье Палладина"),List.of(Text.literal("Святая сила паладина"),Text.literal("Защищает от зла"),Text.literal("Дарует стойкость"))));
        buffs.add(createSplashPotion("Зелье Гнева","[★] Зелье Гнева","Зелье Гнева",0xFF0000,Defaultpricec.getPrice("Зелье Гнева"),List.of(Text.literal("Ярость берсерка"),Text.literal("Увеличивает урон"),Text.literal("Но ослабляет защиту"))));
        buffs.add(createSplashPotion("Зелье Радиации","[★] Зелье Радиации","Зелье Радиации",0x00FF00,Defaultpricec.getPrice("Зелье Радиации"),List.of(Text.literal("Токсичная радиация"),Text.literal("Отравляет врагов"),Text.literal("Наносит урон со временем"))));
        return buffs;
    }
    private static AutoBuyableItem createSplashPotion(String displayName,String buyName,String parseName,int color,int price,List<Text> lore){
        NbtCompound nbt=new NbtCompound(); nbt.putBoolean("HideFlags",true);
        PotionContentsComponent potionContents=new PotionContentsComponent(Optional.empty(),Optional.of(color),List.<StatusEffectInstance>of(),Optional.empty());
        return new CustomItem(displayName,buyName,parseName,nbt,Items.SPLASH_POTION,price,potionContents,lore);
    }
}
