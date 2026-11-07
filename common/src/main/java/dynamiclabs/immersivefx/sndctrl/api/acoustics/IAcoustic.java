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

package dynamiclabs.immersivefx.sndctrl.api.acoustics;

import net.minecraft.world.entity.Entity;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public interface IAcoustic {

    /**
     * Get's the name of the acoustic, if any
     *
     * @return Name of the acoustic
     */
    Identifier getName();

    /**
     * Play the acoustic on the MASTER channel with no attenuation.
     */
    default void play() {
        play(AcousticEvent.NONE);
    }

    void play(@Nonnull final AcousticEvent event);

    /**
     * Play the acoustic at the specified block position.
     */
    default void playAt(@Nonnull final BlockPos pos) {
        playAt(pos, AcousticEvent.NONE);
    }

    void playAt(@Nonnull final BlockPos pos, @Nonnull final AcousticEvent event);

    /**
     * Play the acoustic at the specified block position
     */
    default void playAt(@Nonnull final Vec3 pos) {
        playAt(pos, AcousticEvent.NONE);
    }

    void playAt(@Nonnull final Vec3 pos, @Nonnull final AcousticEvent event);

    /**
     * Play the acoustic near the entity
     */
    default void playNear(@Nonnull final Entity entity) {
        playNear(entity, AcousticEvent.NONE);
    }

    default void playNear(@Nonnull final Entity entity, final int minRange, final int maxRange) {
        playNear(entity, AcousticEvent.NONE, minRange, maxRange);
    }

    void playNear(@Nonnull final Entity entity, @Nonnull final AcousticEvent event);

    void playNear(@Nonnull final Entity entity, @Nonnull final AcousticEvent event, final int minRange, final int maxRange);

    /**
     * Play the acoustic in the background
     */
    default void playBackground() {
        playBackground(AcousticEvent.NONE);
    }

    void playBackground(@Nonnull final AcousticEvent event);

    /**
     * Obtains the underlying factory for creating sounds.
     */
    default IAcousticFactory getFactory() {
        return getFactory(AcousticEvent.NONE);
    }

    IAcousticFactory getFactory(@Nonnull final AcousticEvent event);
}
