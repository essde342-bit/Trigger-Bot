package kronex.fun.display.screens.clickgui.components.implement.autobuy.util;

import net.minecraft.item.ItemStack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AuctionUtils {
    public static final Pattern funTimePricePattern = Pattern.compile("\\$(\\d+(?:[\\\\s,]\\d{3})*(?:\\.\\d{2})?)");

    private AuctionUtils() {}

    public static int getPrice(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return -1;
        }

        Matcher matcher = funTimePricePattern.matcher(stack.getName().getString());
        if (!matcher.find()) {
            return -1;
        }

        try {
            return Integer.parseInt(matcher.group(1).replaceAll("[\\s,]", ""));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static String cleanString(String value) {
        return value == null ? "" : value.toLowerCase()
                .replaceAll("[^\\p{L}\\p{N} ]", "")
                .trim();
    }
}
