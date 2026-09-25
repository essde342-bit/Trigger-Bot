package com.essde342.triggerbot.ui.modules;

import com.essde342.triggerbot.AltManagerScreen;
import com.essde342.triggerbot.TriggerBotClient;
import com.essde342.triggerbot.TriggerBotConfig;
import com.essde342.triggerbot.ui.imple.BooleanSetting;
import com.essde342.triggerbot.ui.imple.NumberSetting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public final class ModuleManager {
    private static final List<Module> MODULES = new ArrayList<Module>();
    private static final Map<Module, Boolean> KEY_STATES = new HashMap<Module, Boolean>();
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
                "Target ESP",
                Category.VISUALS,
                "Visible glow around the selected target.",
                c().targetEsp,
                value -> c().targetEsp = value
        ));

        link(new Module(
                "Jump Circle",
                Category.VISUALS,
                "Animated circle when jumping and landing.",
                c().jumpCircle,
                value -> c().jumpCircle = value
        ));

        Module fullbright = new Module(
                "Fullbright",
                Category.VISUALS,
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
                0.0D,
                1.0D,
                0.05D,
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
                Category.VISUALS,
                "Disables damage camera shake.",
                c().noHurtCam,
                value -> c().noHurtCam = value
        ));

        Module aspect = new Module(
                "Aspect Ratio",
                Category.VISUALS,
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
                Category.OTHER,
                "Mobile performance profile.",
                c().optimization,
                value -> c().optimization = value
        ));

        Module fps = new Module(
                "Adaptive FPS",
                Category.OTHER,
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
                Category.OTHER,
                "Opens the local nickname manager.",
                false,
                value -> {}
        ));

        applyConfiguredBinds();
    }

    public static void link(Module module) {
        MODULES.add(module);
    }

    public static List<Module> getModules() {
        return MODULES;
    }

    public static boolean isInitialized() {
        return initialized;
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

    public static void handleKeyBinds(MinecraftClient client) {
        if (client == null || client.getWindow() == null) {
            return;
        }

        long handle = client.getWindow().getHandle();
        boolean allowToggle = client.currentScreen == null;

        for (Module module : MODULES) {
            int bind = module.getBind();
            boolean down;
            if (bind >= GLFW.GLFW_MOUSE_BUTTON_1 && bind <= GLFW.GLFW_MOUSE_BUTTON_LAST) {
                down = GLFW.glfwGetMouseButton(handle, bind) == GLFW.GLFW_PRESS;
            } else {
                down = bind >= 0 && GLFW.glfwGetKey(handle, bind) == GLFW.GLFW_PRESS;
            }
            boolean previous = KEY_STATES.containsKey(module)
                    && Boolean.TRUE.equals(KEY_STATES.get(module));

            if (allowToggle && down && !previous) {
                if (!"Alt Manager".equals(module.getName())) {
                    module.toggled();
                    TriggerBotClient.saveConfig();
                }
            }

            KEY_STATES.put(module, down);
        }
    }

    public static void applyConfiguredBinds() {
        setBind("TriggerBot", c().bindTriggerBot);
        setBind("Aim Assist", c().bindAimAssist);
        setBind("Target ESP", c().bindTargetEsp);
        setBind("Jump Circle", c().bindJumpCircle);
        setBind("Fullbright", c().bindFullbright);
        setBind("No Hurt Cam", c().bindNoHurtCam);
        setBind("Aspect Ratio", c().bindAspectRatio);
        setBind("Optimization", c().bindOptimization);
        setBind("Adaptive FPS", c().bindAdaptiveOptimization);
    }

    public static void syncBindsToConfig() {
        c().bindTriggerBot = getBind("TriggerBot");
        c().bindAimAssist = getBind("Aim Assist");
        c().bindTargetEsp = getBind("Target ESP");
        c().bindJumpCircle = getBind("Jump Circle");
        c().bindFullbright = getBind("Fullbright");
        c().bindNoHurtCam = getBind("No Hurt Cam");
        c().bindAspectRatio = getBind("Aspect Ratio");
        c().bindOptimization = getBind("Optimization");
        c().bindAdaptiveOptimization = getBind("Adaptive FPS");
    }

    private static int getBind(String name) {
        for (Module module : MODULES) {
            if (module.getName().equals(name)) {
                return module.getBind();
            }
        }
        return -1;
    }

    private static void setBind(String name, int bind) {
        for (Module module : MODULES) {
            if (module.getName().equals(name)) {
                module.setBind(bind);
                return;
            }
        }
    }
}
