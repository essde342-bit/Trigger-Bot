package kronex.fun.features.impl.render;

public final class Interface {
    private static final Interface INSTANCE = new Interface();
    public final ColorSetting colorSetting = new ColorSetting();

    public static Interface getInstance() {
        return INSTANCE;
    }

    public static final class ColorSetting {
        public int getColor() {
            return 0xFF7D8CFF;
        }
    }
}