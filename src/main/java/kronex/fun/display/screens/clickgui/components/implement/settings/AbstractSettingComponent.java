package kronex.fun.display.screens.clickgui.components.implement.settings;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.DrawContext;
import kronex.fun.features.module.setting.Setting;
import kronex.fun.display.screens.clickgui.components.AbstractComponent;

@Getter
@RequiredArgsConstructor
public abstract class AbstractSettingComponent extends AbstractComponent {
    private final Setting setting;

    public void renderOverlay(DrawContext context, int mouseX, int mouseY, float delta) {
    }
}