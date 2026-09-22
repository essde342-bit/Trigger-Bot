package kronex.fun.display.screens.clickgui.components.implement.panels;

import kronex.fun.Kronex;
import kronex.fun.display.screens.clickgui.components.AbstractComponent;
import kronex.fun.display.screens.clickgui.components.implement.module.PanelsModuleComponent;
import kronex.fun.display.screens.clickgui.MenuScreen;
import kronex.fun.features.module.Module;
import kronex.fun.features.module.ModuleCategory;
import kronex.fun.other.utils.display.font.Fonts;
import kronex.fun.other.utils.display.scissor.ScissorAssist;
import kronex.fun.other.utils.display.shape.ShapeProperties;
import kronex.fun.other.utils.math.calc.Calculate;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import kronex.fun.other.utils.display.color.ColorAssist;
import kronex.fun.other.utils.display.color.HudColorutility;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class CategoryPanelComponent extends AbstractComponent {
    private static final float HEADER_HEIGHT_RENDER = 18f, HEADER_HEIGHT_INPUT = 20f;
    private static final float CONTENT_PADDING_X = 6f, CONTENT_PADDING_TOP_RENDER = 4f, CONTENT_PADDING_TOP_INPUT = 5f;
    private static final float CONTENT_PADDING_BOTTOM_RENDER = 10f, CONTENT_PADDING_BOTTOM_INPUT = 11f;
    private static final float CONTENT_WIDTH_PADDING = 12f, TITLE_ICON_GAP = 4f, TITLE_CENTER_Y_DIV = 3f, TITLE_BASELINE_Y_OFFSET = 2.5f;
    private static final float MODULE_GAP_Y = 2f, SCROLL_STEP = 20f, SCROLLBAR_WIDTH = 3f, SCROLLBAR_X_OFFSET = 3.5f, SCROLLBAR_MIN_HANDLE_HEIGHT = 18f;
    private static final Map<ModuleCategory,String> CATEGORY_ICONS = Map.of(
            ModuleCategory.COMBAT,"G", ModuleCategory.MOVEMENT,"E", ModuleCategory.RENDER,"F",
            ModuleCategory.PLAYER,"D", ModuleCategory.MISC,"C", ModuleCategory.THEMES,"G");
    private final ModuleCategory category;
    private final List<PanelsModuleComponent> modules = new ArrayList<>();
    private float scroll, smoothedScroll, maxScroll;
    private boolean draggingScrollbar;
    private float dragStartY, dragStartScroll;

    public CategoryPanelComponent(ModuleCategory category) {
        this.category=category;
        for (Module m: Kronex.getInstance().getModuleRepository().modules()) {
            if (m.getCategory()==category) modules.add(new PanelsModuleComponent(m));
        }
        modules.sort(Comparator.comparing(o->o.getModule().getVisibleName().toLowerCase()));
    }
    private String getCategoryIcon(){return CATEGORY_ICONS.getOrDefault(category,"");}
    private float getContentX(){return x+CONTENT_PADDING_X;}
    private float getContentYForRender(){return y+HEADER_HEIGHT_RENDER+CONTENT_PADDING_TOP_RENDER;}
    private float getContentYForInput(){return y+HEADER_HEIGHT_INPUT+CONTENT_PADDING_TOP_INPUT;}
    private float getContentWidth(){return width-CONTENT_WIDTH_PADDING;}
    private float getContentHeightForRender(){return height-HEADER_HEIGHT_RENDER-CONTENT_PADDING_BOTTOM_RENDER;}
    private float getContentHeightForInput(){return height-HEADER_HEIGHT_INPUT-CONTENT_PADDING_BOTTOM_INPUT;}
    private float getScrollbarX(){return x+width-SCROLLBAR_X_OFFSET;}
    private float getScrollbarY(){return getContentYForRender();}
    private float getScrollbarHeight(){return getContentHeightForRender();}
    private float getHandleHeight(){if(maxScroll<=0)return getScrollbarHeight();float barH=getScrollbarHeight();return Math.max(SCROLLBAR_MIN_HANDLE_HEIGHT,barH*(barH/(barH+maxScroll)));}
    private float getHandleY(){if(maxScroll<=0)return getScrollbarY();float barY=getScrollbarY(),barH=getScrollbarHeight(),handleH=getHandleHeight();return barY+(barH-handleH)*((-smoothedScroll)/maxScroll);}
    public boolean isDraggingScrollbar(){return draggingScrollbar;}

    @Override public void render(DrawContext context,int mouseX,int mouseY,float delta){
        MatrixStack matrix=context.getMatrices(); Matrix4f positionMatrix=matrix.peek().getPositionMatrix();
        if(draggingScrollbar){
            if(org.lwjgl.glfw.GLFW.glfwGetMouseButton(mc.getWindow().getHandle(),GLFW.GLFW_MOUSE_BUTTON_LEFT)!=GLFW.GLFW_PRESS) draggingScrollbar=false;
            else updateScrollFromDrag(mouseY);
        }
        blur.render(ShapeProperties.create(matrix,x,y,width,height).round(6).softness(1).thickness(2.25F)
                .outlineColor(ColorAssist.rgba(62,68,84,150)).color(ColorAssist.rgba(14,16,23,215)).build());
        float headerH=HEADER_HEIGHT_RENDER;
        rectangle.render(ShapeProperties.create(matrix,x+1.25f,y+1.25f,width-2.5f,headerH).round(5,5,0,0).color(ColorAssist.rgba(20,22,30,235)).build());
        rectangle.render(ShapeProperties.create(matrix,x+3,y+headerH,width-6,0.5f).color(ColorAssist.rgba(112,116,132,90)).build());
        String title=category.getReadableName().toUpperCase(), icon=getCategoryIcon();
        float iconW=Fonts.getSize(18,Fonts.Type.ICONS2).getStringWidth(icon), titleW=Fonts.getSize(14,Fonts.Type.DEFAULT).getStringWidth(title);
        float totalHeaderW=iconW+TITLE_ICON_GAP+titleW,startX=x+(width-totalHeaderW)/2f,centerY=y+headerH/TITLE_CENTER_Y_DIV;
        Fonts.getSize(18,Fonts.Type.ICONS2).drawString(matrix,icon,startX,centerY+TITLE_BASELINE_Y_OFFSET-1.25f,HudColorutility.getIconColor());
        Fonts.getSize(14,Fonts.Type.DEFAULT).drawString(matrix,title,startX+iconW+TITLE_ICON_GAP,centerY+TITLE_BASELINE_Y_OFFSET,ColorAssist.rgba(232,234,240,255));
        float contentX=getContentX(),contentY=getContentYForRender(),contentW=getContentWidth(),contentH=getContentHeightForRender();
        ScissorAssist scissor=Kronex.getInstance().scissorManager; scissor.push(positionMatrix,contentX,contentY,contentW,contentH);
        String search=MenuScreen.INSTANCE.getSearchText(); boolean hasSearch=search!=null&&!search.isEmpty(); String searchLower=hasSearch?search.toLowerCase():"";
        float renderScroll=Math.round(smoothedScroll), yOff=0,totalH=0;
        for(PanelsModuleComponent m:modules){
            if(hasSearch&&!m.getModule().getVisibleName().toLowerCase().contains(searchLower)) continue;
            float itemH=m.getComponentHeight(), itemY=contentY+yOff+renderScroll;
            m.position(contentX,itemY).size(contentW,itemH);
            if(itemY+itemH>=contentY&&itemY<=contentY+contentH)m.render(context,mouseX,mouseY,delta);
            yOff+=itemH+MODULE_GAP_Y; totalH+=itemH+MODULE_GAP_Y;
        }
        scissor.pop(); maxScroll=Math.max(0,(float)Math.ceil(totalH-contentH)); scroll=MathHelper.clamp(scroll,-maxScroll,0); smoothedScroll=(float)Calculate.interpolateSmooth(2,smoothedScroll,scroll);
        if(maxScroll>0){
            float barW=SCROLLBAR_WIDTH,barX=getScrollbarX(),barY=getScrollbarY(),barH=getScrollbarHeight();
            boolean hover=Calculate.isHovered(mouseX,mouseY,barX-2,barY,barW+4,barH);
            rectangle.render(ShapeProperties.create(matrix,barX,barY,barW,barH).round(2).color(ColorAssist.rgba(34,36,43,hover||draggingScrollbar?150:100)).build());
            rectangle.render(ShapeProperties.create(matrix,barX,getHandleY(),barW,getHandleHeight()).round(2).color(ColorAssist.rgba(158,163,180,hover||draggingScrollbar?220:180)).build());
        }
    }

    private void updateScrollFromDrag(double mouseY){ if(maxScroll<=0)return; float barY=getScrollbarY(),barH=getScrollbarHeight(),handleH=getHandleHeight(),scrollRange=barH-handleH; if(scrollRange<=0)return; scroll=MathHelper.clamp(dragStartScroll-(((float)mouseY-dragStartY)/scrollRange)*maxScroll,-maxScroll,0); }

    @Override public boolean mouseClicked(double mouseX,double mouseY,int button){
        if(button==0&&maxScroll>0){
            float barX=getScrollbarX(),barY=getScrollbarY(),barW=SCROLLBAR_WIDTH,barH=getScrollbarHeight();
            if(Calculate.isHovered(mouseX,mouseY,barX-2,barY,barW+4,barH)){
                float handleY=getHandleY(),handleH=getHandleHeight();
                if(mouseY>=handleY&&mouseY<=handleY+handleH){draggingScrollbar=true;dragStartY=(float)mouseY;dragStartScroll=scroll;}
                else {float range=barH-handleH;if(range>0){float top=(float)mouseY-handleH/2,ratio=MathHelper.clamp((top-barY)/range,0,1);scroll=-ratio*maxScroll;} draggingScrollbar=true;dragStartY=(float)mouseY;dragStartScroll=scroll;}
                return true;
            }
        }
        float contentX=getContentX(),contentY=getContentYForInput(),contentW=getContentWidth(),contentH=getContentHeightForInput();
        if(!Calculate.isHovered(mouseX,mouseY,contentX,contentY,contentW,contentH)) return false;
        String search=MenuScreen.INSTANCE.getSearchText(); boolean hasSearch=search!=null&&!search.isEmpty(); String searchLower=hasSearch?search.toLowerCase():"";
        for(PanelsModuleComponent m:modules){if(hasSearch&&!m.getModule().getVisibleName().toLowerCase().contains(searchLower))continue;if(m.isHover(mouseX,mouseY))return m.mouseClicked(mouseX,mouseY,button);}
        return false;
    }
    @Override public boolean mouseReleased(double mouseX,double mouseY,int button){if(button==0)draggingScrollbar=false;for(PanelsModuleComponent m:modules)m.mouseReleased(mouseX,mouseY,button);return false;}
    @Override public boolean mouseDragged(double mouseX,double mouseY,int button,double deltaX,double deltaY){if(draggingScrollbar&&button==0&&maxScroll>0){updateScrollFromDrag(mouseY);return true;}return super.mouseDragged(mouseX,mouseY,button,deltaX,deltaY);}
    @Override public boolean mouseScrolled(double mouseX,double mouseY,double amount){float contentX=getContentX(),contentY=getContentYForInput(),contentW=getContentWidth(),contentH=getContentHeightForInput();if(!Calculate.isHovered(mouseX,mouseY,contentX,contentY,contentW,contentH))return false;scroll+=amount*SCROLL_STEP;for(PanelsModuleComponent m:modules)m.mouseScrolled(mouseX,mouseY,amount);return true;}
    @Override public boolean keyPressed(int keyCode,int scanCode,int modifiers){for(PanelsModuleComponent m:modules)if(m.keyPressed(keyCode,scanCode,modifiers))return true;return false;}
    @Override public boolean charTyped(char chr,int modifiers){for(PanelsModuleComponent m:modules)m.charTyped(chr,modifiers);return false;}
}
