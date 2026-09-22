package kronex.fun.features.module;

public enum ModuleCategory {
    COMBAT("Combat", "combat"),
    MOVEMENT("Movement", "movement"),
    PLAYER("Player", "player"),
    RENDER("Render", "render"),
    MISC("Misc", "misc"),
    THEMES("Themes", "themes");

    private final String readableName;
    private final String textureName;

    ModuleCategory(String readableName, String textureName) {
        this.readableName = readableName;
        this.textureName = textureName;
    }

    public String getReadableName() {
        return readableName;
    }

    public String getTextureName() {
        return textureName;
    }
}