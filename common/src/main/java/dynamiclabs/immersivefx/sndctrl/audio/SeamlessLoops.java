/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
/*
 * Dynamic Surroundings: Sound Control
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package dynamiclabs.immersivefx.sndctrl.audio;

import dynamiclabs.immersivefx.platform.Platform;
import com.mojang.blaze3d.audio.SoundBuffer;
import dynamiclabs.immersivefx.lib.logging.IModLog;
import dynamiclabs.immersivefx.sndctrl.SoundControl;
import dynamiclabs.immersivefx.sndctrl.api.sound.ISoundInstance;
import dynamiclabs.immersivefx.sndctrl.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.JOrbisAudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.lwjgl.BufferUtils;

import javax.annotation.Nonnull;
import javax.sound.sampled.AudioFormat;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class SeamlessLoops {

    private static final IModLog LOGGER = SoundControl.LOGGER.createChild(SeamlessLoops.class);

    static final int SILENCE = 33;
    private static final int MIN_LOOP_FRAMES = 2048;
    private static final int MIN_OVERLAP_FRAMES = 64;

    private record Key(Identifier location, boolean mono, int overlapMs) {
    }

    private static final Map<Key, CompletableFuture<SoundBuffer>> CACHE = new ConcurrentHashMap<>();

    private SeamlessLoops() {
    }

    public static boolean isEnabled() {
        return Config.CLIENT != null && Config.CLIENT.sound.loopCrossfade.get();
    }

    public static int overlapMs() {
        return Config.CLIENT.sound.loopCrossfadeLength.get();
    }

    public static boolean applies(@Nonnull final SoundInstance instance) {
        return instance instanceof ISoundInstance && instance.isLooping() && instance.getDelay() <= 0 && isEnabled();
    }

    private static boolean wantsMono(@Nonnull final SoundInstance instance) {
        if (instance instanceof SurroundSoundInstance)
            return true;
        return Config.CLIENT.sound.enableMonoConversion.get()
                && !instance.isRelative()
                && instance.getAttenuation() != SoundInstance.Attenuation.NONE;
    }

    @Nonnull
    public static CompletableFuture<SoundBuffer> getLoopBuffer(@Nonnull final SoundBufferLibrary library, @Nonnull final Identifier location, @Nonnull final SoundInstance instance) {
        final Key key = new Key(location, wantsMono(instance), overlapMs());
        final CompletableFuture<SoundBuffer> future = CACHE.computeIfAbsent(key, k -> CompletableFuture.supplyAsync(() -> build(k), Util.nonCriticalIoPool()));
        return future.handle((buffer, error) -> {
            if (error == null && buffer != null)
                return CompletableFuture.completedFuture(buffer);
            LOGGER.warn("Unable to build seamless loop for %s, playing it unchanged: %s", location, error != null ? error.getMessage() : "no data");
            return library.getCompleteBuffer(location);
        }).thenCompose(f -> f);
    }

    @Nonnull
    public static CompletableFuture<AudioStream> getLoopStream(@Nonnull final CompletableFuture<AudioStream> first, @Nonnull final SoundInstance instance, @Nonnull final SoundBufferLibrary library, @Nonnull final Sound sound) {
        final int overlapMs = overlapMs();
        return first.thenApply(stream -> new CrossfadeLoopStream(stream, () -> Platform.get().getStream(instance, library, sound, false), overlapMs));
    }

    public static void clear() {
        CACHE.values().forEach(f -> f.thenAccept(SoundBuffer::discardAlBuffer));
        CACHE.clear();
    }

    @Nonnull
    private static SoundBuffer build(@Nonnull final Key key) {
        final ByteBuffer data;
        final AudioFormat format;
        try (InputStream is = Minecraft.getInstance().getResourceManager().open(key.location());
             JOrbisAudioStream stream = new JOrbisAudioStream(is)) {
            data = stream.readAll();
            format = stream.getFormat();
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }

        final int channels = format.getChannels();
        if (format.getSampleSizeInBits() != 16 || channels < 1)
            return new SoundBuffer(data, format);

        short[] pcm = new short[data.remaining() / 2];
        for (int i = 0; i < pcm.length; i++)
            pcm[i] = data.getShort(data.position() + i * 2);
        int outChannels = channels;

        if (key.mono() && channels > 1) {
            final short[] mono = new short[pcm.length / channels];
            for (int f = 0; f < mono.length; f++) {
                int sum = 0;
                for (int c = 0; c < channels; c++)
                    sum += pcm[f * channels + c];
                mono[f] = (short) (sum / channels);
            }
            pcm = mono;
            outChannels = 1;
        }

        final float rate = format.getSampleRate();
        final short[] loop = makeSeamless(pcm, outChannels, (int) (key.overlapMs() * rate / 1000F), key.location(), rate);

        final ByteBuffer out = BufferUtils.createByteBuffer(loop.length * 2);
        for (final short s : loop)
            out.putShort(s);
        out.flip();
        return new SoundBuffer(out, new AudioFormat(rate, 16, outChannels, true, false));
    }

    @Nonnull
    static short[] makeSeamless(@Nonnull final short[] pcm, final int channels, final int overlap, @Nonnull final Object name, final float rate) {
        final int frames = pcm.length / channels;
        if (frames < MIN_LOOP_FRAMES)
            return pcm;

        final int maxTrim = frames / 4;
        int lead = 0;
        while (lead < maxTrim && isSilent(pcm, lead, channels))
            lead++;
        int trail = 0;
        while (trail < maxTrim && isSilent(pcm, frames - 1 - trail, channels))
            trail++;

        final int length = frames - lead - trail;
        final int x = Math.min(overlap, length / 4);
        if (length < MIN_LOOP_FRAMES || x < MIN_OVERLAP_FRAMES) {
            if (lead == 0 && trail == 0)
                return pcm;
            final short[] trimmed = new short[length * channels];
            System.arraycopy(pcm, lead * channels, trimmed, 0, trimmed.length);
            return trimmed;
        }

        final int outFrames = length - x;
        final short[] out = new short[outFrames * channels];
        final int tailStart = lead + length - x;
        for (int i = 0; i < x; i++) {
            final double theta = (i + 0.5D) / x * (Math.PI / 2D);
            final double fadeOut = Math.cos(theta);
            final double fadeIn = Math.sin(theta);
            for (int c = 0; c < channels; c++) {
                final double v = pcm[(tailStart + i) * channels + c] * fadeOut + pcm[(lead + i) * channels + c] * fadeIn;
                out[i * channels + c] = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, Math.round(v)));
            }
        }
        System.arraycopy(pcm, (lead + x) * channels, out, x * channels, (outFrames - x) * channels);

        LOGGER.debug("Seamless loop %s: trimmed %.0f ms start / %.0f ms end, crossfade %.0f ms, loop %.2f s",
                name, lead * 1000F / rate, trail * 1000F / rate, x * 1000F / rate, outFrames / rate);
        return out;
    }

    static boolean isSilent(@Nonnull final short[] pcm, final int frame, final int channels) {
        final int base = frame * channels;
        for (int c = 0; c < channels; c++) {
            final int v = pcm[base + c];
            if (v > SILENCE || v < -SILENCE)
                return false;
        }
        return true;
    }
}
