package kronex.fun.display.screens.clickgui.components.implement.window.implement.settings.group;

import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import kronex.fun.features.module.setting.SettingComponentAdder;
import kronex.fun.features.module.setting.implement.GroupSetting;
import kronex.fun.other.utils.display.font.Fonts;
import kronex.fun.other.utils.display.shape.ShapeProperties;
import kronex.fun.other.utils.display.color.ColorAssist;
import kronex.fun.other.utils.math.MathUtil;
import kronex.fun.other.utils.display.scissor.ScissorAssist;
import kronex.fun.Kronex;
import kronex.fun.display.screens.clickgui.components.AbstractComponent;
import kronex.fun.display.screens.clickgui.components.implement.settings.AbstractSettingComponent;
import kronex.fun.display.screens.clickgui.components.implement.window.AbstractWindow;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Getter
public class GroupWindow extends AbstractWindow {
    private final List<AbstractSettingComponent> components = new ArrayList<>();
    private final GroupSetting setting;

    public GroupWindow(GroupSetting setting) {
        this.setting = setting;
        new SettingComponentAdder().addSettingComponent(setting.getSubSettings(), components);
    }

    @Override
    public void drawWindow(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrix = context.getMatrices();
        ScissorAssist scissorManager = Kronex.getInstance().scissorManager;
        height = MathHelper.clamp(getComponentHeight(), 0, 200);
        rectangle.render(ShapeProperties.create(matrix, x, y, width, height)
                .round(4).thickness(2).softness(1).outlineColor(ColorAssist.getOutline()).color(ColorAssist.getGuiRectColor(1)).build());
        Fonts.getSize(15, Fonts.Type.BOLD).drawString(context.getMatrices(), "Settings " + setting.getName(), x + 9, y + 10, -1);

        boolean isLimitedHeight = MathHelper.clamp(height, 0, 200) == 200;
        if (isLimitedHeight) scissorManager.push(matrix.peek().getPositionMatrix(), x, y + 23, width, height - 28);

        float offset = 0;
        int totalHeight = 0;
        for (int i = components.size() - 1; i >= 0; i--) {
            AbstractSettingComponent component = components.get(i);
            Supplier<Boolean> visible = component.getSetting().getVisible();
            if (visible != null && !visible.get()) continue;
            component.x = x;
            component.y = (float) (y + 19 + offset + (getComponentHeight() - 25 - component.height) + smoothedScroll);
            component.width = width;
            component.render(context, mouseX, mouseY, delta);
            offset -= component.height;
            totalHeight += (int) component.height;
        }
        if (isLimitedHeight) scissorManager.pop();

        int maxScroll = (int) Math.max(0, totalHeight - (height - 23));
        scroll = MathHelper.clamp(scroll, -maxScroll, 0);
        smoothedScroll = MathHelper.lerp(0.1F, smoothedScroll, scroll);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        draggable(MathUtil.isHovered(mouseX, mouseY, x, y, width, 19) && button == 0);
        boolean hovered = components.stream().anyMatch(c -> c.isHover(mouseX, mouseY));
        if (hovered) {
            components.forEach(c -> { if (c.isHover(mouseX, mouseY)) c.mouseClicked(mouseX, mouseY, button); });
            return super.mouseClicked(mouseX, mouseY, button);
        }
        components.forEach(c -> c.mouseClicked(mouseX, mouseY, button));
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isHover(double mouseX, double mouseY) {
        for (AbstractComponent component : components) if (component.isHover(mouseX, mouseY)) return true;
        return super.isHover(mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        components.forEach(c -> c.mouseReleased(mouseX, mouseY, button));
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        boolean scrolled = MathHelper.clamp(height, 0, 200) == 200 && MathUtil.isHovered(mouseX, mouseY, x, y, width, height);
        if (scrolled) scroll += amount * 20;
        components.forEach(c -> c.mouseScrolled(mouseX, mouseY, amount));
        return scrolled;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        components.forEach(c -> c.keyPressed(keyCode, scanCode, modifiers));
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        components.forEach(c -> c.charTyped(chr, modifiers));
        return super.charTyped(chr, modifiers);
    }

    public int getComponentHeight() {
        float offsetY = 0;
        for (AbstractSettingComponent component : components) {
            Supplier<Boolean> visible = component.getSetting().getVisible();
            if (visible != null && !visible.get()) continue;
            offsetY += component.height;
        }
        return (int) (offsetY + 25);
    }
}
