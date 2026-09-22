package kronex.fun.features.module.setting.implement;

import com.essde342.triggerbot.ui.imple.NumberSetting;
import kronex.fun.features.module.setting.Setting;

public final class SliderSettings extends Setting {
    private final NumberSetting backend;
    private final double min;
    private final double max;
    private final double step;

    public SliderSettings(String name, NumberSetting backend, double min, double max, double step) {
        super(name, "");
        this.backend = backend;
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public SliderSettings(String name, String description, NumberSetting backend, double min, double max, double step) {
        super(name, description);
        this.backend = backend;
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public double getValue() {
        return backend.getDoubleValue();
    }

    public void setValue(double value) {
        backend.setValue(value);
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getStep() {
        return step;
    }

    public boolean isInteger() {
        return Math.abs(step - Math.rint(step)) < 1.0E-9;
    }
}