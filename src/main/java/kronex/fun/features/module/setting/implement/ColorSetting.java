package kronex.fun.features.module.setting.implement;

import kronex.fun.features.module.setting.Setting;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ColorSetting extends Setting {
    private int color;
    private float alpha=1f;
    private final List<Integer> presets=new ArrayList<>();

    public ColorSetting(String name,String description,int color){super(name,description);this.color=color;presets.add(color);}
    public ColorSetting(String name,int color){this(name,"",color);}
    public int getColor(){return color;}
    public void setColor(int color){this.color=color;}
    public float getAlpha(){return alpha;}
    public void setAlpha(float alpha){this.alpha=Math.max(0f,Math.min(1f,alpha));}
    public int getColorWithAlpha(){return (Math.round(alpha*255)<<24)|(color&0xFFFFFF);}
    public float getHue(){float[] h=Color.RGBtoHSB((color>>16)&255,(color>>8)&255,color&255,null);return h[0];}
    public float getSaturation(){float[] h=Color.RGBtoHSB((color>>16)&255,(color>>8)&255,color&255,null);return h[1];}
    public float getBrightness(){float[] h=Color.RGBtoHSB((color>>16)&255,(color>>8)&255,color&255,null);return h[2];}
    public void setHue(float hue){setHsb(hue,getSaturation(),getBrightness());}
    public void setSaturation(float saturation){setHsb(getHue(),saturation,getBrightness());}
    public void setBrightness(float brightness){setHsb(getHue(),getSaturation(),brightness);}
    private void setHsb(float h,float s,float b){color=Color.HSBtoRGB(Math.max(0,Math.min(1,h)),Math.max(0,Math.min(1,s)),Math.max(0,Math.min(1,b)))&0xFFFFFF;}
    public List<Integer> getPresets(){return presets;}
}
