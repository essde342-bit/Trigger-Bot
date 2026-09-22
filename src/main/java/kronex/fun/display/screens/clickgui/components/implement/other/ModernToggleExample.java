package kronex.fun.display.screens.clickgui.components.implement.other;

import net.minecraft.client.gui.DrawContext;
import kronex.fun.display.screens.clickgui.components.AbstractComponent;

public final class ModernToggleExample extends AbstractComponent {
    private final ModernToggleComponent toggle = new ModernToggleComponent()
            .setTitle("Example")
            .setDescription("Modern toggle component")
            .setToggleRunnable(() -> {});

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        toggle.position(x, y).size(120, 40);
        toggle.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return toggle.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
    }
}
