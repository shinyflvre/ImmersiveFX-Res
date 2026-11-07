/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
package dynamiclabs.immersivefx.neoforge.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dynamiclabs.immersivefx.sndctrl.SoundControl;
import dynamiclabs.immersivefx.sndctrl.audio.SeamlessLoops;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SoundEngine.class)
public class MixinSoundEngineStream {

    @WrapOperation(method = "play", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/sounds/SoundInstance;getStream(Lnet/minecraft/client/sounds/SoundBufferLibrary;Lnet/minecraft/client/resources/sounds/Sound;Z)Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture<AudioStream> sndctrl_loopStream(SoundInstance self, SoundBufferLibrary library, Sound sound, boolean looping, Operation<CompletableFuture<AudioStream>> original) {
        try {
            if (looping && SeamlessLoops.applies(self))
                return SeamlessLoops.getLoopStream(original.call(self, library, sound, false), self, library, sound);
        } catch (final Throwable t) {
            SoundControl.LOGGER.error(t, "Error in sndctrl_loopStream()!");
        }
        return original.call(self, library, sound, looping);
    }
}
