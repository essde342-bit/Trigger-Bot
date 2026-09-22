package kronex.fun.display.screens.clickgui.components.implement.other;

import kronex.fun.display.screens.clickgui.components.AbstractComponent;
import kronex.fun.features.impl.render.Interface;
import kronex.fun.other.utils.display.font.Fonts;
import kronex.fun.other.utils.display.shape.ShapeProperties;
import kronex.fun.other.utils.math.calc.Calculate;
import net.minecraft.client.gui.DrawContext;
import java.awt.Color;
import static net.minecraft.util.math.MathHelper.clamp;

public class ClientColorPickerComponent extends AbstractComponent {
    private boolean expanded;
    private float hue,saturation=1f,brightness=1f;
    public ClientColorPickerComponent(){sync();}
    @Override public void render(DrawContext c,int mx,int my,float d){
        sync();
        float px=x+width-16,py=y+height-16;int color=Color.HSBtoRGB(hue,saturation,brightness)|0xFF000000;
        rectangle.render(ShapeProperties.create(c.getMatrices(),px,py,16,16).round(5).color(color).build());
        Fonts.getSize(10,Fonts.Type.DEFAULT).drawCenteredString(c.getMatrices(),"C",px+8,py+6,0xFFFFFFFF);
        if(!expanded)return;
        float sx=px-82,sy=py-82;
        rectangle.render(ShapeProperties.create(c.getMatrices(),sx,sy,80,80).round(4).color(new Color(12,14,20,240).getRGB()).build());
        int[] colors={0xFF000000,0xFFFFFFFF,0xFF000000,color};
        rectangle.render(ShapeProperties.create(c.getMatrices(),sx,sy,80,80).round(3).color(colors).build());
        float cx=clamp(sx+80*saturation,sx,sx+76),cy=clamp(sy+80*(1-brightness),sy,sy+76);
        rectangle.render(ShapeProperties.create(c.getMatrices(),cx,cy,5,5).round(2.5f).thickness(2).color(0x00FFFFFF).outlineColor(0xFFFFFFFF).build());
    }
    private void sync(){int c=Interface.getInstance().colorSetting.getColor();float[] h=Color.RGBtoHSB((c>>16)&255,(c>>8)&255,c&255,null);hue=h[0];saturation=h[1];brightness=h[2];}
    public void toggleExpanded(){expanded=!expanded;}
    public boolean isExpanded(){return expanded;}
    @Override public boolean mouseClicked(double mx,double my,int button){
        if(button!=0)return false;float px=x+width-16,py=y+height-16;
        if(Calculate.isHovered(mx,my,px-2,py-2,20,20)){expanded=!expanded;return true;}
        return false;
    }
}
