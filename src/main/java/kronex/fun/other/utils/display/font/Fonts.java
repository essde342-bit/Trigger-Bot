package kronex.fun.other.utils.display.font;

public final class Fonts {
    private Fonts() {}

    public enum Type {
        DEFAULT,
        BOLD,
        HUD
    }

    public static FontRenderer getSize(float size, Type type) {
        return new FontRenderer(size);
    }

    public static FontRenderer getSize(int size, Type type) {
        return new FontRenderer(size);
    }
}