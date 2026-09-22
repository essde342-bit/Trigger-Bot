package kronex.fun.features.module.setting.implement;

import kronex.fun.features.module.setting.Setting;

public final class BooleanSetting extends Setting {
    private final com.essde342.triggerbot.ui.imple.BooleanSetting backend;

    public BooleanSetting(
            String name,
            com.essde342.triggerbot.ui.imple.BooleanSetting backend
    ) {
        super(name, "");
        this.backend = backend;
    }

    public BooleanSetting(
            String name,
            String description,
            com.essde342.triggerbot.ui.imple.BooleanSetting backend
    ) {
        super(name, description);
        this.backend = backend;
    }

    public boolean isValue() {
        return backend.isEnabled();
    }

    public void setValue(boolean value) {
        backend.setEnabled(value);
    }
}