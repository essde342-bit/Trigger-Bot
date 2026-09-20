package com.essde342.triggerbot.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

final class ClickGuiRender {
    private ClickGuiRender() {}

    static void rect(int x, int y, int w, int h, Color color) {
        if (w <= 0 || h <= 0) return;

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
        GL11.glVertex2f(x + w, y);
        GL11.glVertex2f(x + w, y + h);
        GL11.glVertex2f(x, y + h);
        GL11.glEnd();

        RenderSystem.enableTexture();
    }

    static void rounded(int x, int y, int w, int h, int radius, Color color) {
        if (w <= 0 || h <= 0) return;
        if (radius <= 0) {
            rect(x, y, w, h, color);
            return;
        }

        int r = Math.min(radius, Math.min(w, h) / 2);

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
        GL11.glVertex2f(x + w / 2.0F, y + h / 2.0F);
        arc(x + r, y + r, r, 180, 270);
        arc(x + w - r, y + r, r, 270, 360);
        arc(x + w - r, y + h - r, r, 0, 90);
        arc(x + r, y + h - r, r, 90, 180);
        arc(x + r, y + r, r, 180, 270);
        GL11.glEnd();

        RenderSystem.enableTexture();
    }

    static void border(int x, int y, int w, int h, int radius, Color color) {
        if (w <= 1 || h <= 1) return;

        int r = Math.min(radius, Math.min(w, h) / 2);

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
        arc(x + w - r, y + r, r, 270, 360);
        arc(x + w - r, y + h - r, r, 0, 90);
        arc(x + r, y + h - r, r, 90, 180);
        arc(x + r, y + r, r, 180, 270);
        GL11.glEnd();

        RenderSystem.enableTexture();
    }

    static void circle(int cx, int cy, int radius, Color color) {
        if (radius <= 0) return;

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
        GL11.glVertex2f(cx, cy);
        for (int angle = 0; angle <= 360; angle += 12) {
            double radians = Math.toRadians(angle);
            GL11.glVertex2d(
                    cx + Math.cos(radians) * radius,
                    cy + Math.sin(radians) * radius
            );
        }
        GL11.glEnd();

        RenderSystem.enableTexture();
    }

    private static void arc(float cx, float cy, float radius, int from, int to) {
        for (int angle = from; angle <= to; angle += 6) {
            double radians = Math.toRadians(angle);
            GL11.glVertex2d(
                    cx + Math.cos(radians) * radius,
                    cy + Math.sin(radians) * radius
            );
        }
    }
}
