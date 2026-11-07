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

package dynamiclabs.immersivefx.environs.shaders.aurora;

import com.mojang.blaze3d.vertex.BufferBuilder;
import dynamiclabs.immersivefx.lib.math.MathStuff;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

import javax.annotation.Nonnull;

public class AuroraShaderBand extends AuroraBase {

	private static final float V1 = 0;
	private static final float V2 = 0.5F;

	protected boolean failed;

	protected final float auroraWidth;
	protected final float panelTexWidth;

	public AuroraShaderBand(final long seed) {
		super(seed);

		this.auroraWidth = this.band.getPanelCount() * this.band.getNodeWidth();
		this.panelTexWidth = this.band.getNodeWidth() / this.auroraWidth;
	}

	@Override
	public void update() {
		super.update();
		this.band.update();
	}

	@Override
	protected float getAlpha() {
		return MathStuff.clamp((this.band.getAlphaLimit() / 255F) * this.tracker.ageRatio() * 2.0F, 0F, 1F);
	}

	protected float getAuroraWidth() {
		return this.auroraWidth;
	}

	protected float getAuroraHeight() {
		return AuroraBand.AURORA_AMPLITUDE;
	}

	protected void generateBand(@Nonnull final BufferBuilder builder, final float dX, final float dY, final float dZ) {

		for (int i = 0; ; i++) {
			final Vector3f[] quad = this.band.getPanelQuad(i);
			if (quad == null)
				break;

			final float u1 = i * this.panelTexWidth;
			final float u2 = u1 + this.panelTexWidth;

			builder.addVertex(quad[0].x() + dX, quad[0].y() + dY, quad[0].z() + dZ).setUv(u1, V1);
			builder.addVertex(quad[1].x() + dX, quad[1].y() + dY, quad[1].z() + dZ).setUv(u2, V1);
			builder.addVertex(quad[2].x() + dX, quad[2].y() + dY, quad[2].z() + dZ).setUv(u2, V2);
			builder.addVertex(quad[3].x() + dX, quad[3].y() + dY, quad[3].z() + dZ).setUv(u1, V2);
		}

	}

	@Override
	public void render(@Nonnull final Matrix4fc modelView, @Nonnull final Vec3 camera, final float partialTick) {

		if (this.failed)
			return;

		this.band.translate(partialTick);

		final double tranY = getTranslationY(partialTick);
		final double tranX = getTranslationX(partialTick);
		final double tranZ = getTranslationZ(partialTick);

		final AuroraRenderer.Parameters params = new AuroraRenderer.Parameters(
				AuroraUtils.getTimeSeconds(partialTick) * 0.75F,
				getAuroraWidth(),
				getAuroraHeight(),
				getFadeColor(),
				getMiddleColor(),
				getBaseColor(),
				getAlpha());

		try {
			AuroraRenderer.draw(modelView, this.bandCount * this.band.getPanelCount(), params, builder -> {
				for (int b = 0; b < this.bandCount; b++) {
					generateBand(builder,
							(float) (tranX - camera.x),
							(float) (tranY - camera.y),
							(float) (tranZ + this.offset * b - camera.z));
				}
			});
		} catch (final Exception ex) {
			ex.printStackTrace();
			this.failed = true;
		}
	}

	@Override
	public String toString() {
		return "<SHADER> " + super.toString();
	}
}
