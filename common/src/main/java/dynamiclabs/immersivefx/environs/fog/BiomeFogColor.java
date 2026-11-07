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

package dynamiclabs.immersivefx.environs.fog;

import dynamiclabs.immersivefx.environs.config.Config;
import dynamiclabs.immersivefx.environs.handlers.FogHandler;
import dynamiclabs.immersivefx.environs.library.BiomeInfo;
import dynamiclabs.immersivefx.environs.library.BiomeUtil;
import dynamiclabs.immersivefx.lib.gui.Color;
import net.minecraft.util.ARGB;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.biome.Biome;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.WeakHashMap;

public final class BiomeFogColor {

    private record Cached(EnvironmentAttributeMap source, Color color, EnvironmentAttributeMap result) {
    }

    private static final Map<Biome, Cached> CACHE = new WeakHashMap<>();

    private BiomeFogColor() {
    }

    public static boolean isActive() {
        try {
            return FogHandler.doFog() && Config.CLIENT.fog.enableBiomeFog.get();
        } catch (final IllegalStateException notLoaded) {
            return false;
        }
    }

    @Nonnull
    public static EnvironmentAttributeMap attributes(@Nonnull final Biome biome) {
        final EnvironmentAttributeMap source = biome.getAttributes();
        final BiomeInfo info = BiomeUtil.getBiomeData(biome);
        final Color color = info.getFogColor();
        if (color == null)
            return source;

        synchronized (CACHE) {
            final Cached cached = CACHE.get(biome);
            if (cached != null && cached.source() == source && cached.color() == color)
                return cached.result();
            final EnvironmentAttributeMap result = EnvironmentAttributeMap.builder()
                    .putAll(source)
                    .set(EnvironmentAttributes.FOG_COLOR, ARGB.vector3fFromRGB24(color.rgb()))
                    .build();
            CACHE.put(biome, new Cached(source, color, result));
            return result;
        }
    }
}
