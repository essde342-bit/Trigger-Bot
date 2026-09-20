package com.essde342.triggerbot.ui.modules;

import com.essde342.triggerbot.TriggerBotClient;
import com.essde342.triggerbot.TriggerBotConfig;
import com.essde342.triggerbot.ui.imple.BooleanSetting;
import com.essde342.triggerbot.ui.imple.NumberSetting;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.MinecraftClient;

public final class ModuleManager {
    private static final List<Module> MODULES = new ArrayList<Module>();
    private static boolean initialized;

    private ModuleManager() {}

    private static TriggerBotConfig c() {
        return TriggerBotClient.CONFIG;
    }

    public static void moduleRegister() {
        if (initialized) return;
        initialized = true;

        Module trigger = new Module("TriggerBot", Category.COMBAT,
                "Attack players under the crosshair.", c().enabled,
                value -> c().enabled = value);
        trigger.addSetting(new BooleanSetting("Only Crits", c().onlyCrits, v -> c().onlyCrits = v));
        trigger.addSetting(new BooleanSetting("Smart Crits", c().smartCrits, v -> c().smartCrits = v));
        trigger.addSetting(new BooleanSetting("Only Weapon", c().onlyWeapon, v -> c().onlyWeapon = v));
        trigger.addSetting(new NumberSetting("Attack Delay", c().triggerDelayMs, 0, 500, 25,
                v -> c().triggerDelayMs = (int) Math.round(v)));
        trigger.addSetting(new NumberSetting("Target Range", c().triggerRange, 2, 6, .5,
                v -> c().triggerRange = v));
        link(trigger);

        Module aim = new Module("Aim Assist", Category.COMBAT,
                "Rotates toward the nearest visible player.", c().aimAssist,
                value -> c().aimAssist = value);
        aim.addSetting(new NumberSetting("Aim Time", c().aimAssistDurationMs, 100, 1500, 50,
                v -> c().aimAssistDurationMs = (int) Math.round(v)));
        aim.addSetting(new NumberSetting("Target Range", c().aimAssistRange, 2, 6, .5,
                v -> c().aimAssistRange = v));
        link(aim);

        link(new Module("Lightning ESP", Category.RENDER,
                "Animated lightning around the current target.", c().lightningEsp,
                value -> c().lightningEsp = value));

        link(new Module("Target ESP", Category.RENDER,
                "Outline the current target.", c().targetEsp,
                value -> c().targetEsp = value));

        Module fullbright = new Module("Fullbright", Category.RENDER,
                "Raises gamma in dark areas.", c().fullbright,
                value -> {
                    c().fullbright = value;
                    TriggerBotClient.setFullbright(MinecraftClient.getInstance(), value);
                });
        fullbright.addSetting(new NumberSetting("Gamma", c().fullbrightGamma, 1, 20, 1,
                v -> {
                    c().fullbrightGamma = v;
                    if (c().fullbright) {
                        TriggerBotClient.setFullbright(MinecraftClient.getInstance(), true);
                    }
                }));
        link(fullbright);

        link(new Module("No Hurt Cam", Category.RENDER,
                "Disable damage camera shake.", c().noHurtCam,
                value -> c().noHurtCam = value));

        Module aspect = new Module("Aspect Ratio", Category.RENDER,
                "Change the camera projection ratio.", true, value -> {});
        aspect.addSetting(new NumberSetting("Ratio", c().aspectRatio, .5, 3, .01,
                v -> c().aspectRatio = v));
        link(aspect);

        link(new Module("Optimization", Category.MOVEMENT,
                "Mobile performance profile.", c().optimization,
                value -> c().optimization = value));

        Module fps = new Module("Adaptive FPS", Category.MOVEMENT,
                "Adaptive FPS target.", c().adaptiveOptimization,
                value -> c().adaptiveOptimization = value);
        fps.addSetting(new NumberSetting("Target FPS", c().targetFps, 30, 60, 5,
                v -> c().targetFps = (int) Math.round(v)));
        link(fps);

        link(new Module("Alt Manager", Category.PLAYER,
                "Local nickname manager.", false, value -> {}));
    }

    public static void link(Module module) {
        MODULES.add(module);
    }

    public static List<Module> getByCategory(Category category) {
        List<Module> result = new ArrayList<Module>();
        for (Module module : MODULES) {
            if (module.getCategory() == category) result.add(module);
        }
        return result;
    }
}
