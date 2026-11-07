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

import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public enum Material {
    AIR(false, false, false, false, true, false),
    STRUCTURE_VOID(false, false, false, false, true, false),
    PORTAL(false, false, false, false, false, false),
    CARPET(false, false, false, true, false, false),
    PLANTS(false, false, false, false, false, false),
    WATER_PLANT(false, false, false, false, false, false),
    REPLACEABLE_PLANT(false, false, false, true, true, false),
    REPLACEABLE_FIREPROOF_PLANT(false, false, false, false, true, false),
    REPLACEABLE_WATER_PLANT(false, false, false, false, true, false),
    WATER(true, false, false, false, true, false),
    BUBBLE_COLUMN(true, false, false, false, true, false),
    LAVA(true, false, false, false, true, false),
    SNOW(false, false, false, false, true, false),
    FIRE(false, false, false, false, true, false),
    MISCELLANEOUS(false, false, false, false, false, false),
    WEB(false, true, false, false, false, false),
    REDSTONE_LIGHT(false, true, true, false, false, true),
    CLAY(false, true, true, false, false, true),
    EARTH(false, true, true, false, false, true),
    ORGANIC(false, true, true, false, false, true),
    PACKED_ICE(false, true, true, false, false, true),
    SAND(false, true, true, false, false, true),
    SPONGE(false, true, true, false, false, true),
    SHULKER(false, true, true, false, false, true),
    WOOD(false, true, true, true, false, true),
    NETHER_WOOD(false, true, true, false, false, true),
    BAMBOO_SAPLING(false, true, false, true, false, true),
    BAMBOO(false, true, true, true, false, true),
    WOOL(false, true, true, true, false, true),
    TNT(false, true, true, true, false, false),
    LEAVES(false, true, true, true, false, false),
    GLASS(false, true, true, false, false, false),
    ICE(false, true, true, false, false, false),
    CACTUS(false, true, true, false, false, false),
    ROCK(false, true, true, false, false, true),
    IRON(false, true, true, false, false, true),
    SNOW_BLOCK(false, true, true, false, false, true),
    ANVIL(false, true, true, false, false, true),
    BARRIER(false, true, true, false, false, true),
    PISTON(false, true, true, false, false, true),
    CORAL(false, true, true, false, false, true),
    GOURD(false, true, true, false, false, true),
    DRAGON_EGG(false, true, true, false, false, true),
    CAKE(false, true, true, false, false, true);

    private final boolean liquid;
    private final boolean solid;
    private final boolean blocksMovement;
    private final boolean flammable;
    private final boolean replaceable;
    private final boolean opaque;

    Material(final boolean liquid, final boolean solid, final boolean blocksMovement, final boolean flammable, final boolean replaceable, final boolean opaque) {
        this.liquid = liquid;
        this.solid = solid;
        this.blocksMovement = blocksMovement;
        this.flammable = flammable;
        this.replaceable = replaceable;
        this.opaque = opaque;
    }

    public boolean isLiquid() {
        return this.liquid;
    }

    public boolean isSolid() {
        return this.solid;
    }

    public boolean blocksMovement() {
        return this.blocksMovement;
    }

    public boolean isFlammable() {
        return this.flammable;
    }

    public boolean isReplaceable() {
        return this.replaceable;
    }

    public boolean isOpaque() {
        return this.opaque;
    }

    @Nonnull
    public static Material of(@Nonnull final BlockState state) {
        return MaterialUtils.getMaterial(state);
    }
}
