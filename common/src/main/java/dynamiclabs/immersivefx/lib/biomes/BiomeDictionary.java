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

import dynamiclabs.immersivefx.platform.IPlatform;
import dynamiclabs.immersivefx.platform.Platform;
import dynamiclabs.immersivefx.platform.event.TagsUpdatedEvent;
import dynamiclabs.immersivefx.ImmersiveFX;
import com.google.common.collect.ImmutableSet;
import dynamiclabs.immersivefx.dsurround.DynamicSurroundings;
import dynamiclabs.immersivefx.lib.fml.ForgeUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import dynamiclabs.immersivefx.lib.tags.CommonTags;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class BiomeDictionary {

    private BiomeDictionary() {
    }

    public static final class Type {

        private static final Map<String, Type> byName = Collections.synchronizedMap(new LinkedHashMap<>());

        public static final Type HOT = new Type("HOT");
        public static final Type COLD = new Type("COLD");

        public static final Type SPARSE = new Type("SPARSE");
        public static final Type DENSE = new Type("DENSE");

        public static final Type WET = new Type("WET");
        public static final Type DRY = new Type("DRY");

        public static final Type SAVANNA = new Type("SAVANNA");
        public static final Type CONIFEROUS = new Type("CONIFEROUS");
        public static final Type JUNGLE = new Type("JUNGLE");

        public static final Type SPOOKY = new Type("SPOOKY");
        public static final Type DEAD = new Type("DEAD");
        public static final Type LUSH = new Type("LUSH");
        public static final Type MUSHROOM = new Type("MUSHROOM");
        public static final Type MAGICAL = new Type("MAGICAL");
        public static final Type RARE = new Type("RARE");
        public static final Type PLATEAU = new Type("PLATEAU");
        public static final Type MODIFIED = new Type("MODIFIED");

        public static final Type OCEAN = new Type("OCEAN");
        public static final Type RIVER = new Type("RIVER");
        public static final Type WATER = new Type("WATER", OCEAN, RIVER);

        public static final Type MESA = new Type("MESA");
        public static final Type FOREST = new Type("FOREST");
        public static final Type PLAINS = new Type("PLAINS");
        public static final Type MOUNTAIN = new Type("MOUNTAIN");
        public static final Type HILLS = new Type("HILLS");
        public static final Type SWAMP = new Type("SWAMP");
        public static final Type SANDY = new Type("SANDY");
        public static final Type SNOWY = new Type("SNOWY");
        public static final Type WASTELAND = new Type("WASTELAND");
        public static final Type BEACH = new Type("BEACH");
        public static final Type VOID = new Type("VOID");
        public static final Type UNDERGROUND = new Type("UNDERGROUND");

        public static final Type OVERWORLD = new Type("OVERWORLD");
        public static final Type NETHER = new Type("NETHER");
        public static final Type END = new Type("END");

        private final String name;
        private final List<Type> subTypes;

        private Type(@Nonnull final String name, @Nonnull final Type... subTypes) {
            this.name = name;
            this.subTypes = List.of(subTypes);
            byName.put(name, this);
        }

        @Nonnull
        public String getName() {
            return this.name;
        }

        @Override
        public String toString() {
            return this.name;
        }

        @Nonnull
        public static Type getType(@Nonnull String name, @Nonnull final Type... subTypes) {
            name = name.toUpperCase(Locale.ROOT);
            synchronized (byName) {
                final Type t = byName.get(name);
                return t != null ? t : new Type(name, subTypes);
            }
        }

        @Nonnull
        public static Optional<Type> fromName(@Nonnull final String name) {
            return Optional.ofNullable(byName.get(name.toUpperCase(Locale.ROOT)));
        }

        @Nonnull
        public static Collection<Type> getAll() {
            synchronized (byName) {
                return List.copyOf(byName.values());
            }
        }
    }

    private static final Map<String, Set<Type>> VANILLA = new HashMap<>();

    private static void vanilla(@Nonnull final String path, @Nonnull final Type... types) {
        VANILLA.put("minecraft:" + path, ImmutableSet.copyOf(types));
    }

    static {
        vanilla("ocean", Type.OCEAN, Type.OVERWORLD);
        vanilla("plains", Type.PLAINS, Type.OVERWORLD);
        vanilla("desert", Type.HOT, Type.DRY, Type.SANDY, Type.OVERWORLD);
        vanilla("windswept_hills", Type.MOUNTAIN, Type.HILLS, Type.OVERWORLD);
        vanilla("forest", Type.FOREST, Type.OVERWORLD);
        vanilla("taiga", Type.COLD, Type.CONIFEROUS, Type.FOREST, Type.OVERWORLD);
        vanilla("swamp", Type.WET, Type.SWAMP, Type.OVERWORLD);
        vanilla("river", Type.RIVER, Type.OVERWORLD);
        vanilla("nether_wastes", Type.HOT, Type.DRY, Type.NETHER);
        vanilla("the_end", Type.COLD, Type.DRY, Type.END);
        vanilla("frozen_ocean", Type.COLD, Type.OCEAN, Type.SNOWY, Type.OVERWORLD);
        vanilla("frozen_river", Type.COLD, Type.RIVER, Type.SNOWY, Type.OVERWORLD);
        vanilla("snowy_plains", Type.COLD, Type.SNOWY, Type.WASTELAND, Type.OVERWORLD);
        vanilla("mushroom_fields", Type.MUSHROOM, Type.RARE, Type.OVERWORLD);
        vanilla("beach", Type.BEACH, Type.OVERWORLD);
        vanilla("jungle", Type.HOT, Type.WET, Type.DENSE, Type.JUNGLE, Type.OVERWORLD);
        vanilla("sparse_jungle", Type.HOT, Type.WET, Type.JUNGLE, Type.FOREST, Type.RARE, Type.OVERWORLD);
        vanilla("deep_ocean", Type.OCEAN, Type.OVERWORLD);
        vanilla("stony_shore", Type.BEACH, Type.OVERWORLD);
        vanilla("snowy_beach", Type.COLD, Type.BEACH, Type.SNOWY, Type.OVERWORLD);
        vanilla("birch_forest", Type.FOREST, Type.OVERWORLD);
        vanilla("dark_forest", Type.SPOOKY, Type.DENSE, Type.FOREST, Type.OVERWORLD);
        vanilla("snowy_taiga", Type.COLD, Type.CONIFEROUS, Type.FOREST, Type.SNOWY, Type.OVERWORLD);
        vanilla("old_growth_pine_taiga", Type.COLD, Type.CONIFEROUS, Type.FOREST, Type.OVERWORLD);
        vanilla("windswept_forest", Type.MOUNTAIN, Type.FOREST, Type.SPARSE, Type.OVERWORLD);
        vanilla("savanna", Type.HOT, Type.SAVANNA, Type.PLAINS, Type.SPARSE, Type.OVERWORLD);
        vanilla("savanna_plateau", Type.HOT, Type.SAVANNA, Type.PLAINS, Type.SPARSE, Type.RARE, Type.OVERWORLD, Type.PLATEAU);
        vanilla("badlands", Type.MESA, Type.SANDY, Type.DRY, Type.OVERWORLD);
        vanilla("wooded_badlands", Type.MESA, Type.SANDY, Type.DRY, Type.SPARSE, Type.OVERWORLD, Type.PLATEAU);
        vanilla("small_end_islands", Type.END);
        vanilla("end_midlands", Type.END);
        vanilla("end_highlands", Type.END);
        vanilla("end_barrens", Type.END);
        vanilla("warm_ocean", Type.OCEAN, Type.HOT, Type.OVERWORLD);
        vanilla("lukewarm_ocean", Type.OCEAN, Type.OVERWORLD);
        vanilla("cold_ocean", Type.OCEAN, Type.COLD, Type.OVERWORLD);
        vanilla("deep_lukewarm_ocean", Type.OCEAN, Type.OVERWORLD);
        vanilla("deep_cold_ocean", Type.OCEAN, Type.COLD, Type.OVERWORLD);
        vanilla("deep_frozen_ocean", Type.OCEAN, Type.COLD, Type.OVERWORLD);
        vanilla("the_void", Type.VOID);
        vanilla("sunflower_plains", Type.PLAINS, Type.RARE, Type.OVERWORLD);
        vanilla("windswept_gravelly_hills", Type.MOUNTAIN, Type.SPARSE, Type.RARE, Type.OVERWORLD);
        vanilla("flower_forest", Type.FOREST, Type.HILLS, Type.RARE, Type.OVERWORLD);
        vanilla("ice_spikes", Type.COLD, Type.SNOWY, Type.HILLS, Type.RARE, Type.OVERWORLD);
        vanilla("old_growth_birch_forest", Type.FOREST, Type.DENSE, Type.HILLS, Type.RARE, Type.OVERWORLD);
        vanilla("old_growth_spruce_taiga", Type.DENSE, Type.FOREST, Type.RARE, Type.OVERWORLD);
        vanilla("windswept_savanna", Type.HOT, Type.DRY, Type.SPARSE, Type.SAVANNA, Type.MOUNTAIN, Type.RARE, Type.OVERWORLD);
        vanilla("eroded_badlands", Type.HOT, Type.DRY, Type.SPARSE, Type.MOUNTAIN, Type.RARE, Type.OVERWORLD);
        vanilla("bamboo_jungle", Type.HOT, Type.WET, Type.RARE, Type.JUNGLE, Type.OVERWORLD);
        vanilla("soul_sand_valley", Type.HOT, Type.DRY, Type.NETHER);
        vanilla("crimson_forest", Type.HOT, Type.DRY, Type.NETHER, Type.FOREST);
        vanilla("warped_forest", Type.HOT, Type.DRY, Type.NETHER, Type.FOREST);
        vanilla("basalt_deltas", Type.HOT, Type.DRY, Type.NETHER);
    }

    private record TagRule(Type type, List<TagKey<Biome>> tags) {
    }

    private static final List<TagRule> TAG_RULES = new ArrayList<>();

    @SafeVarargs
    private static void rule(@Nonnull final Type type, @Nonnull final TagKey<Biome>... tags) {
        TAG_RULES.add(new TagRule(type, List.of(tags)));
    }

    @Nonnull
    private static TagKey<Biome> common(@Nonnull final String path) {
        return TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("c", path));
    }

    static {
        rule(Type.HOT, CommonTags.Biomes.IS_HOT);
        rule(Type.COLD, CommonTags.Biomes.IS_COLD);
        rule(Type.SPARSE, CommonTags.Biomes.IS_SPARSE_VEGETATION);
        rule(Type.DENSE, CommonTags.Biomes.IS_DENSE_VEGETATION);
        rule(Type.WET, CommonTags.Biomes.IS_WET);
        rule(Type.DRY, CommonTags.Biomes.IS_DRY);
        rule(Type.SAVANNA, CommonTags.Biomes.IS_SAVANNA, BiomeTags.IS_SAVANNA, CommonTags.Biomes.IS_SAVANNA_TREE);
        rule(Type.CONIFEROUS, CommonTags.Biomes.IS_CONIFEROUS_TREE, CommonTags.Biomes.IS_TAIGA, BiomeTags.IS_TAIGA);
        rule(Type.JUNGLE, CommonTags.Biomes.IS_JUNGLE, BiomeTags.IS_JUNGLE, CommonTags.Biomes.IS_JUNGLE_TREE);
        rule(Type.SPOOKY, CommonTags.Biomes.IS_SPOOKY);
        rule(Type.DEAD, CommonTags.Biomes.IS_DEAD);
        rule(Type.LUSH, CommonTags.Biomes.IS_LUSH);
        rule(Type.MUSHROOM, CommonTags.Biomes.IS_MUSHROOM);
        rule(Type.MAGICAL, CommonTags.Biomes.IS_MAGICAL);
        rule(Type.RARE, CommonTags.Biomes.IS_RARE);
        rule(Type.PLATEAU, CommonTags.Biomes.IS_PLATEAU);
        rule(Type.OCEAN, CommonTags.Biomes.IS_OCEAN, BiomeTags.IS_OCEAN);
        rule(Type.RIVER, CommonTags.Biomes.IS_RIVER, BiomeTags.IS_RIVER);
        rule(Type.MESA, CommonTags.Biomes.IS_BADLANDS, BiomeTags.IS_BADLANDS);
        rule(Type.FOREST, CommonTags.Biomes.IS_FOREST, BiomeTags.IS_FOREST, CommonTags.Biomes.IS_TAIGA, BiomeTags.IS_TAIGA,
                CommonTags.Biomes.IS_BIRCH_FOREST, CommonTags.Biomes.IS_DARK_FOREST, CommonTags.Biomes.IS_FLOWER_FOREST,
                CommonTags.Biomes.IS_CONIFEROUS_TREE, CommonTags.Biomes.IS_DECIDUOUS_TREE, CommonTags.Biomes.IS_NETHER_FOREST);
        rule(Type.PLAINS, CommonTags.Biomes.IS_PLAINS, CommonTags.Biomes.IS_SNOWY_PLAINS);
        rule(Type.MOUNTAIN, CommonTags.Biomes.IS_MOUNTAIN, BiomeTags.IS_MOUNTAIN, CommonTags.Biomes.IS_WINDSWEPT);
        rule(Type.HILLS, CommonTags.Biomes.IS_HILL, BiomeTags.IS_HILL, CommonTags.Biomes.IS_WINDSWEPT);
        rule(Type.SWAMP, CommonTags.Biomes.IS_SWAMP);
        rule(Type.SANDY, CommonTags.Biomes.IS_SANDY, CommonTags.Biomes.IS_DESERT);
        rule(Type.SNOWY, CommonTags.Biomes.IS_SNOWY, CommonTags.Biomes.IS_SNOWY_PLAINS, CommonTags.Biomes.IS_ICY);
        rule(Type.WASTELAND, CommonTags.Biomes.IS_WASTELAND);
        rule(Type.BEACH, CommonTags.Biomes.IS_BEACH, BiomeTags.IS_BEACH, CommonTags.Biomes.IS_STONY_SHORES);
        rule(Type.VOID, CommonTags.Biomes.IS_VOID);
        rule(Type.UNDERGROUND, CommonTags.Biomes.IS_UNDERGROUND, CommonTags.Biomes.IS_CAVE);
        rule(Type.OVERWORLD, CommonTags.Biomes.IS_OVERWORLD, BiomeTags.IS_OVERWORLD);
        rule(Type.NETHER, CommonTags.Biomes.IS_NETHER, BiomeTags.IS_NETHER);
        rule(Type.END, CommonTags.Biomes.IS_END, BiomeTags.IS_END);
    }

    private static final Map<ResourceKey<Biome>, Set<Type>> cache = new ConcurrentHashMap<>();

    public static void onTagsUpdated(@Nonnull final TagsUpdatedEvent event) {
        cache.clear();
    }

    public static void reset() {
        cache.clear();
    }

    @Nonnull
    public static Set<Type> getTypes(@Nonnull final Holder<Biome> biome) {
        final Optional<ResourceKey<Biome>> key = biome.unwrapKey();
        if (key.isEmpty())
            return computeTypes(null, biome);
        return cache.computeIfAbsent(key.get(), k -> computeTypes(k, biome));
    }

    @Nonnull
    public static Set<Type> getTypes(@Nonnull final ResourceKey<Biome> key) {
        final Set<Type> cached = cache.get(key);
        if (cached != null)
            return cached;
        final Optional<Registry<Biome>> registry = ForgeUtils.getBiomeRegistry();
        if (registry.isPresent()) {
            final Optional<Holder.Reference<Biome>> holder = registry.get().get(key);
            if (holder.isPresent())
                return getTypes(holder.get());
        }
        return withSuperTypes(VANILLA.getOrDefault(key.identifier().toString(), ImmutableSet.of()));
    }

    @Nonnull
    public static Set<Type> getTypes(@Nonnull final Biome biome) {
        final Holder<Biome> holder = BiomeUtilities.getBiomeHolder(biome);
        return holder != null ? getTypes(holder) : ImmutableSet.of();
    }

    public static boolean hasType(@Nonnull final Holder<Biome> biome, @Nonnull final Type type) {
        return getTypes(biome).contains(type);
    }

    public static boolean hasAnyType(@Nonnull final Holder<Biome> biome) {
        return !getTypes(biome).isEmpty();
    }

    @Nonnull
    public static String getCategory(@Nonnull final Holder<Biome> biome) {
        return getCategory(getTypes(biome));
    }

    @Nonnull
    public static String getCategory(@Nonnull final Set<Type> types) {
        if (types.contains(Type.NETHER))
            return "nether";
        if (types.contains(Type.END))
            return "the_end";
        if (types.contains(Type.MUSHROOM))
            return "mushroom";
        if (types.contains(Type.OCEAN))
            return "ocean";
        if (types.contains(Type.RIVER))
            return "river";
        if (types.contains(Type.BEACH))
            return "beach";
        if (types.contains(Type.SWAMP))
            return "swamp";
        if (types.contains(Type.JUNGLE))
            return "jungle";
        if (types.contains(Type.MESA))
            return "mesa";
        if (types.contains(Type.SAVANNA))
            return "savanna";
        if (types.contains(Type.SANDY) && (types.contains(Type.HOT) || types.contains(Type.DRY)))
            return "desert";
        if (types.contains(Type.CONIFEROUS))
            return "taiga";
        if (types.contains(Type.SNOWY))
            return "icy";
        if (types.contains(Type.MOUNTAIN) || types.contains(Type.HILLS))
            return "extreme_hills";
        if (types.contains(Type.FOREST))
            return "forest";
        if (types.contains(Type.PLAINS))
            return "plains";
        if (types.contains(Type.UNDERGROUND))
            return "underground";
        return "none";
    }

    @Nonnull
    private static Set<Type> computeTypes(@Nullable final ResourceKey<Biome> key, @Nonnull final Holder<Biome> biome) {
        if (key != null) {
            final Set<Type> vanilla = VANILLA.get(key.identifier().toString());
            if (vanilla != null)
                return withSuperTypes(vanilla);
        }

        final Set<Type> types = new LinkedHashSet<>();
        for (final TagRule r : TAG_RULES) {
            for (final TagKey<Biome> tag : r.tags()) {
                if (biome.is(tag)) {
                    types.add(r.type());
                    break;
                }
            }
        }

        if (!biome.is(CommonTags.Biomes.IS_HOT) && !biome.is(CommonTags.Biomes.IS_COLD) && !biome.is(CommonTags.Biomes.IS_TEMPERATE)) {
            final Biome b = biome.value();
            final IPlatform.Climate climate = Platform.get().getClimateSettings(b);
            final float temperature = climate.temperature();
            final float downfall = climate.downfall();
            if (temperature > 0.85F)
                types.add(Type.HOT);
            else if (temperature < 0.15F)
                types.add(Type.COLD);
            if (!types.contains(Type.WET) && !types.contains(Type.DRY)) {
                if (downfall > 0.85F)
                    types.add(Type.WET);
                else if (downfall < 0.15F)
                    types.add(Type.DRY);
            }
            if (climate.hasPrecipitation() && temperature < 0.15F)
                types.add(Type.SNOWY);
        }

        return withSuperTypes(types);
    }

    @Nonnull
    private static Set<Type> withSuperTypes(@Nonnull final Set<Type> types) {
        final Set<Type> result = new LinkedHashSet<>(types);
        for (final Type t : Type.getAll()) {
            for (final Type sub : t.subTypes) {
                if (types.contains(sub)) {
                    result.add(t);
                    break;
                }
            }
        }
        return Collections.unmodifiableSet(result);
    }
}
