package kronex.fun.features.impl.render.hud.utils;

import kronex.fun.other.utils.display.interfaces.QuickImports;
import kronex.fun.other.utils.display.shape.ShapeProperties;

public final class HudUtil {
    private HudUtil() {}

    public static void drawHudBlur(ShapeProperties properties) {
        QuickImports.blur.render(properties);
    }
}