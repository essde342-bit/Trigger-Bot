package com.essde342.triggerbot.ui;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import com.essde342.triggerbot.TriggerBotClient;
import com.essde342.triggerbot.ui.imple.BooleanSetting;
import com.essde342.triggerbot.ui.imple.MultiSetting;
import com.essde342.triggerbot.ui.imple.NumberSetting;
import com.essde342.triggerbot.ui.modules.Category;
import com.essde342.triggerbot.ui.modules.Module;
import com.essde342.triggerbot.ui.modules.ModuleManager;
import com.essde342.triggerbot.ui.ISetting;
import com.essde342.triggerbot.ui.util.KeyboardUtils;

public class ClickGuiMain extends Screen {
    private static final int CATEGORY_HEIGHT=18,CATEGORY_WIDTH=120,CATEGORY_SPACING=20,PANEL_HEIGHT=200;
    private static final float ANIMATION_DURATION=250.0F;
    private static int categoryStartX,categoryStartY;
    private final Map<Category,Integer> scrollOffsets=new HashMap<>();
    private final MinecraftClient mc=MinecraftClient.getInstance();
    private final Color darkBlue=new Color(5,10,25,240),mediumBlue=new Color(10,20,45,230);
    private final Color brightBlue=new Color(30,80,200,255),glowColor=new Color(50,100,255,120);
    private final Color activeModuleColor=new Color(19,21,83,215),inactiveModuleColor=new Color(5,15,30,240);
    private Module selectedModuleForSettings,previousSelectedModule,waitingForBind;
    private NumberSetting draggingSlider;
    private float settingsAnimationProgress;
    private long lastSettingsToggleTime,lastKeyboardLayoutCheckTime;
    private boolean isAltPressed;

    public ClickGuiMain(){
        super(Text.of("ClickGui"));
        for(Category c:Category.values())scrollOffsets.put(c,0);
        ModuleManager.moduleRegister();
        KeyboardUtils.updateKeyboardLayout();
    }
    private int calculateCenterX(){
        int n=Category.values().length;
        return (width-(n*CATEGORY_WIDTH+(n-1)*CATEGORY_SPACING))/2;
    }
    private int calculateCenterY(){return (height-(PANEL_HEIGHT+CATEGORY_HEIGHT))/2;}
    @Override public boolean isPauseScreen(){return false;}

    @Override public void render(MatrixStack matrices,int mouseX,int mouseY,float delta){
        isAltPressed=InputUtil.isKeyPressed(mc.getWindow().getHandle(),GLFW.GLFW_KEY_LEFT_ALT)
                ||InputUtil.isKeyPressed(mc.getWindow().getHandle(),GLFW.GLFW_KEY_RIGHT_ALT);
        if(System.currentTimeMillis()-lastKeyboardLayoutCheckTime>1000){
            KeyboardUtils.updateKeyboardLayout();lastKeyboardLayoutCheckTime=System.currentTimeMillis();
        }
        categoryStartX=calculateCenterX();categoryStartY=calculateCenterY();
        if(selectedModuleForSettings!=null)
            settingsAnimationProgress=Math.min(1f,(System.currentTimeMillis()-lastSettingsToggleTime)/ANIMATION_DURATION);
        else if(settingsAnimationProgress>0){
            settingsAnimationProgress=Math.max(0f,1f-(System.currentTimeMillis()-lastSettingsToggleTime)/ANIMATION_DURATION);
            if(settingsAnimationProgress<=0)previousSelectedModule=null;
        }

        RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
        ClickGuiRender.rect(0,0,width,height,new Color(0,0,0,150));
        int x=categoryStartX;
        for(Category category:Category.values()){
            List<Module> modules=ModuleManager.getByCategory(category);
            ClickGuiRender.rounded(x,categoryStartY+214,CATEGORY_WIDTH,PANEL_HEIGHT+CATEGORY_HEIGHT,5,darkBlue);
            String title=category.name();
            mc.textRenderer.draw(matrices,title,x+(CATEGORY_WIDTH-mc.textRenderer.getWidth(title))/2,categoryStartY+5,-1);
            int moduleY=categoryStartY+CATEGORY_HEIGHT+5;
            for(Module module:modules){
                Color color=module.isEnabled()?activeModuleColor:inactiveModuleColor;
                ClickGuiRender.rounded(x+5,moduleY+(module.isEnabled()?CATEGORY_HEIGHT:0),CATEGORY_WIDTH-8,CATEGORY_HEIGHT,4,color);
                String display=module.getName();
                if(waitingForBind==module)display="Нажмите клавишу...";
                else if(isAltPressed&&module.getBind()!=-1)display+=" | "+KeyboardUtils.getKeyName(module.getBind());
                mc.textRenderer.draw(matrices,display,x+8,moduleY+(CATEGORY_HEIGHT-mc.textRenderer.fontHeight)/2f,-1);
                moduleY+=CATEGORY_HEIGHT+2;
            }
            x+=CATEGORY_WIDTH+CATEGORY_SPACING;
        }
        if(selectedModuleForSettings!=null||settingsAnimationProgress>0){
            Module display=selectedModuleForSettings!=null?selectedModuleForSettings:previousSelectedModule;
            if(display!=null)drawSettings(matrices,display);
        }
        RenderSystem.disableBlend();
        super.render(matrices,mouseX,mouseY,delta);
    }

    private void drawSettings(MatrixStack matrices,Module module){
        int targetX=Math.max(10,categoryStartX-170);
        int lastX=categoryStartX+(Category.values().length-1)*(CATEGORY_WIDTH+CATEGORY_SPACING);
        int sx=settingsAnimationProgress<1f?lastX-(int)((lastX-targetX)*settingsAnimationProgress):targetX;
        int alpha=(int)(settingsAnimationProgress*255);
        Color titleColor=fade(mediumBlue,alpha),panelColor=fade(darkBlue,alpha),glow=fade(glowColor,alpha);
        ClickGuiRender.glowRounded(sx,categoryStartY,160,CATEGORY_HEIGHT,5,6,titleColor,glow);
        mc.textRenderer.drawWithShadow(matrices,"Настройки: "+module.getName(),sx+5,categoryStartY+5,new Color(255,255,255,alpha).getRGB());
        int y=categoryStartY+CATEGORY_HEIGHT+5;
        ClickGuiRender.glowRounded(sx,y,160,PANEL_HEIGHT,5,8,panelColor,glow);y+=10;
        for(ISetting setting:module.getSettings()){
            if(setting instanceof BooleanSetting){
                BooleanSetting bs=(BooleanSetting)setting;
                ClickGuiRender.glowRounded(sx+5,y,150,18,4,5,fade(bs.isEnabled()?brightBlue:mediumBlue,alpha),glow);
                mc.textRenderer.drawWithShadow(matrices,bs.getName()+": "+(bs.isEnabled()?"ВКЛ":"ВЫКЛ"),sx+10,y+5,new Color(255,255,255,alpha).getRGB());
                y+=22;
            }else if(setting instanceof NumberSetting){
                NumberSetting ns=(NumberSetting)setting;
                int sliderX=sx+10,sliderY=y+20,sliderW=140;
                double p=(ns.getDoubleValue()-ns.getMin())/(ns.getMax()-ns.getMin());
                ClickGuiRender.rounded(sliderX,sliderY,sliderW,15,4,fade(mediumBlue,alpha));
                if(p>0)ClickGuiRender.rounded(sliderX,sliderY,(int)(p*sliderW),15,4,fade(brightBlue,alpha));
                mc.textRenderer.drawWithShadow(matrices,ns.getName()+": "+String.format(java.util.Locale.ROOT,"%.2f",ns.getDoubleValue()),sx+11,sliderY+6,new Color(255,255,255,alpha).getRGB());
                y+=40;
            }else if(setting instanceof MultiSetting){
                MultiSetting ms=(MultiSetting)setting;
                mc.textRenderer.drawWithShadow(matrices,ms.getName()+":",sx+5,y+1,new Color(255,255,255,alpha).getRGB());y+=15;
                int half=(ms.getOptionsCount()+1)/2;
                for(int i=0;i<half;i++){
                    drawMulti(matrices,ms,ms.getOptionName(i),sx+5,y,alpha);
                    if(i+half<ms.getOptionsCount())drawMulti(matrices,ms,ms.getOptionName(i+half),sx+85,y,alpha);
                    y+=22;
                }y+=5;
            }
        }
    }
    private void drawMulti(MatrixStack m,MultiSetting s,String n,int x,int y,int a){
        ClickGuiRender.glowRounded(x,y,70,18,4,5,fade(s.isEnabled(n)?brightBlue:mediumBlue,a),fade(glowColor,a));
        mc.textRenderer.drawWithShadow(m,n,x+3,y+5,new Color(255,255,255,a).getRGB());
    }
    private Color fade(Color c,int a){return new Color(c.getRed(),c.getGreen(),c.getBlue(),Math.min(c.getAlpha(),Math.max(0,a)));}

    @Override public boolean mouseClicked(double mx,double my,int button){
        int x=categoryStartX;
        for(Category category:Category.values()){
            int panelY=categoryStartY+CATEGORY_HEIGHT;
            if(mx>=x&&mx<=x+CATEGORY_WIDTH&&my>=categoryStartY&&my<=categoryStartY+PANEL_HEIGHT+CATEGORY_HEIGHT){
                if(my<=panelY)return true;
                int moduleY=panelY+5;
                for(Module module:ModuleManager.getByCategory(category)){
                    if(mx>=x+4&&mx<=x+CATEGORY_WIDTH-4&&my>=moduleY&&my<=moduleY+CATEGORY_HEIGHT){
                        if(button==0){module.toggled();TriggerBotClient.saveConfig();return true;}
                        if(button==1){
                            if(selectedModuleForSettings!=module){previousSelectedModule=selectedModuleForSettings;selectedModuleForSettings=module;settingsAnimationProgress=0;lastSettingsToggleTime=System.currentTimeMillis();}
                            else{previousSelectedModule=module;selectedModuleForSettings=null;lastSettingsToggleTime=System.currentTimeMillis();}
                            return true;
                        }
                        if(button==2){waitingForBind=module;return true;}
                    }
                    moduleY+=CATEGORY_HEIGHT+2;
                }return true;
            }
            x+=CATEGORY_WIDTH+CATEGORY_SPACING;
        }
        if(selectedModuleForSettings!=null&&settingsAnimationProgress>=.8f){
            int sx=Math.max(10,categoryStartX-170),y=categoryStartY+CATEGORY_HEIGHT+15;
            for(ISetting setting:selectedModuleForSettings.getSettings()){
                if(setting instanceof BooleanSetting){
                    if(inside(mx,my,sx+5,y,150,18)){((BooleanSetting)setting).toggle();TriggerBotClient.saveConfig();return true;}y+=22;
                }else if(setting instanceof NumberSetting){
                    NumberSetting ns=(NumberSetting)setting;
                    if(inside(mx,my,sx+10,y,140,40)){draggingSlider=ns;updateSlider(ns,mx,sx+10,140);TriggerBotClient.saveConfig();return true;}y+=40;
                }else if(setting instanceof MultiSetting){
                    MultiSetting ms=(MultiSetting)setting;y+=15;int half=(ms.getOptionsCount()+1)/2;
                    for(int i=0;i<half;i++){
                        if(inside(mx,my,sx+5,y,70,18)){ms.toggle(ms.getOptionName(i));return true;}
                        if(i+half<ms.getOptionsCount()&&inside(mx,my,sx+85,y,70,18)){ms.toggle(ms.getOptionName(i+half));return true;}y+=22;
                    }y+=5;
                }
            }
        }
        return super.mouseClicked(mx,my,button);
    }
    private boolean inside(double mx,double my,int x,int y,int w,int h){return mx>=x&&mx<=x+w&&my>=y&&my<=y+h;}
    private void updateSlider(NumberSetting s,double mx,int x,int w){
        double p=Math.max(0,Math.min(1,(mx-x)/w)),v=s.getMin()+p*(s.getMax()-s.getMin()),inc=s.getIncrement();
        s.setValue(Math.max(s.getMin(),Math.min(s.getMax(),Math.round(v/inc)*inc)));
    }
    @Override public boolean mouseDragged(double mx,double my,int button,double dx,double dy){
        if(draggingSlider!=null){updateSlider(draggingSlider,mx,Math.max(10,categoryStartX-160),140);TriggerBotClient.saveConfig();return true;}
        return super.mouseDragged(mx,my,button,dx,dy);
    }
    @Override public boolean mouseReleased(double mx,double my,int button){draggingSlider=null;return super.mouseReleased(mx,my,button);}
    @Override public boolean keyPressed(int keyCode,int scanCode,int modifiers){
        if(waitingForBind!=null){waitingForBind.setBind(keyCode==GLFW.GLFW_KEY_ESCAPE?-1:keyCode);waitingForBind=null;return true;}
        if(keyCode==GLFW.GLFW_KEY_ESCAPE){if(client!=null)client.openScreen(null);return true;}
        return super.keyPressed(keyCode,scanCode,modifiers);
    }
}
