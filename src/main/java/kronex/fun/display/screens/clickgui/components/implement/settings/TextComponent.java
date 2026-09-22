package kronex.fun.display.screens.clickgui.components.implement.settings;

import net.minecraft.client.gui.DrawContext;
import kronex.fun.features.module.setting.implement.TextSetting;
import kronex.fun.display.screens.clickgui.components.AbstractComponent;

public final class TextComponent extends AbstractSettingComponent {
    public static boolean typing;
    private final TextSetting setting;

    public TextComponent(TextSetting setting) {
        super(setting);
        this.setting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        height = 18;
    }
}