package kronex.fun.display.screens.clickgui.components.implement.other;

import net.minecraft.client.gui.DrawContext;
import kronex.fun.display.screens.clickgui.components.AbstractComponent;
import kronex.fun.other.utils.display.font.Fonts;
import kronex.fun.other.utils.display.shape.ShapeProperties;
import kronex.fun.other.utils.math.calc.Calculate;

public class ConfigManagerComponent extends AbstractComponent {
    private Runnable saveAction=()->{}; private Runnable loadAction=()->{};
    public ConfigManagerComponent(){width=150;height=32;}
    public ConfigManagerComponent setSaveAction(Runnable r){saveAction=r==null?()->{}:r;return this;}
    public ConfigManagerComponent setLoadAction(Runnable r){loadAction=r==null?()->{}:r;return this;}
    @Override public void render(DrawContext c,int mx,int my,float d){Fonts.getSize(12,Fonts.Type.BOLD).drawString(c.getMatrices(),"Config",x,y+9,0xFFFFFFFF);rectangle.render(ShapeProperties.create(c.getMatrices(),x+44,y,x+45,16).round(3).color(0xFF2A2D35).build());Fonts.getSize(10,Fonts.Type.DEFAULT).drawString(c.getMatrices(),"Save",x+51,y+9,0xFFE4E6EC);Fonts.getSize(10,Fonts.Type.DEFAULT).drawString(c.getMatrices(),"Load",x+91,y+9,0xFFE4E6EC);}
    @Override public boolean mouseClicked(double mx,double my,int button){if(button!=0)return false;if(Calculate.isHovered(mx,my,x+45,y,35,16)){saveAction.run();return true;}if(Calculate.isHovered(mx,my,x+85,y,35,16)){loadAction.run();return true;}return super.mouseClicked(mx,my,button);}
}
