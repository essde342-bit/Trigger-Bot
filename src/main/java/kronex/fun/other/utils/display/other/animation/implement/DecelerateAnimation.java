package kronex.fun.other.utils.display.other.animation.implement;

import kronex.fun.other.utils.display.other.animation.Animation;

public class DecelerateAnimation extends Animation {
    @Override
    public Number getOutput() {
        double p = Math.min(1.0D, (System.currentTimeMillis() - directionChangedAt) / (double) ms);
        double eased = 1.0D - Math.pow(1.0D - p, 2.0D);
        double t = direction == kronex.fun.other.utils.display.other.animation.Direction.FORWARDS ? eased : 1.0D - eased;
        return value * t;
    }
}