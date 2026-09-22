package com.essde342.triggerbot;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;

public final class TargetESP {
    private static final int SEGMENTS = 64;
    private static final double MAX_RANGE = 36.0D;

    private static PlayerEntity target;

    private TargetESP() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(TargetESP::tick);
        WorldRenderEvents.AFTER_ENTITIES.register(TargetESP::render);
    }

    private static void tick(MinecraftClient client) {
        target = null;

        if (client == null
                || client.player == null
                || client.world == null
                || !TriggerBotClient.CONFIG.targetEsp) {
            return;
        }

        PlayerEntity crosshair = getCrosshairTarget(client);
        if (valid(client, crosshair)) {
            target = crosshair;
            return;
        }

        PlayerEntity combat = TriggerBotClient.getCurrentCombatTarget();
        if (valid(client, combat)) {
            target = combat;
            return;
        }

        target = findNearest(client);
    }

    private static PlayerEntity getCrosshairTarget(MinecraftClient client) {
        if (!(client.crosshairTarget instanceof EntityHitResult)) {
            return null;
        }

        Entity entity = ((EntityHitResult) client.crosshairTarget).getEntity();
        return entity instanceof PlayerEntity ? (PlayerEntity) entity : null;
    }

    private static PlayerEntity findNearest(MinecraftClient client) {
        PlayerEntity nearest = null;
        double bestDistance = MAX_RANGE * MAX_RANGE;

        for (PlayerEntity candidate : client.world.getPlayers()) {
            if (!valid(client, candidate)) {
                continue;
            }

            double distance = client.player.squaredDistanceTo(candidate);
            if (distance < bestDistance) {
                bestDistance = distance;
                nearest = candidate;
            }
        }

        return nearest;
    }

    private static boolean valid(MinecraftClient client, PlayerEntity player) {
        return player != null
                && player != client.player
                && player.isAlive()
                && !player.isSpectator()
                && client.player.squaredDistanceTo(player) <= MAX_RANGE * MAX_RANGE;
    }

    private static void render(WorldRenderContext context) {
        if (!TriggerBotClient.CONFIG.targetEsp) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || target == null || !valid(client, target)) {
            return;
        }

        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider consumers = context.consumers();

        if (matrices == null || consumers == null) {
            return;
        }

        Vec3d camera = context.camera().getPos();
        Vec3d pos = target.getLerpedPos(context.tickCounter().getTickDelta(false));

        double x = pos.x - camera.x;
        double y = pos.y - camera.y;
        double z = pos.z - camera.z;

        double radius = Math.max(0.48D, target.getWidth() * 0.72D);
        double height = Math.max(1.7D, target.getHeight());

        long now = System.nanoTime() / 1_000_000L;
        double pulse = 0.5D + 0.5D * Math.sin(now * 0.008D);

        VertexConsumer buffer = consumers.getBuffer(RenderLayer.getLines());
        MatrixStack.Entry entry = matrices.peek();

        drawRing(buffer, entry, x, y + 0.035D, z,
                radius + 0.10D + pulse * 0.08D, 135, 65, 255, 220);
        drawRing(buffer, entry, x, y + 0.042D, z,
                radius + pulse * 0.05D, 210, 120, 255, 255);
        drawRing(buffer, entry, x, y + height * 0.52D, z,
                radius * 0.72D + pulse * 0.05D, 175, 85, 255, 180);
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
        for (int i = 0; i <= SEGMENTS; i++) {
            double angle = i * (Math.PI * 2.0D / SEGMENTS);
            float px = (float) (x + Math.cos(angle) * radius);
            float pz = (float) (z + Math.sin(angle) * radius);

            buffer.vertex(entry.getPositionMatrix(), px, (float) y, pz)
                    .color(red, green, blue, alpha)
                    .normal(entry, 0.0F, 1.0F, 0.0F);
        }
    }
}
