package kronex.fun.display.screens.clickgui.components.implement.autobuy.items;

import kronex.fun.display.screens.clickgui.components.implement.autobuy.settings.AutoBuyItemSettings;
import net.minecraft.item.ItemStack;

public interface AutoBuyableItem {
    String getDisplayName();
    String getBuyName(); // Название в аукционе (например "[★] Сфера Хаоса")
    String getParseName(); // Название для парсинга (например "Сфера Хаоса")
    ItemStack createItemStack();
    int getPrice();
    boolean isEnabled();
    void setEnabled(boolean enabled);
    AutoBuyItemSettings getSettings();
}


