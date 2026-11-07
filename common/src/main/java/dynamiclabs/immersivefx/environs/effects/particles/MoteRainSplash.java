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

package dynamiclabs.immersivefx.environs.effects.particles;

import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;

public class MoteRainSplash extends MoteWaterSpray {

	public MoteRainSplash(final BlockGetter world, final double x, final double y, final double z) {
		super(world, x, y, z, 0, 0, 0);

		// Setup motion
		this.xd = (RANDOM.nextDouble() * 2.0D - 1.0D) * 0.4000000059604645D;
		this.yd = (RANDOM.nextDouble() * 2.0D - 1.0D) * 0.4000000059604645D;
		this.zd = (RANDOM.nextDouble() * 2.0D - 1.0D) * 0.4000000059604645D;
		final float f = (float) (RANDOM.nextDouble() + RANDOM.nextDouble() + 1.0D) * 0.15F;
		final float f1 = (float) Mth.length(this.xd, this.yd, this.zd);
		this.xd = this.xd / f1 * f * 0.4000000059604645D;
		this.yd = this.yd / f1 * f * 0.4000000059604645D + 0.10000000149011612D;
		this.zd = this.zd / f1 * f * 0.4000000059604645D;

		this.xd *= 0.30000001192092896D;
		this.yd = RANDOM.nextDouble() * 0.20000000298023224D + 0.10000000149011612D;
		this.zd *= 0.30000001192092896D;
	}

	@Override
	public void configureColor() {
		this.rCol = this.gCol = this.bCol = 1F;
		this.alpha = 0.99F;
	}

}
