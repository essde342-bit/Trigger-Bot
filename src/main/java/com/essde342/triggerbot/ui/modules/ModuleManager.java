package com.essde342.triggerbot.ui.modules;

import com.essde342.triggerbot.TriggerBotClient;
import com.essde342.triggerbot.TriggerBotConfig;
import com.essde342.triggerbot.ui.imple.BooleanSetting;
import com.essde342.triggerbot.ui.imple.MultiSetting;
import com.essde342.triggerbot.ui.imple.NumberSetting;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.MinecraftClient;

public final class ModuleManager {
    private static final List<Module> MODULES = new ArrayList<>();
    private static boolean initialized;

    private ModuleManager() {
    }

    private static TriggerBotConfig config() {
        return TriggerBotClient.CONFIG;
    }

    public static void moduleRegister() {
        if (initialized) {
            return;
        }
        initialized = true;

        Module triggerBot = new Module(
                "TriggerBot",
                Category.COMBAT,
                "Automatic attack on the player under your crosshair.",
                config().enabled,
                value -> config().enabled = value
        );
        triggerBot.addSetting(new BooleanSetting(
                "Only Crits",
                config().onlyCrits,
                value -> config().onlyCrits = value
        ));
        triggerBot.addSetting(new BooleanSetting(
                "Smart Crits",
                config().smartCrits,
                value -> config().smartCrits = value
        ));
        triggerBot.addSetting(new BooleanSetting(
                "Only Weapon",
                config().onlyWeapon,
                value -> config().onlyWeapon = value
        ));
        link(triggerBot);

        link(new Module(
                "Aim Assist",
                Category.COMBAT,
                "Smoothly rotates toward the nearest visible player.",
                config().aimAssist,
                value -> config().aimAssist = value
        ));

        link(new Module(
                "Lightning ESP",
                Category.COMBAT,
                "Renders a visual lightning effect around the current target.",
                config().lightningEsp,
                value -> config().lightningEsp = value
        ));

        Module fullbright = new Module(
                "Fullbright",
                Category.RENDER,
                "Raises gamma so dark areas are easier to see.",
                config().fullbright,
                value -> {
                    config().fullbright = value;
                    TriggerBotClient.setFullbright(MinecraftClient.getInstance(), value);
                }
        );
        fullbright.addSetting(new NumberSetting(
                "Gamma",
                config().fullbrightGamma,
                1.0D,
                20.0D,
                1.0D,
                value -> {
                    config().fullbrightGamma = value;
                    if (config().fullbright) {
                        TriggerBotClient.setFullbright(MinecraftClient.getInstance(), true);
                    }
                }
        ));
        link(fullbright);

        link(new Module(
                "No Hurt Cam",
                Category.RENDER,
                "Removes camera shake after taking damage.",
                config().noHurtCam,
                value -> config().noHurtCam = value
        ));

        Module aspect = new Module(
                "Aspect Ratio",
                Category.RENDER,
                "Changes the camera projection ratio.",
                true,
                value -> {
                }
        );
        aspect.addSetting(new NumberSetting(
                "Ratio",
                config().aspectRatio,
                0.50D,
                3.00D,
                0.01D,
                value -> config().aspectRatio = value
        ));
        link(aspect);

        link(new Module(
                "Optimization",
                Category.MOVEMENT,
                "Applies the mobile performance profile.",
                config().optimization,
                value -> config().optimization = value
        ));

        Module adaptiveFps = new Module(
                "Adaptive FPS",
                Category.MOVEMENT,
                "Adjusts the target FPS according to the selected profile.",
                config().adaptiveOptimization,
                value -> config().adaptiveOptimization = value
        );
        adaptiveFps.addSetting(new NumberSetting(
                "Target FPS",
                config().targetFps,
                30.0D,
                60.0D,
                5.0D,
                value -> config().targetFps = (int) Math.round(value)
        ));
        link(adaptiveFps);

        link(new Module(
                "Alt Manager",
                Category.PLAYER,
                "Manage local nicknames saved by Astra Client.",
                false,
                value -> {
                }
        ));
    }

    public static void link(Module module) {
        MODULES.add(module);
    }

    public static List<Module> getByCategory(Category category) {
        List<Module> result = new ArrayList<>();
        for (Module module : MODULES) {
            if (module.getCategory() == category) {
                result.add(module);
            }
        }
        return result;
    }
}
