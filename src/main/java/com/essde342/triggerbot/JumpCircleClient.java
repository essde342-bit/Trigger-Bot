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
    private static final int SEGMENTS = 48;
    private static final double BASE_RADIUS = 0.48D;
    private static final double OUTER_RADIUS = 0.82D;
    private static final long LANDING_LIFETIME_MS = 520L;
    private static final long JUMP_LIFETIME_MS = 850L;

    private static boolean airborne;
    private static long jumpStartTime;
    private static long landTime;
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
            reset();
            return;
        }

        if (trackedPlayer != player || trackedWorld != world) {
            trackedPlayer = player;
            trackedWorld = world;
            airborne = false;
            jumpStartTime = 0L;
            landTime = 0L;
        }

        boolean nowAirborne = !player.isOnGround()
                && !player.isTouchingWater()
                && !player.isClimbing()
                && !player.hasVehicle();

        long now = System.nanoTime() / 1_000_000L;

        if (TriggerBotClient.CONFIG.jumpCircle) {
            if (nowAirborne && !airborne && player.getVelocity().y > 0.05D) {
                jumpStartTime = now;
            }

            if (!nowAirborne && airborne) {
                landTime = now;
            }
        } else {
            jumpStartTime = 0L;
            landTime = 0L;
        }

        airborne = nowAirborne;
    }

    private static void render(WorldRenderContext context) {
        if (!TriggerBotClient.CONFIG.jumpCircle || context.matrixStack() == null) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;

        if (player == null || client.world == null) {
            return;
        }

        long now = System.nanoTime() / 1_000_000L;
        boolean jumping = airborne
                && jumpStartTime > 0L
                && now - jumpStartTime <= JUMP_LIFETIME_MS;

        boolean landing = !airborne
                && landTime > 0L
                && now - landTime <= LANDING_LIFETIME_MS;

        if (!jumping && !landing) {
            return;
        }

        float tickDelta = context.tickDelta();
        double x = player.prevX + (player.getX() - player.prevX) * tickDelta;
        double y = player.prevY + (player.getY() - player.prevY) * tickDelta + 0.035D;
        double z = player.prevZ + (player.getZ() - player.prevZ) * tickDelta;

        double progress;
        double radius;
        float alpha;

        if (jumping) {
            progress = Math.max(0.0D, Math.min(1.0D,
                    (now - jumpStartTime) / (double) JUMP_LIFETIME_MS));
            double pulse = 0.5D + 0.5D * Math.sin(progress * Math.PI);
            radius = BASE_RADIUS + (OUTER_RADIUS - BASE_RADIUS) * pulse;
            alpha = (float) (0.72D - progress * 0.28D);
        } else {
            progress = Math.max(0.0D, Math.min(1.0D,
                    (now - landTime) / (double) LANDING_LIFETIME_MS));
            radius = BASE_RADIUS + (OUTER_RADIUS - BASE_RADIUS) * progress;
            alpha = (float) (0.85D * (1.0D - progress));
        }

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(
                GlStateManager.SrcFactor.SRC_ALPHA,
                GlStateManager.DstFactor.ONE
        );
        RenderSystem.disableTexture();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        drawRing(context, x, y, z, radius + 0.10D,
                145, 85, 255, (int) (alpha * 35.0F));
        drawRing(context, x, y + 0.004D, z, radius,
                185, 120, 255, (int) (alpha * 95.0F));
        drawRing(context, x, y + 0.008D, z, Math.max(0.05D, radius - 0.045D),
                250, 242, 255, (int) (alpha * 220.0F));

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.enableTexture();
        RenderSystem.blendFunc(
                GlStateManager.SrcFactor.SRC_ALPHA,
                GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA
        );
        RenderSystem.disableBlend();
    }

    private static void drawRing(WorldRenderContext context, double centerX, double y,
            double centerZ, double radius, int red, int green, int blue, int alpha) {
        if (alpha <= 0 || radius <= 0.0D) {
            return;
        }

        double inner = Math.max(0.01D, radius - 0.045D);
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

        for (int i = 0; i <= SEGMENTS; i++) {
            double angle = (i / (double) SEGMENTS) * Math.PI * 2.0D;
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            buffer.vertex(
                    context.matrixStack().peek().getModel(),
                    (float) (centerX + cos * radius),
                    (float) y,
                    (float) (centerZ + sin * radius)
            ).color(red, green, blue, alpha).next();

            buffer.vertex(
                    context.matrixStack().peek().getModel(),
                    (float) (centerX + cos * inner),
                    (float) y,
                    (float) (centerZ + sin * inner)
            ).color(red, green, blue, alpha).next();
        }

        Tessellator.getInstance().draw();
    }

    private static void reset() {
        airborne = false;
        jumpStartTime = 0L;
        landTime = 0L;
        trackedPlayer = null;
        trackedWorld = null;
    }
}
