/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
/*
 * Dynamic Surroundings: Sound Control
 * Copyright (C) 2019  OreCruncher
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

package dynamiclabs.immersivefx.sndctrl.audio;

import dynamiclabs.immersivefx.platform.StreamingSoundInstance;
import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.audio.Channel;
import dynamiclabs.immersivefx.lib.TickCounter;
import dynamiclabs.immersivefx.sndctrl.api.sound.ISoundInstance;
import dynamiclabs.immersivefx.sndctrl.mixins.AccessorChannel;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class SurroundSoundInstance extends FadableSoundInstance implements StreamingSoundInstance {

    private static final float GAIN_GLIDE = 0.15F;

    private double offsetX;
    private double offsetY;
    private double offsetZ;
    private final float startOffset;

    private float gain;
    private float gainTarget;
    private long lastGainTick = TickCounter.getTickCount();

    private double posX;
    private double posY;
    private double posZ;

    @Nullable
    private Sound staticSource;
    @Nullable
    private Sound staticSound;

    public SurroundSoundInstance(@Nonnull final ISoundInstance sound, @Nonnull final Vec3 offset, final float startOffset, final float gain) {
        super(sound);
        this.offsetX = offset.x;
        this.offsetY = offset.y;
        this.offsetZ = offset.z;
        this.startOffset = startOffset - (float) Math.floor(startOffset);
        this.gain = this.gainTarget = Math.max(0F, gain);
        updatePosition();
    }

    public void setOffset(@Nonnull final Vec3 offset) {
        this.offsetX = offset.x;
        this.offsetY = offset.y;
        this.offsetZ = offset.z;
        updatePosition();
    }

    public void setGain(final float gain) {
        this.gainTarget = Math.max(0F, gain);
    }

    public void setGainImmediate(final float gain) {
        this.gain = this.gainTarget = Math.max(0F, gain);
    }

    public float getGain() {
        return this.gain;
    }

    public float getStartOffset() {
        return this.startOffset;
    }

    @Override
    public float getVolume() {
        return super.getVolume() * this.gain;
    }

    @Override
    public void tick() {
        super.tick();

        updatePosition();

        final long now = TickCounter.getTickCount();
        final long delta = now - this.lastGainTick;
        if (delta > 0) {
            this.lastGainTick = now;
            for (long i = 0; i < Math.min(delta, 20); i++)
                this.gain += (this.gainTarget - this.gain) * GAIN_GLIDE;
            if (Math.abs(this.gainTarget - this.gain) < 0.001F)
                this.gain = this.gainTarget;
        }
    }

    private void updatePosition() {
        final Vec3 listener = getListenerPosition();
        if (listener != null) {
            this.posX = listener.x + this.offsetX;
            this.posY = listener.y + this.offsetY;
            this.posZ = listener.z + this.offsetZ;
        }
    }

    @Nullable
    public static Vec3 getListenerPosition() {
        final Minecraft mc = Minecraft.getInstance();
        final Camera camera = mc.gameRenderer.mainCamera();
        if (camera.isInitialized())
            return camera.position();
        final Entity entity = mc.getCameraEntity();
        return entity != null ? entity.getEyePosition() : null;
    }

    @Override
    public double getX() {
        return this.posX;
    }

    @Override
    public double getY() {
        return this.posY;
    }

    @Override
    public double getZ() {
        return this.posZ;
    }

    @Override
    public boolean isRelative() {
        return false;
    }

    @Nonnull
    @Override
    public Attenuation getAttenuation() {
        return Attenuation.NONE;
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Nullable
    @Override
    public Sound getSound() {
        final Sound sound = super.getSound();
        if (sound == null || !sound.shouldStream())
            return sound;
        if (this.staticSource != sound || this.staticSound == null) {
            this.staticSource = sound;
            this.staticSound = new Sound(
                    sound.getLocation(),
                    sound.getVolume(),
                    sound.getPitch(),
                    sound.getWeight(),
                    sound.getType(),
                    false,
                    sound.shouldPreload(),
                    sound.getAttenuationDistance());
        }
        return this.staticSound;
    }

    @Nonnull
    @Override
    public CompletableFuture<AudioStream> getStream(@Nonnull final SoundBufferLibrary soundBuffers, @Nonnull final Sound sound, final boolean looping) {
        return StreamingSoundInstance.vanillaStream(soundBuffers, sound, looping).thenApply(Conversion::convert);
    }

    public static void applyStartOffset(@Nonnull final Channel channel, @Nonnull final SurroundSoundInstance sound) {
        final float offset = sound.getStartOffset();
        if (offset <= 0F || !sound.isLooping() || sound.getDelay() > 0)
            return;
        final int source = ((AccessorChannel) channel).sndctrl_getSource();
        final int buffer = AL10.alGetSourcei(source, AL10.AL_BUFFER);
        if (buffer != 0) {
            final int size = AL10.alGetBufferi(buffer, AL10.AL_SIZE);
            final int bits = AL10.alGetBufferi(buffer, AL10.AL_BITS);
            final int channels = AL10.alGetBufferi(buffer, AL10.AL_CHANNELS);
            final int frameBytes = Math.max(1, (bits >> 3) * channels);
            final int samples = size / frameBytes;
            if (samples > 1) {
                final int sample = Math.min(samples - 1, (int) (offset * samples));
                AL10.alSourcei(source, AL11.AL_SAMPLE_OFFSET, sample);
            }
        }
        AL10.alGetError();
    }

    @Override
    @Nonnull
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .addValue(getIdentifier().toString())
                .addValue(getState().toString())
                .add("v", getVolume())
                .add("g", this.gain)
                .add("o", String.format("%.0f,%.0f", this.offsetX, this.offsetZ))
                .toString();
    }
}
