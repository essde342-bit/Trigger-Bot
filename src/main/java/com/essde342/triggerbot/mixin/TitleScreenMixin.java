package com.essde342.triggerbot.mixin;

import com.essde342.triggerbot.AltManagerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.LiteralText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin() {
        super(new LiteralText("Minecraft"));
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void triggerBot$addAltManagerButton(CallbackInfo info) {
        this.addButton(new ButtonWidget(
                this.width - 110,
                8,
                102,
                20,
                new LiteralText("Alt Manager"),
                button -> {
                    if (this.client != null) {
                        this.client.openScreen(AltManagerScreen.create((Screen) (Object) this));
                    }
                }
        ));
    }
}
