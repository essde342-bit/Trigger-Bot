package kronex.fun.display.screens.clickgui.components.implement.autobuy.items.defaultsetpricec;

import java.util.HashMap;
import java.util.Map;

public final class Defaultpricec {
    private static final Map<String,Integer> PRICES=new HashMap<>();
    static{
        PRICES.put("Алмаз",1500); PRICES.put("Золотое яблоко",1000); PRICES.put("Тотем бессмертия",1000);
        PRICES.put("Элитры",100000); PRICES.put("Незеритовый слиток",50000);
        PRICES.put("Сфера Ареса",3000000); PRICES.put("Сфера Бестии",3000000); PRICES.put("Сфера Гидры",3000000);
        PRICES.put("Сфера Икара",3000000); PRICES.put("Сфера Сатира",3000000); PRICES.put("Сфера Титана",3000000);
        PRICES.put("Сфера Хаоса",3000000); PRICES.put("Сфера Эрида",3000000);
    }
    private Defaultpricec(){}
    public static int getPrice(String displayName){return PRICES.getOrDefault(displayName,5000);}
}
