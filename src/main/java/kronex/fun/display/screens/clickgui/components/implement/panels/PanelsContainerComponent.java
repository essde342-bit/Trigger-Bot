package kronex.fun.display.screens.clickgui.components.implement.panels;

import kronex.fun.display.screens.clickgui.components.AbstractComponent;
import kronex.fun.features.module.ModuleCategory;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class PanelsContainerComponent extends AbstractComponent {
    private static final float DEFAULT_PANEL_WIDTH = 120f;
    private static final float DEFAULT_PANEL_HEIGHT = 268f;
    private static final float DEFAULT_PANEL_GAP = 8f;
    private static final float TOP_PADDING = 8f;
    private final List<CategoryPanelComponent> panels = new ArrayList<>();

    public PanelsContainerComponent() {
        for (ModuleCategory category : EnumSet.of(ModuleCategory.COMBAT, ModuleCategory.MOVEMENT, ModuleCategory.RENDER, ModuleCategory.PLAYER, ModuleCategory.MISC)) {
            panels.add(new CategoryPanelComponent(category));
        }
    }

    @Override public void render(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta) {
        float totalWidth = panels.size() * DEFAULT_PANEL_WIDTH + (panels.size() - 1) * DEFAULT_PANEL_GAP;
        float startX = x + (width - totalWidth) / 2f;
        float startY = y + TOP_PADDING;
        for (int i=0;i<panels.size();i++) {
            CategoryPanelComponent panel=panels.get(i);
            panel.position(startX+i*(DEFAULT_PANEL_WIDTH+DEFAULT_PANEL_GAP),startY).size(DEFAULT_PANEL_WIDTH,DEFAULT_PANEL_HEIGHT);
            panel.render(context,mouseX,mouseY,delta);
        }
    }
    @Override public boolean mouseClicked(double mouseX,double mouseY,int button){for(CategoryPanelComponent p:panels)if(p.mouseClicked(mouseX,mouseY,button))return true;return false;}
    @Override public boolean mouseReleased(double mouseX,double mouseY,int button){for(CategoryPanelComponent p:panels)if(p.mouseReleased(mouseX,mouseY,button))return true;return false;}
    @Override public boolean mouseScrolled(double mouseX,double mouseY,double amount){for(CategoryPanelComponent p:panels)if(p.mouseScrolled(mouseX,mouseY,amount))return true;return false;}
    @Override public boolean keyPressed(int keyCode,int scanCode,int modifiers){for(CategoryPanelComponent p:panels)if(p.keyPressed(keyCode,scanCode,modifiers))return true;return false;}
    @Override public boolean charTyped(char chr,int modifiers){for(CategoryPanelComponent p:panels)if(p.charTyped(chr,modifiers))return true;return false;}
}
