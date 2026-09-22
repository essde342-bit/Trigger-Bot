package kronex.fun.other.utils.other;

public final class StringUtil {
    private StringUtil() {}

    public static String getBindName(int key) {
        if (key < 0) return "NONE";
        if (key >= 32 && key <= 126) return Character.toString((char) key).toUpperCase();
        return Integer.toString(key);
    }

    public static String getUserRole() {
        return "User";
    }
}