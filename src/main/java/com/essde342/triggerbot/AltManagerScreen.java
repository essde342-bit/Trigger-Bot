package com.essde342.triggerbot;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

import java.util.List;

public final class AltManagerScreen extends Screen {
    private static final int PAGE_SIZE = 8;

    private final Screen parent;
    private int page;
    private TextFieldWidget nicknameField;

    private AltManagerScreen(Screen parent, int page) {
        super(new LiteralText("Alt Manager"));
        this.parent = parent;
        this.page = Math.max(0, page);
    }

    public static Screen create(Screen parent) {
        return new AltManagerScreen(parent, 0);
    }

    private static Screen create(Screen parent, int page) {
        return new AltManagerScreen(parent, page);
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
                44,
                inputWidth,
                20,
                new LiteralText("Minecraft nickname")
        ));
        nicknameField.setMaxLength(16);
        nicknameField.setSuggestion("Nickname 3-16 chars");
        nicknameField.setFocusUnlocked(true);

        int buttonY = 70;
        int buttonWidth = 82;
        int gap = 4;
        int startX = center - (buttonWidth * 3 + gap * 2) / 2;

        this.addButton(new ButtonWidget(
                startX,
                buttonY,
                buttonWidth,
                20,
                new LiteralText("Add"),
                button -> addFromField()
        ));

        this.addButton(new ButtonWidget(
                startX + buttonWidth + gap,
                buttonY,
                buttonWidth,
                20,
                new LiteralText("Random"),
                button -> nicknameField.setText(AltManager.randomNickname())
        ));

        this.addButton(new ButtonWidget(
                startX + (buttonWidth + gap) * 2,
                buttonY,
                buttonWidth,
                20,
                new LiteralText("Save"),
                button -> AltManager.save()
        ));

        List<String> alts = AltManager.getAll();
        int totalPages = Math.max(1, (alts.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        page = Math.min(page, totalPages - 1);

        int start = page * PAGE_SIZE;
        int end = Math.min(alts.size(), start + PAGE_SIZE);

        int rowY = 100;
        for (int index = start; index < end; index++) {
            final int altIndex = index;
            String name = alts.get(index);

            this.addButton(new ButtonWidget(
                    center - 154,
                    rowY,
                    124,
                    20,
                    new LiteralText(name),
                    button -> {
                        nicknameField.setText(AltManager.get(altIndex));
                        nicknameField.setCursorToEnd();
                        nicknameField.setTextFieldFocused(true);
                    }
            ));

            this.addButton(new ButtonWidget(
                    center - 26,
                    rowY,
                    52,
                    20,
                    new LiteralText("DELETE"),
                    button -> {
                        AltManager.remove(altIndex);
                        rebuildWidgets();
                    }
            ));

            rowY += 24;
        }

        if (alts.isEmpty()) {
            this.addButton(new ButtonWidget(
                    center - 90,
                    122,
                    180,
                    20,
                    new LiteralText("No saved alts"),
                    button -> { }
            )).active = false;
        }

        this.addButton(new ButtonWidget(
                center - 154,
                this.height - 32,
                74,
                20,
                new LiteralText("Prev"),
                button -> {
                    if (page > 0) {
                        MinecraftClient.getInstance().openScreen(create(parent, page - 1));
                    }
                }
        ));

        this.addButton(new ButtonWidget(
                center - 75,
                this.height - 32,
                150,
                20,
                new LiteralText("Back"),
                button -> onClose()
        ));

        this.addButton(new ButtonWidget(
                center + 80,
                this.height - 32,
                74,
                20,
                new LiteralText("Next"),
                button -> {
                    if (page + 1 < totalPages) {
                        MinecraftClient.getInstance().openScreen(create(parent, page + 1));
                    }
                }
        ));
    }

    private void addFromField() {
        String name = nicknameField.getText().trim();

        if (AltManager.add(name)) {
            nicknameField.setText("");
            page = Math.max(0, (AltManager.size() - 1) / PAGE_SIZE);
            rebuildWidgets();
        }
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
        int totalPages = Math.max(1, (alts.size() + PAGE_SIZE - 1) / PAGE_SIZE);

        drawCenteredText(
                matrices,
                this.textRenderer,
                new LiteralText("Alt Manager"),
                center,
                14,
                0xFFFFFF
        );

        drawCenteredText(
                matrices,
                this.textRenderer,
                new LiteralText("Saved: " + alts.size() + "   Page: " + (page + 1) + "/" + totalPages),
                center,
                29,
                0xAAAAAA
        );

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
