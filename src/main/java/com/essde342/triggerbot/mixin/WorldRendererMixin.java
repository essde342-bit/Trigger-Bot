package com.essde342.triggerbot.mixin;

import com.essde342.triggerbot.TriggerBotOptimizer;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @ModifyVariable(
            method = "updateChunks",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private long triggerBot$limitChunkUpdateBudget(long limitTime) {
        if (!TriggerBotOptimizer.isRendererOptimizationActive()) {
            return limitTime;
        }

        long budget = TriggerBotOptimizer.getChunkUpdateBudgetNanos();
        long hardLimit = System.nanoTime() + budget;
        return Math.min(limitTime, hardLimit);
    }
}
