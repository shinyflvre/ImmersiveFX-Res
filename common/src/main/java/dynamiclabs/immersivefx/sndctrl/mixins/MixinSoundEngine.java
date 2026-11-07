/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
/*
 * Dynamic Surroundings
 * Copyright (C) 2020 OreCruncher
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

import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.audio.Library;
import com.mojang.blaze3d.audio.SoundBuffer;
import dynamiclabs.immersivefx.sndctrl.SoundControl;
import dynamiclabs.immersivefx.sndctrl.api.sound.Category;
import dynamiclabs.immersivefx.sndctrl.api.sound.ISoundInstance;
import dynamiclabs.immersivefx.sndctrl.audio.AudioEngine;
import dynamiclabs.immersivefx.sndctrl.audio.SeamlessLoops;
import dynamiclabs.immersivefx.sndctrl.audio.SoundUtils;
import dynamiclabs.immersivefx.sndctrl.audio.handlers.SoundFXProcessor;
import dynamiclabs.immersivefx.sndctrl.audio.handlers.SoundVolumeEvaluator;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import net.minecraft.client.Options;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Mixin(SoundEngine.class)
public class MixinSoundEngine {

    @Final
    @Shadow
    private Library library;

    @Final
    @Shadow
    private Map<SoundInstance, ChannelAccess.ChannelHandle> instanceToChannel;

    @Final
    @Shadow
    private Options options;

    @Final
    @Shadow
    private Map<SoundInstance, Integer> soundDeleteTime;

    @Final
    @Shadow
    private Multimap<SoundSource, SoundInstance> instanceBySource;

    @Final
    @Shadow
    private List<TickableSoundInstance> tickingSounds;

    @Final
    @Shadow
    private Object2FloatMap<SoundSource> gainBySource;

    private float sndctrl_volume(@Nonnull final SoundInstance sound) {
        final float master = Mth.clamp(this.options.getSoundSourceVolume(SoundSource.MASTER), 0F, 1F);
        return SoundVolumeEvaluator.getClampedVolume(sound) * master * this.gainBySource.getFloat(sound.getSource());
    }

    /**
     * Calculates the volume of sound based on the myriad of factors in the game as well as configuration.  Used by
     * the engine when ticking sounds and when a category volume changes.
     *
     * @param sound The sound instance that is being evaluated
     * @param ci    The callback context used to return the result of the calculation
     */
    @Inject(method = "calculateVolume(Lnet/minecraft/client/resources/sounds/SoundInstance;)F", at = @At("HEAD"), cancellable = true)
    private void getClampedVolume(SoundInstance sound, CallbackInfoReturnable<Float> ci) {
        try {
            ci.setReturnValue(sndctrl_volume(sound));
        } catch (final Throwable ignored) {
        }
    }

    @WrapOperation(method = "play", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundEngine;calculateVolume(FLnet/minecraft/sounds/SoundSource;)F"))
    private float sndctrl_playVolume(SoundEngine self, float volume, SoundSource source, Operation<Float> original, @Local(argsOnly = true) SoundInstance sound) {
        try {
            return sndctrl_volume(sound);
        } catch (final Throwable ignored) {
        }
        return original.call(self, volume, source);
    }

    /**
     * Callback hook to initialize sndcntrl when the SoundEngine initializes the OpenAL library.
     *
     * @param ci ignored
     */
    @Inject(method = "loadLibrary()V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/audio/Library;init(Ljava/lang/String;Lcom/mojang/blaze3d/audio/DeviceList;Z)V", shift = At.Shift.AFTER))
    public void initialize(CallbackInfo ci) {
        SoundUtils.initialize(this.library);
    }

    /**
     * Callback hook to deinitialize sndctrl when the SoundEngine is unloaded.
     *
     * @param ci ignored
     */
    @Inject(method = "destroy()V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/audio/Library;cleanup()V"))
    public void deinitialize(CallbackInfo ci) {
        SoundUtils.deinitialize(this.library);
        SeamlessLoops.clear();
    }

    @WrapOperation(method = "play", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundBufferLibrary;getCompleteBuffer(Lnet/minecraft/resources/Identifier;)Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture<SoundBuffer> sndctrl_loopBuffer(SoundBufferLibrary self, Identifier location, Operation<CompletableFuture<SoundBuffer>> original, @Local(argsOnly = true) SoundInstance sound) {
        try {
            if (SeamlessLoops.applies(sound))
                return SeamlessLoops.getLoopBuffer(self, location, sound);
        } catch (final Throwable t) {
            SoundControl.LOGGER.error(t, "Error in sndctrl_loopBuffer()!");
        }
        return original.call(self, location);
    }

    @Inject(method = "play", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/ChannelAccess$ChannelHandle;execute(Ljava/util/function/Consumer;)V", shift = At.Shift.AFTER))
    public void onSoundPlay(SoundInstance sound, CallbackInfoReturnable<SoundEngine.PlayResult> ci, @Local ChannelAccess.ChannelHandle handle) {
        try {
            SoundFXProcessor.onSoundPlay(sound, handle);
            AudioEngine.onPlaySound(sound);
        } catch (@Nonnull final Throwable t) {
            SoundControl.LOGGER.error(t, "Error in onSoundPlay()!");
        }
    }

    /**
     * Need to tick and handle sounds that are tagged with CONFIG category even if the game is paused.
     *
     * @param isGamePaused Flag indicating whether game is paused
     * @param ci           Ignored
     */
    @Inject(method = "tick(Z)V", at = @At("RETURN"))
    public void tick(final boolean isGamePaused, @Nonnull final CallbackInfo ci) {
        if (!isGamePaused)
            return;

        final Iterator<Map.Entry<SoundInstance, ChannelAccess.ChannelHandle>> iterator = this.instanceToChannel.entrySet().iterator();

        while (iterator.hasNext()) {
            final Map.Entry<SoundInstance, ChannelAccess.ChannelHandle> entry = iterator.next();
            if (entry.getKey() instanceof ISoundInstance instance) {
                // Skip non-config sounds
                if (instance.getSoundCategory() != Category.CONFIG)
                    continue;
                final ChannelAccess.ChannelHandle handle = entry.getValue();
                final float f2 = this.options.getSoundSourceVolume(instance.getSource());
                if (f2 <= 0.0F) {
                    handle.execute(Channel::stop);
                    iterator.remove();
                } else if (handle.isStopped()) {
                    iterator.remove();
                    this.soundDeleteTime.remove(instance);

                    try {
                        this.instanceBySource.remove(instance.getSource(), instance);
                    } catch (RuntimeException ignore) {
                    }

                    if (instance instanceof TickableSoundInstance) {
                        this.tickingSounds.remove(instance);
                    }
                }
            }
        }
    }
}
