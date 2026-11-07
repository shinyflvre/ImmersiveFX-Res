/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.fabric.mixins;

import dynamiclabs.immersivefx.platform.event.ClientEvents;
import dynamiclabs.immersivefx.platform.event.KeyInputEvent;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class MixinKeyboardHandler {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "keyPress", at = @At("TAIL"))
    private void ifx$keyInput(final long handle, final int action, final KeyEvent event, final CallbackInfo ci) {
        if (handle != 0L && handle == this.minecraft.getWindow().handle() && ClientEvents.KEY_INPUT.hasListeners())
            ClientEvents.KEY_INPUT.post(new KeyInputEvent(event.key(), event.keycode(), action, event.modifiers()));
    }
}
