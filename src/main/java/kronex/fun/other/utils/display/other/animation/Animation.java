package kronex.fun.other.utils.display.other.animation;

public abstract class Animation {
    protected long ms = 200;
    protected double value = 1.0;
    protected Direction direction = Direction.FORWARDS;
    protected long directionChangedAt = System.currentTimeMillis();

    public Animation setMs(long ms) {
        this.ms = Math.max(1L, ms);
        return this;
    }

    public Animation setValue(double value) {
        this.value = value;
        return this;
    }

    public void setDirection(Direction direction) {
        if (this.direction != direction) {
            this.direction = direction;
            this.directionChangedAt = System.currentTimeMillis();
        }
    }

    public Number getOutput() {
        double p = Math.min(1.0D, (System.currentTimeMillis() - directionChangedAt) / (double) ms);
        double t = direction == Direction.FORWARDS ? p : 1.0D - p;
        return value * t;
    }

    public boolean isFinished(Direction target) {
        if (target == Direction.FORWARDS) return getOutput().doubleValue() >= value;
        return getOutput().doubleValue() <= 0.0D;
    }
}