package kronex.fun;

import com.essde342.triggerbot.ui.modules.ModuleManager;
import kronex.fun.features.module.Module;
import kronex.fun.features.module.ModuleCategory;
import kronex.fun.other.utils.display.scissor.ScissorAssist;
import kronex.fun.other.common.discord.DiscordManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Kronex {
    private static final Kronex INSTANCE = new Kronex();
    private final ScissorAssist scissorManager = new ScissorAssist();
    private final DiscordManager discordManager = new DiscordManager();
    private final ModuleRepository moduleRepository = new ModuleRepository();

    private Kronex() {}

    public static Kronex getInstance() { return INSTANCE; }
    public ScissorAssist getScissorManager() { return scissorManager; }
    public DiscordManager getDiscordManager() { return discordManager; }
    public ModuleRepository getModuleRepository() { return moduleRepository; }

    public final class ModuleRepository {
        private final List<Module> modules = new ArrayList<>();
        private boolean built;

        public List<Module> modules() {
            if (!built) rebuild();
            return Collections.unmodifiableList(modules);
        }

        private void rebuild() {
            modules.clear();
            ModuleManager.moduleRegister();
            for (com.essde342.triggerbot.ui.modules.Module backend : ModuleManager.getModules()) {
                modules.add(new Module(backend, mapCategory(backend.getCategory())));
            }
            built = true;
        }

        private ModuleCategory mapCategory(com.essde342.triggerbot.ui.modules.Category category) {
            return switch (category) {
                case COMBAT -> ModuleCategory.COMBAT;
                case VISUALS -> ModuleCategory.RENDER;
                case OTHER -> ModuleCategory.MISC;
            };
        }
    }
}