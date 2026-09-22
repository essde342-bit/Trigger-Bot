package kronex.fun.features.module.setting;

import java.util.function.Supplier;

public abstract class Setting {
    private final String name;
    private final String description;
    private Supplier<Boolean> visible = () -> true;

    protected Setting(String name, String description) {
        this.name = name;
        this.description = description == null ? "" : description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Supplier<Boolean> getVisible() {
        return visible;
    }

    public void setVisible(Supplier<Boolean> visible) {
        this.visible = visible == null ? () -> true : visible;
    }
}