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

package dynamiclabs.immersivefx.sndctrl.library;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;

public enum LegacyMaterial {
    AIR(false),
    STRUCTURE_VOID(false),
    PORTAL(false),
    CARPET(false),
    PLANTS(false),
    OCEAN_PLANT(false),
    TALL_PLANTS(false),
    NETHER_PLANTS(false),
    SEA_GRASS(false),
    WATER(false),
    BUBBLE_COLUMN(false),
    LAVA(false),
    SNOW(false),
    FIRE(false),
    MISCELLANEOUS(false),
    WEB(false),
    REDSTONE_LIGHT(true),
    CLAY(true),
    EARTH(true),
    ORGANIC(true),
    PACKED_ICE(true),
    SAND(true),
    SPONGE(true),
    SHULKER(true),
    WOOD(true),
    NETHER_WOOD(true),
    BAMBOO_SAPLING(true),
    BAMBOO(true),
    WOOL(true),
    TNT(false),
    LEAVES(false),
    GLASS(false),
    ICE(false),
    CACTUS(false),
    ROCK(true),
    IRON(true),
    SNOW_BLOCK(true),
    ANVIL(true),
    BARRIER(true),
    PISTON(true),
    CORAL(true),
    GOURD(true),
    DRAGON_EGG(true),
    CAKE(true);

    private final boolean opaque;

    LegacyMaterial(final boolean opaque) {
        this.opaque = opaque;
    }

    public boolean isOpaque() {
        return this.opaque;
    }

    @Nullable
    public static LegacyMaterial byName(@Nonnull final String name) {
        try {
            return valueOf(name.toUpperCase(Locale.ROOT));
        } catch (@Nonnull final IllegalArgumentException ignored) {
            return null;
        }
    }

    @Nonnull
    public static LegacyMaterial of(@Nonnull final BlockState state) {
        final Block block = state.getBlock();

        if (block instanceof StructureVoidBlock)
            return STRUCTURE_VOID;
        if (state.isAir())
            return AIR;
        if (block instanceof BubbleColumnBlock)
            return BUBBLE_COLUMN;
        if (block instanceof LiquidBlock)
            return state.getFluidState().is(FluidTags.LAVA) ? LAVA : WATER;
        if (block instanceof SeagrassBlock || block instanceof TallSeagrassBlock)
            return SEA_GRASS;
        if (block instanceof KelpBlock || block instanceof KelpPlantBlock || block instanceof SeaPickleBlock || block instanceof BaseCoralPlantTypeBlock)
            return OCEAN_PLANT;
        if (block instanceof NetherPortalBlock || block instanceof EndPortalBlock || block instanceof EndGatewayBlock)
            return PORTAL;
        if (block instanceof CarpetBlock || state.is(BlockTags.WOOL_CARPETS))
            return CARPET;
        if (block instanceof BaseFireBlock)
            return FIRE;
        if (block instanceof WebBlock)
            return WEB;
        if (block instanceof RedstoneLampBlock)
            return REDSTONE_LIGHT;
        if (block instanceof TntBlock)
            return TNT;
        if (block instanceof LeavesBlock || state.is(BlockTags.LEAVES))
            return LEAVES;
        if (block instanceof CactusBlock)
            return CACTUS;
        if (block instanceof AnvilBlock || state.is(BlockTags.ANVIL))
            return ANVIL;
        if (block instanceof BarrierBlock)
            return BARRIER;
        if (block instanceof PistonBaseBlock || block instanceof PistonHeadBlock || block instanceof MovingPistonBlock)
            return PISTON;
        if (block instanceof DragonEggBlock)
            return DRAGON_EGG;
        if (block instanceof CakeBlock || block instanceof CandleCakeBlock)
            return CAKE;
        if (block instanceof SpongeBlock || block instanceof WetSpongeBlock)
            return SPONGE;
        if (block instanceof ShulkerBoxBlock)
            return SHULKER;
        if (block instanceof BambooSaplingBlock)
            return BAMBOO_SAPLING;
        if (block instanceof BambooStalkBlock)
            return BAMBOO;
        if (block == Blocks.PACKED_ICE || block == Blocks.BLUE_ICE)
            return PACKED_ICE;
        if (block instanceof IceBlock || state.is(BlockTags.ICE))
            return ICE;
        if (block instanceof SnowLayerBlock)
            return SNOW;
        if (block == Blocks.SNOW_BLOCK || block instanceof PowderSnowBlock)
            return SNOW_BLOCK;
        if (block instanceof CoralBlock || state.is(BlockTags.CORAL_BLOCKS))
            return CORAL;
        if (block instanceof PumpkinBlock || block instanceof CarvedPumpkinBlock || block == Blocks.MELON)
            return GOURD;
        if (block == Blocks.CLAY)
            return CLAY;
        if (block instanceof GrassBlock || block instanceof MyceliumBlock || block instanceof HayBlock)
            return ORGANIC;
        if (block == Blocks.GRAVEL)
            return SAND;
        if (state.is(BlockTags.WOOL))
            return WOOL;
        if (block instanceof NetherRootsBlock || block instanceof NetherSproutsBlock)
            return NETHER_PLANTS;
        if (block instanceof VineBlock)
            return TALL_PLANTS;
        if (block instanceof VegetationBlock)
            return state.canBeReplaced() ? TALL_PLANTS : PLANTS;
        if (state.is(BlockTags.DIRT))
            return EARTH;

        final SoundType st = state.getSoundType();
        if (st == SoundType.GLASS)
            return GLASS;
        if (st == SoundType.WOOD || st == SoundType.BAMBOO_WOOD || st == SoundType.CHERRY_WOOD || st == SoundType.LADDER
                || st == SoundType.CHISELED_BOOKSHELF || st == SoundType.SHELF || state.is(BlockTags.LOGS) || state.is(BlockTags.PLANKS))
            return WOOD;
        if (st == SoundType.NETHER_WOOD || st == SoundType.STEM)
            return NETHER_WOOD;
        if (st == SoundType.METAL || st == SoundType.NETHERITE_BLOCK || st == SoundType.CHAIN || st == SoundType.LANTERN
                || st == SoundType.COPPER || st == SoundType.COPPER_BULB || st == SoundType.COPPER_GRATE || st == SoundType.IRON
                || st == SoundType.LODESTONE || st == SoundType.HEAVY_CORE)
            return IRON;
        if (st == SoundType.SAND || st == SoundType.SUSPICIOUS_SAND || st == SoundType.SOUL_SAND)
            return SAND;
        if (st == SoundType.GRAVEL || st == SoundType.SUSPICIOUS_GRAVEL || st == SoundType.ROOTED_DIRT || st == SoundType.MUD
                || st == SoundType.SOUL_SOIL)
            return EARTH;
        if (st == SoundType.WOOL)
            return WOOL;
        if (st == SoundType.SNOW || st == SoundType.POWDER_SNOW)
            return SNOW_BLOCK;
        if (st == SoundType.GRASS || st == SoundType.MOSS || st == SoundType.WART_BLOCK)
            return ORGANIC;

        if (state.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).isEmpty())
            return MISCELLANEOUS;
        return ROCK;
    }
}
