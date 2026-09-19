package com.essde342.triggerbot.mixin;

import com.essde342.triggerbot.TriggerBotOptimizer;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @ModifyVariable(
            method = "render",
            at = @At("HEAD"),
            argsOnly = true,
            index = 2
    )
    private long triggerBot$limitChunkUpdateBudget(long limitTime) {
        if (!TriggerBotOptimizer.isRendererOptimizationActive()) {
            return limitTime;
        }

        long now = System.nanoTime();
        long budget = TriggerBotOptimizer.getChunkUpdateBudgetNanos();
        long hardLimit = now + budget;

        return Math.min(limitTime, hardLimit);
    }
}
