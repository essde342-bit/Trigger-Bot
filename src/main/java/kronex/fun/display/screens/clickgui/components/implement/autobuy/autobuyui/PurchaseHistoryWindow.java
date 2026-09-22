package kronex.fun.display.screens.clickgui.components.implement.autobuy.autobuyui;

import kronex.fun.display.screens.clickgui.components.implement.autobuy.items.AutoBuyableItem;
import kronex.fun.other.utils.display.font.Fonts;
import kronex.fun.other.utils.display.shape.ShapeProperties;
import net.minecraft.client.gui.DrawContext;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PurchaseHistoryWindow {
    private static final List<String> purchases=new CopyOnWriteArrayList<>();
    public static void addPurchase(AutoBuyableItem item,int price){addPurchase(item.getDisplayName(),price);}
    public static void addPurchase(String itemName,int price){purchases.remove(itemName);purchases.add(0,itemName+"  $"+price);if(purchases.size()>50)purchases.remove(purchases.size()-1);}
    public void render(DrawContext c,int mx,int my,float d,int width,int height,int backgroundWidth,int backgroundHeight){
        float px=(width-backgroundWidth)/2f-185,py=(height-backgroundHeight)/2f;
        rectangle(c,px,py,180,backgroundHeight);
        Fonts.getSize(14,Fonts.Type.SEMI).drawString(c.getMatrices(),"История покупок",px+10,py+10,0xFFFFFFFF);
        float oy=py+30; for(String p:purchases){Fonts.getSize(11,Fonts.Type.DEFAULT).drawString(c.getMatrices(),p,px+8,oy,0xFFE1E2E7);oy+=16;if(oy>py+backgroundHeight-14)break;}
    }
    private static void rectangle(DrawContext c,float x,float y,float w,float h){kronex.fun.other.utils.display.interfaces.QuickImports.rectangle.render(ShapeProperties.create(c.getMatrices(),x,y,w,h).round(6).thickness(2).outlineColor(0xFF36383F).color(0xC80C0C0C).build());}
    public boolean mouseScrolled(double mx,double my,double amount,int width,int height,int backgroundWidth,int backgroundHeight){return false;}
    public static void clear(){purchases.clear();}
}
