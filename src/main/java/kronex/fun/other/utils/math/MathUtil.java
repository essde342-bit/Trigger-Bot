package kronex.fun.other.utils.math;

import net.minecraft.client.util.math.MatrixStack;

public final class MathUtil {
    private MathUtil() {}

    public static boolean isHovered(double mouseX, double mouseY, double x, double y, double width, double height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public static double interpolate(double current, double target) {
        return current + (target - current) * 0.35D;
    }

    public static float interpolateSmooth(float speed, float current, float target) {
        return current + (target - current) / Math.max(1F, speed);
    }

    public static float interpolateSmooth(double speed, float current, float target) {
        return current + (target - current) / Math.max(1F, (float) speed);
    }

    public static void scale(MatrixStack matrices, float centerX, float centerY, float scale, Runnable runnable) {
        matrices.push();
        matrices.translate(centerX, centerY, 0);
        matrices.scale(scale, scale, 1);
        matrices.translate(-centerX, -centerY, 0);
        runnable.run();
        matrices.pop();
    }
}