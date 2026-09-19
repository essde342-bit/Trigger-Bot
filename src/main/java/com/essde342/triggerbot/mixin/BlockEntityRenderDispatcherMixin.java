package com.essde342.triggerbot.mixin;

import com.essde342.triggerbot.TriggerBotOptimizer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private <E extends BlockEntity> void triggerBot$distanceCull(
            E blockEntity,
            float tickDelta,
            net.minecraft.client.util.math.MatrixStack matrix,
            net.minecraft.client.render.VertexConsumerProvider vertexConsumers,
            CallbackInfo ci
    ) {
        if (!TriggerBotOptimizer.isRendererOptimizationActive() || blockEntity == null) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;

        if (player == null) {
            return;
        }

        if (blockEntity.getPos().getSquaredDistance(player.getX(), player.getY(), player.getZ(), true)
                > TriggerBotOptimizer.getBlockEntityRenderDistance()
                    * TriggerBotOptimizer.getBlockEntityRenderDistance()) {
            ci.cancel();
        }
    }
}
