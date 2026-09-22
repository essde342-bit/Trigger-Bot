package kronex.fun.display.screens.clickgui.components.implement.window.implement.module;

import lombok.RequiredArgsConstructor;
import kronex.fun.features.module.Module;
import kronex.fun.display.screens.clickgui.components.implement.window.implement.AbstractBindWindow;

@RequiredArgsConstructor
public class ModuleBindWindow extends AbstractBindWindow {
    private final Module module;

    @Override protected int getKey() { return module.getKey(); }
    @Override protected void setKey(int key) { module.setKey(key); }
    @Override protected int getType() { return module.getType(); }
    @Override protected void setType(int type) { module.setType(type); }
    @Override protected String getTitle() { return "Binding module  " + module.getVisibleName(); }
}