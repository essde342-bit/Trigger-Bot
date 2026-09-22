package kronex.fun.other.utils.theme;

import java.io.File;
import java.util.Collections;
import java.util.List;

public final class ThemeManager {
    private ThemeManager() {}

    public static List<String> listThemes() {
        return Collections.emptyList();
    }

    public static File getThemeDir() {
        return new File(System.getProperty("user.dir"), "config/triggerbot/themes");
    }

    public static String getCurrentThemeName() {
        return "Default";
    }

    public static void loadTheme(String name) {
    }
}