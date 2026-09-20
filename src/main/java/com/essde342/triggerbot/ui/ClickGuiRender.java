package com.essde342.triggerbot.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;
import java.awt.Color;

final class ClickGuiRender {
    private ClickGuiRender() {}

    static void rect(int x,int y,int w,int h,Color c) {
        if (w <= 0 || h <= 0) return;
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        GL11.glColor4f(c.getRed()/255f,c.getGreen()/255f,c.getBlue()/255f,c.getAlpha()/255f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x,y);
        GL11.glVertex2f(x+w,y);
        GL11.glVertex2f(x+w,y+h);
        GL11.glVertex2f(x,y+h);
        GL11.glEnd();
        RenderSystem.enableTexture();
    }

    static void rounded(int x,int y,int w,int h,int r,Color c) {
        if (w <= 0 || h <= 0) return;
        if (r <= 0) { rect(x,y,w,h,c); return; }
        int rr=Math.min(r,Math.min(w,h)/2);
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        GL11.glColor4f(c.getRed()/255f,c.getGreen()/255f,c.getBlue()/255f,c.getAlpha()/255f);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f(x+w/2f,y+h/2f);
        arc(x+rr,y+rr,rr,180,270);
        arc(x+w-rr,y+rr,rr,270,360);
        arc(x+w-rr,y+h-rr,rr,0,90);
        arc(x+rr,y+h-rr,rr,90,180);
        arc(x+rr,y+rr,rr,180,270);
        GL11.glEnd();
        RenderSystem.enableTexture();
    }

    static void border(int x,int y,int w,int h,int r,Color c) {
        if (w <= 1 || h <= 1) return;
        int rr=Math.min(r,Math.min(w,h)/2);
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        GL11.glLineWidth(1f);
        GL11.glColor4f(c.getRed()/255f,c.getGreen()/255f,c.getBlue()/255f,c.getAlpha()/255f);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        arc(x+rr,y+rr,rr,180,270);
        arc(x+w-rr,y+rr,rr,270,360);
        arc(x+w-rr,y+h-rr,rr,0,90);
        arc(x+rr,y+h-rr,rr,90,180);
        arc(x+rr,y+rr,rr,180,270);
        GL11.glEnd();
        RenderSystem.enableTexture();
    }

    static void circle(int cx,int cy,int radius,Color c) {
        if (radius <= 0) return;
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        GL11.glColor4f(c.getRed()/255f,c.getGreen()/255f,c.getBlue()/255f,c.getAlpha()/255f);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f(cx,cy);
        for(int a=0;a<=360;a+=12){
            double d=Math.toRadians(a);
            GL11.glVertex2d(cx+Math.cos(d)*radius,cy+Math.sin(d)*radius);
        }
        GL11.glEnd();
        RenderSystem.enableTexture();
    }

    private static void arc(float cx,float cy,float r,int from,int to){
        for(int a=from;a<=to;a+=6){
            double d=Math.toRadians(a);
            GL11.glVertex2d(cx+Math.cos(d)*r,cy+Math.sin(d)*r);
        }
    }
}
