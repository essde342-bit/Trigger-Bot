package kronex.fun.display.screens.clickgui.components.implement.module;

import kronex.fun.features.module.Module;

public final class ModuleDescriptions {
    private ModuleDescriptions(){}
    public static String getDescription(Module module){
        if(module==null)return "";
        String name=module.getVisibleName();
        return switch(name){
            case "TriggerBot" -> "Hits a target when the crosshair is over it.";
            case "Aim Assist" -> "Assists camera movement toward a target.";
            case "Target ESP" -> "Highlights the selected target.";
            case "Jump Circle" -> "Draws a ring when jumping.";
            default -> name;
        };
    }
}
