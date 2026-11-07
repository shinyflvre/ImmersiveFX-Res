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

import dynamiclabs.immersivefx.platform.Platform;
import dynamiclabs.immersivefx.lib.biomes.BiomeDictionary;

import java.util.Set;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import dynamiclabs.immersivefx.lib.GameUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.Noise;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import net.minecraft.world.level.biome.Biome;
import dynamiclabs.immersivefx.lib.biomes.BiomeUtilities;

public class BiomeAdapter implements IBiome {

	private static final Noise TEMPERATURE_NOISE = new SimplexNoise(new WorldgenRandom(new LegacyRandomSource(1234L)), true);

	protected final Biome biome;
	protected final Identifier key;
	protected final Set<BiomeDictionary.Type> types;

	public BiomeAdapter(@Nonnull final Biome biome) {
		this.biome = biome;
		this.key = BiomeUtil.getKey(biome);
		this.types = ImmutableSet.copyOf(BiomeUtilities.getBiomeTypes(biome));
	}

	@Override
	public Biome getBiome() {
		return this.biome;
	}

	@Override
	public Identifier getKey() {
		return this.key;
	}

	@Override
	public String getName() {
		return BiomeUtilities.getBiomeName(this.biome);
	}

	@Override
	public Set<BiomeDictionary.Type> getTypes() {
		return this.types;
	}

	@Override
	public Biome.Precipitation getPrecipitationType() {
		if (!this.biome.hasPrecipitation())
			return Biome.Precipitation.NONE;
		return this.biome.getBaseTemperature() < 0.15F ? Biome.Precipitation.SNOW : Biome.Precipitation.RAIN;
	}

	@Override
	public float getFloatTemperature(@Nonnull final BlockPos pos) {
		final int seaLevel = GameUtils.getWorld() != null ? GameUtils.getWorld().getSeaLevel() : 63;
		final float adjusted = Platform.get().getClimateSettings(this.biome).temperatureModifier().modifyTemperature(pos, this.biome.getBaseTemperature());
		final int snowLevel = seaLevel + 17;
		if (pos.getY() > snowLevel) {
			final float v = TEMPERATURE_NOISE.get(pos.getX() / 8.0F, pos.getZ() / 8.0F) * 8.0F;
			return adjusted - (v + pos.getY() - snowLevel) * 0.05F / 40.0F;
		}
		return adjusted;
	}

	@Override
	public float getTemperature() {
		return this.biome.getBaseTemperature();
	}

	@Override
	public boolean isHighHumidity() {
		return getDownfall() > 0.85F;
	}

	@Override
	public float getDownfall() {
		return Platform.get().getClimateSettings(this.biome).downfall();
	}

	@Override
	public boolean isFake() {
		return false;
	}
}
