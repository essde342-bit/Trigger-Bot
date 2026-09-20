package com.essde342.triggerbot.ui.modules;

import com.essde342.triggerbot.ui.ISetting;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Module {
    private final String name;
    private final String desc;
    private final Category category;
    private int bind = -1;
    private boolean enabled;
    private final Consumer<Boolean> changeCallback;
    private final List<ISetting> settings = new ArrayList<>();

    public Module(String name, Category category, String desc, boolean enabled, Consumer<Boolean> callback) {
        this.name = name;
        this.category = category;
        this.desc = desc;
        this.enabled = enabled;
        this.changeCallback = callback;
    }

    public List<ISetting> getSettings() {
        return settings;
    }

    public void addSetting(ISetting setting) {
        settings.add(setting);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean value) {
        if (enabled == value) {
            return;
        }
        enabled = value;
        if (changeCallback != null) {
            changeCallback.accept(value);
        }
    }

    public void toggled() {
        setEnabled(!enabled);
    }

    public String getName() {
        return name;
    }

    public Category getCategory() {
        return category;
    }

    public String getDesc() {
        return desc;
    }

    public int getBind() {
        return bind;
    }

    public void setBind(int bind) {
        this.bind = bind;
    }
}
