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

package dynamiclabs.immersivefx.environs.fog;

import dynamiclabs.immersivefx.environs.library.BiomeInfo;
import dynamiclabs.immersivefx.environs.library.BiomeUtil;
import dynamiclabs.immersivefx.lib.GameUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.attribute.GaussianSampler;
import net.minecraft.world.level.biome.BiomeManager;
import dynamiclabs.immersivefx.platform.event.RenderFogEvent;
import dynamiclabs.immersivefx.environs.config.Config;

import javax.annotation.Nonnull;

/**
 * Scans the biome area around the player to determine the fog parameters.
 */
public class BiomeFogRangeCalculator extends VanillaFogRangeCalculator {

    protected final FogResult cached = new FogResult();

    public BiomeFogRangeCalculator() {
        super("BiomeFogRangeCalculator");
    }

    @Override
    public boolean enabled() {
        return Config.CLIENT.fog.enableBiomeFog.get();
    }

    @Override
    @Nonnull
    public FogResult calculate(@Nonnull final RenderFogEvent event) {

        final ClientLevel world = GameUtils.getWorld();
        assert world != null;

        final BiomeManager biomemanager = world.getBiomeManager();
        final double[] survey = new double[2];
        GaussianSampler.sample(event.getCamera().position().scale(0.25D), biomemanager::getNoiseBiomeAtQuart, (weight, biome) -> {
            final BiomeInfo info = BiomeUtil.getBiomeData(biome.value());
            survey[0] += weight * info.getVisibility();
            survey[1] += weight;
        });

        // Lower values means less visibility
        final double visibility = survey[1] > 0 ? survey[0] / survey[1] : 1D;
        final double farPlaneDistance = visibility * FogResult.renderDistance(event);
        final double farPlaneDistanceScaleBiome = 0.1D * (1D - visibility) + FogResult.DEFAULT_PLANE_SCALE * visibility;

        this.cached.setScaled((float) farPlaneDistance, (float) farPlaneDistanceScaleBiome);

        return cached;
    }

}
