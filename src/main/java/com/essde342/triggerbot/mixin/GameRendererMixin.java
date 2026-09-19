package com.essde342.triggerbot.mixin;

import com.essde342.triggerbot.TriggerBotClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
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