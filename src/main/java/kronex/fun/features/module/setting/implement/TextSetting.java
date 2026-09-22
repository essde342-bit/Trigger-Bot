package kronex.fun.features.module.setting.implement;

import kronex.fun.features.module.setting.Setting;

public final class TextSetting extends Setting {
    private String text = "";
    private final int min;
    private final int max;

    public TextSetting(String name, String description, int min, int max) {
        super(name, description);
        this.min = min;
        this.max = max;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text == null ? "" : text;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }
}