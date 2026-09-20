package com.essde342.triggerbot;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public final class JumpCircleClient implements ClientModInitializer {
    private static final int SEGMENTS = 64;
    private static final double BASE_RADIUS = 0.38D;
    private static final double OUTER_RADIUS = 0.95D;
    private static final long EFFECT_LIFETIME_MS = 700L;

    private static boolean airborne;
    private static long effectTime;
    private static double effectX;
    private static double effectY;
    private static double effectZ;
    private static PlayerEntity trackedPlayer;
    private static ClientWorld trackedWorld;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(JumpCircleClient::tick);
        WorldRenderEvents.AFTER_ENTITIES.register(JumpCircleClient::render);
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
            effectTime = 0L;
        }

        boolean nowAirborne = !player.isOnGround()
                && !player.isTouchingWater()
                && !player.isClimbing()
                && !player.hasVehicle();

        long now = System.nanoTime() / 1_000_000L;

        if (!TriggerBotClient.CONFIG.jumpCircle) {
            effectTime = 0L;
            airborne = nowAirborne;
            return;
        }

        boolean jumpStarted = !airborne && nowAirborne && player.getVelocity().y > 0.02D;
        boolean jumpInput = player.input != null && player.input.jumping && player.isOnGround();

        if (jumpStarted || jumpInput) {
            effectX = player.getX();
            effectY = player.getY() + 0.035D;
            effectZ = player.getZ();
            effectTime = now;
        }

        if (!nowAirborne && airborne) {
            effectX = player.getX();
            effectY = player.getY() + 0.035D;
            effectZ = player.getZ();
            effectTime = now;
        }

        airborne = nowAirborne;
    }

    private static void render(WorldRenderContext context) {
        if (!TriggerBotClient.CONFIG.jumpCircle || effectTime == 0L) {
            return;
        }

        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider consumers = context.consumers();
        if (matrices == null || consumers == null) {
            return;
        }

        long now = System.nanoTime() / 1_000_000L;
        double progress = (now - effectTime) / (double) EFFECT_LIFETIME_MS;

        if (progress < 0.0D || progress > 1.0D) {
            return;
        }

        Vec3d camera = context.camera().getPos();
        double x = effectX - camera.x;
        double y = effectY - camera.y;
        double z = effectZ - camera.z;

        double eased = 1.0D - Math.pow(1.0D - progress, 2.0D);
        double radius = BASE_RADIUS + (OUTER_RADIUS - BASE_RADIUS) * eased;
        int alpha = (int) Math.round(255.0D * (1.0D - progress));

        VertexConsumer buffer = consumers.getBuffer(RenderLayer.getLines());
        MatrixStack.Entry entry = matrices.peek();

        drawRing(buffer, entry, x, y, z, radius + 0.09D, 135, 65, 255, alpha / 3);
        drawRing(buffer, entry, x, y + 0.006D, z, radius, 190, 105, 255, alpha);
    }

    private static void drawRing(
            VertexConsumer buffer,
            MatrixStack.Entry entry,
            double x,
            double y,
            double z,
            double radius,
            int red,
            int green,
            int blue,
            int alpha
    ) {
        if (alpha <= 0) {
            return;
        }

        for (int i = 0; i <= SEGMENTS; i++) {
            double angle = i * (Math.PI * 2.0D / SEGMENTS);
            float px = (float) (x + Math.cos(angle) * radius);
            float pz = (float) (z + Math.sin(angle) * radius);

            buffer.vertex(entry.getModel(), px, (float) y, pz)
                    .color(red, green, blue, alpha)
                    .next();
        }
    }

    private static void reset() {
        airborne = false;
        effectTime = 0L;
        effectX = 0.0D;
        effectY = 0.0D;
        effectZ = 0.0D;
        trackedPlayer = null;
        trackedWorld = null;
    }
}
