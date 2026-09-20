package com.essde342.triggerbot.ui;
import java.awt.Color;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;
final class ClickGuiRender {
 private ClickGuiRender(){}
 static void rect(int x,int y,int w,int h,Color c){
  RenderSystem.disableTexture();RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
  GL11.glColor4f(c.getRed()/255f,c.getGreen()/255f,c.getBlue()/255f,c.getAlpha()/255f);
  GL11.glBegin(GL11.GL_QUADS);GL11.glVertex2f(x,y);GL11.glVertex2f(x+w,y);GL11.glVertex2f(x+w,y+h);GL11.glVertex2f(x,y+h);GL11.glEnd();RenderSystem.enableTexture();
 }
 static void rounded(int x,int y,int w,int h,int r,Color c){
  if(r<=0){rect(x,y,w,h,c);return;} RenderSystem.disableTexture();RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
  GL11.glColor4f(c.getRed()/255f,c.getGreen()/255f,c.getBlue()/255f,c.getAlpha()/255f);GL11.glBegin(GL11.GL_TRIANGLE_FAN);
  GL11.glVertex2f(x+w/2f,y+h/2f); arc(x+r,y+r,r,180,270);arc(x+w-r,y+r,r,270,360);arc(x+w-r,y+h-r,r,0,90);arc(x+r,y+h-r,r,90,180);arc(x+r,y+r,r,180,270);
  GL11.glEnd();RenderSystem.enableTexture();
 }
 static void glowRounded(int x,int y,int w,int h,int r,int glow,Color c,Color gc){
  for(int i=glow;i>=1;i--){int a=(int)(gc.getAlpha()*(1f-i/(float)glow)*.45f);rounded(x-i,y-i,w+i*2,h+i*2,r+i,new Color(gc.getRed(),gc.getGreen(),gc.getBlue(),Math.max(0,a)));}
  rounded(x,y,w,h,r,c);
 }
 private static void arc(float cx,float cy,float r,int from,int to){for(int a=from;a<=to;a+=5){double d=Math.toRadians(a);GL11.glVertex2d(cx+Math.cos(d)*r,cy+Math.sin(d)*r);}}
}
