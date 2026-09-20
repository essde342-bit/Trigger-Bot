package com.essde342.triggerbot;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import org.lwjgl.opengl.GL11;

public final class JumpCircleClient implements ClientModInitializer {
    private static final int SEGMENTS = 64;
    private static final double BASE_RADIUS = 0.62D;
    private static final long EFFECT_LIFETIME_MS = 520L;

    private static boolean airborne;
    private static long jumpStartTime;
    private static long lastLandTime;
    private static PlayerEntity trackedPlayer;
    private static ClientWorld trackedWorld;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(JumpCircleClient::tick);
        WorldRenderEvents.LAST.register(JumpCircleClient::render);
    }

    private static void tick(MinecraftClient client) {
        PlayerEntity player = client.player;
        ClientWorld world = client.world;
        if (player == null || world == null) {
            resetState();
            return;
        }

        if (trackedPlayer != player || trackedWorld != world) {
            trackedPlayer = player;
            trackedWorld = world;
            airborne = false;
            jumpStartTime = 0L;
            lastLandTime = 0L;
        }

        boolean nowAirborne = !player.isOnGround()
                && !player.isTouchingWater()
                && !player.isClimbing();

        long now = System.nanoTime() / 1_000_000L;

        if (nowAirborne && !airborne && player.getVelocity().y > 0.05D) {
            jumpStartTime = now;
        } else if (!nowAirborne && airborne) {
            lastLandTime = now;
        }

        airborne = nowAirborne;
    }

    private static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }
        PlayerEntity player = client.player;

        if (player == null || client.world == null
                || context.matrixStack() == null) {
            return;
        }

        long now = System.nanoTime() / 1_000_000L;
        boolean jumpActive = airborne && now - jumpStartTime <= 900L;
        boolean landingActive = !airborne && lastLandTime > 0L
                && now - lastLandTime <= EFFECT_LIFETIME_MS;

        if (!jumpActive && !landingActive) {
            return;
        }

        float tickDelta = context.tickDelta();
        double x = player.prevX + (player.getX() - player.prevX) * tickDelta;
        double y = player.prevY + (player.getY() - player.prevY) * tickDelta + 0.035D;
        double z = player.prevZ + (player.getZ() - player.prevZ) * tickDelta;

        float progress = jumpActive
                ? (float) Math.max(0.0D, Math.min(1.0D, (now - jumpStartTime) / 900.0D))
                : (float) Math.max(0.0D, Math.min(1.0D,
                        (now - lastLandTime) / (double) EFFECT_LIFETIME_MS));

        double radius;
        float alpha;

        if (jumpActive) {
            double pulse = 0.5D + 0.5D * Math.sin(progress * Math.PI);
            radius = BASE_RADIUS + 0.17D * pulse;
            alpha = (float) (0.42D + 0.30D * pulse);
        } else {
            radius = BASE_RADIUS + 0.48D * progress;
            alpha = 0.70F * (1.0F - progress);
        }

        double rotation = now * 0.0025D;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA,
                GlStateManager.DstFactor.ONE);
        RenderSystem.disableTexture();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        RenderSystem.lineWidth(5.0F);
        drawRing(context, x, y, z, radius + 0.035D, rotation,
                145, 85, 255, (int) (alpha * 90.0F));

        RenderSystem.lineWidth(2.2F);
        drawRing(context, x, y + 0.006D, z, radius, -rotation * 1.2D,
                205, 145, 255, (int) (alpha * 210.0F));

        RenderSystem.lineWidth(1.0F);
        drawRing(context, x, y + 0.012D, z, radius - 0.022D, rotation * 1.65D,
                248, 242, 255, (int) (alpha * 255.0F));

        RenderSystem.lineWidth(1.0F);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.enableTexture();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA,
                GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableBlend();
    }

    private static void resetState() {
        airborne = false;
        jumpStartTime = 0L;
        lastLandTime = 0L;
        trackedPlayer = null;
        trackedWorld = null;
    }

    private static void drawRing(WorldRenderContext context, double centerX, double y,
            double centerZ, double radius, double rotation, int red, int green,
            int blue, int alpha) {
        if (alpha <= 0 || radius <= 0.0D) {
            return;
        }

        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_LINE_STRIP, VertexFormats.POSITION_COLOR);

        for (int i = 0; i <= SEGMENTS; i++) {
            double angle = rotation + (i / (double) SEGMENTS) * Math.PI * 2.0D;
            double px = centerX + Math.cos(angle) * radius;
            double pz = centerZ + Math.sin(angle) * radius;

            buffer.vertex(context.matrixStack().peek().getModel(),
                    (float) px, (float) y, (float) pz)
                    .color(red, green, blue, alpha).next();
        }

        Tessellator.getInstance().draw();
    }
}
