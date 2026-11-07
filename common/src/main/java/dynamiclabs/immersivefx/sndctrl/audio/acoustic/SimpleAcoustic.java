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

package dynamiclabs.immersivefx.sndctrl.audio.acoustic;

import com.google.common.base.MoreObjects;
import dynamiclabs.immersivefx.sndctrl.api.acoustics.AcousticEvent;
import dynamiclabs.immersivefx.sndctrl.api.acoustics.IAcoustic;
import dynamiclabs.immersivefx.sndctrl.api.acoustics.IAcousticFactory;
import dynamiclabs.immersivefx.sndctrl.api.sound.ISoundCategory;
import dynamiclabs.immersivefx.sndctrl.api.sound.ISoundInstance;
import dynamiclabs.immersivefx.sndctrl.audio.AudioEngine;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * A simple acoustic that uses an AcousticFactory to produce sound instances for playing.
 */
public class SimpleAcoustic implements IAcoustic {

    private final AcousticFactory factory;
    private final Identifier name;

    public SimpleAcoustic(@Nonnull final SoundEvent event, @Nonnull final ISoundCategory category) {
        this(event.location(), event);
        this.factory.setCategory(category);
    }

    public SimpleAcoustic(@Nonnull final SoundEvent event) {
        this(event.location(), event);
    }

    public SimpleAcoustic(@Nonnull final Identifier name, @Nonnull final SoundEvent evt) {
        this(name, new AcousticFactory(evt));
    }

    public SimpleAcoustic(@Nonnull final Identifier name, @Nonnull final AcousticFactory factory) {
        this.name = Objects.requireNonNull(name);
        this.factory = factory;
    }

    @Nonnull
    public AcousticFactory getFactory() {
        return this.factory;
    }

    @Nonnull
    public Identifier getName() {
        return this.name;
    }

    @Override
    public void play(@Nonnull final AcousticEvent ignored) {
        play(this.factory.createSound());
    }

    @Override
    public void playAt(@Nonnull final BlockPos pos, @Nonnull final AcousticEvent ignored) {
        play(this.factory.createSoundAt(pos));
    }

    @Override
    public void playAt(@Nonnull final Vec3 pos, @Nonnull final AcousticEvent ignored) {
        play(this.factory.createSoundAt(pos));
    }

    @Override
    public void playNear(@Nonnull final Entity entity, @Nonnull final AcousticEvent ignored) {
        play(this.factory.createSoundNear(entity));
    }

    @Override
    public void playNear(@Nonnull final Entity entity, @Nonnull final AcousticEvent ignored, final int minRange, final int maxRange) {
        play(this.factory.createSoundNear(entity, minRange, maxRange));
    }

    @Override
    public void playBackground(@Nonnull final AcousticEvent ignored) {
        play(this.factory.createBackgroundSound());
    }

    @Override
    public IAcousticFactory getFactory(@Nonnull final AcousticEvent event) {
        return this.factory;
    }

    protected void play(@Nonnull final ISoundInstance sound) {
        AudioEngine.play(sound);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).addValue(getName().toString()).toString();
    }
}
