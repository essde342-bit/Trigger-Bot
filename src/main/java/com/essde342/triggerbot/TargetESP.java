package com.essde342.triggerbot;

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
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public final class TargetESP {
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
        target = findTarget(client);
    }

    private static PlayerEntity findTarget(MinecraftClient client) {
        if (client.crosshairTarget instanceof EntityHitResult) {
            Entity entity = ((EntityHitResult) client.crosshairTarget).getEntity();
            if (entity instanceof PlayerEntity && entity != client.player) {
                PlayerEntity p = (PlayerEntity) entity;
                if (valid(client, p)) return p;
            }
        }

        PlayerEntity nearest = null;
        double best = 36.0D;
        for (PlayerEntity p : client.world.getPlayers()) {
            if (!valid(client, p)) continue;
            double d = client.player.squaredDistanceTo(p);
            if (d < best) {
                best = d;
                nearest = p;
            }
        }
        return nearest;
    }

    private static boolean valid(MinecraftClient client, PlayerEntity p) {
        return p != client.player
                && p.isAlive()
                && !p.isSpectator()
                && client.player.squaredDistanceTo(p) <= 36.0D
                && client.player.canSee(p);
    }

    private static void render(WorldRenderContext context) {
        if (!TriggerBotClient.CONFIG.targetEsp
                || target == null || !target.isAlive()
                || context.matrixStack() == null) {
            return;
        }

        Vec3d pos = new Vec3d(
                target.prevX + (target.getX() - target.prevX) * context.tickDelta(),
                target.prevY + (target.getY() - target.prevY) * context.tickDelta(),
                target.prevZ + (target.getZ() - target.prevZ) * context.tickDelta());

        double half = target.getWidth() * .5D + .08D;
        double height = target.getHeight() + .10D;
        Box b = new Box(pos.x - half, pos.y - .05D, pos.z - half,
                pos.x + half, pos.y + height, pos.z + half);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.lineWidth(2.0F);

        BufferBuilder buf = Tessellator.getInstance().getBuffer();
        buf.begin(GL11.GL_LINES, VertexFormats.POSITION_COLOR);

        line(buf, context, b.minX,b.minY,b.minZ, b.maxX,b.minY,b.minZ);
        line(buf, context, b.maxX,b.minY,b.minZ, b.maxX,b.minY,b.maxZ);
        line(buf, context, b.maxX,b.minY,b.maxZ, b.minX,b.minY,b.maxZ);
        line(buf, context, b.minX,b.minY,b.maxZ, b.minX,b.minY,b.minZ);

        line(buf, context, b.minX,b.maxY,b.minZ, b.maxX,b.maxY,b.minZ);
        line(buf, context, b.maxX,b.maxY,b.minZ, b.maxX,b.maxY,b.maxZ);
        line(buf, context, b.maxX,b.maxY,b.maxZ, b.minX,b.maxY,b.maxZ);
        line(buf, context, b.minX,b.maxY,b.maxZ, b.minX,b.maxY,b.minZ);

        line(buf, context, b.minX,b.minY,b.minZ, b.minX,b.maxY,b.minZ);
        line(buf, context, b.maxX,b.minY,b.minZ, b.maxX,b.maxY,b.minZ);
        line(buf, context, b.maxX,b.minY,b.maxZ, b.maxX,b.maxY,b.maxZ);
        line(buf, context, b.minX,b.minY,b.maxZ, b.minX,b.maxY,b.maxZ);

        Tessellator.getInstance().draw();

        RenderSystem.lineWidth(1.0F);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    private static void line(BufferBuilder buf, WorldRenderContext context,
                             double x1,double y1,double z1,double x2,double y2,double z2) {
        buf.vertex(context.matrixStack().peek().getModel(),
                (float)x1,(float)y1,(float)z1).color(220,220,220,235).next();
        buf.vertex(context.matrixStack().peek().getModel(),
                (float)x2,(float)y2,(float)z2).color(220,220,220,235).next();
    }
}
