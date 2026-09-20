package com.essde342.triggerbot;

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
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public final class LightningESP {
    private static final Random RANDOM = new Random();

    private static final int MAX_BOLTS = 8;
    private static final int SPAWN_PER_WAVE = 2;
    private static final long SPAWN_INTERVAL_MS = 95L;
    private static final int PATH_DEPTH = 3;
    private static final long MIN_LIFETIME_MS = 170L;
    private static final long MAX_LIFETIME_MS = 340L;

    private static final class LightningBolt {
        private List<Vec3d> points;
        private long spawnTime;
        private long lifetimeMs;
    }

    private static final List<LightningBolt> BOLTS = new ArrayList<LightningBolt>();
    private static PlayerEntity lastTarget;
    private static long lastTargetSeen;
    private static long lastSpawn;

    private LightningESP() {
    }

    public static void register() {
        WorldRenderEvents.LAST.register(LightningESP::render);
    }

    public static void tick(MinecraftClient client) {
        if (!TriggerBotClient.CONFIG.lightningEsp || client == null
                || client.world == null || client.player == null) {
            lastTarget = null;
            BOLTS.clear();
            return;
        }

        PlayerEntity target = findTarget(client);
        long now = System.currentTimeMillis();

        if (target != null) {
            lastTarget = target;
            lastTargetSeen = now;
        } else if (lastTarget != null && now - lastTargetSeen > 450L) {
            lastTarget = null;
        }
    }

    private static PlayerEntity findTarget(MinecraftClient client) {
        if (client.crosshairTarget instanceof EntityHitResult) {
            Entity entity = ((EntityHitResult) client.crosshairTarget).getEntity();
            if (entity instanceof PlayerEntity && entity != client.player) {
                PlayerEntity player = (PlayerEntity) entity;
                if (player.isAlive() && !player.isSpectator()
                        && client.player.squaredDistanceTo(player) <= 36.0D
                        && client.player.canSee(player)) {
                    return player;
                }
            }
        }

        PlayerEntity nearest = null;
        double bestDistance = 36.0D;

        for (PlayerEntity candidate : client.world.getPlayers()) {
            if (candidate == client.player || !candidate.isAlive() || candidate.isSpectator()) {
                continue;
            }

            double distance = client.player.squaredDistanceTo(candidate);
            if (distance < bestDistance && client.player.canSee(candidate)) {
                bestDistance = distance;
                nearest = candidate;
            }
        }

        return nearest;
    }

    private static void render(WorldRenderContext context) {
        if (!TriggerBotClient.CONFIG.lightningEsp) {
            reset();
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.world == null
                || context.matrixStack() == null || lastTarget == null
                || !lastTarget.isAlive()) {
            reset();
            return;
        }

        long now = System.currentTimeMillis();
        Vector3d basePos = interpolate(lastTarget, context.tickDelta());

        if (now - lastSpawn >= SPAWN_INTERVAL_MS && BOLTS.size() < MAX_BOLTS) {
            for (int i = 0; i < SPAWN_PER_WAVE && BOLTS.size() < MAX_BOLTS; i++) {
                BOLTS.add(spawnBolt(lastTarget, basePos));
            }
            lastSpawn = now;
        }

        Iterator<LightningBolt> iterator = BOLTS.iterator();
        while (iterator.hasNext()) {
            LightningBolt bolt = iterator.next();
            if (now - bolt.spawnTime >= bolt.lifetimeMs) {
                iterator.remove();
            }
        }

        if (BOLTS.isEmpty()) {
            return;
        }

        renderBolts(context, now);
    }

    private static void renderBolts(WorldRenderContext context, long now) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(
                GlStateManager.SrcFactor.SRC_ALPHA,
                GlStateManager.DstFactor.ONE
        );
        RenderSystem.disableTexture();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.shadeModel(GL11.GL_SMOOTH);
        RenderSystem.pushMatrix();

        BufferBuilder buffer = Tessellator.getInstance().getBuffer();

        // The LAST event already has the camera view transform active.
        for (int pass = 0; pass < 3; pass++) {
            float lineWidth = pass == 0 ? 5.5F : (pass == 1 ? 2.8F : 1.15F);
            RenderSystem.lineWidth(lineWidth);

            buffer.begin(GL11.GL_LINES, VertexFormats.POSITION_COLOR);

            for (LightningBolt bolt : BOLTS) {
                float life = (now - bolt.spawnTime) / (float) bolt.lifetimeMs;
                float fade = 1.0F - life;
                if (fade <= 0.02F) {
                    continue;
                }

                float alpha = fade * (pass == 0 ? 0.16F : (pass == 1 ? 0.42F : 0.95F));
                int a = clampColor((int) (alpha * 255.0F));

                int r = pass == 2 ? 235 : 120;
                int g = pass == 2 ? 250 : 190;
                int b = 255;

                for (int i = 0; i < bolt.points.size() - 1; i++) {
                    Vector3d first = bolt.points.get(i);
                    Vector3d second = bolt.points.get(i + 1);

                    buffer.vertex(
                            context.matrixStack().peek().getModel(),
                            (float) first.x,
                            (float) first.y,
                            (float) first.z
                    ).color(r, g, b, a).next();

                    buffer.vertex(
                            context.matrixStack().peek().getModel(),
                            (float) second.x,
                            (float) second.y,
                            (float) second.z
                    ).color(r, g, b, a).next();
                }
            }

            Tessellator.getInstance().draw();
        }

        RenderSystem.popMatrix();
        RenderSystem.lineWidth(1.0F);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.shadeModel(GL11.GL_FLAT);
    }

    private static LightningBolt spawnBolt(PlayerEntity target, Vector3d basePos) {
        double width = target.getWidth();
        double height = target.getHeight();

        double angle = RANDOM.nextDouble() * Math.PI * 2.0D;
        double radius = width * 0.5D + 0.07D;
        double startY = 0.10D + RANDOM.nextDouble() * Math.max(0.10D, height - 0.12D);

        Vector3d start = basePos.add(
                Math.cos(angle) * radius,
                startY,
                Math.sin(angle) * radius
        );

        Vector3d dir = randomDirection();
        double length = 0.30D + RANDOM.nextDouble() * 0.38D;

        Vector3d end = start.add(
                dir.x * length,
                dir.y * length,
                dir.z * length
        );

        LightningBolt bolt = new LightningBolt();
        bolt.points = generatePath(start, end, PATH_DEPTH, length * 0.42D);
        bolt.spawnTime = System.currentTimeMillis();
        bolt.lifetimeMs = MIN_LIFETIME_MS
                + RANDOM.nextInt((int) (MAX_LIFETIME_MS - MIN_LIFETIME_MS + 1));

        return bolt;
    }

    private static List<Vector3d> generatePath(
            Vector3d start,
            Vector3d end,
            int depth,
            double maxOffset
    ) {
        if (depth <= 0) {
            List<Vector3d> result = new ArrayList<Vector3d>(2);
            result.add(start);
            result.add(end);
            return result;
        }

        Vector3d direction = end.subtract(start);
        Vector3d midpoint = start.add(direction.scale(0.5D));

        Vector3d perpendicular;
        if (Math.abs(direction.y) < 0.9D) {
            perpendicular = direction.crossProduct(new Vector3d(0.0D, 1.0D, 0.0D)).normalize();
        } else {
            perpendicular = direction.crossProduct(new Vector3d(1.0D, 0.0D, 0.0D)).normalize();
        }

        double offset = (RANDOM.nextDouble() - 0.5D) * 2.0D * maxOffset;
        Vector3d displaced = midpoint.add(perpendicular.scale(offset));

        List<Vector3d> left = generatePath(
                start,
                displaced,
                depth - 1,
                maxOffset * 0.5D
        );
        List<Vector3d> right = generatePath(
                displaced,
                end,
                depth - 1,
                maxOffset * 0.5D
        );

        left.remove(left.size() - 1);
        left.addAll(right);
        return left;
    }

    private static Vector3d randomDirection() {
        double yaw = RANDOM.nextDouble() * Math.PI * 2.0D;
        double pitch = Math.toRadians(-70.0D + RANDOM.nextDouble() * 140.0D);
        double cosPitch = Math.cos(pitch);

        return new Vector3d(
                Math.cos(yaw) * cosPitch,
                Math.sin(pitch),
                Math.sin(yaw) * cosPitch
        );
    }

    private static Vector3d interpolate(Entity entity, float tickDelta) {
        return new Vector3d(
                entity.prevX + (entity.getX() - entity.prevX) * tickDelta,
                entity.prevY + (entity.getY() - entity.prevY) * tickDelta,
                entity.prevZ + (entity.getZ() - entity.prevZ) * tickDelta
        );
    }

    private static int clampColor(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private static void reset() {
        BOLTS.clear();
        lastTarget = null;
        lastSpawn = 0L;
    }
}
