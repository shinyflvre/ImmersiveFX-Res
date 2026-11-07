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

package dynamiclabs.immersivefx.environs.library;

import dynamiclabs.immersivefx.lib.biomes.BiomeDictionary;

import java.util.Set;

import javax.annotation.Nonnull;

import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;

public interface IBiome {

	Biome getBiome();

	Identifier getKey();

	String getName();

	Set<BiomeDictionary.Type> getTypes();

	Biome.Precipitation getPrecipitationType();

	float getFloatTemperature(@Nonnull final BlockPos pos);

	float getTemperature();

	boolean isHighHumidity();

	float getDownfall();

	boolean isFake();
}
