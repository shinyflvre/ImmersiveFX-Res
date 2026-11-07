/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
/*
 *  Dynamic Surroundings
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

package dynamiclabs.immersivefx.mobeffects.footsteps.accents;

import dynamiclabs.immersivefx.mobeffects.config.Config;
import dynamiclabs.immersivefx.mobeffects.library.FootstepLibrary;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.core.BlockPos;
import dynamiclabs.immersivefx.lib.collections.ObjectArray;
import dynamiclabs.immersivefx.sndctrl.api.acoustics.IAcoustic;

import javax.annotation.Nonnull;

class WaterLoggedAccent implements IFootstepAccentProvider {

    @Override
    public boolean isEnabled() {
        return Config.CLIENT.footsteps.enableWaterLoggedAccent.get();
    }

    @Override
    public void provide(
            @Nonnull final LivingEntity entity,
            @Nonnull final BlockPos blockPos,
            @Nonnull final BlockState posState,
            @Nonnull final ObjectArray<IAcoustic> acoustics) {
        if (posState.getBlock() instanceof SimpleWaterloggedBlock) {
            final FluidState fluid = posState.getFluidState();
            if (!fluid.isEmpty())
                acoustics.add(FootstepLibrary.getWaterLoggedAcoustic());
        }
    }
}
