package com.essde342.triggerbot.mixin;

import com.essde342.triggerbot.AltManagerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin() {
        super(Text.literal("Minecraft"));
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void triggerBot$addAltManagerButton(CallbackInfo info) {
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Alt Manager"), button -> {
                    if (this.client != null) {
                        this.client.setScreen(AltManagerScreen.create((Screen) (Object) this));
                    }
                })
                .dimensions(this.width - 110, 8, 102, 20)
                .build());
    }
}
