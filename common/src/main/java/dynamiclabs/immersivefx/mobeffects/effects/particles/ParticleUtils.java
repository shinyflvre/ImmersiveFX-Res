/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
/*
 *  Dynamic Surroundings: Mob Effects
 *  Copyright (C) 2019  OreCruncher
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

package dynamiclabs.immersivefx.mobeffects.effects.particles;

import dynamiclabs.immersivefx.lib.GameUtils;
import dynamiclabs.immersivefx.lib.random.XorShiftRandom;
import dynamiclabs.immersivefx.mobeffects.mixins.AccessorParticleEngine;
import dynamiclabs.immersivefx.mobeffects.mixins.AccessorParticleResources;
import net.minecraft.client.particle.ParticleResources;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.Random;

public final class ParticleUtils {
    private static final Random RANDOM = XorShiftRandom.current();
    private static final RandomSource SPRITE_RANDOM = RandomSource.create();

    private ParticleUtils() {

    }

    @Nonnull
    public static Vec3 getBreathOrigin(@Nonnull final LivingEntity entity) {
        final Vec3 eyePosition = eyePosition(entity).subtract(0D, entity.isBaby() ? 0.1D : 0.2D, 0D);
        final Vec3 look = entity.getViewVector(1F); // Don't use the other look vector method!
        return eyePosition.add(look.scale(entity.isBaby() ? 0.25D : 0.5D));
    }

    @Nonnull
    public static Vec3 getLookTrajectory(@Nonnull final LivingEntity entity) {
        return entity.getViewVector(1F).yRot(RANDOM.nextFloat() * 2F).xRot(RANDOM.nextFloat() * 2F)
                .normalize();
    }

    @Nonnull
    public static SpriteSet getSpriteSet(@Nonnull final ParticleType<?> type) {
        final ParticleResources resources = ((AccessorParticleEngine) GameUtils.getMC().particleEngine).mobeffects$getResourceManager();
        final Identifier id = BuiltInRegistries.PARTICLE_TYPE.getKey(type);
        final SpriteSet set = ((AccessorParticleResources) (Object) resources).mobeffects$getSpriteSets().get(id);
        if (set == null)
            throw new IllegalStateException("No sprite set registered for particle type " + id);
        return set;
    }

    @Nonnull
    public static RandomSource spriteRandom() {
        return SPRITE_RANDOM;
    }

    /*
     * Use some corrective lenses because the MC routine just doesn't lower the
     * height enough for our rendering purpose.
     */
    private static Vec3 eyePosition(final Entity e) {
        Vec3 t = e.getEyePosition(1F);
        if (e.isShiftKeyDown())
            t = t.subtract(0D, 0.25D, 0D);
        return t;
    }

}
