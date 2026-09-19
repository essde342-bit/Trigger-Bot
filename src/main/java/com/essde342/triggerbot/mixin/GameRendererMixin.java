package com.essde342.triggerbot.mixin;

import com.essde342.triggerbot.TriggerBotClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(
            method = "bobViewWhenHurt(Lnet/minecraft/client/util/math/MatrixStack;F)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void triggerBot$disableHurtCamera(
            MatrixStack matrices,
            float tickDelta,
            CallbackInfo info
    ) {
        if (TriggerBotClient.CONFIG.noHurtCam) {
            info.cancel();
        }
    }
}
