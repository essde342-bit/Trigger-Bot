package com.essde342.triggerbot.ui.modules;

import com.essde342.triggerbot.AltManagerScreen;
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

        Module trigger = new Module(
                "TriggerBot",
                Category.COMBAT,
                "Automatic attack on a player under the crosshair.",
                c().enabled,
                value -> c().enabled = value
        );
        trigger.addSetting(new BooleanSetting("Only Crits", c().onlyCrits,
                value -> c().onlyCrits = value));
        trigger.addSetting(new BooleanSetting("Smart Crits", c().smartCrits,
                value -> c().smartCrits = value));
        trigger.addSetting(new BooleanSetting("Only Weapon", c().onlyWeapon,
                value -> c().onlyWeapon = value));
        trigger.addSetting(new NumberSetting("Attack Delay", c().triggerDelayMs,
                0.0D, 500.0D, 25.0D,
                value -> c().triggerDelayMs = (int) Math.round(value)));
        trigger.addSetting(new NumberSetting("Target Range", c().triggerRange,
                2.0D, 6.0D, 0.5D,
                value -> c().triggerRange = value));
        link(trigger);

        Module aim = new Module(
                "Aim Assist",
                Category.COMBAT,
                "Smoothly rotates toward the nearest visible player.",
                c().aimAssist,
                value -> c().aimAssist = value
        );
        aim.addSetting(new NumberSetting("Aim Time", c().aimAssistDurationMs,
                100.0D, 1500.0D, 50.0D,
                value -> c().aimAssistDurationMs = (int) Math.round(value)));
        aim.addSetting(new NumberSetting("Target Range", c().aimAssistRange,
                2.0D, 6.0D, 0.5D,
                value -> c().aimAssistRange = value));
        link(aim);

        link(new Module(
                "Lightning ESP",
                Category.RENDER,
                "Animated lightning around the current target.",
                c().lightningEsp,
                value -> c().lightningEsp = value
        ));

        link(new Module(
                "Target ESP",
                Category.RENDER,
                "Clean outline around the current target.",
                c().targetEsp,
                value -> c().targetEsp = value
        ));

        Module fullbright = new Module(
                "Fullbright",
                Category.RENDER,
                "Raises gamma in dark areas.",
                c().fullbright,
                value -> {
                    c().fullbright = value;
                    TriggerBotClient.setFullbright(MinecraftClient.getInstance(), value);
                }
        );
        fullbright.addSetting(new NumberSetting(
                "Gamma",
                c().fullbrightGamma,
                1.0D,
                20.0D,
                1.0D,
                value -> {
                    c().fullbrightGamma = value;
                    if (c().fullbright) {
                        TriggerBotClient.setFullbright(
                                MinecraftClient.getInstance(),
                                true
                        );
                    }
                }
        ));
        link(fullbright);

        link(new Module(
                "No Hurt Cam",
                Category.RENDER,
                "Disables damage camera shake.",
                c().noHurtCam,
                value -> c().noHurtCam = value
        ));

        Module aspect = new Module(
                "Aspect Ratio",
                Category.RENDER,
                "Changes the camera projection ratio.",
                c().aspectRatioEnabled,
                value -> c().aspectRatioEnabled = value
        );
        aspect.addSetting(new NumberSetting(
                "Ratio",
                c().aspectRatio,
                0.50D,
                3.00D,
                0.01D,
                value -> c().aspectRatio = value
        ));
        link(aspect);

        link(new Module(
                "Optimization",
                Category.MOVEMENT,
                "Mobile performance profile.",
                c().optimization,
                value -> c().optimization = value
        ));

        Module fps = new Module(
                "Adaptive FPS",
                Category.MOVEMENT,
                "Adaptive target FPS for the mobile profile.",
                c().adaptiveOptimization,
                value -> c().adaptiveOptimization = value
        );
        fps.addSetting(new NumberSetting(
                "Target FPS",
                c().targetFps,
                30.0D,
                60.0D,
                5.0D,
                value -> c().targetFps = (int) Math.round(value)
        ));
        link(fps);

        link(new Module(
                "Alt Manager",
                Category.PLAYER,
                "Opens the local nickname manager.",
                false,
                value -> {}
        ));
    }

    public static void link(Module module) {
        MODULES.add(module);
    }

    public static List<Module> getByCategory(Category category) {
        List<Module> result = new ArrayList<Module>();

        for (Module module : MODULES) {
            if (module.getCategory() == category) {
                result.add(module);
            }
        }

        return result;
    }
}
