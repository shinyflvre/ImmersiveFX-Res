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

import com.google.common.base.MoreObjects;
import dynamiclabs.immersivefx.lib.TickCounter;
import dynamiclabs.immersivefx.lib.math.MathStuff;
import dynamiclabs.immersivefx.sndctrl.api.sound.IFadableSoundInstance;
import dynamiclabs.immersivefx.sndctrl.api.sound.ISoundCategory;
import dynamiclabs.immersivefx.sndctrl.api.sound.ISoundInstance;
import dynamiclabs.immersivefx.sndctrl.config.Config;

import javax.annotation.Nonnull;

public class FadingLoopSoundInstance extends LoopingSoundInstance implements IFadableSoundInstance {

    private static final float RANGE_GLIDE = 0.2F;

    private final int fadeTicks;
    private float fade;
    private boolean fading;
    private boolean done;
    private float fadeVolume = 1F;
    private float rangeGain = 1F;
    private float rangeGainTarget = 1F;
    private boolean rangeGainSet;
    private long lastTick = TickCounter.getTickCount();

    public FadingLoopSoundInstance(@Nonnull final ISoundInstance sound, @Nonnull final ISoundCategory category) {
        super(sound, category);
        this.fadeTicks = configuredFadeTicks();
    }

    public FadingLoopSoundInstance(@Nonnull final ISoundInstance sound) {
        super(sound);
        this.fadeTicks = configuredFadeTicks();
    }

    public static int configuredFadeTicks() {
        try {
            return Math.max(0, Math.round(Config.CLIENT.sound.fadeInTime.get() / 50F));
        } catch (final Throwable t) {
            return 30;
        }
    }

    private static float curve(final float p) {
        return p * p * (3F - 2F * p);
    }

    public void setRangeGain(final float gain) {
        this.rangeGainTarget = MathStuff.clamp1(gain);
        if (!this.rangeGainSet) {
            this.rangeGainSet = true;
            this.rangeGain = this.rangeGainTarget;
        }
    }

    public float getRangeGain() {
        return this.rangeGain;
    }

    @Override
    public float getVolume() {
        return super.getVolume() * curve(this.fade) * this.fadeVolume * this.rangeGain;
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public void setState(@Nonnull final SoundState state) {
        if (state == SoundState.QUEUING) {
            this.fade = 0F;
            this.fading = false;
            this.done = false;
            this.lastTick = TickCounter.getTickCount();
        }
        super.setState(state);
    }

    @Override
    public boolean isStopped() {
        return this.done || super.isStopped();
    }

    @Override
    public void tick() {
        final long now = TickCounter.getTickCount();
        final long delta = now - this.lastTick;
        if (delta < 1)
            return;
        this.lastTick = now;

        super.tick();
        if (this.done)
            return;

        final float step = this.fadeTicks <= 0 ? 1F : (float) delta / this.fadeTicks;
        if (this.fading) {
            this.fade -= step;
            if (this.fade <= 0F) {
                this.fade = 0F;
                this.done = true;
            }
        } else {
            this.fade = Math.min(1F, this.fade + step);
        }

        for (long i = 0; i < Math.min(delta, 20); i++)
            this.rangeGain += (this.rangeGainTarget - this.rangeGain) * RANGE_GLIDE;
        if (Math.abs(this.rangeGainTarget - this.rangeGain) < 0.001F)
            this.rangeGain = this.rangeGainTarget;
    }

    @Override
    public void noFade() {
        this.fading = false;
        this.fade = 1F;
    }

    @Override
    public void fade() {
        this.fading = true;
    }

    @Override
    public void unfade() {
        if (!this.done)
            this.fading = false;
    }

    @Override
    public boolean isFading() {
        return this.fading;
    }

    @Override
    public void setFadeVolume(final float v) {
        this.fadeVolume = MathStuff.clamp1(v);
    }

    @Override
    @Nonnull
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .addValue(getIdentifier().toString())
                .addValue(getSoundCategory().toString())
                .addValue(getState().toString())
                .add("v", getVolume())
                .add("f", this.fade)
                .add("r", this.rangeGain)
                .add("x", getX())
                .add("y", getY())
                .add("z", getZ())
                .toString();
    }
}
