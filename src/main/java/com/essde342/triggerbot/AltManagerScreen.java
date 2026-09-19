package com.essde342.triggerbot;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

import java.util.List;

public final class AltManagerScreen extends Screen {
    private static final int ROW_HEIGHT = 24;
    private static final int MAX_VISIBLE = 8;

    private final Screen parent;
    private TextFieldWidget nicknameField;
    private int scrollOffset;
    private String status = "";

    private AltManagerScreen(Screen parent) {
        super(new LiteralText("Alt Manager"));
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
        this.buttons.clear();
        this.children.clear();

        int center = this.width / 2;
        int inputWidth = Math.min(260, this.width - 20);
        int inputX = center - inputWidth / 2;

        nicknameField = this.addButton(new TextFieldWidget(
                this.textRenderer,
                inputX,
                40,
                inputWidth,
                20,
                new LiteralText("Nickname")
        ));
        nicknameField.setMaxLength(16);
        nicknameField.setSuggestion("3-16 chars");
        nicknameField.setFocusUnlocked(true);
        nicknameField.setText(AltManager.currentNickname());

        int buttonY = 66;
        int smallWidth = 78;
        int gap = 4;

        this.addButton(new ButtonWidget(
                center - smallWidth - gap / 2,
                buttonY,
                smallWidth,
                20,
                new LiteralText("Apply"),
                button -> applyFromField()
        ));

        this.addButton(new ButtonWidget(
                center + gap / 2,
                buttonY,
                smallWidth,
                20,
                new LiteralText("Save"),
                button -> saveFromField()
        ));

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
            TextFieldWidget altField = this.addButton(new TextFieldWidget(
                    this.textRenderer,
                    listX,
                    rowY,
                    fieldWidth,
                    20,
                    new LiteralText("Saved alt")
            ));
            altField.setMaxLength(16);
            altField.setText(alts.get(index));
            altField.setEditable(false);
            altField.setFocusUnlocked(false);
            altField.setDrawsBackground(true);

            this.addButton(new ButtonWidget(
                    listX + fieldWidth + 4,
                    rowY,
                    deleteWidth,
                    20,
                    new LiteralText("Delete"),
                    button -> {
                        AltManager.remove(altIndex);
                        status = "";
                        rebuildWidgets();
                    }
            ));

            rowY += ROW_HEIGHT;
        }

        this.addButton(new ButtonWidget(
                center - 75,
                this.height - 28,
                150,
                20,
                new LiteralText("Close"),
                button -> onClose()
        ));
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
            nicknameField.setCursorToEnd();
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
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        List<String> alts = AltManager.getAll();
        int maxOffset = Math.max(0, alts.size() - MAX_VISIBLE);

        if (maxOffset > 0 && mouseY >= 94 && mouseY <= this.height - 42) {
            if (amount < 0) {
                scrollOffset = Math.min(maxOffset, scrollOffset + 1);
            } else if (amount > 0) {
                scrollOffset = Math.max(0, scrollOffset - 1);
            }

            rebuildWidgets();
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public void tick() {
        if (nicknameField != null) {
            nicknameField.tick();
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        int center = this.width / 2;
        List<String> alts = AltManager.getAll();

        drawCenteredText(
                matrices,
                this.textRenderer,
                new LiteralText("Alt Manager"),
                center,
                12,
                0xFFFFFF
        );

        drawCenteredText(
                matrices,
                this.textRenderer,
                new LiteralText("Saved: " + alts.size()),
                center,
                28,
                0xAAAAAA
        );

        if (!status.isEmpty()) {
            drawCenteredText(
                    matrices,
                    this.textRenderer,
                    new LiteralText(status),
                    center,
                    this.height - 44,
                    0xFFFFFF
            );
        }

        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        AltManager.save();

        if (this.client != null) {
            this.client.openScreen(this.parent);
        }
    }
}
