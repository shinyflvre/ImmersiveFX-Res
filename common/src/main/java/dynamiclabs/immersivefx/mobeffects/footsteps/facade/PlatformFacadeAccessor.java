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

package dynamiclabs.immersivefx.mobeffects.footsteps.facade;

import dynamiclabs.immersivefx.mobeffects.MobEffects;
import dynamiclabs.immersivefx.platform.Platform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

final class PlatformFacadeAccessor implements IFacadeAccessor {

    @Override
    @Nonnull
    public String getName() {
        final String api = Platform.get().getBlockAppearanceApi();
        return api != null ? api : "none";
    }

    @Override
    public boolean instanceOf(@Nonnull final Block block) {
        try {
            return Platform.get().hasCustomAppearance(block);
        } catch (@Nonnull final Throwable t) {
            return false;
        }
    }

    @Override
    public boolean isValid() {
        return Platform.get().getBlockAppearanceApi() != null;
    }

    @Override
    @Nullable
    public BlockState getBlockState(@Nonnull final LivingEntity entity, @Nonnull final BlockState state,
                                    @Nonnull final BlockGetter world, @Nonnull final Vec3 pos, @Nullable final Direction side) {
        if (world instanceof BlockAndLightGetter level) {
            try {
                final BlockState appearance = Platform.get().getAppearance(state, level, BlockPos.containing(pos), side == null ? Direction.UP : side);
                return appearance == state ? null : appearance;
            } catch (@Nonnull final Throwable t) {
                MobEffects.LOGGER.error(t, "Unable to obtain the appearance of %s", state);
            }
        }
        return null;
    }
}
