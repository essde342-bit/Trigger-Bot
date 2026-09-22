package kronex.fun.display.screens.clickgui.components.implement.module;

import kronex.fun.display.screens.clickgui.components.implement.settings.AbstractSettingComponent;
import kronex.fun.display.screens.clickgui.components.implement.window.AbstractWindow;
import kronex.fun.features.module.Module;
import kronex.fun.features.module.setting.SettingComponentAdder;
import kronex.fun.other.utils.display.font.Fonts;
import kronex.fun.other.utils.display.shape.ShapeProperties;
import net.minecraft.client.gui.DrawContext;
import java.util.ArrayList;
import java.util.List;

public class ModuleSettingsWindow extends AbstractWindow {
    private final Module module;
    private final List<AbstractSettingComponent> components=new ArrayList<>();
    private ModuleSettingsWindow(Module module){this.module=module;new SettingComponentAdder().addSettingComponent(module.settings(),components);}
    public static void open(Module module,float x,float y){
        for(AbstractWindow w:windowManager.getWindows())if(w instanceof ModuleSettingsWindow s&&s.module==module){s.position(x,y);return;}
        windowManager.add(new ModuleSettingsWindow(module).position(x,y).size(170,Math.max(54,30+module.settings().size()*28)).draggable(true));
    }
    @Override protected void drawWindow(DrawContext c,int mx,int my,float d){
        rectangle.render(ShapeProperties.create(c.getMatrices(),x,y,width,height).round(6).thickness(2).outlineColor(0xFF3A3D48).color(0xEE1D1F27).build());
        Fonts.getSize(13,Fonts.Type.BOLD).drawString(c.getMatrices(),module.getVisibleName(),x+8,y+10,0xFFF2F2F5);
        float oy=y+24;for(AbstractSettingComponent comp:components){if(comp.getSetting().getVisible()!=null&&!comp.getSetting().getVisible().get())continue;comp.position(x,oy).size(width,28);comp.render(c,mx,my,d);oy+=Math.max(20,comp.height);}
    }
    @Override public boolean mouseClicked(double mx,double my,int b){for(AbstractSettingComponent c:components)if(c.isHover(mx,my)){c.mouseClicked(mx,my,b);return true;}return super.mouseClicked(mx,my,b);}
}
