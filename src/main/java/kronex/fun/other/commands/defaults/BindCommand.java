package kronex.fun.other.commands.defaults;

public final class BindCommand {
    private BindCommand() {}

    public static final class ClickGuiManager {
        private static int clickGuiKey = org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT;

        private ClickGuiManager() {}

        public static int getClickGuiKey() {
            return clickGuiKey;
        }

        public static void setClickGuiKey(int key) {
            clickGuiKey = key;
        }
    }
}