package com.essde342.triggerbot;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LightningEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class LightningESP {
    private static final int SEGMENTS = 48;
    private static final double SEARCH_RANGE = 128.0D;
    private static final long MARKER_LIFETIME_TICKS = 100L;
    private static final Map<UUID, Marker> MARKERS = new HashMap<>();
    private static net.minecraft.client.world.ClientWorld trackedWorld;

    private LightningESP() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(LightningESP::tick);
        WorldRenderEvents.AFTER_ENTITIES.register(LightningESP::render);
    }

    private static void tick(MinecraftClient client) {
        if (client == null || client.player == null || client.world == null
                || !TriggerBotClient.CONFIG.lightningEsp) {
            MARKERS.clear();
            trackedWorld = client == null ? null : client.world;
            return;
        }

        if (trackedWorld != client.world) {
            MARKERS.clear();
            trackedWorld = client.world;
        }

        long now = client.world.getTime();
        Box area = client.player.getBoundingBox().expand(SEARCH_RANGE);
        for (LightningEntity bolt : client.world.getEntitiesByClass(
                LightningEntity.class, area, entity -> true)) {
            MARKERS.put(bolt.getUuid(), new Marker(
                    bolt.getX(), bolt.getY(), bolt.getZ(), now + MARKER_LIFETIME_TICKS
            ));
        }

        Iterator<Marker> iterator = MARKERS.values().iterator();
        while (iterator.hasNext()) {
            Marker marker = iterator.next();
            double dx = marker.x - client.player.getX();
            double dy = marker.y - client.player.getY();
            double dz = marker.z - client.player.getZ();
            if (marker.expiresAt < now || dx * dx + dy * dy + dz * dz > SEARCH_RANGE * SEARCH_RANGE) {
                iterator.remove();
            }
        }
    }

    private static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!TriggerBotClient.CONFIG.lightningEsp || client.player == null || client.world == null
                || MARKERS.isEmpty()) {
            return;
        }

        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider consumers = context.consumers();
        if (matrices == null || consumers == null) {
            return;
        }

        Vec3d camera = context.camera().getPos();
        VertexConsumer buffer = consumers.getBuffer(RenderLayer.getLines());
        MatrixStack.Entry entry = matrices.peek();
        long now = client.world.getTime();

        for (Marker marker : MARKERS.values()) {
            if (marker.expiresAt < now) {
                continue;
            }
            double x = marker.x - camera.x;
            double y = marker.y - camera.y + 0.04D;
            double z = marker.z - camera.z;

            drawRing(buffer, entry, x, y, z, 1.35D, 255, 210, 70, 255);
            drawRing(buffer, entry, x, y + 0.015D, z, 0.9D, 255, 245, 180, 220);
            drawLine(buffer, entry, x, y, z, x, y + 5.0D, z, 255, 225, 100, 210);
        }
    }

    private static void drawRing(
            VertexConsumer buffer, MatrixStack.Entry entry,
            double x, double y, double z, double radius,
            int red, int green, int blue, int alpha
    ) {
        for (int i = 0; i < SEGMENTS; i++) {
            double a0 = i * (Math.PI * 2.0D / SEGMENTS);
            double a1 = (i + 1) * (Math.PI * 2.0D / SEGMENTS);
            float x0 = (float) (x + Math.cos(a0) * radius);
            float z0 = (float) (z + Math.sin(a0) * radius);
            float x1 = (float) (x + Math.cos(a1) * radius);
            float z1 = (float) (z + Math.sin(a1) * radius);
            drawLine(buffer, entry, x0, y, z0, x1, y, z1, red, green, blue, alpha);
        }
    }

    private static void drawLine(
            VertexConsumer buffer, MatrixStack.Entry entry,
            double x0, double y0, double z0, double x1, double y1, double z1,
            int red, int green, int blue, int alpha
    ) {
        buffer.vertex(entry.getPositionMatrix(), (float) x0, (float) y0, (float) z0)
                .color(red, green, blue, alpha)
                .normal(entry, 0.0F, 1.0F, 0.0F);
        buffer.vertex(entry.getPositionMatrix(), (float) x1, (float) y1, (float) z1)
                .color(red, green, blue, alpha)
                .normal(entry, 0.0F, 1.0F, 0.0F);
    }

    private static final class Marker {
        private final double x;
        private final double y;
        private final double z;
        private final long expiresAt;

        private Marker(double x, double y, double z, long expiresAt) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.expiresAt = expiresAt;
        }
    }
}
