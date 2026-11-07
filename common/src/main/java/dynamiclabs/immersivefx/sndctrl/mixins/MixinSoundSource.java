/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
/*
 * Dynamic Surroundings
 * Copyright (C) 2020  OreCruncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package dynamiclabs.immersivefx.sndctrl.mixins;

import dynamiclabs.immersivefx.sndctrl.SoundControl;
import dynamiclabs.immersivefx.sndctrl.audio.handlers.SoundFXProcessor;
import dynamiclabs.immersivefx.sndctrl.audio.handlers.SourceContext;
import dynamiclabs.immersivefx.sndctrl.misc.IMixinSoundContext;
import com.mojang.blaze3d.audio.SoundBuffer;
import com.mojang.blaze3d.audio.Channel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mixin(Channel.class)
public class MixinSoundSource implements IMixinSoundContext {

    private SourceContext sndctrl_data = null;

    @Nullable
    @Override
    public SourceContext getData() {
        return this.sndctrl_data;
    }

    @Override
    public void setData(@Nullable SourceContext data) {
        this.sndctrl_data = data;
    }

    /**
     * Called when the sound is started by the sound engine. This will set the sound effect properties for the sound
     * at the time of play.
     * @param ci Ignored
     */
    @Inject(method = "play()V", at = @At("HEAD"))
    public void onPlay(CallbackInfo ci) {
        try {
            SoundFXProcessor.tick((Channel) ((Object) this));
        } catch(@Nonnull final Throwable t) {
            SoundControl.LOGGER.error(t, "Error in onPlay()!");
        }
    }

    @Inject(method = "updateStream()V", at = @At("HEAD"))
    public void onTick(CallbackInfo ci) {
        try {
            SoundFXProcessor.tick((Channel) ((Object) this));
        } catch(@Nonnull final Throwable t) {
            SoundControl.LOGGER.error(t, "Error in onTick()!");
        }
    }

    /**
     * Called when a sounds stops playing.  Any context information sndctrl has generated will be cleaned up.
     * @param ci Ignored
     */
    @Inject(method = "stop()V", at = @At("HEAD"))
    public void onStop(CallbackInfo ci) {
        try {
            SoundFXProcessor.stopSoundPlay((Channel) ((Object) this));
        } catch(@Nonnull final Throwable t) {
            SoundControl.LOGGER.error(t, "Error in onStop()!");
        }
    }

    @Inject(method = "destroy()V", at = @At("HEAD"))
    public void onDestroy(CallbackInfo ci) {
        try {
            SoundFXProcessor.stopSoundPlay((Channel) ((Object) this));
        } catch(@Nonnull final Throwable t) {
            SoundControl.LOGGER.error(t, "Error in onDestroy()!");
        }
    }

    @ModifyVariable(method = "attachStaticBuffer(Lcom/mojang/blaze3d/audio/SoundBuffer;)V", at = @At("HEAD"), argsOnly = true)
    private SoundBuffer onPlayBuffer(SoundBuffer buffer) {
        try {
            return SoundFXProcessor.playBuffer((Channel) ((Object) this), buffer);
        } catch(@Nonnull final Throwable t) {
            SoundControl.LOGGER.error(t, "Error in onPlayBuffer()!");
        }
        return buffer;
    }
}
