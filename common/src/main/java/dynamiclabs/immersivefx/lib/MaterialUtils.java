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

package dynamiclabs.immersivefx.lib;

import dynamiclabs.immersivefx.platform.event.TagsUpdatedEvent;
import dynamiclabs.immersivefx.ImmersiveFX;
import dynamiclabs.immersivefx.dsurround.DynamicSurroundings;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public final class MaterialUtils {

    private static final Reference2ObjectOpenHashMap<Material, String> materialMap = new Reference2ObjectOpenHashMap<>();
    private static final Map<String, Material> materialMapInv = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private static final Map<BlockState, Material> stateCache = new ConcurrentHashMap<>();

    static {
        materialMap.defaultReturnValue("CUSTOM");

        materialMap.put(Material.AIR, "AIR");
        materialMap.put(Material.STRUCTURE_VOID, "STRUCTURE_VOID");
        materialMap.put(Material.PORTAL, "PORTAL");
        materialMap.put(Material.CARPET, "CARPET");
        materialMap.put(Material.PLANTS, "PLANTS");
        materialMap.put(Material.WATER_PLANT, "OCEAN_PLANT");
        materialMap.put(Material.REPLACEABLE_PLANT, "TALL_PLANTS");
        materialMap.put(Material.REPLACEABLE_FIREPROOF_PLANT, "NETHER_PLANTS");
        materialMap.put(Material.REPLACEABLE_WATER_PLANT, "SEA_GRASS");
        materialMap.put(Material.WATER, "WATER");
        materialMap.put(Material.BUBBLE_COLUMN, "BUBBLE_COLUMN");
        materialMap.put(Material.LAVA, "LAVA");
        materialMap.put(Material.SNOW, "SNOW");
        materialMap.put(Material.FIRE, "FIRE");
        materialMap.put(Material.MISCELLANEOUS, "MISCELLANEOUS");
        materialMap.put(Material.WEB, "WEB");
        materialMap.put(Material.REDSTONE_LIGHT, "REDSTONE_LIGHT");
        materialMap.put(Material.CLAY, "CLAY");
        materialMap.put(Material.EARTH, "EARTH");
        materialMap.put(Material.ORGANIC, "ORGANIC");
        materialMap.put(Material.PACKED_ICE, "PACKED_ICE");
        materialMap.put(Material.SAND, "SAND");
        materialMap.put(Material.SPONGE, "SPONGE");
        materialMap.put(Material.SHULKER, "SHULKER");
        materialMap.put(Material.WOOD, "WOOD");
        materialMap.put(Material.NETHER_WOOD, "NETHER_WOOD");
        materialMap.put(Material.BAMBOO_SAPLING, "BAMBOO_SAPLING");
        materialMap.put(Material.BAMBOO, "BAMBOO");
        materialMap.put(Material.WOOL, "WOOL");
        materialMap.put(Material.TNT, "TNT");
        materialMap.put(Material.LEAVES, "LEAVES");
        materialMap.put(Material.GLASS, "GLASS");
        materialMap.put(Material.ICE, "ICE");
        materialMap.put(Material.CACTUS, "CACTUS");
        materialMap.put(Material.ROCK, "ROCK");
        materialMap.put(Material.IRON, "IRON");
        materialMap.put(Material.SNOW_BLOCK, "SNOW_BLOCK");
        materialMap.put(Material.ANVIL, "ANVIL");
        materialMap.put(Material.BARRIER, "BARRIER");
        materialMap.put(Material.PISTON, "PISTON");
        materialMap.put(Material.CORAL, "CORAL");
        materialMap.put(Material.GOURD, "GOURD");
        materialMap.put(Material.DRAGON_EGG, "DRAGON_EGG");
        materialMap.put(Material.CAKE, "CAKE");

        // Create the inverse map
        for (final Map.Entry<Material, String> kvp : materialMap.entrySet()) {
            materialMapInv.put(kvp.getValue(), kvp.getKey());
        }
    }

    private MaterialUtils() {

    }

    @Nonnull
    public static Set<Material> getMaterials() {
        return materialMap.keySet();
    }

    @Nullable
    public static Material getMaterial(@Nonnull final String name) {
        return materialMapInv.get(name);
    }

    @Nullable
    public static String getMaterialName(@Nonnull final Material mat) {
        return materialMap.get(mat);
    }

    @Nonnull
    public static Material getMaterial(@Nonnull final BlockState state) {
        Material result = stateCache.get(state);
        if (result == null) {
            result = classify(state);
            stateCache.put(state, result);
        }
        return result;
    }

    public static void onTagsUpdated(@Nonnull final TagsUpdatedEvent event) {
        stateCache.clear();
    }

    @Nonnull
    private static Material classify(@Nonnull final BlockState state) {
        final Block block = state.getBlock();
        final SoundType sound = state.getSoundType();

        if (block == Blocks.STRUCTURE_VOID)
            return Material.STRUCTURE_VOID;
        if (state.isAir())
            return Material.AIR;
        if (block == Blocks.NETHER_PORTAL || block == Blocks.END_PORTAL || block == Blocks.END_GATEWAY || state.is(BlockTags.PORTALS))
            return Material.PORTAL;
        if (block == Blocks.BUBBLE_COLUMN)
            return Material.BUBBLE_COLUMN;
        if (block instanceof LiquidBlock)
            return state.getFluidState().is(FluidTags.LAVA) ? Material.LAVA : Material.WATER;
        if (block instanceof BaseFireBlock || state.is(BlockTags.FIRE))
            return Material.FIRE;
        if (block == Blocks.SEAGRASS || block == Blocks.TALL_SEAGRASS)
            return Material.REPLACEABLE_WATER_PLANT;
        if (block == Blocks.KELP || block == Blocks.KELP_PLANT || block == Blocks.SEA_PICKLE
                || state.is(BlockTags.CORAL_PLANTS) || state.is(BlockTags.WALL_CORALS) || state.is(BlockTags.CORALS))
            return Material.WATER_PLANT;
        if (state.is(BlockTags.CORAL_BLOCKS))
            return Material.CORAL;
        if (block instanceof CarpetBlock || state.is(BlockTags.WOOL_CARPETS))
            return Material.CARPET;
        if (block instanceof SnowLayerBlock)
            return Material.SNOW;
        if (block == Blocks.SNOW_BLOCK || block instanceof PowderSnowBlock)
            return Material.SNOW_BLOCK;
        if (block == Blocks.PACKED_ICE || block == Blocks.BLUE_ICE)
            return Material.PACKED_ICE;
        if (block instanceof IceBlock || state.is(BlockTags.ICE))
            return Material.ICE;
        if (block instanceof WebBlock)
            return Material.WEB;
        if (block instanceof RedstoneLampBlock)
            return Material.REDSTONE_LIGHT;
        if (block instanceof TntBlock)
            return Material.TNT;
        if (block instanceof SpongeBlock || block instanceof WetSpongeBlock)
            return Material.SPONGE;
        if (block == Blocks.CLAY)
            return Material.CLAY;
        if (block instanceof CactusBlock)
            return Material.CACTUS;
        if (block instanceof CakeBlock || block instanceof CandleCakeBlock)
            return Material.CAKE;
        if (block == Blocks.DRAGON_EGG)
            return Material.DRAGON_EGG;
        if (block == Blocks.PUMPKIN || block == Blocks.CARVED_PUMPKIN || block == Blocks.JACK_O_LANTERN || block == Blocks.MELON)
            return Material.GOURD;
        if (state.is(BlockTags.SHULKER_BOXES))
            return Material.SHULKER;
        if (state.is(BlockTags.ANVIL))
            return Material.ANVIL;
        if (block instanceof PistonBaseBlock || block instanceof PistonHeadBlock || block instanceof MovingPistonBlock)
            return Material.PISTON;
        if (block == Blocks.BARRIER)
            return Material.BARRIER;
        if (block == Blocks.BAMBOO_SAPLING)
            return Material.BAMBOO_SAPLING;
        if (block == Blocks.BAMBOO)
            return Material.BAMBOO;
        if (state.is(BlockTags.LEAVES))
            return Material.LEAVES;
        if (state.is(BlockTags.WOOL))
            return Material.WOOL;
        if (block instanceof NetherSproutsBlock || block instanceof NetherRootsBlock)
            return Material.REPLACEABLE_FIREPROOF_PLANT;
        if (block instanceof VegetationBlock || block instanceof VineBlock || block instanceof SugarCaneBlock
                || state.is(BlockTags.FLOWERS) || state.is(BlockTags.SAPLINGS) || state.is(BlockTags.CROPS)) {
            return state.canBeReplaced() ? Material.REPLACEABLE_PLANT : Material.PLANTS;
        }
        if (block == Blocks.LILY_PAD)
            return Material.PLANTS;
        if (block == Blocks.GRASS_BLOCK || block == Blocks.MYCELIUM || block == Blocks.PODZOL || state.is(BlockTags.GRASS_BLOCKS))
            return Material.ORGANIC;
        if (block == Blocks.FARMLAND || block == Blocks.DIRT_PATH || block == Blocks.SOUL_SOIL || state.is(BlockTags.DIRT))
            return Material.EARTH;
        if (state.is(BlockTags.SAND) || state.is(BlockTags.CONCRETE_POWDERS) || block == Blocks.GRAVEL
                || block == Blocks.SOUL_SAND || block instanceof FallingBlock && (sound == SoundType.SAND || sound == SoundType.GRAVEL))
            return Material.SAND;
        if (sound == SoundType.GLASS || block instanceof TransparentBlock || block instanceof StainedGlassPaneBlock)
            return Material.GLASS;
        if (sound == SoundType.NETHER_WOOD || sound == SoundType.STEM || sound == SoundType.NETHER_WOOD_HANGING_SIGN)
            return Material.NETHER_WOOD;
        if (state.is(BlockTags.LOGS) || state.is(BlockTags.PLANKS)
                || (state.is(BlockTags.MINEABLE_WITH_AXE) && state.ignitedByLava()))
            return Material.WOOD;
        if (sound == SoundType.METAL || sound == SoundType.NETHERITE_BLOCK || sound == SoundType.CHAIN
                || sound == SoundType.LANTERN || sound == SoundType.COPPER || sound == SoundType.COPPER_BULB
                || sound == SoundType.COPPER_GRATE || sound == SoundType.IRON || sound == SoundType.ANVIL)
            return Material.IRON;
        if (!state.is(BlockTags.BLOCKS_MOTION))
            return Material.MISCELLANEOUS;
        return Material.ROCK;
    }
}
