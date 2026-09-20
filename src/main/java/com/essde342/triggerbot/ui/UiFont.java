package com.essde342.triggerbot.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class UiFont {
    public static final Identifier FONT = new Identifier("triggerbot", "ui");

    private UiFont() {
    }

    public static Text text(String value) {
        return new LiteralText(value).setStyle(Style.EMPTY.withFont(FONT));
    }

    public static int width(MinecraftClient client, String value) {
        return client.textRenderer.getWidth(text(value));
    }
}
