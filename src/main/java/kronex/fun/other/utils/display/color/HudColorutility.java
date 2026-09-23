package kronex.fun.other.utils.display.color;

public final class HudColorutility {
    private HudColorutility() {}

    public static int getRectGradient(float a, float b) {
        return ColorAssist.applyOpacity(ColorAssist.PANEL, 245);
    }

    public static int getIconColor() {
        return ColorAssist.getClientColor();
    }
}
