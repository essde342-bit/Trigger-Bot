package com.essde342.triggerbot;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public final class TargetESP {
    private static final int SEGMENTS = 48;
    private static final double MAX_RANGE = 36.0D;

    private static PlayerEntity target;

    private TargetESP() {
    }

    public static void register() {
        WorldRenderEvents.LAST.register(TargetESP::render);
    }

    public static void tick(MinecraftClient client) {
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
        double bestDistance = MAX_RANGE;

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
                && client.player.squaredDistanceTo(player) <= MAX_RANGE
                && client.player.canSee(player);
    }

    private static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (!TriggerBotClient.CONFIG.targetEsp
                || client.player == null
                || client.world == null
                || context.matrixStack() == null
                || target == null
                || !valid(client, target)) {
            return;
        }

        Vec3d pos = interpolate(target, context.tickDelta());
        double radius = Math.max(0.46D, target.getWidth() * 0.62D);
        double height = Math.max(1.75D, target.getHeight());

        long now = System.nanoTime() / 1_000_000L;
        double pulse = 0.5D + 0.5D * Math.sin(now * 0.008D);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(
                GlStateManager.SrcFactor.SRC_ALPHA,
                GlStateManager.DstFactor.ONE
        );
        RenderSystem.disableTexture();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        // GL_LINE_STRIP is reliable on GL4ES; no GL_POINTS or thick line dependence.
        drawCircle(context, pos.x, pos.y + 0.04D, pos.z,
                radius + 0.08D + pulse * 0.08D, 120, 55, 220, 35);
        drawCircle(context, pos.x, pos.y + 0.045D, pos.z,
                radius + pulse * 0.05D, 185, 90, 255, 110);
        drawCircle(context, pos.x, pos.y + 0.052D, pos.z,
                radius - 0.035D, 245, 235, 255, 235);

        // A second ring above the waist makes the target visible even on small screens.
        drawCircle(context, pos.x, pos.y + height * 0.50D, pos.z,
                radius * 0.72D + pulse * 0.04D, 155, 70, 240, 65);

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

    private static void drawCircle(WorldRenderContext context, double x, double y, double z,
            double radius, int red, int green, int blue, int alpha) {
        if (radius <= 0.0D || alpha <= 0) {
            return;
        }

        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_LINE_STRIP, VertexFormats.POSITION_COLOR);

        for (int i = 0; i <= SEGMENTS; i++) {
            double angle = i * (Math.PI * 2.0D / SEGMENTS);
            buffer.vertex(
                    context.matrixStack().peek().getModel(),
                    (float) (x + Math.cos(angle) * radius),
                    (float) y,
                    (float) (z + Math.sin(angle) * radius)
            ).color(red, green, blue, alpha).next();
        }

        Tessellator.getInstance().draw();
    }

    private static Vec3d interpolate(Entity entity, float tickDelta) {
        return new Vec3d(
                entity.prevX + (entity.getX() - entity.prevX) * tickDelta,
                entity.prevY + (entity.getY() - entity.prevY) * tickDelta,
                entity.prevZ + (entity.getZ() - entity.prevZ) * tickDelta
        );
    }
}
