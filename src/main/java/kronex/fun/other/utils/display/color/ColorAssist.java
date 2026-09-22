package kronex.fun.other.utils.display.color;

public final class ColorAssist {
    private static final int CLIENT=0xFF7D8CFF;
    private ColorAssist(){}
    public static int getClientColor(){return CLIENT;}
    public static int getClientColor(float alpha){return applyOpacity(CLIENT,alpha);}
    public static int getText(){return 0xFFD4D6E1;}
    public static int getText(float alpha){return applyOpacity(getText(),alpha);}
    public static int getRect(float alpha){return applyOpacity(0xFF181B22,alpha);}
    public static int getMainGuiColor(){return 0xFF20242D;}
    public static int getGuiRectColor(float alpha){return applyOpacity(0xFF181B22,alpha);}
    public static int getGuiRectColor2(float alpha){return applyOpacity(0xFF20242D,alpha);}
    public static int getOutline(){return 0xFF3A414D;}
    public static int getOutline(float alpha,int ignored){return applyOpacity(getOutline(),alpha);}
    public static int rgba(int r,int g,int b,int a){return ((a&255)<<24)|((r&255)<<16)|((g&255)<<8)|(b&255);}
    public static int applyOpacity(int color,int alpha){int a=Math.max(0,Math.min(255,alpha));return (color&0x00FFFFFF)|(a<<24);}
    public static int applyOpacity(int color,float alpha){return applyOpacity(color,(int)(Math.max(0F,Math.min(1F,alpha))*255F));}
    public static int fade(int offset){return getClientColor();}
    public static int multAlpha(int color,float alpha){int a=(int)(((color>>>24)&0xFF)*Math.max(0F,Math.min(1F,alpha)));return applyOpacity(color,a);}
}
