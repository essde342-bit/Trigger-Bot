package kronex.fun.other.utils.math.calc;

import kronex.fun.other.utils.math.MathUtil;

public final class Calculate {
    private Calculate(){}
    public static boolean isHovered(double mouseX,double mouseY,double x,double y,double width,double height){return MathUtil.isHovered(mouseX,mouseY,x,y,width,height);}
    public static float interpolate(float current,float target){return (float)MathUtil.interpolate(current,target);}
    public static float interpolateSmooth(float speed,float current,float target){return MathUtil.interpolateSmooth(speed,current,target);}
    public static float interpolateSmooth(double speed,float current,float target){return MathUtil.interpolateSmooth(speed,current,target);}
}
