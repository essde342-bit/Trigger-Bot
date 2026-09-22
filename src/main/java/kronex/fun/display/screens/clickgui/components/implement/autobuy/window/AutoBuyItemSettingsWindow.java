package kronex.fun.display.screens.clickgui.components.implement.autobuy.window;

import kronex.fun.display.screens.clickgui.components.implement.autobuy.items.AutoBuyableItem;
import kronex.fun.display.screens.clickgui.components.implement.autobuy.settings.AutoBuyItemSettings;
import kronex.fun.display.screens.clickgui.components.implement.autobuy.settings.AutoBuySettingsComponent;
import kronex.fun.display.screens.clickgui.components.implement.window.AbstractWindow;
import kronex.fun.other.utils.display.font.Fonts;
import kronex.fun.other.utils.display.shape.ShapeProperties;
import net.minecraft.client.gui.DrawContext;

public class AutoBuyItemSettingsWindow extends AbstractWindow {
    private final AutoBuyableItem item;
    private final AutoBuySettingsComponent.BuyBelowComponent buy;
    private final AutoBuySettingsComponent.MinQuantityComponent qty;
    public AutoBuyItemSettingsWindow(AutoBuyableItem item){this.item=item;AutoBuyItemSettings s=item.getSettings();buy=new AutoBuySettingsComponent.BuyBelowComponent(s);qty=new AutoBuySettingsComponent.MinQuantityComponent(s);}
    @Override protected void drawWindow(DrawContext c,int mx,int my,float d){
        height=90;
        rectangle.render(ShapeProperties.create(c.getMatrices(),x,y,width,height).round(6).thickness(2).outlineColor(0xFF3A3D48).color(0xEE1D1F27).build());
        Fonts.getSize(13,Fonts.Type.BOLD).drawString(c.getMatrices(),item.getDisplayName(),x+8,y+10,0xFFF2F2F5);
        buy.position(x+4,y+22).size(width-8,18);qty.position(x+4,y+42).size(width-8,18);
        buy.render(c,mx,my,d);qty.render(c,mx,my,d);
    }
    @Override public boolean mouseClicked(double mx,double my,int b){if(buy.mouseClicked(mx,my,b))return true;if(qty.mouseClicked(mx,my,b))return true;return super.mouseClicked(mx,my,b);}
    @Override public boolean keyPressed(int k,int s,int m){if(buy.keyPressed(k,s,m)||qty.keyPressed(k,s,m))return true;return super.keyPressed(k,s,m);}
    @Override public boolean charTyped(char c,int m){if(buy.charTyped(c,m)||qty.charTyped(c,m))return true;return super.charTyped(c,m);}
}
