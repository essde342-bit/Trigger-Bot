package kronex.fun.other.common.animation.implement;

public final class Decelerate {
    private Decelerate() {}

    public static double ease(double value) {
        double v = Math.max(0.0D, Math.min(1.0D, value));
        return 1.0D - Math.pow(1.0D - v, 2.0D);
    }
}