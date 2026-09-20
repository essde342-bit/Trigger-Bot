package com.essde342.triggerbot.ui;

import com.essde342.triggerbot.TriggerBotClient;
import com.essde342.triggerbot.ui.imple.BooleanSetting;
import com.essde342.triggerbot.ui.imple.NumberSetting;
import com.essde342.triggerbot.ui.modules.Category;
import com.essde342.triggerbot.ui.modules.Module;
import com.essde342.triggerbot.ui.modules.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import java.awt.Color;
import java.util.List;
import java.util.Locale;

public class ClickGuiMain extends Screen {
    private static final int W=600,H=370;

    private static final Color BG=new Color(12,12,13,255);
    private static final Color PANEL=new Color(25,25,26,255);
    private static final Color CARD=new Color(33,33,34,255);
    private static final Color HOVER=new Color(42,42,43,255);
    private static final Color BORDER=new Color(58,58,59,255);
    private static final Color TEXT=new Color(238,238,239,255);
    private static final Color MUTED=new Color(145,145,148,255);
    private static final Color ACCENT=new Color(190,190,193,255);
    private static final Color ACCENT_DARK=new Color(76,76,79,255);
    private static final Color OFF=new Color(66,66,68,255);

    private final MinecraftClient mc=MinecraftClient.getInstance();

    private Category category=Category.COMBAT;
    private Module settingsModule;
    private Module binding;
    private NumberSetting dragging;

    private float scale=1f;
    private int ox,oy;

    public ClickGuiMain(){
        super(Text.of("Astra Client"));
        ModuleManager.moduleRegister();
    }

    @Override public boolean isPauseScreen(){ return false; }

    @Override public void render(MatrixStack matrices,int mouseX,int mouseY,float delta){
        resize();
        ClickGuiRender.rect(0,0,width,height,new Color(7,7,8,255));

        matrices.push();
        matrices.translate(ox,oy,0);
        matrices.scale(scale,scale,1f);

        int mx=localX(mouseX), my=localY(mouseY);
        drawBase(matrices,mx,my);

        if(settingsModule!=null) drawSettings(matrices,mx,my);

        matrices.pop();
    }

    private void resize(){
        scale=Math.min(1f,Math.min((width-8f)/W,(height-8f)/H));
        if(scale<0.55f) scale=0.55f;
        ox=(int)((width-W*scale)/2f);
        oy=(int)((height-H*scale)/2f);
    }

    private int localX(double x){ return (int)((x-ox)/scale); }
    private int localY(double y){ return (int)((y-oy)/scale); }

    private void drawBase(MatrixStack m,int mx,int my){
        ClickGuiRender.rounded(0,0,W,H,10,BG);
        ClickGuiRender.border(0,0,W,H,10,BORDER);

        drawText(m,"ASTRA",18,12,TEXT.getRGB());
        drawText(m,"CLIENT 1.0.0",67,14,MUTED.getRGB());
        drawText(m,"RIGHT SHIFT",W-88,14,MUTED.getRGB());

        drawCategories(m,mx,my);
        drawModules(m,mx,my);

        drawText(m,"ESC  CLOSE",18,H-18,MUTED.getRGB());
    }

    private void drawCategories(MatrixStack m,int mx,int my){
        int x=14,y=38,gap=5;
        int bw=(W-28-gap*4)/5;

        ClickGuiRender.rounded(10,34,W-20,38,9,PANEL);

        int i=0;
        for(Category c:Category.values()){
            int bx=x+i*(bw+gap);
            boolean sel=c==category;
            boolean hover=inside(mx,my,bx,y,bw,30);
            ClickGuiRender.rounded(bx,y,bw,30,7,sel?ACCENT_DARK:(hover?HOVER:PANEL));
            drawCentered(m,pretty(c),bx+bw/2,y+9,sel?TEXT.getRGB():MUTED.getRGB());
            i++;
        }
    }

    private void drawModules(MatrixStack m,int mx,int my){
        int x=14,y=82,w=W-28,h=H-103;
        ClickGuiRender.rounded(x,y,w,h,9,PANEL);
        ClickGuiRender.border(x,y,w,h,9,BORDER);

        drawText(m,pretty(category),x+13,y+10,TEXT.getRGB());
        drawText(m,"MODULES",x+13,y+27,MUTED.getRGB());

        List<Module> modules=ModuleManager.getByCategory(category);
        int cy=y+45;

        for(Module mod:modules){
            if(cy+44>y+h-7) break;

            boolean hover=inside(mx,my,x+9,cy,w-18,44);
            boolean enabled=mod.isEnabled();

            ClickGuiRender.rounded(x+9,cy,w-18,44,7,hover?HOVER:CARD);
            if(enabled) ClickGuiRender.rounded(x+9,cy,4,44,2,ACCENT);

            drawText(m,fit(mod.getName(),170),x+20,cy+7,TEXT.getRGB());
            drawText(m,fit(mod.getDesc(),230),x+20,cy+24,MUTED.getRGB());

            int switchX=x+w-101;
            ClickGuiRender.rounded(switchX,cy+11,38,22,9,enabled?ACCENT_DARK:OFF);
            ClickGuiRender.circle(enabled?switchX+29:switchX+9,cy+22,6,
                    enabled?TEXT:new Color(145,145,148,255));

            int dotX=x+w-45;
            ClickGuiRender.circle(dotX,cy+22,15,
                    settingsModule==mod?ACCENT_DARK:new Color(48,48,49,255));
            ClickGuiRender.circle(dotX,cy+16,2,TEXT);
            ClickGuiRender.circle(dotX,cy+22,2,TEXT);
            ClickGuiRender.circle(dotX,cy+28,2,TEXT);

            cy+=51;
        }
    }

    private void drawSettings(MatrixStack m,int mx,int my){
        int x=345,y=72,w=241,h=283;

        ClickGuiRender.rounded(x-4,y-4,w+8,h+8,11,BG);
        ClickGuiRender.border(x-4,y-4,w+8,h+8,11,BORDER);
        ClickGuiRender.rounded(x,y,w,h,8,PANEL);

        drawText(m,settingsModule.getName(),x+12,y+10,TEXT.getRGB());
        drawText(m,"MODULE SETTINGS",x+12,y+27,MUTED.getRGB());

        ClickGuiRender.rounded(x+w-30,y+8,20,20,6,CARD);
        drawCentered(m,"X",x+w-20,y+14,MUTED.getRGB());

        int cy=y+46;
        int availableBottom=y+h-38;

        for(ISetting s:settingsModule.getSettings()){
            if(cy>=availableBottom) break;
            if(s instanceof BooleanSetting){
                cy=drawBoolean(m,(BooleanSetting)s,x+9,cy,w-18,mx,my);
            }else if(s instanceof NumberSetting){
                cy=drawNumber(m,(NumberSetting)s,x+9,cy,w-18,mx,my);
            }
        }

        int bindY=y+h-31;
        ClickGuiRender.rounded(x+9,bindY,w-18,23,6,CARD);
        drawText(m,"BIND",x+17,bindY+7,MUTED.getRGB());

        String b=binding==settingsModule?"PRESS KEY":
                settingsModule.getBind()<0?"NONE":keyName(settingsModule.getBind());
        int bw=mc.textRenderer.getWidth(b);
        drawText(m,b,x+w-17-bw,bindY+7,TEXT.getRGB());
    }

    private int drawBoolean(MatrixStack m,BooleanSetting s,int x,int y,int w,int mx,int my){
        boolean hover=inside(mx,my,x,y,w,30);
        ClickGuiRender.rounded(x,y,w,30,6,hover?HOVER:CARD);
        drawText(m,fit(s.getName(),w-65),x+10,y+8,TEXT.getRGB());

        int sx=x+w-41;
        boolean on=s.isEnabled();
        ClickGuiRender.rounded(sx,y+6,31,18,9,on?ACCENT_DARK:OFF);
        ClickGuiRender.circle(on?sx+23:sx+8,y+15,6,
                on?TEXT:new Color(145,145,148,255));
        return y+34;
    }

    private int drawNumber(MatrixStack m,NumberSetting s,int x,int y,int w,int mx,int my){
        boolean hover=inside(mx,my,x,y,w,51);
        ClickGuiRender.rounded(x,y,w,51,6,hover?HOVER:CARD);

        String val=format(s);
        drawText(m,fit(s.getName(),w-80),x+10,y+7,TEXT.getRGB());
        int vw=mc.textRenderer.getWidth(val);
        drawText(m,val,x+w-10-vw,y+7,MUTED.getRGB());

        int sx=x+10,sy=y+32,sw=w-20;
        ClickGuiRender.rounded(sx,sy,sw,5,3,OFF);

        double p=(s.getDoubleValue()-s.getMin())/
                Math.max(0.000001D,s.getMax()-s.getMin());
        p=Math.max(0d,Math.min(1d,p));
        int fill=(int)Math.round(sw*p);
        if(fill>0) ClickGuiRender.rounded(sx,sy,fill,5,3,ACCENT_DARK);
        ClickGuiRender.circle(sx+fill,sy+2,7,ACCENT);
        return y+57;
    }

    @Override public boolean mouseClicked(double mouseX,double mouseY,int button){
        int x=localX(mouseX),y=localY(mouseY);

        if(settingsModule!=null){
            int px=341,py=68,pw=249,ph=291;
            if(inside(x,y,px+pw-34,py+8,24,24)){
                settingsModule=null; binding=null; dragging=null; return true;
            }

            int sy=118,sx=354,sw=223;
            for(ISetting s:settingsModule.getSettings()){
                if(s instanceof BooleanSetting){
                    if(inside(x,y,sx,sy,sw,30)){
                        ((BooleanSetting)s).toggle();
                        TriggerBotClient.saveConfig();
                        return true;
                    }
                    sy+=34;
                }else if(s instanceof NumberSetting){
                    if(inside(x,y,sx,sy,sw,51)){
                        dragging=(NumberSetting)s;
                        updateSlider(dragging,x,sx+10,sw-20);
                        TriggerBotClient.saveConfig();
                        return true;
                    }
                    sy+=57;
                }
            }

            if(inside(x,y,354,324,223,23)){
                binding=settingsModule;
                return true;
            }

            if(!inside(x,y,px,py,pw,ph)){
                settingsModule=null; binding=null; dragging=null;
                return true;
            }
            return true;
        }

        int cx=14,cy=38,gap=5,bw=(W-28-gap*4)/5;
        for(Category c:Category.values()){
            if(inside(x,y,cx,cy,bw,30)){
                category=c;
                return true;
            }
            cx+=bw+gap;
        }

        int listX=14,listY=127,listW=W-28;
        for(Module mod:ModuleManager.getByCategory(category)){
            if(inside(x,y,listX+9,listY,listW-18,44)){
                int dotX=listX+listW-45;
                if(inside(x,y,dotX-17,listY+5,34,34)){
                    settingsModule=mod;
                    binding=null;
                    dragging=null;
                    return true;
                }

                if(button==0){
                    mod.toggled();
                    TriggerBotClient.saveConfig();
                    return true;
                }
            }
            listY+=51;
        }

        return super.mouseClicked(mouseX,mouseY,button);
    }

    @Override public boolean mouseDragged(double mouseX,double mouseY,int button,double dx,double dy){
        if(dragging!=null){
            int x=localX(mouseX);
            updateSlider(dragging,x,364,203);
            TriggerBotClient.saveConfig();
            return true;
        }
        return super.mouseDragged(mouseX,mouseY,button,dx,dy);
    }

    @Override public boolean mouseReleased(double mouseX,double mouseY,int button){
        dragging=null;
        return super.mouseReleased(mouseX,mouseY,button);
    }

    @Override public boolean keyPressed(int keyCode,int scanCode,int modifiers){
        if(binding!=null){
            binding.setBind(keyCode==GLFW.GLFW_KEY_ESCAPE?-1:keyCode);
            TriggerBotClient.saveConfig();
            binding=null;
            return true;
        }
        if(keyCode==GLFW.GLFW_KEY_ESCAPE){
            if(settingsModule!=null){
                settingsModule=null;
                dragging=null;
                return true;
            }
            if(client!=null) client.openScreen(null);
            return true;
        }
        return super.keyPressed(keyCode,scanCode,modifiers);
    }

    private void updateSlider(NumberSetting s,double mx,int sx,int sw){
        double p=Math.max(0d,Math.min(1d,(mx-sx)/sw));
        double v=s.getMin()+p*(s.getMax()-s.getMin());
        double inc=s.getIncrement();
        if(inc>0) v=Math.round(v/inc)*inc;
        s.setValue(v);
    }

    private String fit(String t,int maxW){
        if(t==null) return "";
        if(mc.textRenderer.getWidth(t)<=maxW) return t;
        String dots="...";
        int dw=mc.textRenderer.getWidth(dots);
        if(dw>=maxW) return "";
        StringBuilder b=new StringBuilder();
        for(int i=0;i<t.length();i++){
            String n=b.toString()+t.charAt(i);
            if(mc.textRenderer.getWidth(n)+dw>maxW) break;
            b.append(t.charAt(i));
        }
        return b+dots;
    }

    private String format(NumberSetting s){
        double v=s.getDoubleValue();
        String n=s.getName();
        if(n.contains("Time")||n.contains("Delay")) return Math.round(v)+" ms";
        if(n.contains("FPS")) return Math.round(v)+" FPS";
        if(n.contains("Gamma")) return String.format(Locale.ROOT,"%.0f",v);
        if(n.contains("Ratio")) return String.format(Locale.ROOT,"%.2f",v);
        return s.getIncrement()>=1?String.valueOf(Math.round(v)):
                String.format(Locale.ROOT,"%.1f",v);
    }

    private String keyName(int code){
        if(code<0) return "NONE";
        String s=GLFW.glfwGetKeyName(code,0);
        return s==null?String.valueOf(code):s.toUpperCase(Locale.ROOT);
    }

    private String pretty(Category c){
        String s=c.name().toLowerCase(Locale.ROOT);
        return Character.toUpperCase(s.charAt(0))+s.substring(1);
    }

    private void drawText(MatrixStack m,String t,int x,int y,int color){
        mc.textRenderer.draw(m,t,x,y,color);
    }

    private void drawCentered(MatrixStack m,String t,int centerX,int y,int color){
        drawText(m,t,centerX-mc.textRenderer.getWidth(t)/2,y,color);
    }

    private boolean inside(double mx,double my,int x,int y,int w,int h){
        return mx>=x&&mx<=x+w&&my>=y&&my<=y+h;
    }
}
