package kronex.fun.display.screens.clickgui.components.implement.autobuy.settings;

import kronex.fun.display.screens.clickgui.components.AbstractComponent;
import kronex.fun.display.screens.clickgui.components.implement.autobuy.originalitems.ItemRegistry;
import kronex.fun.other.utils.display.color.ColorAssist;
import kronex.fun.other.utils.display.font.Fonts;
import kronex.fun.other.utils.display.shape.ShapeProperties;
import kronex.fun.other.utils.math.calc.Calculate;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

public abstract class AutoBuySettingsComponent extends AbstractComponent {
    protected final AutoBuyItemSettings settings;
    protected AutoBuySettingsComponent(AutoBuyItemSettings settings){this.settings=settings;height=18;}

    public static class BuyBelowComponent extends AutoBuySettingsComponent {
        private boolean editing; private String inputText="";
        public BuyBelowComponent(AutoBuyItemSettings s){super(s);}
        public void render(DrawContext c,int mx,int my,float d){String text=editing?inputText+"_":settings.getBuyBelow()+"$";Fonts.getSize(13,Fonts.Type.SEMI).drawString(c.getMatrices(),"Покупать ниже:",x+8,y+10,ColorAssist.getText(.8f));Fonts.getSize(13,Fonts.Type.DEFAULT).drawString(c.getMatrices(),text,x+width-50,y+10,ColorAssist.getText());}
        public boolean mouseClicked(double mx,double my,int b){if(b==0&&Calculate.isHovered(mx,my,x+width/2,y,width/2,height)){editing=true;inputText=""+settings.getBuyBelow();return true;}return false;}
        public boolean keyPressed(int k,int s,int m){if(!editing)return false;if(k==GLFW.GLFW_KEY_ENTER){try{settings.setBuyBelow(Math.max(1,Integer.parseInt(inputText)));AutoBuySettingsManager.getInstance().saveSettings(settings.getItemName(),settings);ItemRegistry.reloadSettings();}catch(NumberFormatException ignored){}editing=false;return true;}if(k==GLFW.GLFW_KEY_BACKSPACE&&!inputText.isEmpty()){inputText=inputText.substring(0,inputText.length()-1);return true;}if(k==GLFW.GLFW_KEY_ESCAPE){editing=false;return true;}return true;}
        public boolean charTyped(char c,int m){if(editing&&Character.isDigit(c)&&inputText.length()<9){inputText+=c;return true;}return editing;}
    }
    public static class MinQuantityComponent extends AutoBuySettingsComponent {
        private boolean editing; private String inputText="";
        public MinQuantityComponent(AutoBuyItemSettings s){super(s);}
        public void render(DrawContext c,int mx,int my,float d){String text=editing?inputText+"_":""+settings.getMinQuantity();Fonts.getSize(13,Fonts.Type.SEMI).drawString(c.getMatrices(),"Количество от:",x+8,y+10,ColorAssist.getText(.8f));Fonts.getSize(13,Fonts.Type.DEFAULT).drawString(c.getMatrices(),text,x+width-45,y+10,ColorAssist.getText());}
        public boolean mouseClicked(double mx,double my,int b){if(b==0&&Calculate.isHovered(mx,my,x+width/2,y,width/2,height)){editing=true;inputText=""+settings.getMinQuantity();return true;}return false;}
        public boolean keyPressed(int k,int s,int m){if(!editing)return false;if(k==GLFW.GLFW_KEY_ENTER){try{settings.setMinQuantity(Math.max(1,Math.min(64,Integer.parseInt(inputText))));AutoBuySettingsManager.getInstance().saveSettings(settings.getItemName(),settings);ItemRegistry.reloadSettings();}catch(NumberFormatException ignored){}editing=false;return true;}if(k==GLFW.GLFW_KEY_BACKSPACE&&!inputText.isEmpty()){inputText=inputText.substring(0,inputText.length()-1);return true;}if(k==GLFW.GLFW_KEY_ESCAPE){editing=false;return true;}return true;}
        public boolean charTyped(char c,int m){if(editing&&Character.isDigit(c)&&inputText.length()<2){inputText+=c;return true;}return editing;}
    }
}
