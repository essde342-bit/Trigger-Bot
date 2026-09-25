package kronex.fun.display.screens.clickgui.components.implement.module;

import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import kronex.fun.features.module.Module;
import kronex.fun.features.module.setting.SettingComponentAdder;
import kronex.fun.features.module.setting.Setting;
import kronex.fun.other.utils.display.font.Fonts;
import kronex.fun.other.utils.display.shape.ShapeProperties;
import kronex.fun.other.utils.display.color.ColorAssist;
import kronex.fun.other.utils.math.MathUtil;
import kronex.fun.other.utils.display.other.animation.Direction;
import kronex.fun.other.utils.display.other.animation.implement.DecelerateAnimation;
import kronex.fun.display.screens.clickgui.components.AbstractComponent;
import kronex.fun.display.screens.clickgui.components.implement.other.CheckComponent;
import kronex.fun.display.screens.clickgui.components.implement.settings.AbstractSettingComponent;
import kronex.fun.display.screens.clickgui.components.implement.settings.SliderComponent;
import kronex.fun.display.screens.clickgui.components.implement.window.implement.module.ModuleBindWindow;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import static kronex.fun.other.utils.display.font.Fonts.Type.BOLD;

@Getter
public class ModuleComponent extends AbstractComponent {
    private static final float HEADER_HEIGHT = 18f;
    private static final float SETTINGS_TOP_PADDING = 6f;
    private static final float SETTINGS_BOTTOM_PADDING = 6f;
    private final List<AbstractSettingComponent> components = new ArrayList<>();
    private final CheckComponent checkComponent = new CheckComponent();
    private final Module module;
    private final kronex.fun.other.utils.display.other.animation.Animation hoverAnimation = new DecelerateAnimation().setMs(200).setValue(1);
    private final kronex.fun.other.utils.display.other.animation.Animation expandAnimation = new DecelerateAnimation().setMs(250).setValue(1);
    private final kronex.fun.other.utils.display.other.animation.Animation enableAnimation = new DecelerateAnimation().setMs(300).setValue(1);

    private void initialize() {
        new SettingComponentAdder().addSettingComponent(module.settings(), components);
    }

    public ModuleComponent(Module module) {
        this.module = module;
        initialize();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean hovered = MathUtil.isHovered(mouseX, mouseY, x, y, width, HEADER_HEIGHT);
        hoverAnimation.setDirection(hovered ? Direction.FORWARDS : Direction.BACKWARDS);
        float hoverAnim = hoverAnimation.getOutput().floatValue();

        boolean hasSettings = !components.isEmpty();
        expandAnimation.setDirection(hasSettings ? Direction.FORWARDS : Direction.BACKWARDS);
        enableAnimation.setDirection(module.isState() ? Direction.FORWARDS : Direction.BACKWARDS);
        float enableAnim = enableAnimation.getOutput().floatValue();

        rectangle.render(ShapeProperties.create(context.getMatrices(), x, y, width, HEADER_HEIGHT).round(5, 0, 5, 0).color(ColorAssist.getGuiRectColor2(1)).build());
        if (hoverAnim > 0) {
            rectangle.render(ShapeProperties.create(context.getMatrices(), x, y, width, HEADER_HEIGHT)
                    .round(5, 0, 5, 0).color(ColorAssist.applyOpacity(ColorAssist.getClientColor(), (int) (hoverAnim * 18))).build());
        }
        if (enableAnim > 0) {
            rectangle.render(ShapeProperties.create(context.getMatrices(), x, y, width, HEADER_HEIGHT)
                    .round(5, 0, 5, 0).color(ColorAssist.applyOpacity(ColorAssist.getClientColor(), (int) (enableAnim * 100))).build());
        }
        rectangle.render(ShapeProperties.create(context.getMatrices(), x, y, width, height = getComponentHeight())
                .round(5).softness(0.8F).thickness(1F)
                .outlineColor(ColorAssist.applyOpacity(ColorAssist.getOutline(), 120))
                .color(ColorAssist.getGuiRectColor(1)).build());

        Fonts.getSize(14, BOLD).drawString(context.getMatrices(), module.getVisibleName(), x + 10, y + 8, enableAnim > 0.5f ? 0xFFFFFFFF : 0xFFD4D6E1);
        drawBind(context);

        float offset = y + HEADER_HEIGHT + SETTINGS_TOP_PADDING;
        for (AbstractSettingComponent component : components) {
            Setting setting = component.getSetting();
            Supplier<Boolean> visible = setting.getVisible();
            if (visible != null && !visible.get()) continue;
            component.x = x;
            component.y = offset;
            component.width = width;
            component.render(context, mouseX, mouseY, delta);
            offset += getSettingHeight(component);
        }

        for (AbstractSettingComponent component : components) {
            Setting setting = component.getSetting();
            Supplier<Boolean> visible = setting.getVisible();
            if (visible != null && !visible.get()) continue;
            component.renderOverlay(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isBindHovered(mouseX, mouseY)) {
            openBindWindow(mouseX, mouseY);
            return true;
        }
        if (button == 0 && MathUtil.isHovered(mouseX, mouseY, x, y, width, HEADER_HEIGHT)) {
            module.switchState();
            return true;
        }
        for (AbstractSettingComponent component : components) {
            if (component.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    @Override public boolean isHover(double mouseX, double mouseY) {
        for (AbstractComponent abstractComponent : components) if (abstractComponent.isHover(mouseX, mouseY)) return true;
        return MathUtil.isHovered(mouseX, mouseY, x, y, width, height);
    }

    @Override public void tick() { components.forEach(AbstractComponent::tick); super.tick(); }

    @Override public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        components.forEach(c -> c.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)); return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override public boolean mouseReleased(double mouseX, double mouseY, int button) {
        components.forEach(c -> c.mouseReleased(mouseX, mouseY, button)); return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        components.forEach(c -> c.mouseScrolled(mouseX, mouseY, amount)); return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        components.forEach(c -> c.keyPressed(keyCode, scanCode, modifiers)); return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override public boolean charTyped(char chr, int modifiers) {
        components.forEach(c -> c.charTyped(chr, modifiers)); return super.charTyped(chr, modifiers);
    }

    public int getComponentHeight() {
        float offsetY = HEADER_HEIGHT + SETTINGS_TOP_PADDING + SETTINGS_BOTTOM_PADDING;
        for (AbstractSettingComponent component : components) {
            Setting setting = component.getSetting();
            Supplier<Boolean> visible = setting.getVisible();
            if (visible != null && !visible.get()) continue;
            offsetY += getSettingHeight(component);
        }
        return (int) offsetY;
    }

    private float getSettingHeight(AbstractSettingComponent component) {
        if (component.height > 0) return component.height;
        if (component instanceof SliderComponent) return 28f;
        return 18f;
    }

    private void drawBind(DrawContext context) {
        float iconWidth = Fonts.getSize(13, Fonts.Type.ICONS2).getStringWidth("⚙");
        Fonts.getSize(13, Fonts.Type.ICONS2).drawString(context.getMatrices(), "⚙",
                getBindX() + (getBindWidth() - iconWidth) / 2f, getBindY() + 2.5f, ColorAssist.getText());
    }

    private float getBindX() { return x + width - 21f; }
    private float getBindY() { return y + 4f; }
    private float getBindWidth() { return 14f; }
    private float getBindHeight() { return 10f; }

    private boolean isBindHovered(double mouseX, double mouseY) {
        return MathUtil.isHovered(mouseX, mouseY, getBindX(), getBindY(), getBindWidth(), getBindHeight());
    }

    private void openBindWindow(double mouseX, double mouseY) {
        ModuleBindWindow bindWindow = new ModuleBindWindow(module);
        bindWindow.position((float) mouseX + 8f, (float) mouseY + 8f).size(120, 57).draggable(true);
        windowManager.add(bindWindow);
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return module.equals(((ModuleComponent) o).module);
    }

    @Override public int hashCode() { return Objects.hash(module); }
}