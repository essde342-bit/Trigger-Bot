package com.essde342.triggerbot.mixin;

import com.essde342.triggerbot.TriggerBotOptimizer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private <T extends Entity> void triggerBot$distanceCull(
            T entity,
            Frustum frustum,
            double x,
            double y,
            double z,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!TriggerBotOptimizer.isRendererOptimizationActive()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity localPlayer = client.player;

        if (localPlayer == null || entity == null || entity == localPlayer) {
            return;
        }

        // Keep players visible farther away so TriggerBot/gameplay still feels normal.
        double distance = entity.squaredDistanceTo(localPlayer);

        double limit;
        if (entity instanceof PlayerEntity) {
            limit = TriggerBotOptimizer.getEntityRenderDistance() + 16.0D;
        } else if (entity instanceof LivingEntity) {
            limit = TriggerBotOptimizer.getEntityRenderDistance();
        } else {
            limit = TriggerBotOptimizer.getEntityRenderDistance() - 4.0D;
        }

        if (distance > limit * limit) {
            cir.setReturnValue(false);
        }
    }
}
