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

import dynamiclabs.immersivefx.sndctrl.SoundControl;
import net.minecraft.client.sounds.AudioStream;
import org.lwjgl.BufferUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

final class CrossfadeLoopStream implements AudioStream {

    private static final int CHUNK_BYTES = 16384;
    private static final int FRAMES_PER_STEP = 4096;
    private static final int MAX_SILENCE_SECONDS = 2;

    private final AudioFormat format;
    private final int channels;
    private final Supplier<CompletableFuture<AudioStream>> reopen;
    private final int overlap;
    private final int maxSilence;
    private final short[] frame;
    private final short[] mixed;

    private final ShortQueue hold = new ShortQueue();
    private final ShortQueue out = new ShortQueue();

    private Reader current;
    @Nullable
    private CompletableFuture<AudioStream> next;

    private boolean firstPass = true;
    private boolean inLead = true;
    private int leadCount;
    private int leadFrames;
    private long passFrames;
    private int silentRun;
    private int emptyPasses;
    private boolean ended;

    CrossfadeLoopStream(@Nonnull final AudioStream first, @Nonnull final Supplier<CompletableFuture<AudioStream>> reopen, final int overlapMs) {
        this.format = first.getFormat();
        this.channels = Math.max(1, this.format.getChannels());
        this.reopen = reopen;
        final float rate = this.format.getSampleRate();
        this.overlap = Math.max(0, (int) (overlapMs * rate / 1000F));
        this.maxSilence = (int) (MAX_SILENCE_SECONDS * rate);
        this.frame = new short[this.channels];
        this.mixed = new short[this.channels];
        this.current = new Reader(first);
        this.next = prefetch();
    }

    @Override
    @Nonnull
    public AudioFormat getFormat() {
        return this.format;
    }

    @Override
    @Nonnull
    public ByteBuffer read(final int expectedSize) throws IOException {
        final int wanted = Math.max(this.channels, (expectedSize / 2) / this.channels * this.channels);
        while (!this.ended && this.out.size() < wanted)
            step();
        final int n = Math.min(this.out.size(), wanted);
        final ByteBuffer result = BufferUtils.createByteBuffer(n * 2);
        for (int i = 0; i < n; i++)
            result.putShort(this.out.poll());
        result.flip();
        return result;
    }

    @Override
    public void close() throws IOException {
        this.ended = true;
        final CompletableFuture<AudioStream> pending = this.next;
        this.next = null;
        if (pending != null)
            pending.thenAccept(CrossfadeLoopStream::closeQuietly);
        this.current.stream.close();
    }

    private void step() throws IOException {
        for (int k = 0; k < FRAMES_PER_STEP; k++) {
            if (!this.current.next(this.frame)) {
                endOfPass();
                return;
            }
            accept(this.frame);
        }
    }

    private void accept(@Nonnull final short[] f) {
        if (this.inLead) {
            if (this.firstPass) {
                if (this.leadCount < this.maxSilence && isSilent(f)) {
                    this.leadCount++;
                    return;
                }
                this.leadFrames = this.leadCount;
            }
            this.inLead = false;
        }

        this.hold.addFrame(f);
        this.passFrames++;
        this.silentRun = isSilent(f) ? this.silentRun + 1 : 0;

        final int keep = this.overlap + Math.min(this.silentRun, this.maxSilence);
        final int release = this.hold.size() / this.channels - keep;
        if (release > 0)
            this.hold.moveTo(this.out, release * this.channels);
    }

    private void endOfPass() throws IOException {
        final int trailing = Math.min(Math.min(this.silentRun, this.maxSilence), this.hold.size() / this.channels);
        final long body = this.passFrames - trailing;
        if (body <= 0) {
            if (++this.emptyPasses > 2) {
                finish();
                return;
            }
        } else {
            this.emptyPasses = 0;
        }

        this.hold.dropLast(trailing * this.channels);
        final int x = (int) Math.min(Math.min(this.overlap, this.hold.size() / this.channels), Math.max(0L, body / 4));
        this.hold.moveTo(this.out, this.hold.size() - x * this.channels);
        final short[] tail = this.hold.drain();

        final AudioStream stream = openNext();
        if (stream == null) {
            this.out.addAll(tail);
            finish();
            return;
        }
        closeQuietly(this.current.stream);
        this.current = new Reader(stream);
        this.firstPass = false;
        this.inLead = false;
        this.passFrames = 0;
        this.silentRun = 0;

        for (int i = 0; i < this.leadFrames; i++)
            if (!this.current.next(this.frame))
                break;

        int i = 0;
        for (; i < x; i++) {
            if (!this.current.next(this.frame))
                break;
            final double theta = (i + 0.5D) / x * (Math.PI / 2D);
            final double fadeOut = Math.cos(theta);
            final double fadeIn = Math.sin(theta);
            for (int c = 0; c < this.channels; c++)
                this.mixed[c] = clamp(tail[i * this.channels + c] * fadeOut + this.frame[c] * fadeIn);
            this.out.addFrame(this.mixed);
            this.passFrames++;
        }
        for (; i < x; i++) {
            final double fadeOut = Math.cos((i + 0.5D) / x * (Math.PI / 2D));
            for (int c = 0; c < this.channels; c++)
                this.mixed[c] = clamp(tail[i * this.channels + c] * fadeOut);
            this.out.addFrame(this.mixed);
        }
    }

    @Nullable
    private AudioStream openNext() {
        try {
            final CompletableFuture<AudioStream> pending = this.next != null ? this.next : prefetch();
            final AudioStream stream = pending != null ? pending.join() : null;
            this.next = prefetch();
            return stream;
        } catch (final Throwable t) {
            SoundControl.LOGGER.warn("Unable to restart looping stream: %s", t.getMessage());
            this.next = null;
            return null;
        }
    }

    @Nullable
    private CompletableFuture<AudioStream> prefetch() {
        try {
            return this.reopen.get();
        } catch (final Throwable t) {
            return null;
        }
    }

    private void finish() {
        this.ended = true;
        this.hold.moveTo(this.out, this.hold.size());
    }

    private boolean isSilent(@Nonnull final short[] f) {
        for (final short v : f)
            if (v > SeamlessLoops.SILENCE || v < -SeamlessLoops.SILENCE)
                return false;
        return true;
    }

    private static short clamp(final double v) {
        return (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, Math.round(v)));
    }

    private static void closeQuietly(@Nullable final AudioStream stream) {
        if (stream == null)
            return;
        try {
            stream.close();
        } catch (final Throwable ignored) {
        }
    }

    private final class Reader {
        final AudioStream stream;
        @Nullable
        private ByteBuffer buffer;
        private boolean eof;

        Reader(@Nonnull final AudioStream stream) {
            this.stream = stream;
        }

        boolean next(@Nonnull final short[] f) throws IOException {
            final int frameBytes = CrossfadeLoopStream.this.channels * 2;
            while (this.buffer == null || this.buffer.remaining() < frameBytes) {
                if (this.eof)
                    return false;
                this.buffer = this.stream.read(CHUNK_BYTES);
                if (this.buffer == null || !this.buffer.hasRemaining()) {
                    this.buffer = null;
                    this.eof = true;
                    return false;
                }
            }
            for (int c = 0; c < f.length; c++)
                f[c] = this.buffer.getShort();
            return true;
        }
    }

    private static final class ShortQueue {
        private short[] data = new short[16384];
        private int head;
        private int size;

        int size() {
            return this.size;
        }

        private void ensure(final int capacity) {
            if (capacity <= this.data.length)
                return;
            int len = this.data.length;
            while (len < capacity)
                len <<= 1;
            final short[] grown = new short[len];
            for (int i = 0; i < this.size; i++)
                grown[i] = this.data[(this.head + i) & (this.data.length - 1)];
            this.data = grown;
            this.head = 0;
        }

        void add(final short v) {
            ensure(this.size + 1);
            this.data[(this.head + this.size) & (this.data.length - 1)] = v;
            this.size++;
        }

        void addFrame(@Nonnull final short[] f) {
            for (final short v : f)
                add(v);
        }

        void addAll(@Nonnull final short[] values) {
            for (final short v : values)
                add(v);
        }

        short poll() {
            final short v = this.data[this.head];
            this.head = (this.head + 1) & (this.data.length - 1);
            this.size--;
            return v;
        }

        void moveTo(@Nonnull final ShortQueue target, final int count) {
            for (int i = 0; i < count && this.size > 0; i++)
                target.add(poll());
        }

        void dropLast(final int count) {
            this.size -= Math.max(0, Math.min(count, this.size));
        }

        @Nonnull
        short[] drain() {
            final short[] result = new short[this.size];
            for (int i = 0; i < result.length; i++)
                result[i] = poll();
            return result;
        }
    }
}
