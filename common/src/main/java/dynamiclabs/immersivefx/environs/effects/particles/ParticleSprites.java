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

package dynamiclabs.immersivefx.environs.effects.particles;

import dynamiclabs.immersivefx.environs.mixins.ParticleEngineAccessor;
import dynamiclabs.immersivefx.environs.mixins.ParticleResourcesAccessor;
import dynamiclabs.immersivefx.lib.GameUtils;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import javax.annotation.Nonnull;

public final class ParticleSprites {

    private ParticleSprites() {
    }

    @Nonnull
    public static SpriteSet get(@Nonnull final ParticleType<?> type) {
        final Identifier id = BuiltInRegistries.PARTICLE_TYPE.getKey(type);
        final SpriteSet set = ((ParticleResourcesAccessor) ((ParticleEngineAccessor) GameUtils.getMC().particleEngine).environs$getResourceManager())
                .environs$getSpriteSets().get(id);
        if (set == null)
            throw new IllegalStateException("No sprite set for particle type " + id);
        return set;
    }
}
