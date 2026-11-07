/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
/*
 *  Dynamic Surroundings: Environs
 *  Copyright (C) 2020  OreCruncher
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package dynamiclabs.immersivefx.environs.scanner;

import java.util.Iterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import dynamiclabs.immersivefx.lib.BlockPosUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;

/**
 * Implements a "peeking" iterator. The iterator uses mutables for position so
 * they aren't safe to cache.
 */
public class CuboidPointIterator implements IPointIterator {

	static final CuboidPointIterator NULL_ITERATOR = new CuboidPointIterator() {

		@Override
		public BlockPos next() {
			return null;
		}

		@Override
		public BlockPos peek() {
			return null;
		}

	};

	protected final Iterator<MutableBlockPos> itr;
	protected BlockPos peeked;

	private CuboidPointIterator() {
		this.itr = null;
	}

	public CuboidPointIterator(@Nonnull final BlockPos[] points) {
		this(points[0], points[1]);
	}

	public CuboidPointIterator(@Nonnull final BlockPos p1, @Nonnull final BlockPos p2) {
		// The getAllInBox() deals with figuring the min/max points
		this.itr = BlockPosUtil.getAllInBoxMutable(p1, p2).iterator();
		if (this.itr.hasNext())
			this.peeked = this.itr.next();
	}

	@Override
	@Nullable
	public BlockPos next() {
		final BlockPos result = this.peeked;
		this.peeked = this.itr.hasNext() ? this.itr.next() : null;
		return result;
	}

	@Override
	@Nullable
	public BlockPos peek() {
		return this.peeked;
	}

}
