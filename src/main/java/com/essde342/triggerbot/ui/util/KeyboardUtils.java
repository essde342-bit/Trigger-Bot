package com.essde342.triggerbot.ui.util;

import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class KeyboardUtils {
    private static boolean altLayout;

    private KeyboardUtils() {}

    public static void updateKeyboardLayout() {
        altLayout = false;
    }

    public static String getKeyName(int keyCode) {
        if (keyCode < 0) return "NONE";
        String name = InputUtil.fromKeyCode(keyCode, 0).getLocalizedText().getString();
        if (name == null || name.trim().isEmpty()) {
            return GLFW.glfwGetKeyName(keyCode, 0) != null
                    ? GLFW.glfwGetKeyName(keyCode, 0).toUpperCase()
                    : String.valueOf(keyCode);
        }
        return name;
    }
}
