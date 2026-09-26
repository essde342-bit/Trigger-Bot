package kronex.fun.display.screens.clickgui.components.implement.category;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import kronex.fun.features.module.Module;
import kronex.fun.features.module.ModuleCategory;
import kronex.fun.other.utils.display.other.animation.Direction;
import kronex.fun.other.utils.display.other.animation.implement.DecelerateAnimation;
import kronex.fun.other.utils.display.font.Fonts;
import kronex.fun.other.utils.display.shape.ShapeProperties;
import kronex.fun.other.utils.display.color.ColorAssist;
import kronex.fun.other.utils.math.MathUtil;
import kronex.fun.Kronex;
import kronex.fun.display.screens.clickgui.MenuScreen;
import kronex.fun.display.screens.clickgui.components.AbstractComponent;
import kronex.fun.display.screens.clickgui.components.implement.module.ModuleComponent;

import java.util.ArrayList;
import java.util.List;

public class CategoryComponent extends AbstractComponent {
    private final List<ModuleComponent> moduleComponents = new ArrayList<>();
    private final ModuleCategory category;
    private final kronex.fun.other.utils.display.other.animation.Animation alphaAnimation =
            new DecelerateAnimation().setMs(300).setValue(1);

    private void initialize() {
        List<Module> modules = Kronex.getInstance().getModuleRepository().modules();
        for (Module module : modules) {
            moduleComponents.add(new ModuleComponent(module));
        }
    }

    public CategoryComponent(ModuleCategory category) {
        this.category = category;
        initialize();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MenuScreen menuScreen = MenuScreen.INSTANCE;
        drawCategoryTab(context, context.getMatrices());

        int[] offsets = calculateOffsets();
        int columnWidth = 137;
        int column = 0;
        int maxScroll = 0;
        float offsetX = 84, offsetY = 29;

        final float contentTop = menuScreen.y + offsetY;
        final float contentBottom = menuScreen.y + menuScreen.height - 1;
        for (int i = moduleComponents.size() - 1; i >= 0; i--) {
            ModuleComponent component = moduleComponents.get(i);
            if (shouldRenderComponent(component)) {
                int componentHeight = component.getComponentHeight() + 9;

                component.x = menuScreen.x + 95 + (column * (columnWidth + 10));
                component.y = (float) (menuScreen.y + 39 + offsets[column] - componentHeight + smoothedScroll);
                component.width = columnWidth;

                float componentBottom = component.y + componentHeight;
                if (component.y >= contentTop
                        && componentBottom <= contentBottom) {
                    component.render(context, mouseX, mouseY, delta);
                }

                offsets[column] -= componentHeight;
                maxScroll = Math.max(maxScroll, offsets[column]);
                column = (column + 1) % 2;
            }
        }
        int clamped = MathHelper.clamp(maxScroll - (menuScreen.height / 2 - 80), 0, maxScroll);
        scroll = MathHelper.clamp(scroll, -clamped, 0);
        smoothedScroll = MathUtil.interpolateSmooth(2, (float) smoothedScroll, (float) scroll);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        MenuScreen menuScreen = MenuScreen.INSTANCE;

        if (MathUtil.isHovered(mouseX, mouseY, x, y, width, height) && button == 0) {
            MenuScreen.INSTANCE.setCategory(category);
            MenuScreen.INSTANCE.setCosmeticsOpen(false);
            return true;
        }

        float offsetX = 84, offsetY = 29;
        if (MathUtil.isHovered(mouseX, mouseY, menuScreen.x + offsetX, menuScreen.y + offsetY,
                menuScreen.width - offsetX, menuScreen.height - offsetY)) {
            for (ModuleComponent moduleComponent : moduleComponents) {
                if (shouldRenderComponent(moduleComponent)
                        && moduleComponent.isHover(mouseX, mouseY)
                        && moduleComponent.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isHover(double mouseX, double mouseY) {
        for (ModuleComponent moduleComponent : moduleComponents) {
            if (moduleComponent.isHover(mouseX, mouseY)) return true;
        }
        return super.isHover(mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        moduleComponents.forEach(moduleComponent -> moduleComponent.mouseReleased(mouseX, mouseY, button));
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        MenuScreen menuScreen = MenuScreen.INSTANCE;
        float offsetX = 84, offsetY = 29;
        boolean overPanel = MathUtil.isHovered(mouseX, mouseY, menuScreen.x + offsetX, menuScreen.y + offsetY,
                menuScreen.width - offsetX, menuScreen.height - offsetY);
        boolean searchActive = !menuScreen.getSearchComponent().getText().isEmpty();
        boolean overGui = MathUtil.isHovered(mouseX, mouseY, menuScreen.x, menuScreen.y, menuScreen.width, menuScreen.height);

        if (overPanel || (searchActive && overGui)) scroll += amount * 20;

        moduleComponents.forEach(moduleComponent -> {
            if (shouldRenderComponent(moduleComponent)) moduleComponent.mouseScrolled(mouseX, mouseY, amount);
        });
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        moduleComponents.forEach(moduleComponent -> {
            if (shouldRenderComponent(moduleComponent)) moduleComponent.keyPressed(keyCode, scanCode, modifiers);
        });
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        moduleComponents.forEach(moduleComponent -> {
            if (shouldRenderComponent(moduleComponent)) moduleComponent.charTyped(chr, modifiers);
        });
        return super.charTyped(chr, modifiers);
    }

    private void drawCategoryTab(DrawContext context, MatrixStack matrix) {
        boolean selected = MenuScreen.INSTANCE.getCategory().equals(category) && !MenuScreen.INSTANCE.isCosmeticsOpen();
        alphaAnimation.setDirection(selected ? Direction.FORWARDS : Direction.BACKWARDS);

        float anim = alphaAnimation.getOutput().floatValue();
        int selectColor = selected ? ColorAssist.getClientColor() : ColorAssist.getText();

        if (anim != 0) {
            int outlineColor = selected ? ColorAssist.getClientColor() : ColorAssist.getClientColor(anim);
            int bgColor = selected ? ColorAssist.applyOpacity(ColorAssist.getClientColor(), 40)
                    : ColorAssist.getGuiRectColor2(anim / 2);

            rectangle.render(ShapeProperties.create(matrix, x, y, width, height)
                    .round(2.25F).thickness(2).outlineColor(outlineColor).color(bgColor).build());
        }

        String icon = switch (category) {
            case COMBAT -> "⚔";
            case MOVEMENT -> "✦";
            case PLAYER -> "●";
            case RENDER -> "◉";
            case MISC -> "⚙";
            case THEMES -> "◇";
        };
        Fonts.getSize(14, Fonts.Type.ICONS2).drawString(matrix, icon, x + 7, y + 5, selectColor);
        Fonts.getSize(14, Fonts.Type.BOLD).drawString(matrix, category.getReadableName(), (int) (x + 22), y + 5, selectColor);
    }

    private int[] calculateOffsets() {
        int[] offsets = new int[2];
        int column = 0;
        for (int i = moduleComponents.size() - 1; i >= 0; i--) {
            ModuleComponent component = moduleComponents.get(i);
            if (shouldRenderComponent(component)) {
                offsets[column] += component.getComponentHeight() + 9;
                column = (column + 1) % 2;
            }
        }
        return offsets;
    }

    private boolean shouldRenderComponent(ModuleComponent component) {
        MenuScreen menuScreen = MenuScreen.INSTANCE;
        ModuleCategory moduleCategory = component.getModule().getCategory();
        String text = menuScreen.getSearchComponent().getText().toLowerCase();
        String moduleName = component.getModule().getVisibleName().toLowerCase();
        return text.isEmpty() ? moduleCategory.equals(menuScreen.getCategory()) : moduleName.contains(text);
    }
}