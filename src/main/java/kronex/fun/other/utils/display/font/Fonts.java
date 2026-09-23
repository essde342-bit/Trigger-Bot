package kronex.fun.other.utils.display.font;

public final class Fonts {
    private Fonts() {}

    public enum Type {
        DEFAULT, BOLD, HUD, SEMI, REGULAR, ICONS2
    }

    public static FontRenderer getSize(float size, Type type) {
        return new FontRenderer(size, type);
    }

    public static FontRenderer getSize(int size, Type type) {
        return new FontRenderer(size, type);
    }
}
