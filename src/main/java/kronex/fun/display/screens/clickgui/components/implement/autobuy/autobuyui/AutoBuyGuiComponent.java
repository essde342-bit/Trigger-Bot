package kronex.fun.display.screens.clickgui.components.implement.autobuy.autobuyui;

import kronex.fun.display.screens.clickgui.components.AbstractComponent;
import kronex.fun.display.screens.clickgui.components.implement.autobuy.items.AutoBuyableItem;
import kronex.fun.display.screens.clickgui.components.implement.autobuy.manager.AutoBuyManager;
import kronex.fun.display.screens.clickgui.components.implement.autobuy.originalitems.ItemRegistry;
import kronex.fun.display.screens.clickgui.components.implement.autobuy.window.AutoBuyItemSettingsWindow;
import kronex.fun.display.screens.clickgui.components.implement.other.StatusRender;
import kronex.fun.other.utils.display.color.ColorAssist;
import kronex.fun.other.utils.display.font.Fonts;
import kronex.fun.other.utils.display.shape.ShapeProperties;
import kronex.fun.other.utils.math.calc.Calculate;
import net.minecraft.client.gui.DrawContext;
import java.util.List;

public class AutoBuyGuiComponent extends AbstractComponent {
    private final AutoBuyManager manager=AutoBuyManager.getInstance();
    private final StatusRender status=new StatusRender();
    public AutoBuyGuiComponent(){width=300;height=220;}
    @Override public void render(DrawContext c,int mx,int my,float d){
        List<AutoBuyableItem> items=ItemRegistry.getAllItems();
        rectangle.render(ShapeProperties.create(c.getMatrices(),x,y,width,height).round(6).thickness(2).outlineColor(ColorAssist.getOutline()).color(ColorAssist.getGuiRectColor(1)).build());
        Fonts.getSize(14,Fonts.Type.BOLD).drawString(c.getMatrices(),"AutoBuy",x+10,y+10,0xFFF2F2F5);
        float oy=y+30; int shown=0;
        for(AutoBuyableItem item:items){if(shown>=7)break;int bg=item.isEnabled()?ColorAssist.applyOpacity(ColorAssist.getClientColor(),100):ColorAssist.getGuiRectColor(.7f);rectangle.render(ShapeProperties.create(c.getMatrices(),x+8,oy,width-16,22).round(4).color(bg).build());Fonts.getSize(11,Fonts.Type.DEFAULT).drawString(c.getMatrices(),item.getDisplayName(),x+14,oy+8,0xFFE8EAF0);Fonts.getSize(10,Fonts.Type.DEFAULT).drawString(c.getMatrices(),"$"+item.getSettings().getBuyBelow(),x+width-60,oy+8,0xFFBFC3D0);oy+=25;shown++;}
        status.position(x+10,y+height-25).size(125,18);status.render(c,mx,my,d);
    }
    @Override public boolean mouseClicked(double mx,double my,int button){List<AutoBuyableItem> items=ItemRegistry.getAllItems();int index=(int)((my-(y+30))/25);if(index>=0&&index<items.size()&&Calculate.isHovered(mx,my,x+8,y+30,width-16,175)){AutoBuyableItem item=items.get(index);if(button==0){manager.toggleItem(item);status.setStatus(item.isEnabled()?"Enabled":"Disabled",1200);return true;}if(button==1){windowManager.add(new AutoBuyItemSettingsWindow(item).position((float)mx+4,(float)my+4).size(175,90).draggable(true));return true;}}return super.mouseClicked(mx,my,button);}
}
