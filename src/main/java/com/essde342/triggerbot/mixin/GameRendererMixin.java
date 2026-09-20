package com.essde342.triggerbot.mixin;

import com.essde342.triggerbot.TriggerBotClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.client.util.Window;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Minimal Mixin bridge for the custom No Hurt Cam feature.
 *
 * The feature itself lives in TriggerBotClient; this bridge only cancels
 * vanilla GameRenderer#bobViewWhenHurt when the setting is enabled.
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "getBasicProjectionMatrix", at = @At("RETURN"), cancellable = true)
    private void triggerBot$applyAspectRatio(
            net.minecraft.client.render.Camera camera,
            float tickDelta,
            boolean changingFov,
            org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Matrix4f> cir
    ) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getWindow() == null) {
            return;
        }

        Window window = client.getWindow();
        int width = window.getFramebufferWidth();
        int height = window.getFramebufferHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        float actualAspect = (float) width / (float) height;
        float desiredAspect = (float) TriggerBotClient.CONFIG.aspectRatio;
        if (desiredAspect <= 0.0F) {
            return;
        }

        float correction = actualAspect / desiredAspect;
        Matrix4f projection = cir.getReturnValue();
        projection.multiply(Matrix4f.scale(correction, 1.0F, 1.0F));
        cir.setReturnValue(projection);
    }


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
        if (TriggerBotClient.isNoHurtCamEnabled()) {
            info.cancel();
        }
    }
}