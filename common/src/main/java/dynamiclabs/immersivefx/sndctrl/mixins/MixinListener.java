/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.sndctrl.mixins;

import com.mojang.blaze3d.audio.Listener;
import com.mojang.blaze3d.audio.ListenerTransform;
import dynamiclabs.immersivefx.sndctrl.SoundControl;
import dynamiclabs.immersivefx.sndctrl.audio.NaturalPanning;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nonnull;

@Mixin(Listener.class)
public class MixinListener {

    @Inject(method = "setTransform(Lcom/mojang/blaze3d/audio/ListenerTransform;)V", at = @At("RETURN"))
    private void sndctrl_onTransform(ListenerTransform transform, CallbackInfo ci) {
        try {
            NaturalPanning.onListener(transform);
        } catch (@Nonnull final Throwable t) {
            SoundControl.LOGGER.error(t, "Error in sndctrl_onTransform()!");
        }
    }
}
