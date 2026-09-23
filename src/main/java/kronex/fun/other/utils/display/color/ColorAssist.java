package kronex.fun.other.utils.display.color;

import java.awt.Color;

public final class ColorAssist {
    public static final int BACKGROUND = rgba(16, 16, 18, 255);
    public static final int HEADER = rgba(22, 22, 27, 255);
    public static final int PANEL = rgba(30, 30, 38, 255);
    public static final int PANEL_HOVER = rgba(38, 37, 47, 255);
    public static final int OUTLINE = rgba(54, 53, 61, 210);
    public static final int CLIENT = rgba(151, 135, 212, 255);
    public static final int CLIENT_HOVER = rgba(162, 145, 225, 255);
    public static final int TEXT = rgba(235, 234, 239, 255);
    public static final int TEXT_SECONDARY = rgba(199, 198, 206, 255);
    public static final int TEXT_MUTED = rgba(162, 161, 166, 255);

    private ColorAssist() {}

    public static int getClientColor() {
        return CLIENT;
    }

    public static int getClientColor(float alpha) {
        return applyOpacity(CLIENT, alpha);
    }

    public static int getText() {
        return TEXT_SECONDARY;
    }

    public static int getText(float alpha) {
        return applyOpacity(TEXT_SECONDARY, alpha);
    }

    public static int getRect(float alpha) {
        return applyOpacity(PANEL, alpha);
    }

    public static int getMainGuiColor() {
        return PANEL;
    }

    public static int getGuiRectColor(float alpha) {
        return applyOpacity(PANEL, alpha);
    }

    public static int getGuiRectColor2(float alpha) {
        return applyOpacity(PANEL_HOVER, alpha);
    }

    public static int getOutline() {
        return OUTLINE;
    }

    public static int getOutline(float alpha) {
        return applyOpacity(OUTLINE, alpha);
    }

    public static int getOutline(float alpha, int ignored) {
        return getOutline(alpha);
    }

    public static int rgba(int r, int g, int b, int a) {
        return new Color(r, g, b, a).getRGB();
    }

    public static int applyOpacity(int color, int alpha) {
        int a = Math.max(0, Math.min(255, alpha));
        return (color & 0x00FFFFFF) | (a << 24);
    }

    public static int applyOpacity(int color, float alpha) {
        return applyOpacity(color, (int) (Math.max(0F, Math.min(1F, alpha)) * 255F));
    }

    public static int fade(int offset) {
        int base = CLIENT;
        int shifted = Math.max(65, Math.min(255, 255 - offset));
        return applyOpacity(base, shifted / 255F);
    }

    public static int multAlpha(int color, float alpha) {
        int a = (int) (((color >>> 24) & 0xFF) * Math.max(0F, Math.min(1F, alpha)));
        return applyOpacity(color, a);
    }
}
