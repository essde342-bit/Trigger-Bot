package com.essde342.triggerbot;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.List;

public final class AltManagerScreen extends Screen {
    private static final int ROW_HEIGHT = 24;
    private static final int MAX_VISIBLE = 8;

    private final Screen parent;
    private TextFieldWidget nicknameField;
    private int scrollOffset;
    private String status = "";

    private AltManagerScreen(Screen parent) {
        super(Text.literal("Alt Manager"));
        this.parent = parent;
    }

    public static Screen create(Screen parent) {
        return new AltManagerScreen(parent);
    }

    @Override
    protected void init() {
        rebuildWidgets();
    }

    private void rebuildWidgets() {
        clearChildren();

        int center = this.width / 2;
        int inputWidth = Math.min(260, this.width - 20);
        int inputX = center - inputWidth / 2;

        nicknameField = addDrawableChild(new TextFieldWidget(
                this.textRenderer,
                inputX,
                40,
                inputWidth,
                20,
                Text.literal("Nickname")
        ));
        nicknameField.setMaxLength(16);
        nicknameField.setSuggestion("3-16 chars");
        nicknameField.setFocusUnlocked(true);
        nicknameField.setText(AltManager.currentNickname());

        int buttonY = 66;
        int smallWidth = 78;
        int gap = 4;

        addDrawableChild(ButtonWidget.builder(Text.literal("Apply"), button -> applyFromField())
                .dimensions(center - smallWidth - gap / 2, buttonY, smallWidth, 20)
                .build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Save"), button -> saveFromField())
                .dimensions(center + gap / 2, buttonY, smallWidth, 20)
                .build());

        List<String> alts = AltManager.getAll();
        int maxOffset = Math.max(0, alts.size() - MAX_VISIBLE);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxOffset));

        int rowY = 100;
        int listWidth = Math.min(260, this.width - 60);
        int deleteWidth = 58;
        int fieldWidth = listWidth - deleteWidth - 4;
        int listX = center - listWidth / 2;

        int end = Math.min(alts.size(), scrollOffset + MAX_VISIBLE);
        for (int index = scrollOffset; index < end; index++) {
            final int altIndex = index;
            TextFieldWidget altField = addDrawableChild(new TextFieldWidget(
                    this.textRenderer,
                    listX,
                    rowY,
                    fieldWidth,
                    20,
                    Text.literal("Saved alt")
            ));
            altField.setMaxLength(16);
            altField.setText(alts.get(index));
            altField.setEditable(false);
            altField.setFocusUnlocked(false);
            altField.setDrawsBackground(true);

            addDrawableChild(ButtonWidget.builder(Text.literal("Delete"), button -> {
                        AltManager.remove(altIndex);
                        status = "";
                        rebuildWidgets();
                    })
                    .dimensions(listX + fieldWidth + 4, rowY, deleteWidth, 20)
                    .build());

            rowY += ROW_HEIGHT;
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Close"), button -> close())
                .dimensions(center - 75, this.height - 28, 150, 20)
                .build());
    }

    private void applyFromField() {
        String name = nicknameField.getText().trim();

        if (!AltManager.isValidName(name)) {
            status = "Invalid nickname";
            return;
        }

        if (AltManager.apply(name)) {
            status = "Applied: " + name;
            nicknameField.setText(name);
            nicknameField.setCursorToEnd(false);
        } else {
            status = "Apply failed";
        }
    }

    private void saveFromField() {
        String name = nicknameField.getText().trim();

        if (!AltManager.isValidName(name)) {
            status = "Invalid nickname";
            return;
        }

        if (AltManager.add(name)) {
            status = "Saved: " + name;
            rebuildWidgets();
        } else {
            status = "Already saved";
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int center = this.width / 2;
        int listWidth = Math.min(260, this.width - 60);
        int listX = center - listWidth / 2;
        int rowY = 100;

        List<String> alts = AltManager.getAll();
        int end = Math.min(alts.size(), scrollOffset + MAX_VISIBLE);

        for (int index = scrollOffset; index < end; index++) {
            if (mouseX >= listX
                    && mouseX <= listX + listWidth - 62
                    && mouseY >= rowY
                    && mouseY <= rowY + 20) {
                String name = alts.get(index);
                nicknameField.setText(name);
                nicknameField.setCursorToEnd(false);
                nicknameField.setFocused(true);
                status = "Selected: " + name;
                return true;
            }

            rowY += ROW_HEIGHT;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        List<String> alts = AltManager.getAll();
        int maxOffset = Math.max(0, alts.size() - MAX_VISIBLE);

        if (maxOffset > 0 && mouseY >= 94 && mouseY <= this.height - 42) {
            if (verticalAmount < 0) {
                scrollOffset = Math.min(maxOffset, scrollOffset + 1);
            } else if (verticalAmount > 0) {
                scrollOffset = Math.max(0, scrollOffset - 1);
            }

            rebuildWidgets();
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);

        int center = this.width / 2;
        List<String> alts = AltManager.getAll();

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Alt Manager"),
                center,
                12,
                0xFFFFFF
        );

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Saved: " + alts.size()),
                center,
                28,
                0xAAAAAA
        );

        if (!status.isEmpty()) {
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    Text.literal(status),
                    center,
                    this.height - 44,
                    0xFFFFFF
            );
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        AltManager.save();

        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }
}
