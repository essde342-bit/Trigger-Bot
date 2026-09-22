package kronex.fun.display.screens.clickgui.components.implement.other;

import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;
import kronex.fun.other.utils.display.font.FontRenderer;
import kronex.fun.other.utils.display.font.Fonts;
import kronex.fun.other.utils.display.shape.ShapeProperties;
import kronex.fun.other.utils.display.color.ColorAssist;
import kronex.fun.other.utils.math.MathUtil;
import kronex.fun.other.utils.display.geometry.Render2D;
import kronex.fun.other.utils.display.scissor.ScissorAssist;
import kronex.fun.Kronex;
import kronex.fun.display.screens.clickgui.components.AbstractComponent;

public class SearchComponent extends AbstractComponent {
    public static boolean typing = false;
    private boolean dragging;
    private int cursorPosition = 0;
    private int selectionStart = -1;
    private int selectionEnd = -1;
    private long lastClickTime = 0;
    private float xOffset = 0;

    @Getter
    private String text = "";

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrix = context.getMatrices();
        FontRenderer font = Fonts.getSize(12, Fonts.Type.DEFAULT);

        updateXOffset(font, cursorPosition);
        width = 80;
        height = 15;

        rectangle.render(ShapeProperties.create(matrix, x, y, width, height)
                .round(2.5F).thickness(2).softness(0.5F).outlineColor(ColorAssist.getOutline()).color(ColorAssist.getGuiRectColor(0.5F)).build());

        String displayText = text.equalsIgnoreCase("") && !typing ? "Search" : text;
        ScissorAssist scissor = Kronex.getInstance().getScissorManager();
        scissor.push(matrix.peek().getPositionMatrix(), x + 1, y, width - 3, height);

        if (typing && selectionStart != -1 && selectionEnd != -1 && selectionStart != selectionEnd) {
            int start = Math.max(0, Math.min(getStartOfSelection(), text.length()));
            int end = Math.max(0, Math.min(getEndOfSelection(), text.length()));
            if (start < end) {
                float selectionXStart = x + 4 - xOffset + font.getStringWidth(text.substring(0, start));
                float selectionXEnd = x + 4 - xOffset + font.getStringWidth(text.substring(0, end));
                rectangle.render(ShapeProperties.create(matrix, selectionXStart, y + (height / 2) - 4, selectionXEnd - selectionXStart, 8).color(0xFF5585E8).build());
            }
        }

        font.drawString(context.getMatrices(), displayText, x + 4, y + (height / 2) - 1.0F, typing ? -1 : 0xFF878894);
        scissor.pop();

        if (typing && System.currentTimeMillis() % 1000 < 500 && (selectionStart == -1 || selectionStart == selectionEnd)) {
            float cursorX = font.getStringWidth(text.substring(0, cursorPosition));
            rectangle.render(ShapeProperties.create(matrix, x + 4 - xOffset + cursorX, y + (height / 2) - 3.5F, 0.5F, 7).color(-1).build());
        }

        if (dragging) {
            cursorPosition = getCursorIndexAt(mouseX);
            if (selectionStart == -1) selectionStart = cursorPosition + 1;
            selectionEnd = cursorPosition;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (MathUtil.isHovered(mouseX, mouseY, x, y, width, height) && button == 0) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime < 250) {
                selectionStart = 0;
                selectionEnd = text.length();
            } else {
                typing = true;
                dragging = true;
                lastClickTime = currentTime;
                cursorPosition = getCursorIndexAt(mouseX);
                selectionStart = cursorPosition;
                selectionEnd = cursorPosition;
            }
        } else {
            typing = false;
            clearSelection();
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (typing && Fonts.getSize(12, Fonts.Type.DEFAULT).getStringWidth(text) < 55) {
            deleteSelectedText();
            text = text.substring(0, cursorPosition) + chr + text.substring(cursorPosition);
            cursorPosition++;
            clearSelection();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (typing) {
            if (Screen.hasControlDown()) {
                switch (keyCode) {
                    case GLFW.GLFW_KEY_A -> selectAllText();
                    case GLFW.GLFW_KEY_V -> pasteFromClipboard();
                    case GLFW.GLFW_KEY_C -> copyToClipboard();
                    default -> {}
                }
            } else {
                switch (keyCode) {
                    case GLFW.GLFW_KEY_BACKSPACE, GLFW.GLFW_KEY_ENTER -> handleTextModification(keyCode);
                    case GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_RIGHT -> moveCursor(keyCode);
                    default -> {}
                }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void pasteFromClipboard() {
        String clipboardText = GLFW.glfwGetClipboardString(window.getHandle());
        if (clipboardText != null) replaceText(cursorPosition, cursorPosition, clipboardText);
    }

    private void copyToClipboard() {
        if (hasSelection()) GLFW.glfwSetClipboardString(window.getHandle(), getSelectedText());
    }

    private void selectAllText() { selectionStart = 0; selectionEnd = text.length(); }

    private void handleTextModification(int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (hasSelection()) replaceText(getStartOfSelection(), getEndOfSelection(), "");
            else if (cursorPosition > 0) replaceText(cursorPosition - 1, cursorPosition, "");
        } else if (keyCode == GLFW.GLFW_KEY_ENTER) {
            typing = false;
        }
    }

    private void moveCursor(int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_LEFT && cursorPosition > 0) cursorPosition--;
        else if (keyCode == GLFW.GLFW_KEY_RIGHT && cursorPosition < text.length()) cursorPosition++;
        updateSelectionAfterCursorMove();
    }

    private void updateSelectionAfterCursorMove() {
        if (Screen.hasShiftDown()) {
            if (selectionStart == -1) selectionStart = cursorPosition;
            selectionEnd = cursorPosition;
        } else clearSelection();
    }

    private void replaceText(int start, int end, String replacement) {
        start = Math.max(0, start);
        end = Math.min(text.length(), end);
        if (start > end) start = end;
        text = text.substring(0, start) + replacement + text.substring(end);
        cursorPosition = start + replacement.length();
        clearSelection();
    }

    private boolean hasSelection() {
        return selectionStart != -1 && selectionEnd != -1 && selectionStart != selectionEnd;
    }

    private String getSelectedText() { return text.substring(getStartOfSelection(), getEndOfSelection()); }
    private int getStartOfSelection() { return Math.min(selectionStart, selectionEnd); }
    private int getEndOfSelection() { return Math.max(selectionStart, selectionEnd); }
    private void clearSelection() { selectionStart = -1; selectionEnd = -1; }

    private int getCursorIndexAt(double mouseX) {
        FontRenderer font = Fonts.getSize(12, Fonts.Type.BOLD);
        float relativeX = (float) mouseX - x - 3 + xOffset;
        int position = 0;
        while (position < text.length()) {
            if (font.getStringWidth(text.substring(0, position + 1)) > relativeX) break;
            position++;
        }
        return position;
    }

    private void updateXOffset(FontRenderer font, int cursorPosition) {
        float cursorX = font.getStringWidth(text.substring(0, cursorPosition));
        if (cursorX < xOffset) xOffset = cursorX;
        else if (cursorX - xOffset > width - 7) xOffset = cursorX - (width - 7);
    }

    private void deleteSelectedText() {
        if (hasSelection()) replaceText(getStartOfSelection(), getEndOfSelection(), "");
    }
}