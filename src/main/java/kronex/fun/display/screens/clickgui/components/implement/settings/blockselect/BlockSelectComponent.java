package kronex.fun.display.screens.clickgui.components.implement.settings.blockselect;

import kronex.fun.display.screens.clickgui.components.implement.settings.AbstractSettingComponent;
import kronex.fun.features.module.setting.implement.BlockSelectSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import java.util.List;

public class BlockSelectComponent extends AbstractSettingComponent {
    private final BlockSelectSetting setting;
    private int selectedSlot = -1;
    public BlockSelectComponent(BlockSelectSetting setting){super(setting);this.setting=setting;}
    @Override public void render(DrawContext context,int mouseX,int mouseY,float delta){
        MatrixStack matrix=context.getMatrices();
        String selected=setting.getSelectedBlockName();
        Fonts.getSize(14,Fonts.Type.BOLD).drawString(matrix,setting.getName(),x+9,y+6,0xFFD4D6E1);
        Fonts.getSize(12,Fonts.Type.DEFAULT).drawString(matrix,setting.getDescription(),x+9,y+15,0xFF878894);
        rectangle.render(ShapeProperties.create(matrix,x+width-72,y+4,64,14).round(2).thickness(2).outlineColor(ColorAssist.getOutline()).color(ColorAssist.getGuiRectColor(1)).build());
        Fonts.getSize(12,Fonts.Type.DEFAULT).drawString(matrix,selected==null?"None":selected,x+width-68,y+10,0xFFD4D6E1);
        height=22;
    }
    @Override public boolean mouseClicked(double mouseX,double mouseY,int button){if(button==0&&MathUtil.isHovered(mouseX,mouseY,x+width-72,y+4,64,14)){setting.openSelector();return true;}return super.mouseClicked(mouseX,mouseY,button);}
}
