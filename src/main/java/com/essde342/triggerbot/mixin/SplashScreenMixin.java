package com.essde342.triggerbot.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.SplashScreen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SplashScreen.class)
public abstract class SplashScreenMixin {
    private static final Identifier ASTRA_BACKGROUND =
            new Identifier("triggerbot", "textures/gui/astra_loading.jpg");

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void triggerBot$renderAstraLoading(
            MatrixStack matrices,
            int mouseX,
            int mouseY,
            float delta,
            CallbackInfo info
    ) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client == null || client.getWindow() == null) {
            return;
        }

        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();

        if (width <= 0 || height <= 0) {
            return;
        }

        info.cancel();

        client.getTextureManager().bindTexture(ASTRA_BACKGROUND);

        RenderSystem.enableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        float imageWidth = 1536.0F;
        float imageHeight = 864.0F;
        float scale = Math.max(width / imageWidth, height / imageHeight);
        float drawWidth = imageWidth * scale;
        float drawHeight = imageHeight * scale;
        float x = (width - drawWidth) * 0.5F;
        float y = (height - drawHeight) * 0.5F;

        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE);

        buffer.vertex(matrices.peek().getModel(), x, y + drawHeight, 0.0F)
                .texture(0.0F, 1.0F).next();
        buffer.vertex(matrices.peek().getModel(), x + drawWidth, y + drawHeight, 0.0F)
                .texture(1.0F, 1.0F).next();
        buffer.vertex(matrices.peek().getModel(), x + drawWidth, y, 0.0F)
                .texture(1.0F, 0.0F).next();
        buffer.vertex(matrices.peek().getModel(), x, y, 0.0F)
                .texture(0.0F, 0.0F).next();

        Tessellator.getInstance().draw();

        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
}
