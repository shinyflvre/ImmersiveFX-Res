/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
/*
 *  Dynamic Surroundings: Environs
 *  Copyright (C) 2020  OreCruncher
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

package dynamiclabs.immersivefx.environs.handlers;

import dynamiclabs.immersivefx.lib.random.XorShiftRandom;
import dynamiclabs.immersivefx.sndctrl.api.acoustics.IAcoustic;
import dynamiclabs.immersivefx.sndctrl.api.acoustics.IAcousticFactory;
import dynamiclabs.immersivefx.sndctrl.audio.SurroundSoundInstance;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

final class SurroundAcousticEmitter {

    static final double EMITTER_DISTANCE = 8D;

    private static final Random RANDOM = new XorShiftRandom();

    @Nonnull
    private final IAcoustic acousticSource;
    @Nonnull
    private final BackgroundAcousticEmitter[] emitters;
    @Nonnull
    private final SurroundSoundInstance[] sounds;

    private SurroundAcousticEmitter(@Nonnull final IAcoustic acoustic, @Nonnull final IAcousticFactory factory, @Nonnull final float[] gains) {
        this.acousticSource = acoustic;
        final int count = AmbienceDirections.EMITTERS;
        this.emitters = new BackgroundAcousticEmitter[count];
        this.sounds = new SurroundSoundInstance[count];
        final float base = RANDOM.nextFloat();
        for (int i = 0; i < count; i++) {
            final double angle = AmbienceDirections.emitterAngle(i);
            final Vec3 offset = new Vec3(Math.cos(angle) * EMITTER_DISTANCE, 0D, Math.sin(angle) * EMITTER_DISTANCE);
            final float startOffset = base + (float) i / count;
            this.sounds[i] = new SurroundSoundInstance(factory.createSound(), offset, startOffset, gains[i]);
            this.emitters[i] = new BackgroundAcousticEmitter(acoustic, this.sounds[i]);
        }
    }

    @Nullable
    static SurroundAcousticEmitter create(@Nonnull final IAcoustic acoustic, @Nonnull final float[] gains) {
        final IAcousticFactory factory = acoustic.getFactory();
        if (factory == null)
            return null;
        return new SurroundAcousticEmitter(acoustic, factory, gains);
    }

    @Nonnull
    public IAcoustic getSource() {
        return this.acousticSource;
    }

    public void tick() {
        for (final BackgroundAcousticEmitter e : this.emitters)
            e.tick();
    }

    public void setVolumeThrottle(final float throttle) {
        for (final BackgroundAcousticEmitter e : this.emitters)
            e.setVolumeThrottle(throttle);
    }

    public void setGains(@Nonnull final float[] gains) {
        for (int i = 0; i < this.sounds.length; i++)
            this.sounds[i].setGain(gains[i]);
    }

    public void fade() {
        for (final BackgroundAcousticEmitter e : this.emitters)
            e.fade();
    }

    public void unfade() {
        for (final BackgroundAcousticEmitter e : this.emitters)
            e.unfade();
    }

    public boolean isFading() {
        return this.emitters[0].isFading();
    }

    public boolean isDonePlaying() {
        boolean all = true;
        boolean any = false;
        for (final BackgroundAcousticEmitter e : this.emitters) {
            final boolean done = e.isDonePlaying();
            all &= done;
            any |= done;
        }
        return all || (any && isFading());
    }

    public void stop() {
        for (final BackgroundAcousticEmitter e : this.emitters)
            e.stop();
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(this.acousticSource.getName()).append(" [");
        for (int i = 0; i < this.sounds.length; i++) {
            if (i > 0)
                builder.append(' ');
            builder.append(String.format("%.2f", this.sounds[i].getVolume()));
        }
        builder.append("] ").append(this.sounds[0].getState());
        return builder.toString();
    }
}
