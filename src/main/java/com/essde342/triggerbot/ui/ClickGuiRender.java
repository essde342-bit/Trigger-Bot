package com.essde342.triggerbot.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

final class ClickGuiRender {
    private ClickGuiRender() {
    }

    static void panel(int x, int y, int width, int height, int radius, Color fill) {
        rounded(x, y, width, height, radius, fill);
    }

    static void border(int x, int y, int width, int height, int radius, Color color) {
        roundedOutline(x, y, width, height, radius, color);
    }

    static void shadow(int x, int y, int width, int height, int radius, int amount, Color color) {
        for (int i = amount; i >= 1; i--) {
            int alpha = Math.max(0, color.getAlpha() / Math.max(1, amount));
            rounded(
                    x - i / 2,
                    y + i / 3,
                    width + i,
                    height + i / 2,
                    radius + i / 3,
                    new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha)
            );
        }
    }

    static void rect(int x, int y, int width, int height, Color color) {
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        GL11.glColor4f(
                color.getRed() / 255.0F,
                color.getGreen() / 255.0F,
                color.getBlue() / 255.0F,
                color.getAlpha() / 255.0F
        );

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x + width, y);
        GL11.glVertex2f(x + width, y + height);
        GL11.glVertex2f(x, y + height);
        GL11.glEnd();

        RenderSystem.enableTexture();
    }

    static void rounded(int x, int y, int width, int height, int radius, Color color) {
        if (width <= 0 || height <= 0) {
            return;
        }
        if (radius <= 0) {
            rect(x, y, width, height, color);
            return;
        }

        int r = Math.min(radius, Math.min(width, height) / 2);

        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        GL11.glColor4f(
                color.getRed() / 255.0F,
                color.getGreen() / 255.0F,
                color.getBlue() / 255.0F,
                color.getAlpha() / 255.0F
        );

        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f(x + width / 2.0F, y + height / 2.0F);
        arc(x + r, y + r, r, 180, 270);
        arc(x + width - r, y + r, r, 270, 360);
        arc(x + width - r, y + height - r, r, 0, 90);
        arc(x + r, y + height - r, r, 90, 180);
        arc(x + r, y + r, r, 180, 270);
        GL11.glEnd();

        RenderSystem.enableTexture();
    }

    static void roundedOutline(int x, int y, int width, int height, int radius, Color color) {
        if (width <= 1 || height <= 1) {
            return;
        }

        int r = Math.min(radius, Math.min(width, height) / 2);

        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        GL11.glLineWidth(1.0F);
        GL11.glColor4f(
                color.getRed() / 255.0F,
                color.getGreen() / 255.0F,
                color.getBlue() / 255.0F,
                color.getAlpha() / 255.0F
        );

        GL11.glBegin(GL11.GL_LINE_STRIP);
        arc(x + r, y + r, r, 180, 270);
        arc(x + width - r, y + r, r, 270, 360);
        arc(x + width - r, y + height - r, r, 0, 90);
        arc(x + r, y + height - r, r, 90, 180);
        arc(x + r, y + r, r, 180, 270);
        GL11.glEnd();

        RenderSystem.enableTexture();
    }

    private static void arc(float cx, float cy, float radius, int from, int to) {
        for (int angle = from; angle <= to; angle += 5) {
            double radians = Math.toRadians(angle);
            GL11.glVertex2d(
                    cx + Math.cos(radians) * radius,
                    cy + Math.sin(radians) * radius
            );
        }
    }
}
