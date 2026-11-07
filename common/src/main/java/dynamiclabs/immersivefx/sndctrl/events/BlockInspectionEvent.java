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

package dynamiclabs.immersivefx.sndctrl.events;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.Level;
import dynamiclabs.immersivefx.platform.event.EventHook;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class BlockInspectionEvent {

    public static final EventHook<BlockInspectionEvent> BUS = new EventHook<>("BlockInspectionEvent");

    public final List<String> data = new ArrayList<>();
    public final BlockHitResult rayTrace;
    public final Level world;
    public final BlockState state;
    public final BlockPos pos;

    public BlockInspectionEvent(@Nonnull final BlockHitResult trace, @Nonnull final Level world, @Nonnull final BlockState state, @Nonnull final BlockPos pos) {
        this.rayTrace = trace;
        this.world = world;
        this.state = state;
        this.pos = pos;
    }
}
