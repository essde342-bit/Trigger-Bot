package com.essde342.triggerbot.ui.modules;

public enum Category {
    COMBAT("Combat"),
    MOVEMENT("Movement"),
    PLAYER("Player"),
    RENDER("Render"),
    MISC("Misc");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
