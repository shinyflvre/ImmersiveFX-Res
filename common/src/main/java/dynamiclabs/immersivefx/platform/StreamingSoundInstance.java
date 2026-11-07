/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.platform;

import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;

public interface StreamingSoundInstance {

    @Nonnull
    CompletableFuture<AudioStream> getStream(@Nonnull SoundBufferLibrary soundBuffers, @Nonnull Sound sound, boolean looping);

    @Nonnull
    static CompletableFuture<AudioStream> vanillaStream(@Nonnull final SoundBufferLibrary soundBuffers, @Nonnull final Sound sound, final boolean looping) {
        return soundBuffers.getStream(sound.getPath(), looping);
    }
}
