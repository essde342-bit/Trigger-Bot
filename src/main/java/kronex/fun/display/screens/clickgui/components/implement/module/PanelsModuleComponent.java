package kronex.fun.display.screens.clickgui.components.implement.module;

import kronex.fun.Kronex;
import kronex.fun.display.screens.clickgui.components.AbstractComponent;
import kronex.fun.display.screens.clickgui.components.implement.module.ModuleSettingsWindow;
import kronex.fun.features.module.Module;
import kronex.fun.other.utils.display.color.ColorAssist;
import kronex.fun.other.utils.display.font.Fonts;
import kronex.fun.other.utils.display.shape.ShapeProperties;
import kronex.fun.other.utils.math.calc.Calculate;
import net.minecraft.client.gui.DrawContext;

public class PanelsModuleComponent extends AbstractComponent {
    private final Module module;
    private boolean expanded;
    public PanelsModuleComponent(Module module){this.module=module;this.height=22;}
    public Module getModule(){return module;}
    public float getComponentHeight(){return expanded?28:22;}

    @Override public void render(DrawContext context,int mouseX,int mouseY,float delta){
        height=getComponentHeight();
        boolean hovered=Calculate.isHovered(mouseX,mouseY,x,y,width,height);
        int base=module.isState()?ColorAssist.applyOpacity(ColorAssist.getClientColor(),120):ColorAssist.getGuiRectColor(1);
        if(hovered)base=module.isState()?ColorAssist.applyOpacity(ColorAssist.getClientColor(),155):ColorAssist.getGuiRectColor(.9f);
        rectangle.render(ShapeProperties.create(context.getMatrices(),x,y,width,20).round(3).thickness(1).outlineColor(ColorAssist.getOutline(.8f,1)).color(base).build());
        Fonts.getSize(12,Fonts.Type.BOLD).drawString(context.getMatrices(),module.getVisibleName(),x+7,y+7,0xFFE8EAF0);
        if(expanded) Fonts.getSize(10,Fonts.Type.DEFAULT).drawString(context.getMatrices(),"Settings",x+7,y+24,0xFF9EA3B4);
    }

    @Override public boolean mouseClicked(double mouseX,double mouseY,int button){
        if(!Calculate.isHovered(mouseX,mouseY,x,y,width,height))return false;
        if(button==0){module.switchState();return true;}
        if(button==1){
            expanded=!expanded;
            if(expanded){
                ModuleSettingsWindow.open(module,x+width+6,y);
            }
            return true;
        }
        return false;
    }
    @Override public boolean mouseScrolled(double mouseX,double mouseY,double amount){return isHover(mouseX,mouseY);}
}
