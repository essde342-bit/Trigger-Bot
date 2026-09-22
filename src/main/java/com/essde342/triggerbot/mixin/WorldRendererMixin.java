package com.essde342.triggerbot.mixin;

import com.essde342.triggerbot.TriggerBotOptimizer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.Camera;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Inject(method = "updateChunks", at = @At("HEAD"))
    private void triggerBot$optimizeChunkUpdates(Camera camera, CallbackInfo ci) {
        // Minecraft 1.21.4 moved the chunk-update time budget into WorldRenderer
        // internals. Keep the hook so the optimizer remains compatible while the
        // actual budget is exposed through TriggerBotOptimizer.
        if (TriggerBotOptimizer.isRendererOptimizationActive()) {
            TriggerBotOptimizer.getChunkUpdateBudgetNanos();
        }
    }
}
