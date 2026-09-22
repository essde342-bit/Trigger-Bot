package kronex.fun.display.screens.clickgui.components.implement.window.implement.module;

import kronex.fun.Kronex;
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
        ModuleSettingsWindow existing=null;
        for(AbstractWindow w:windowManager.getWindows()) if(w instanceof ModuleSettingsWindow s && s.module==module){existing=s;break;}
        if(existing!=null){existing.position(x,y);return;}
        windowManager.add(new ModuleSettingsWindow(module).position(x,y).size(170,Math.max(50,Math.min(260,30+componentHeight(module)))));
    }

    private static int componentHeight(Module module){
        int h=0;for(var s:module.settings()){if(s.getVisible()!=null&&!s.getVisible().get())continue;h+=28;}return h;
    }

    @Override protected void drawWindow(DrawContext context,int mouseX,int mouseY,float delta){
        rectangle.render(ShapeProperties.create(context.getMatrices(),x,y,width,height).round(6).thickness(2).outlineColor(0xFF3A3D48).color(0xEE1D1F27).build());
        Fonts.getSize(13,Fonts.Type.BOLD).drawString(context.getMatrices(),module.getVisibleName(),x+8,y+10,0xFFF2F2F5);
        float oy=y+24;
        for(AbstractSettingComponent c:components){
            if(c.getSetting().getVisible()!=null&&!c.getSetting().getVisible().get())continue;
            c.position(x,oy).size(width,28);
            c.render(context,mouseX,mouseY,delta);
            oy+=c.height;
        }
    }

    @Override public boolean mouseClicked(double mouseX,double mouseY,int button){
        for(AbstractSettingComponent c:components)if(c.isHover(mouseX,mouseY)){c.mouseClicked(mouseX,mouseY,button);return true;}
        return super.mouseClicked(mouseX,mouseY,button);
    }
}
