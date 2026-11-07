/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
/*
 *  Dynamic Surroundings
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

package dynamiclabs.immersivefx.environs.effects.emitters;

import net.minecraft.server.level.ParticleStatus;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.BlockGetter;
import dynamiclabs.immersivefx.environs.Environs;
import dynamiclabs.immersivefx.environs.config.Config;
import dynamiclabs.immersivefx.environs.effects.JetEffect;
import dynamiclabs.immersivefx.environs.effects.WaterfallSplashEffect;
import dynamiclabs.immersivefx.environs.effects.particles.Collections;
import dynamiclabs.immersivefx.environs.handlers.CommonState;
import dynamiclabs.immersivefx.environs.library.Constants;
import dynamiclabs.immersivefx.lib.GameUtils;
import dynamiclabs.immersivefx.lib.WorldUtils;
import dynamiclabs.immersivefx.lib.math.MathStuff;

import net.minecraft.core.BlockPos;
import dynamiclabs.immersivefx.sndctrl.api.acoustics.IAcoustic;
import dynamiclabs.immersivefx.sndctrl.api.acoustics.Library;
import dynamiclabs.immersivefx.sndctrl.audio.AudioEngine;
import dynamiclabs.immersivefx.sndctrl.audio.FadingLoopSoundInstance;
import dynamiclabs.immersivefx.sndctrl.audio.SoundUtils;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;

public class WaterSplashJet extends Jet {

	private static final Identifier[] waterfallAcoustics = new Identifier[JetEffect.MAX_STRENGTH + 1];
	static {
		final Identifier defaultAcoustic = Identifier.fromNamespaceAndPath(Environs.MOD_ID, "waterfall/0");
		Arrays.fill(waterfallAcoustics, defaultAcoustic);
		waterfallAcoustics[2] = waterfallAcoustics[3] = Identifier.fromNamespaceAndPath(Environs.MOD_ID, "waterfall/1");
		waterfallAcoustics[4] = Identifier.fromNamespaceAndPath(Environs.MOD_ID, "waterfall/2");
		waterfallAcoustics[5] = waterfallAcoustics[6] = Identifier.fromNamespaceAndPath(Environs.MOD_ID, "waterfall/3");
		waterfallAcoustics[7] = waterfallAcoustics[8] = Identifier.fromNamespaceAndPath(Environs.MOD_ID, "waterfall/4");
		waterfallAcoustics[9] = waterfallAcoustics[10] = Identifier.fromNamespaceAndPath(Environs.MOD_ID, "waterfall/5");
	}

	private static final double RANGE_FADE = 6D;
	private static final double RANGE_MARGIN = 2.5D;

	protected FadingLoopSoundInstance sound;
	protected int particleLimit;
	protected final double deltaY;

	public WaterSplashJet(final int strength, final BlockGetter world, final BlockPos loc, final double dY) {
		super(0, strength, world, loc.getX() + 0.5D, loc.getY() + 0.5D, loc.getZ() + 0.5D, 4);
		this.deltaY = loc.getY() + dY;
		setSpawnCount((int) (strength * 2.5F));
	}

	public void setSpawnCount(final int limit) {
		this.particleLimit = MathStuff.clamp(limit, 5, 20);
	}

	public int getSpawnCount() {
		final ParticleStatus state = GameUtils.getGameSettings().particles().get();
		switch (state) {
			case MINIMAL:
				return 0;
			case ALL:
				return this.particleLimit;
			default:
				return this.particleLimit / 2;
		}
	}

	@Override
	public boolean shouldDie() {
		// Check every half second
		return (this.particleAge % 10) == 0
				&& !WaterfallSplashEffect.isValidSpawnBlock(this.world, this.position);
	}

	@Override
	protected void soundUpdate() {
		if (!isAlive())
			return;

		if (this.jetStrength <= Config.CLIENT.effects.waterfallCutoff.get()) {
			if (this.sound != null)
				this.sound.fade();
			return;
		}

		if (this.sound == null) {
			final int idx = MathStuff.clamp(this.jetStrength, 0, waterfallAcoustics.length - 1);
			final IAcoustic acoustic = Library.resolve(waterfallAcoustics[idx]);
			this.sound = new FadingLoopSoundInstance(acoustic.getFactory().createSoundAt(this.position), Constants.WATERFALL);
		}

		final Vec3 eye = CommonState.getPlayerEyePosition();
		final float rangeGain = rangeGain(eye);
		this.sound.setRangeGain(rangeGain);

		final boolean audible = rangeGain > 0F && SoundUtils.inRange(eye, this.sound, 4);
		final boolean isActive = this.sound.getState().isActive();

		if (audible) {
			if (!isActive)
				AudioEngine.play(this.sound);
			else if (this.sound.isFading())
				this.sound.unfade();
		} else if (isActive && !this.sound.isFading()) {
			this.sound.fade();
		}
	}

	private float rangeGain(final Vec3 eye) {
		final double range = Config.CLIENT.effects.get_effectRange();
		final double d = Math.max(Math.abs(eye.x - this.posX), Math.max(Math.abs(eye.y - this.posY), Math.abs(eye.z - this.posZ)));
		final double p = MathStuff.clamp((range - RANGE_MARGIN - d) / RANGE_FADE, 0D, 1D);
		return (float) (p * p * (3D - 2D * p));
	}

	@Override
	protected void cleanUp() {
		if (this.sound != null) {
			if (this.sound.getState().isActive())
				this.sound.fade();
			else
				AudioEngine.stop(this.sound);
			this.sound = null;
		}
		super.cleanUp();
	}

	@Override
	protected void spawnJetParticle() {
		if (Collections.canFitWaterSpray()) {
			final int splashCount = getSpawnCount();

			for (int j = 0; (float) j < splashCount; ++j) {
				final double xOffset = (RANDOM.nextFloat() * 2.0F - 1.0F);
				final double zOffset = (RANDOM.nextFloat() * 2.0F - 1.0F);

				if (WorldUtils.isBlockSolid(this.world, BlockPos.containing(this.posX + xOffset, this.deltaY, this.posZ + zOffset)))
					continue;

				final int motionStr = this.jetStrength + 3;
				final double motionX = xOffset * (motionStr / 20.0D);
				final double motionZ = zOffset * (motionStr / 20.0D);
				final double motionY = 0.1D + RANDOM.nextFloat() * motionStr / 20.0D;
				final boolean added = Collections.addWaterSpray(this.world, this.posX + xOffset,
						this.deltaY, this.posZ + zOffset, motionX, motionY, motionZ);
				// If we could not add the collection is full. No sense beating a dead horse.
				if (!added)
					break;
			}
		}
	}

}
