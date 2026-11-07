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

import dynamiclabs.immersivefx.lib.random.XorShiftRandom;
import dynamiclabs.immersivefx.sndctrl.api.acoustics.IAcousticFactory;
import dynamiclabs.immersivefx.sndctrl.audio.BackgroundSoundInstance;
import dynamiclabs.immersivefx.sndctrl.audio.EntitySoundInstance;
import dynamiclabs.immersivefx.sndctrl.audio.SoundInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import dynamiclabs.immersivefx.sndctrl.api.sound.IFadableSoundInstance;
import dynamiclabs.immersivefx.sndctrl.api.sound.ISoundCategory;
import dynamiclabs.immersivefx.sndctrl.api.sound.ISoundInstance;
import dynamiclabs.immersivefx.sndctrl.api.sound.SoundBuilder;
import dynamiclabs.immersivefx.sndctrl.api.sound.Category;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * Helper that creates sound instances using a SoundBuilder, but tweaks based on the circumstances requested.
 */
public class AcousticFactory extends SoundBuilder implements IAcousticFactory {

    public static final int SOUND_RANGE = 16;
    private static final Random RANDOM = new XorShiftRandom();

    public AcousticFactory(@Nonnull final SoundEvent event) {
        super(event, Category.AMBIENT);
    }

    public AcousticFactory(@Nonnull final SoundEvent event, @Nonnull final ISoundCategory category) {
        super(event, category);
    }

    private static Vec3 randomPoint(final int minRange, final int maxRange) {
        // Establish a random unit vector
        final double x = RANDOM.nextDouble() - 0.5D;
        final double y = RANDOM.nextDouble() - 0.5D;
        final double z = RANDOM.nextDouble() - 0.5D;
        final Vec3 vec = new Vec3(x, y, z).normalize();

        // Establish the range and scaling value
        final int range = maxRange - minRange;
        final double magnitude;

        if (range <= 0) {
            magnitude = minRange;
        } else {
            magnitude = minRange + RANDOM.nextDouble() * range;
        }

        // Generate a vector based on the generated scaling values
        return vec.scale(magnitude);
    }

    /**
     * Creates a sound instance that is non-attenuated
     */
    @Override
    @Nonnull
    public ISoundInstance createSound() {
        return createSoundAt(BlockPos.ZERO);
    }

    /**
     * Creates a sound instance centered in the block at the specified position.
     */
    @Override
    @Nonnull
    public ISoundInstance createSoundAt(@Nonnull final BlockPos pos) {
        final SoundInstance sound = makeSound();
        sound.setPosition(pos);
        return sound;
    }

    /**
     * Creates a sound instance centered at the specified location.
     */
    @Override
    @Nonnull
    public ISoundInstance createSoundAt(@Nonnull final Vec3 pos) {
        final SoundInstance sound = makeSound();
        sound.setPosition(pos);
        return sound;
    }

    /**
     * Creates a sound instance within a random range centered on the specified entity.
     * @param entity Entity on which the sound will be centered
     * @return Sound instance for playing
     */
    @Override
    @Nonnull
    public ISoundInstance createSoundNear(@Nonnull final Entity entity) {
        return createSoundNear(entity, 0, SOUND_RANGE);
    }

    /**
     * Creates a sound instance within a random range centered on the specific entity.  The range variation is set by
     * range parameters.
     * @param entity Entity on which the sound will be centered
     * @param minRange Minimum range from the entity
     * @param maxRange Maximum range from the entity
     * @return Sound instance for playing
     */
    @Nonnull
    public ISoundInstance createSoundNear(@Nonnull final Entity entity, final int minRange, final int maxRange) {
        final Vec3 offset = randomPoint(minRange, maxRange);
        final float posX = (float) (entity.getX() + offset.x);
        final float posY = (float) (entity.getY() + entity.getEyeHeight() + offset.y);
        final float posZ = (float) (entity.getZ() + offset.z);
        final SoundInstance sound = makeSound();
        sound.setPosition(posX, posY, posZ);
        return sound;
    }

    /**
     * Attaches a sound instance to the specified entity, and will move as the entity moves.
     */
    @Override
    @Nonnull
    public ISoundInstance attachSound(@Nonnull final Entity entity) {
        return new EntitySoundInstance(entity, makeSound());
    }

    /**
     * Creates a sound instance with no attenuation and is not affected by range or other sound effects.
     */
    @Override
    @Nonnull
    public IFadableSoundInstance createBackgroundSound() {
        return new BackgroundSoundInstance(createSound());
    }

}
