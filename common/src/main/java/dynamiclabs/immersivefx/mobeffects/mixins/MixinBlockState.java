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

package dynamiclabs.immersivefx.mobeffects.mixins;

import dynamiclabs.immersivefx.mobeffects.misc.IMixinFootstepData;
import net.minecraft.world.level.block.state.BlockState;
import dynamiclabs.immersivefx.sndctrl.api.acoustics.IAcoustic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;

@Mixin(BlockState.class)
public class MixinBlockState implements IMixinFootstepData {

    @Unique
    private Boolean mobeffects_hasFootprint;
    @Unique
    private IAcoustic[] mobeffects_acoustics;

    @Override
    @Nullable
    public Boolean hasFootprint() {
        return this.mobeffects_hasFootprint;
    }

    @Override
    public void setHasFootprint(final boolean flag) {
        this.mobeffects_hasFootprint = flag;
    }

    @Override
    public void resetFootprint() {
        this.mobeffects_hasFootprint = null;
    }

    @Nullable
    @Override
    public IAcoustic[] getAcoustics() {
        return this.mobeffects_acoustics;
    }

    @Override
    public void setAcoustics(@Nullable final IAcoustic[] acoustics) {
        this.mobeffects_acoustics = acoustics;
    }
}
