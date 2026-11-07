/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.sndctrl.mixins;

import com.mojang.blaze3d.audio.Channel;
import dynamiclabs.immersivefx.sndctrl.SoundControl;
import dynamiclabs.immersivefx.sndctrl.audio.NaturalPanning;
import dynamiclabs.immersivefx.sndctrl.misc.IPanningChannel;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nonnull;

@Mixin(Channel.class)
public class MixinChannelPanning implements IPanningChannel {

    @Shadow
    @Final
    private int source;

    @Unique
    private final NaturalPanning.State sndctrl_pan = new NaturalPanning.State();

    @Nonnull
    @Override
    public NaturalPanning.State sndctrl_panState() {
        return this.sndctrl_pan;
    }

    @Override
    public int sndctrl_panSource() {
        return this.source;
    }

    @ModifyVariable(method = "setSelfPosition(Lnet/minecraft/world/phys/Vec3;)V", at = @At("HEAD"), argsOnly = true)
    private Vec3 sndctrl_panPosition(Vec3 position) {
        try {
            return NaturalPanning.onSetPosition((Channel) (Object) this, position);
        } catch (@Nonnull final Throwable t) {
            SoundControl.LOGGER.error(t, "Error in sndctrl_panPosition()!");
        }
        return position;
    }

    @ModifyVariable(method = "setVolume(F)V", at = @At("HEAD"), argsOnly = true)
    private float sndctrl_panVolume(float volume) {
        try {
            return NaturalPanning.onSetVolume((Channel) (Object) this, volume);
        } catch (@Nonnull final Throwable t) {
            SoundControl.LOGGER.error(t, "Error in sndctrl_panVolume()!");
        }
        return volume;
    }

    @Inject(method = "setRelative(Z)V", at = @At("RETURN"))
    private void sndctrl_panRelative(boolean relative, CallbackInfo ci) {
        try {
            NaturalPanning.onSetRelative((Channel) (Object) this, relative);
        } catch (@Nonnull final Throwable t) {
            SoundControl.LOGGER.error(t, "Error in sndctrl_panRelative()!");
        }
    }

    @Inject(method = "linearAttenuation(F)V", at = @At("HEAD"))
    private void sndctrl_panLinear(float maxDistance, CallbackInfo ci) {
        NaturalPanning.onAttenuation((Channel) (Object) this, true);
    }

    @Inject(method = "disableAttenuation()V", at = @At("HEAD"))
    private void sndctrl_panNoAttenuation(CallbackInfo ci) {
        NaturalPanning.onAttenuation((Channel) (Object) this, false);
    }

    @Inject(method = "attachStaticBuffer(Lcom/mojang/blaze3d/audio/SoundBuffer;)V", at = @At("RETURN"))
    private void sndctrl_panStaticBuffer(CallbackInfo ci) {
        try {
            NaturalPanning.onStaticBufferAttached((Channel) (Object) this);
        } catch (@Nonnull final Throwable t) {
            SoundControl.LOGGER.error(t, "Error in sndctrl_panStaticBuffer()!");
        }
    }

    @Inject(method = "attachBufferStream(Lnet/minecraft/client/sounds/AudioStream;)V", at = @At("HEAD"))
    private void sndctrl_panStream(AudioStream stream, CallbackInfo ci) {
        try {
            NaturalPanning.onFormat((Channel) (Object) this, stream.getFormat().getChannels());
        } catch (@Nonnull final Throwable t) {
            SoundControl.LOGGER.error(t, "Error in sndctrl_panStream()!");
        }
    }

    @Inject(method = "destroy()V", at = @At("HEAD"))
    private void sndctrl_panDestroy(CallbackInfo ci) {
        NaturalPanning.onDestroy((Channel) (Object) this);
    }
}
