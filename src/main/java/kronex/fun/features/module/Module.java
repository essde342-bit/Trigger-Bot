package kronex.fun.features.module;

import com.essde342.triggerbot.AltManagerScreen;
import com.essde342.triggerbot.TriggerBotClient;
import com.essde342.triggerbot.ui.ISetting;
import com.essde342.triggerbot.ui.imple.NumberSetting;
import com.essde342.triggerbot.ui.modules.ModuleManager;
import kronex.fun.display.screens.clickgui.MenuScreen;
import kronex.fun.features.module.setting.Setting;
import kronex.fun.features.module.setting.implement.BooleanSetting;
import kronex.fun.features.module.setting.implement.SliderSettings;

import java.util.ArrayList;
import java.util.List;

public final class Module {
    private final com.essde342.triggerbot.ui.modules.Module backend;
    private final ModuleCategory category;
    private final List<Setting> settings = new ArrayList<>();

    public Module(com.essde342.triggerbot.ui.modules.Module backend, ModuleCategory category) {
        this.backend = backend;
        this.category = category;

        for (ISetting setting : backend.getSettings()) {
            if (setting instanceof com.essde342.triggerbot.ui.imple.BooleanSetting booleanSetting) {
                settings.add(new BooleanSetting(setting.getName(), booleanSetting));
            } else if (setting instanceof NumberSetting numberSetting) {
                settings.add(new SliderSettings(
                        setting.getName(),
                        numberSetting,
                        numberSetting.getMin(),
                        numberSetting.getMax(),
                        numberSetting.getIncrement()
                ));
            }
        }
    }

    public String getVisibleName() {
        return backend.getName();
    }

    public ModuleCategory getCategory() {
        return category;
    }

    public boolean isState() {
        return backend.isEnabled();
    }

    public void switchState() {
        if ("Alt Manager".equals(backend.getName())) {
            var client = net.minecraft.client.MinecraftClient.getInstance();
            client.setScreen(AltManagerScreen.create(MenuScreen.INSTANCE));
            return;
        }

        backend.toggled();
        TriggerBotClient.saveConfig();
    }

    public List<Setting> settings() {
        return settings;
    }

    public void addSetting(Setting setting) {
        settings.add(setting);
    }

    public int getKey() {
        return backend.getBind();
    }

    public void setKey(int key) {
        backend.setBind(key);
        ModuleManager.syncBindsToConfig();
        TriggerBotClient.saveConfig();
    }

    public int getType() {
        return 0;
    }

    public void setType(int type) {
    }
}