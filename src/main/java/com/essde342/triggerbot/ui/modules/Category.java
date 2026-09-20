package com.essde342.triggerbot.ui.modules;

public enum Category {
    COMBAT("Combat"),
    VISUALS("Visuals"),
    OTHER("Other");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
