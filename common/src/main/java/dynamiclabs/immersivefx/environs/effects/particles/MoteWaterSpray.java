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

import dynamiclabs.immersivefx.lib.biomes.BiomeUtilities;
import dynamiclabs.immersivefx.lib.gui.Color;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.material.Fluids;

import javax.annotation.Nonnull;

// TODO: Should this be animated?  Seems to overlap.
public class MoteWaterSpray extends MotionMoteParticle {

	protected float scale;

	protected final float texU1, texU2;
	protected final float texV1, texV2;

	public MoteWaterSpray(final BlockGetter world, final double x, final double y, final double z, final double dX,
						  final double dY, final double dZ) {
		super(world, x, y, z, dX, dY, dZ, ParticleSprites.get(ParticleTypes.RAIN).first());

		this.lifetime = (int) (8.0F / (RANDOM.nextFloat() * 0.8F + 0.2F));
		this.scale = (RANDOM.nextFloat() * 0.5F + 0.5F) * 2.0F * 0.07F;

		final int textureIdx = RANDOM.nextInt(4);
		final int texX = textureIdx % 2;
		final int texY = textureIdx / 2;
		this.texU1 = texX * 0.5F;
		this.texU2 = this.texU1 + 0.5F;
		this.texV1 = texY * 0.5F;
		this.texV2 = this.texV1 + 0.5F;
	}

	@Override
	public void configureColor() {
		final Color waterColor = BiomeUtilities.getColorForLiquid(this.world, this.position);
		this.rCol = waterColor.red();
		this.gCol = waterColor.green();
		this.bCol = waterColor.blue();
		this.alpha = 0.99F;
	}

	@Override
	protected void handleCollision(@Nonnull final CollisionResult collision) {
		// Do the drip splash, but don't play the sound.  Sounds funny with waterfall effects
		ParticleHooks.splashHandler(Fluids.WATER, collision, false);
		super.handleCollision(collision);
	}

	@Nonnull
	@Override
	protected Layer getLayer() {
		return ParticleLayers.SPRAY;
	}

	@Override
	public float getQuadSize(final float partialTick) {
		return this.scale;
	}

	@Override
	protected float getU0() {
		return this.texU2;
	}

	@Override
	protected float getU1() {
		return this.texU1;
	}

	@Override
	protected float getV0() {
		return this.texV1;
	}

	@Override
	protected float getV1() {
		return this.texV2;
	}
}
