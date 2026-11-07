/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.lib.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

public final class CommonTags {
    private CommonTags() {
    }

    public static final class Blocks {
        private Blocks() {
        }

        public static final TagKey<Block> CHESTS = tag("chests");
        public static final TagKey<Block> FENCES = tag("fences");
        public static final TagKey<Block> FENCE_GATES = tag("fence_gates");
        public static final TagKey<Block> GLASS_PANES = tag("glass_panes");

        private static TagKey<Block> tag(final String name) {
            return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", name));
        }
    }

    public static final class Biomes {
        private Biomes() {
        }

        public static final TagKey<Biome> IS_VOID = tag("is_void");
        public static final TagKey<Biome> IS_HOT = tag("is_hot");
        public static final TagKey<Biome> IS_TEMPERATE = tag("is_temperate");
        public static final TagKey<Biome> IS_COLD = tag("is_cold");
        public static final TagKey<Biome> IS_SPARSE_VEGETATION = tag("is_sparse_vegetation");
        public static final TagKey<Biome> IS_DENSE_VEGETATION = tag("is_dense_vegetation");
        public static final TagKey<Biome> IS_WET = tag("is_wet");
        public static final TagKey<Biome> IS_DRY = tag("is_dry");
        public static final TagKey<Biome> IS_OVERWORLD = tag("is_overworld");
        public static final TagKey<Biome> IS_CONIFEROUS_TREE = tag("is_tree/coniferous");
        public static final TagKey<Biome> IS_SAVANNA_TREE = tag("is_tree/savanna");
        public static final TagKey<Biome> IS_JUNGLE_TREE = tag("is_tree/jungle");
        public static final TagKey<Biome> IS_DECIDUOUS_TREE = tag("is_tree/deciduous");
        public static final TagKey<Biome> IS_MOUNTAIN = tag("is_mountain");
        public static final TagKey<Biome> IS_PLAINS = tag("is_plains");
        public static final TagKey<Biome> IS_SNOWY_PLAINS = tag("is_snowy_plains");
        public static final TagKey<Biome> IS_FOREST = tag("is_forest");
        public static final TagKey<Biome> IS_BIRCH_FOREST = tag("is_birch_forest");
        public static final TagKey<Biome> IS_DARK_FOREST = tag("is_dark_forest");
        public static final TagKey<Biome> IS_FLOWER_FOREST = tag("is_flower_forest");
        public static final TagKey<Biome> IS_TAIGA = tag("is_taiga");
        public static final TagKey<Biome> IS_HILL = tag("is_hill");
        public static final TagKey<Biome> IS_WINDSWEPT = tag("is_windswept");
        public static final TagKey<Biome> IS_JUNGLE = tag("is_jungle");
        public static final TagKey<Biome> IS_SAVANNA = tag("is_savanna");
        public static final TagKey<Biome> IS_SWAMP = tag("is_swamp");
        public static final TagKey<Biome> IS_DESERT = tag("is_desert");
        public static final TagKey<Biome> IS_BADLANDS = tag("is_badlands");
        public static final TagKey<Biome> IS_BEACH = tag("is_beach");
        public static final TagKey<Biome> IS_STONY_SHORES = tag("is_stony_shores");
        public static final TagKey<Biome> IS_MUSHROOM = tag("is_mushroom");
        public static final TagKey<Biome> IS_RIVER = tag("is_river");
        public static final TagKey<Biome> IS_OCEAN = tag("is_ocean");
        public static final TagKey<Biome> IS_UNDERGROUND = tag("is_underground");
        public static final TagKey<Biome> IS_CAVE = tag("is_cave");
        public static final TagKey<Biome> IS_LUSH = tag("is_lush");
        public static final TagKey<Biome> IS_MAGICAL = tag("is_magical");
        public static final TagKey<Biome> IS_RARE = tag("is_rare");
        public static final TagKey<Biome> IS_PLATEAU = tag("is_plateau");
        public static final TagKey<Biome> IS_SPOOKY = tag("is_spooky");
        public static final TagKey<Biome> IS_DEAD = tag("is_dead");
        public static final TagKey<Biome> IS_WASTELAND = tag("is_wasteland");
        public static final TagKey<Biome> IS_SANDY = tag("is_sandy");
        public static final TagKey<Biome> IS_SNOWY = tag("is_snowy");
        public static final TagKey<Biome> IS_ICY = tag("is_icy");
        public static final TagKey<Biome> IS_NETHER = tag("is_nether");
        public static final TagKey<Biome> IS_NETHER_FOREST = tag("is_nether_forest");
        public static final TagKey<Biome> IS_END = tag("is_end");

        private static TagKey<Biome> tag(final String name) {
            return TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("c", name));
        }
    }
}
