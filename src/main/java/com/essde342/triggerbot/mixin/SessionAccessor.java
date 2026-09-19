package com.essde342.triggerbot.mixin;

import net.minecraft.client.util.Session;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Session.class)
public interface SessionAccessor {
    @Accessor("accountType")
    Session.AccountType triggerBot$getAccountType();
}
