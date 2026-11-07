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

package dynamiclabs.immersivefx.lib.biomes;

import dynamiclabs.immersivefx.platform.Platform;
import dynamiclabs.immersivefx.lib.GameUtils;
import dynamiclabs.immersivefx.lib.Localization;
import dynamiclabs.immersivefx.lib.fml.ForgeUtils;
import dynamiclabs.immersivefx.lib.gui.Color;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.FluidState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public class BiomeUtilities {
    private BiomeUtilities() {

    }

    private static final Color NO_COLOR = new Color(1F, 1F, 1F);

    @Nullable
    public static Identifier getBiomeId(@Nonnull final Biome biome) {
        return ForgeUtils.getBiomeRegistry().map(r -> r.getKey(biome)).orElse(null);
    }

    @Nullable
    public static Holder<Biome> getBiomeHolder(@Nonnull final Biome biome) {
        final Optional<Registry<Biome>> registry = ForgeUtils.getBiomeRegistry();
        if (registry.isEmpty())
            return null;
        final Optional<ResourceKey<Biome>> key = registry.get().getResourceKey(biome);
        return key.flatMap(k -> registry.get().get(k)).orElse(null);
    }

    @Nonnull
    public static String getBiomeName(@Nonnull final Biome biome) {
        final Identifier loc = getBiomeId(biome);
        if (loc == null)
            return "UNKNOWN";
        return getBiomeName(loc);
    }

    @Nonnull
    public static String getBiomeName(@Nonnull final Holder<Biome> biome) {
        return biome.unwrapKey().map(k -> getBiomeName(k.identifier())).orElse("UNKNOWN");
    }

    @Nonnull
    public static String getBiomeName(@Nonnull final Identifier loc) {
        final String fmt = String.format("biome.%s.%s", loc.getNamespace(), loc.getPath());
        return Localization.load(fmt);
    }

    // ===================================
    //
    // Miscellaneous Support Functions
    //
    // ===================================

    @Nonnull
    public static Collection<BiomeDictionary.Type> getBiomeTypes() {
        return BiomeDictionary.Type.getAll();
    }

    @Nonnull
    public static Color getColorForLiquid(@Nonnull final BlockGetter world, @Nonnull final BlockPos pos) {
        final FluidState fluidState = world.getFluidState(pos);

        if (fluidState.isEmpty())
            return NO_COLOR;

        // If the fluid is water, need to check the biome for coloration
        if (fluidState.is(FluidTags.WATER)) {
            final Biome biome = BiomeUtilities.getClientBiome(pos);
            if (biome != null)
                return new Color(biome.getWaterColor());
        }

        return new Color(0xFFFFFFFF);
    }

    @Nonnull
    public static Set<BiomeDictionary.Type> getBiomeTypes(@Nonnull final Biome biome) {
        return BiomeDictionary.getTypes(biome);
    }

    @Nonnull
    public static Set<BiomeDictionary.Type> getBiomeTypes(@Nonnull final Holder<Biome> biome) {
        return BiomeDictionary.getTypes(biome);
    }

    @Nonnull
    public static Biome.Precipitation getPrecipitationType(@Nonnull final Biome biome) {
        if (!biome.hasPrecipitation())
            return Biome.Precipitation.NONE;
        return biome.getBaseTemperature() < 0.15F ? Biome.Precipitation.SNOW : Biome.Precipitation.RAIN;
    }

    public static float getRainfall(@Nonnull final Biome biome) {
        return Platform.get().getClimateSettings(biome).downfall();
    }

    public static float getTemperature(@Nonnull final Biome biome) {
        return biome.getBaseTemperature();
    }

    @Nullable
    public static Holder<Biome> getClientBiomeHolder(@Nonnull final BlockPos pos) {
        final ClientLevel world = GameUtils.getWorld();
        if (world == null)
            return null;
        return world.getBiome(pos);
    }

    @Nullable
    public static Biome getClientBiome(@Nonnull final BlockPos pos) {
        final Holder<Biome> holder = getClientBiomeHolder(pos);
        return holder != null ? holder.value() : null;
    }

    @Nullable
    public static Biome getClientBiome(@Nonnull final Biome biome) {
        if (GameUtils.getWorld() == null)
            return null;
        return getBiomeId(biome) != null ? biome : null;
    }
}
