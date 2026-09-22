package kronex.fun.display.screens.clickgui.components.implement.settings;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ColorHelper;
import org.lwjgl.glfw.GLFW;
import kronex.fun.features.module.setting.implement.BindSetting;
import kronex.fun.other.utils.display.font.Fonts;
import kronex.fun.other.utils.display.shape.ShapeProperties;
import kronex.fun.other.utils.math.calc.Calculate;
import java.awt.*;
import static kronex.fun.other.utils.display.font.Fonts.Type.*;

public class IconBindComponent extends AbstractSettingComponent {
    private final BindSetting setting; private boolean binding;
    public IconBindComponent(BindSetting setting){super(setting);this.setting=setting;}

    private static String getBindName(int key) {
        if (key < 0) return "NONE";
        if (key == GLFW.GLFW_MOUSE_BUTTON_LEFT) return "LMB";
        if (key == GLFW.GLFW_MOUSE_BUTTON_RIGHT) return "RMB";
        if (key == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) return "MMB";
        if (key >= GLFW.GLFW_MOUSE_BUTTON_4 && key <= GLFW.GLFW_MOUSE_BUTTON_8) return "MOUSE" + (key - GLFW.GLFW_MOUSE_BUTTON_4 + 4);
        String name = GLFW.glfwGetKeyName(key, 0);
        if (name != null && !name.isEmpty()) return name.toUpperCase();
        return "KEY_" + key;
    }

    @Override public void render(DrawContext context,int mouseX,int mouseY,float delta){
        MatrixStack matrix=context.getMatrices(); String bindName=getBindName(setting.getKey());
        String name=binding?"("+bindName+") ...":bindName; float stringWidth=Fonts.getSize(12,SEMI).getStringWidth(name)-2; height=22;
        rectangle.render(ShapeProperties.create(matrix,x+width-stringWidth-17,y+6.5f,stringWidth+10,12).round(3f)
            .outlineColor(new Color(200,200,200,255).getRGB()).color(new Color(61,67,71,80).getRGB(),new Color(71,77,81,80).getRGB(),new Color(81,87,91,80).getRGB(),new Color(91,97,101,80).getRGB()).build());
        Fonts.getSize(12,SEMI).drawString(matrix,name,x+width-12-stringWidth-1,y+12.25f,ColorHelper.getArgb(255,135,136,148));
        Fonts.getSize(14,DEFAULT).drawString(matrix,setting.getName(),x+8,y+12.25f,0xFFD4D6E1);
    }
    @Override public boolean mouseClicked(double mouseX,double mouseY,int button){
        if(button==0) binding=Calculate.isHovered(mouseX,mouseY,x,y,width,height)&&!binding;
        if(binding&&button>1){setting.setKey(button);binding=false;} return super.mouseClicked(mouseX,mouseY,button);
    }
    @Override public boolean keyPressed(int keyCode,int scanCode,int modifiers){
        if(binding){setting.setKey(keyCode==GLFW.GLFW_KEY_DELETE?-1:keyCode);binding=false;} return super.keyPressed(keyCode,scanCode,modifiers);
    }
}
