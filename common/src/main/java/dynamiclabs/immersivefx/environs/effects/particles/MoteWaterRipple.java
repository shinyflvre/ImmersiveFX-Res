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

import dynamiclabs.immersivefx.environs.config.Config;
import dynamiclabs.immersivefx.lib.biomes.BiomeUtilities;
import dynamiclabs.immersivefx.lib.gui.Color;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.BlockGetter;
import org.joml.Matrix3f;
import org.joml.Quaternionf;

import javax.annotation.Nonnull;

public class MoteWaterRipple extends MoteParticle {

	private static final float TEX_SIZE_HALF = 0.5F;

	private static final Quaternionf FLAT = new Quaternionf().setFromNormalized(new Matrix3f(
			0F, 0F, 1F,
			1F, 0F, 0F,
			0F, 1F, 0F));

	protected final float growthRate;
	protected float scale;
	protected float scaledWidth;

	protected float texU1, texU2;
	protected float texV1, texV2;

	public MoteWaterRipple(final BlockGetter world, final double x, final double y, final double z) {
		super(world, x, y, z, ParticleSprites.get(ParticleTypes.RAIN).first());

		final RippleStyle style = Config.CLIENT.effects.waterRippleStyle.get();

		this.lifetime = style.getMaxAge();

		if (style.doScaling()) {
			this.growthRate = this.lifetime / 500F;
			this.scale = this.growthRate;
			this.scaledWidth = this.scale * TEX_SIZE_HALF;
		} else {
			this.growthRate = 0F;
			this.scale = 0F;
			this.scaledWidth = 0.5F;
		}

		this.y -= 0.2D;
		this.yo = this.y;
		setPos(this.x, this.y, this.z);

		final Color waterColor = BiomeUtilities.getColorForLiquid(world, this.position.below());
		this.rCol = waterColor.red();
		this.gCol = waterColor.green();
		this.bCol = waterColor.blue();
		this.alpha = 0.99F;

		this.texU1 = style.getU1(this.age);
		this.texU2 = style.getU2(this.age);
		this.texV1 = style.getV1(this.age);
		this.texV2 = style.getV2(this.age);
	}

	@Override
	protected void update() {
		final RippleStyle style = Config.CLIENT.effects.waterRippleStyle.get();
		if (style.doScaling()) {
			this.scale += this.growthRate;
			this.scaledWidth = this.scale * TEX_SIZE_HALF;
		}

		if (style.doAlpha()) {
			this.alpha = (float) (this.lifetime - this.age) / (float) (this.lifetime + 3);
		}

		this.texU1 = style.getU1(this.age);
		this.texU2 = style.getU2(this.age);
		this.texV1 = style.getV1(this.age);
		this.texV2 = style.getV2(this.age);
	}

	@Nonnull
	@Override
	protected Layer getLayer() {
		return ParticleLayers.ripple(Config.CLIENT.effects.waterRippleStyle.get());
	}

	@Override
	public float getQuadSize(final float partialTick) {
		return this.scaledWidth;
	}

	@Override
	public void extract(@Nonnull final QuadParticleRenderState state, @Nonnull final Camera camera, final float partialTick) {
		extractRotatedQuad(state, camera, FLAT, partialTick);
	}

	@Override
	protected float getU0() {
		return this.texU1;
	}

	@Override
	protected float getU1() {
		return this.texU2;
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
