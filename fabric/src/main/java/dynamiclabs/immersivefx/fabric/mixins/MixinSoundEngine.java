/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
package dynamiclabs.immersivefx.fabric.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dynamiclabs.immersivefx.fabric.FabricEventBridge;
import dynamiclabs.immersivefx.platform.StreamingSoundInstance;
import dynamiclabs.immersivefx.sndctrl.SoundControl;
import dynamiclabs.immersivefx.sndctrl.audio.SeamlessLoops;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundEngine.class)
public abstract class MixinSoundEngine {

    @Shadow
    private boolean loaded;

    @Inject(method = "play", at = @At("HEAD"), cancellable = true)
    private void ifx$playSound(final SoundInstance instance, final CallbackInfoReturnable<SoundEngine.PlayResult> cir,
                               @Local(argsOnly = true) final LocalRef<SoundInstance> sound) {
        if (!this.loaded)
            return;
        final SoundInstance result = FabricEventBridge.playSound((SoundEngine) (Object) this, instance);
        if (result == null) {
            cir.setReturnValue(SoundEngine.PlayResult.NOT_STARTED);
        } else if (result != instance) {
            sound.set(result);
        }
    }

    @WrapOperation(method = "play", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;thenAccept(Ljava/util/function/Consumer;)Ljava/util/concurrent/CompletableFuture;", ordinal = 0))
    private CompletableFuture<Void> ifx$staticSourceStarted(final CompletableFuture<Object> future, final Consumer<Object> action,
                                                           final Operation<CompletableFuture<Void>> original,
                                                           @Local(argsOnly = true) final SoundInstance sound,
                                                           @Local final ChannelAccess.ChannelHandle handle) {
        return original.call(future, ifx$afterSourceStart(action, sound, handle, false));
    }

    @WrapOperation(method = "play", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;thenAccept(Ljava/util/function/Consumer;)Ljava/util/concurrent/CompletableFuture;", ordinal = 1))
    private CompletableFuture<Void> ifx$streamingSourceStarted(final CompletableFuture<Object> future, final Consumer<Object> action,
                                                              final Operation<CompletableFuture<Void>> original,
                                                              @Local(argsOnly = true) final SoundInstance sound,
                                                              @Local final ChannelAccess.ChannelHandle handle) {
        return original.call(future, ifx$afterSourceStart(action, sound, handle, true));
    }

    @Unique
    private Consumer<Object> ifx$afterSourceStart(final Consumer<Object> action, final SoundInstance sound,
                                                  final ChannelAccess.ChannelHandle handle, final boolean streaming) {
        final SoundEngine engine = (SoundEngine) (Object) this;
        return value -> {
            action.accept(value);
            handle.execute(channel -> FabricEventBridge.sourceStarted(engine, sound, channel, streaming));
        };
    }

    @WrapOperation(method = "play", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundBufferLibrary;getStream(Lnet/minecraft/resources/Identifier;Z)Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture<AudioStream> ifx$stream(final SoundBufferLibrary library, final Identifier path, final boolean looping,
                                                     final Operation<CompletableFuture<AudioStream>> original,
                                                     @Local(argsOnly = true) final SoundInstance instance, @Local final Sound sound) {
        try {
            if (looping && SeamlessLoops.applies(instance))
                return SeamlessLoops.getLoopStream(ifx$open(library, path, false, original, instance, sound), instance, library, sound);
        } catch (final Throwable t) {
            SoundControl.LOGGER.error(t, "Error in sndctrl_loopStream()!");
        }
        return ifx$open(library, path, looping, original, instance, sound);
    }

    @Unique
    private static CompletableFuture<AudioStream> ifx$open(final SoundBufferLibrary library, final Identifier path, final boolean looping,
                                                          final Operation<CompletableFuture<AudioStream>> original,
                                                          final SoundInstance instance, final Sound sound) {
        if (instance instanceof StreamingSoundInstance streaming)
            return streaming.getStream(library, sound, looping);
        return original.call(library, path, looping);
    }
}
