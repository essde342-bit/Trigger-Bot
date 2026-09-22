package kronex.fun.other.utils.display.font;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;

public final class FontRenderer {
    private final float scale;
    FontRenderer(float scale) { this.scale=Math.max(0.5F,scale/9.0F); }
    public float getStringWidth(String text){return MinecraftClient.getInstance().textRenderer.getWidth(text==null?"":text)*scale;}
    public float getStringHeight(String text){return MinecraftClient.getInstance().textRenderer.fontHeight*scale;}
    public void drawString(MatrixStack matrix,String text,float x,float y,int color){drawString(matrix,text,(double)x,(double)y,color);}
    public void drawString(MatrixStack matrix,String text,double x,double y,int color){
        var context=kronex.fun.other.utils.display.GuiRenderContext.get();
        if(context==null)return;
        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,net.minecraft.text.Text.literal(text==null?"":text),(int)Math.round(x),(int)Math.round(y),color);
    }
    public void drawCenteredString(MatrixStack matrix,String text,double centerX,double y,int color){drawString(matrix,text,centerX-getStringWidth(text)/2.0,y,color);}
    public void drawStringWithScroll(MatrixStack matrix,String text,float x,float y,float maxWidth,int color){drawString(matrix,text,(double)x,(double)y,color);}
    public void drawGradientString(MatrixStack matrix,String text,float x,float y,int color1,int color2){drawGradientString(matrix,text,(double)x,(double)y,color1,color2);}
    public void drawGradientString(MatrixStack matrix,String text,double x,double y,int color1,int color2){drawString(matrix,text,x,y,color1);}
}
