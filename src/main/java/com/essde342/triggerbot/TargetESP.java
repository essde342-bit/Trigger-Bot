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
    private static final int PARTICLE_COUNT = 24;
    private static final double ROTATION_SPEED = 0.0045D;

    private static PlayerEntity target;

    private TargetESP() {}

    public static void register() {
        WorldRenderEvents.LAST.register(TargetESP::render);
    }

    public static void tick(MinecraftClient client) {
        if (!TriggerBotClient.CONFIG.targetEsp
                || client == null || client.player == null || client.world == null) {
            target = null;
            return;
        }

        PlayerEntity combatTarget = TriggerBotClient.getCurrentCombatTarget();
        if (valid(client, combatTarget)) {
            target = combatTarget;
            return;
        }

        target = findFallbackTarget(client);
    }

    private static PlayerEntity findFallbackTarget(MinecraftClient client) {
        if (client.crosshairTarget instanceof EntityHitResult) {
            Entity entity = ((EntityHitResult) client.crosshairTarget).getEntity();
            if (entity instanceof PlayerEntity && entity != client.player) {
                PlayerEntity p = (PlayerEntity) entity;
                if (valid(client, p)) {
                    return p;
                }
            }
        }

        PlayerEntity nearest = null;
        double best = 36.0D;

        for (PlayerEntity p : client.world.getPlayers()) {
            if (!valid(client, p)) {
                continue;
            }

            double distance = client.player.squaredDistanceTo(p);
            if (distance < best) {
                best = distance;
                nearest = p;
            }
        }

        return nearest;
    }

    private static boolean valid(MinecraftClient client, PlayerEntity player) {
        return player != null
                && player != client.player
                && player.isAlive()
                && !player.isSpectator()
                && client.player.squaredDistanceTo(player) <= 36.0D
                && client.player.canSee(player);
    }

    private static void render(WorldRenderContext context) {
        if (!TriggerBotClient.CONFIG.targetEsp
                || target == null
                || !target.isAlive()
                || context.matrixStack() == null) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.gameRenderer == null) {
            return;
        }

        Vec3d targetPos = interpolate(target, context.tickDelta());
        Vec3d cameraPos = client.gameRenderer.getCamera().getPos();

        context.matrixStack().push();
        context.matrixStack().translate(
                -cameraPos.x,
                -cameraPos.y,
                -cameraPos.z
        );

        renderParticles(context, targetPos, target.getWidth(), target.getHeight());

        context.matrixStack().pop();
    }

    private static void renderParticles(
            WorldRenderContext context,
            Vec3d base,
            float targetWidth,
            float targetHeight
    ) {
        long now = System.nanoTime();
        double rotation = now * ROTATION_SPEED * 0.000001D;

        double radius = Math.max(0.48D, targetWidth * 0.56D);
        double height = Math.max(1.4D, targetHeight);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(
                GlStateManager.SrcFactor.SRC_ALPHA,
                GlStateManager.DstFactor.ONE
        );
        RenderSystem.disableTexture();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        BufferBuilder buffer = Tessellator.getInstance().getBuffer();

        // Wide violet glow.
        RenderSystem.enableBlend();
        RenderSystem.lineWidth(1.0F);
        GL11.glPointSize(11.0F);
        buffer.begin(GL11.GL_POINTS, VertexFormats.POSITION_COLOR);
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            Vec3d p = particlePosition(base, radius, height, rotation, i, false);
            buffer.vertex(
                    context.matrixStack().peek().getModel(),
                    (float) p.x, (float) p.y, (float) p.z
            ).color(155, 95, 255, 70).next();
        }
        Tessellator.getInstance().draw();

        // Mid glow.
        GL11.glPointSize(6.0F);
        buffer.begin(GL11.GL_POINTS, VertexFormats.POSITION_COLOR);
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            Vec3d p = particlePosition(base, radius, height, rotation, i, true);
            buffer.vertex(
                    context.matrixStack().peek().getModel(),
                    (float) p.x, (float) p.y, (float) p.z
            ).color(200, 150, 255, 140).next();
        }
        Tessellator.getInstance().draw();

        // White hot core.
        GL11.glPointSize(2.8F);
        buffer.begin(GL11.GL_POINTS, VertexFormats.POSITION_COLOR);
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            Vec3d p = particlePosition(base, radius, height, rotation, i, true);
            buffer.vertex(
                    context.matrixStack().peek().getModel(),
                    (float) p.x, (float) p.y, (float) p.z
            ).color(245, 235, 255, 255).next();
        }
        Tessellator.getInstance().draw();

        GL11.glPointSize(1.0F);
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

    private static Vec3d particlePosition(
            Vec3d base,
            double radius,
            double height,
            double rotation,
            int index,
            boolean reverse
    ) {
        double t = index / (double) (PARTICLE_COUNT - 1);
        double angle = rotation
                + index * (Math.PI * 2.0D / PARTICLE_COUNT)
                + (reverse ? Math.PI : 0.0D);

        double y = base.y
                + 0.18D
                + height * 0.72D * t
                + Math.sin(angle * 2.0D) * 0.045D;

        double localRadius = radius
                + Math.sin(angle * 3.0D + rotation) * 0.025D;

        return new Vec3d(
                base.x + Math.cos(angle) * localRadius,
                y,
                base.z + Math.sin(angle) * localRadius
        );
    }

    private static Vec3d interpolate(Entity entity, float tickDelta) {
        return new Vec3d(
                entity.prevX + (entity.getX() - entity.prevX) * tickDelta,
                entity.prevY + (entity.getY() - entity.prevY) * tickDelta,
                entity.prevZ + (entity.getZ() - entity.prevZ) * tickDelta
        );
    }
}
