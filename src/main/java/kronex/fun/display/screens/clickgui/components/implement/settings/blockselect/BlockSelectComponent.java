package kronex.fun.display.screens.clickgui.components.implement.settings.blockselect;

import kronex.fun.display.screens.clickgui.components.implement.settings.AbstractSettingComponent;
import kronex.fun.features.module.setting.implement.BlockSelectSetting;
import kronex.fun.other.utils.display.font.Fonts;
import kronex.fun.other.utils.display.shape.ShapeProperties;
import kronex.fun.other.utils.display.color.ColorAssist;
import kronex.fun.other.utils.math.MathUtil;
import net.minecraft.client.gui.DrawContext;

public class BlockSelectComponent extends AbstractSettingComponent {
    private final BlockSelectSetting setting;
    public BlockSelectComponent(BlockSelectSetting setting){super(setting);this.setting=setting;}
    @Override public void render(DrawContext context,int mouseX,int mouseY,float delta){
        height=22;
        Fonts.getSize(14,Fonts.Type.BOLD).drawString(context.getMatrices(),setting.getName(),x+9,y+6,0xFFD4D6E1);
        Fonts.getSize(12,Fonts.Type.DEFAULT).drawString(context.getMatrices(),setting.getDescription(),x+9,y+15,0xFF878894);
        rectangle.render(ShapeProperties.create(context.getMatrices(),x+width-72,y+4,64,14).round(2).thickness(2)
                .outlineColor(ColorAssist.getOutline()).color(ColorAssist.getGuiRectColor(1)).build());
        String selected=setting.getSelectedBlockName();
        Fonts.getSize(12,Fonts.Type.DEFAULT).drawString(context.getMatrices(),selected==null?"None":selected,x+width-68,y+10,0xFFD4D6E1);
    }
    @Override public boolean mouseClicked(double mouseX,double mouseY,int button){
        if(button==0&&MathUtil.isHovered(mouseX,mouseY,x+width-72,y+4,64,14)){setting.openSelector();return true;}
        return super.mouseClicked(mouseX,mouseY,button);
    }
}
