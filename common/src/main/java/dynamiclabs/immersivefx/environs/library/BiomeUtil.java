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

import dynamiclabs.immersivefx.environs.misc.IMixinBiomeData;
import dynamiclabs.immersivefx.lib.fml.ForgeUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public final class BiomeUtil {

    private BiomeUtil() {
    }

    @Nonnull
    public static BiomeInfo getBiomeData(@Nonnull final Biome biome) {
        BiomeInfo result = ((IMixinBiomeData) (Object) biome).getInfo();
        if (result == null) {
            final BiomeAdapter handler = new BiomeAdapter(biome);
            result = new BiomeInfo(handler);
            ((IMixinBiomeData) (Object) biome).setInfo(result);
        }
        return result;
    }

    public static void setBiomeData(@Nonnull final Biome biome, @Nullable final BiomeInfo data) {
        ((IMixinBiomeData) (Object) biome).setInfo(data);
    }

    @Nullable
    public static Identifier getKey(@Nonnull final Biome biome) {
        return ForgeUtils.getBiomeRegistry().map(r -> r.getKey(biome)).orElse(null);
    }

    @Nonnull
    public static Optional<Holder<Biome>> getHolder(@Nonnull final Biome biome) {
        final Optional<Registry<Biome>> registry = ForgeUtils.getBiomeRegistry();
        if (registry.isEmpty() || registry.get().getKey(biome) == null)
            return Optional.empty();
        return Optional.of(registry.get().wrapAsHolder(biome));
    }
}
