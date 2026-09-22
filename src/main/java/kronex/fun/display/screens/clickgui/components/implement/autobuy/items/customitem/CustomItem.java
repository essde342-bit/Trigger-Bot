package kronex.fun.display.screens.clickgui.components.implement.autobuy.items.customitem;

import kronex.fun.display.screens.clickgui.components.implement.autobuy.items.AutoBuyableItem;
import kronex.fun.display.screens.clickgui.components.implement.autobuy.settings.AutoBuyItemSettings;
import kronex.fun.display.screens.clickgui.components.implement.autobuy.settings.AutoBuySettingsManager;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import java.util.List;

public class CustomItem implements AutoBuyableItem {
    private final String displayName,buyName,parseName;
    private final NbtCompound nbt;
    private final Item material;
    private final int price;
    private final PotionContentsComponent potionContents;
    private final List<Text> loreTexts;
    private final AutoBuyItemSettings settings;
    private boolean enabled=true;

    public CustomItem(String displayName,String buyName,String parseName,NbtCompound nbt,Item material,int price,PotionContentsComponent potionContents,List<Text> loreTexts){
        this.displayName=displayName;this.buyName=buyName;this.parseName=parseName;this.nbt=nbt;this.material=material;this.price=price;this.potionContents=potionContents;this.loreTexts=loreTexts;
        this.settings=new AutoBuyItemSettings(price,material,displayName);
        AutoBuySettingsManager.getInstance().loadSettings(displayName,settings);
    }
    public CustomItem(String displayName,NbtCompound nbt,Item material,int price,PotionContentsComponent potionContents,List<Text> loreTexts){
        this(displayName,displayName,displayName.replaceAll("^\\[.+?\\]\\s*",""),nbt,material,price,potionContents,loreTexts);
    }
    public CustomItem(String displayName,NbtCompound nbt,Item material,int price){this(displayName,nbt,material,price,null,null);}
    @Override public String getDisplayName(){return displayName;}
    @Override public String getBuyName(){return buyName;}
    @Override public String getParseName(){return parseName;}
    @Override public ItemStack createItemStack(){return new ItemStack(material);}
    @Override public int getPrice(){return price;}
    @Override public boolean isEnabled(){return enabled;}
    @Override public void setEnabled(boolean enabled){this.enabled=enabled;}
    @Override public AutoBuyItemSettings getSettings(){return settings;}
    public NbtCompound getNbt(){return nbt;}
    public PotionContentsComponent getPotionContents(){return potionContents;}
    public List<Text> getLoreTexts(){return loreTexts;}
}
