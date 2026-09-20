package com.essde342.triggerbot.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.SplashScreen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SplashScreen.class)
public abstract class SplashScreenMixin {
    private static final long START_TIME = System.currentTimeMillis();

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

        float centerX = width * 0.5F;
        float centerY = height * 0.5F;

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Base background: almost black with a deep purple lower edge.
        RenderSystem.disableTexture();
        drawGradientRect(matrices, 0.0F, 0.0F, width, height,
                7, 5, 11, 255,
                24, 8, 38, 255);

        long elapsed = System.currentTimeMillis() - START_TIME;
        float pulse = 0.5F + 0.5F * (float) Math.sin(elapsed * 0.0025F);
        float sweep = (elapsed % 3600L) / 3600.0F;

        // Soft purple ambient glow without requiring an external texture.
        drawGlow(matrices, centerX, centerY - height * 0.08F,
                width * 0.48F, 24, 8, 38, 48 + (int) (pulse * 18.0F));

        drawGlow(matrices, centerX + (float) Math.sin(sweep * Math.PI * 2.0F) * width * 0.28F,
                centerY + height * 0.22F,
                width * 0.28F, 118, 54, 220, 30 + (int) (pulse * 22.0F));

        drawGlow(matrices, centerX - width * 0.30F,
                centerY - height * 0.28F,
                width * 0.24F, 86, 34, 170, 18);

        // Thin center accent line.
        drawColorRect(matrices,
                centerX - width * 0.22F,
                centerY + height * 0.115F,
                centerX + width * 0.22F,
                centerY + height * 0.118F,
                124, 58, 255, 115);

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();

        // Logo / title.
        matrices.push();
        float titleScale = Math.max(1.0F, Math.min(1.85F, width / 620.0F));
        matrices.translate(centerX, centerY - height * 0.065F, 0.0F);
        matrices.scale(titleScale, titleScale, 1.0F);

        String title = "ASTRA CLIENT";
        int titleWidth = client.textRenderer.getWidth(title);
        client.textRenderer.drawWithShadow(
                matrices,
                title,
                -titleWidth / 2.0F,
                -8.0F,
                0xFFF5F1FF
        );
        matrices.pop();

        String version = "1.0.0";
        int versionWidth = client.textRenderer.getWidth(version);
        client.textRenderer.drawWithShadow(
                matrices,
                version,
                centerX - versionWidth / 2.0F,
                centerY + height * 0.025F,
                0xFFB88CFF
        );

        String loading = "LOADING";
        int loadingWidth = client.textRenderer.getWidth(loading);
        client.textRenderer.drawWithShadow(
                matrices,
                loading,
                centerX - loadingWidth / 2.0F,
                centerY + height * 0.155F,
                0xFFE6D9FF
        );

        // Animated loading bar.
        float barWidth = Math.min(width * 0.58F, 420.0F);
        float barHeight = 5.0F;
        float barX = centerX - barWidth * 0.5F;
        float barY = centerY + height * 0.20F;

        RenderSystem.disableTexture();
        RenderSystem.enableBlend();

        drawColorRect(matrices,
                barX,
                barY,
                barX + barWidth,
                barY + barHeight,
                34, 22, 46, 230);

        float progress = 0.18F + 0.82F * ((float) Math.sin(elapsed * 0.0017F) * 0.5F + 0.5F);
        float filled = Math.max(8.0F, barWidth * progress);

        drawGradientRect(matrices,
                barX,
                barY,
                barX + filled,
                barY + barHeight,
                92, 35, 185, 255,
                182, 92, 255, 255);

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    private static void drawGradientRect(
            MatrixStack matrices,
            float left,
            float top,
            float right,
            float bottom,
            int topR,
            int topG,
            int topB,
            int topA,
            int bottomR,
            int bottomG,
            int bottomB,
            int bottomA
    ) {
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);

        buffer.vertex(matrices.peek().getModel(), left, bottom, 0.0F)
                .color(bottomR, bottomG, bottomB, bottomA).next();
        buffer.vertex(matrices.peek().getModel(), right, bottom, 0.0F)
                .color(bottomR, bottomG, bottomB, bottomA).next();
        buffer.vertex(matrices.peek().getModel(), right, top, 0.0F)
                .color(topR, topG, topB, topA).next();
        buffer.vertex(matrices.peek().getModel(), left, top, 0.0F)
                .color(topR, topG, topB, topA).next();

        Tessellator.getInstance().draw();
    }

    private static void drawColorRect(
            MatrixStack matrices,
            float left,
            float top,
            float right,
            float bottom,
            int red,
            int green,
            int blue,
            int alpha
    ) {
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);

        buffer.vertex(matrices.peek().getModel(), left, bottom, 0.0F)
                .color(red, green, blue, alpha).next();
        buffer.vertex(matrices.peek().getModel(), right, bottom, 0.0F)
                .color(red, green, blue, alpha).next();
        buffer.vertex(matrices.peek().getModel(), right, top, 0.0F)
                .color(red, green, blue, alpha).next();
        buffer.vertex(matrices.peek().getModel(), left, top, 0.0F)
                .color(red, green, blue, alpha).next();

        Tessellator.getInstance().draw();
    }

    private static void drawGlow(
            MatrixStack matrices,
            float centerX,
            float centerY,
            float size,
            int red,
            int green,
            int blue,
            int alpha
    ) {
        drawColorRect(
                matrices,
                centerX - size,
                centerY - size * 0.24F,
                centerX + size,
                centerY + size * 0.24F,
                red,
                green,
                blue,
                alpha
        );
    }
}
