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

package dynamiclabs.immersivefx.lib.scripting.sets;

import dynamiclabs.immersivefx.lib.GameUtils;
import dynamiclabs.immersivefx.lib.WorldUtils;
import dynamiclabs.immersivefx.lib.scripting.VariableSet;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.dimension.DimensionType;

import javax.annotation.Nonnull;

public class DimensionVariables extends VariableSet<IDimensionVariables> implements IDimensionVariables {

    private String id;
    private String name;
    private boolean hasSky;
    private boolean isSuperFlat;

    public DimensionVariables() {
        super("dim");
    }

    @Nonnull
    @Override
    public IDimensionVariables getInterface() {
        return this;
    }

    @Override
    public void update() {
        if (GameUtils.isInGame()) {
            assert GameUtils.getWorld() != null;
            final DimensionType dim = GameUtils.getWorld().dimensionType();
            final Identifier location = GameUtils.getWorld().dimension().identifier();
            this.id = location.toString();
            this.hasSky = dim.hasSkyLight();
            this.name = location.getPath();
            this.isSuperFlat = WorldUtils.isSuperFlat(GameUtils.getWorld());
        } else {
            this.id = "UNKNOWN";
            this.hasSky = false;
            this.name = "UNKNOWN";
            this.isSuperFlat = false;
        }
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getDimName() {
        return this.name;
    }

    @Override
    public boolean hasSky() {
        return this.hasSky;
    }

    @Override
    public boolean isSuperFlat() {
        return this.isSuperFlat;
    }
}
