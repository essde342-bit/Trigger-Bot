package com.essde342.triggerbot.mixin;

import com.essde342.triggerbot.TriggerBotClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "getBasicProjectionMatrix", at = @At("RETURN"), cancellable = true)
    private void triggerBot$applyAspectRatio(
            float fovDegrees,
            CallbackInfoReturnable<Matrix4f> cir
    ) {
        if (TriggerBotClient.isGl4esRenderer() || !TriggerBotClient.CONFIG.aspectRatioEnabled) {
            return;
        }

        float desiredAspect = (float) TriggerBotClient.CONFIG.aspectRatio;
        if (desiredAspect <= 0.0F) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getWindow() == null) {
            return;
        }

        int width = client.getWindow().getFramebufferWidth();
        int height = client.getWindow().getFramebufferHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        float actualAspect = (float) width / (float) height;
        float correction = actualAspect / desiredAspect;

        Matrix4f projection = cir.getReturnValue();
        projection.scale(correction, 1.0F, 1.0F);
        cir.setReturnValue(projection);
    }

    @Inject(
            method = "tiltViewWhenHurt(Lnet/minecraft/client/util/math/MatrixStack;F)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void triggerBot$disableHurtTilt(
            MatrixStack matrices,
            float tickDelta,
            CallbackInfo info
    ) {
        if (!TriggerBotClient.isGl4esRenderer() && TriggerBotClient.isNoHurtCamEnabled()) {
            info.cancel();
        }
    }

    @Inject(
            method = "bobView(Lnet/minecraft/client/util/math/MatrixStack;F)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void triggerBot$disableHurtBob(
            MatrixStack matrices,
            float tickDelta,
            CallbackInfo info
    ) {
        if (TriggerBotClient.isNoHurtCamEnabled()) {
            info.cancel();
        }
    }
}
