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

package dynamiclabs.immersivefx.environs.mixins;

import dynamiclabs.immersivefx.environs.library.BiomeInfo;
import dynamiclabs.immersivefx.environs.misc.IMixinBiomeData;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;

@Mixin(Biome.class)
public class MixinBiome implements IMixinBiomeData {

    @Unique
    private BiomeInfo environs_biomeInfo;

    @Nullable
    @Override
    public BiomeInfo getInfo() {
        return this.environs_biomeInfo;
    }

    @Override
    public void setInfo(@Nullable BiomeInfo info) {
        this.environs_biomeInfo = info;
    }
}
