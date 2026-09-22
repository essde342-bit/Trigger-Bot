package kronex.fun.display.screens.cosmetics;

import net.minecraft.client.gui.DrawContext;

public final class CosmeticsPanel {
    public void init() {
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawTextWithShadow(
                net.minecraft.client.MinecraftClient.getInstance().textRenderer,
                net.minecraft.text.Text.literal("Cosmetics"),
                120,
                80,
                0xFFD4D6E1
        );
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return false;
    }
}