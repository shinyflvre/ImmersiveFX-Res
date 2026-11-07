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

package dynamiclabs.immersivefx.environs.handlers;

import dynamiclabs.immersivefx.platform.event.Priority;
import dynamiclabs.immersivefx.platform.event.RenderEvents;
import dynamiclabs.immersivefx.platform.event.AfterSkyEvent;
import javax.annotation.Nonnull;

import dynamiclabs.immersivefx.environs.shaders.aurora.AuroraFactory;
import dynamiclabs.immersivefx.environs.shaders.aurora.AuroraRenderer;
import dynamiclabs.immersivefx.environs.shaders.aurora.AuroraUtils;
import dynamiclabs.immersivefx.environs.shaders.aurora.IAurora;
import dynamiclabs.immersivefx.lib.GameUtils;
import dynamiclabs.immersivefx.lib.events.DiagnosticEvent;
import dynamiclabs.immersivefx.lib.math.LoggingTimerEMA;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;
import dynamiclabs.immersivefx.environs.config.Config;
import dynamiclabs.immersivefx.environs.Environs;
import dynamiclabs.immersivefx.lib.logging.IModLog;

public final class AuroraHandler extends HandlerBase {

	private static final IModLog LOGGER = Environs.LOGGER.createChild(AuroraHandler.class);
	private static AuroraHandler handler;

	private final LoggingTimerEMA render = new LoggingTimerEMA("Render Aurora");
	private IAurora current;
	private int dimensionId;

	public AuroraHandler() {
		super("Aurora");
	}

	@Override
	public void onConnect() {
		handler = this;
		this.current = null;
	}

	@Override
	public void onDisconnect() {
		this.current = null;
		handler = null;
		AuroraRenderer.close();
	}

	private boolean isAuroraTimeOfDay() {
		return CommonState.getDayCycle().isAuroraVisible();
	}

	private boolean canSpawnAurora() {
		return this.current == null && canAuroraStay();
	}

	private boolean canAuroraStay() {
		if (!Config.CLIENT.aurora.auroraEnabled.get())
			return false;

		return isAuroraTimeOfDay()
				&& AuroraUtils.getChunkRenderDistance() >= 6
				&& AuroraUtils.dimensionHasAuroras()
				&& CommonState.getTruePlayerBiome().getHasAurora();
	}

	@Override
	public void process(@Nonnull final Player player) {

		// Process the current aurora
		if (this.current != null) {
			// If completed or the player changed dimensions we want to kill
			// outright
			if (this.current.isComplete() || this.dimensionId != CommonState.getDimensionId()
					|| !Config.CLIENT.aurora.auroraEnabled.get()) {
				this.current = null;
			} else {
				this.current.update();
				final boolean isDying = this.current.isDying();
				final boolean canStay = canAuroraStay();
				if (isDying && canStay) {
					LOGGER.debug("Unfading aurora...");
					this.current.setFading(false);
				} else if (!isDying && !canStay) {
					LOGGER.debug("Aurora fade...");
					this.current.setFading(true);
				}
			}
		}

		// If there isn't a current aurora see if it needs to spawn
		if (canSpawnAurora()) {
			this.current = AuroraFactory.produce(AuroraUtils.getSeed());
			LOGGER.debug("New aurora [%s]", this.current.toString());
		}

		// Set the dimension in case it changed
		this.dimensionId = CommonState.getDimensionId();
	}

	private void doRender(@Nonnull final Matrix4fc modelView, @Nonnull final Vec3 camera, final float partialTick) {
		this.render.begin();
		if (this.current != null) {
			this.current.render(modelView, camera, partialTick);
		}
		this.render.end();
	}

	@Override
	protected void registerEvents() {
		listen(RenderEvents.AFTER_SKY, Priority.NORMAL, this::onAfterSky);
		listen(DiagnosticEvent.BUS, Priority.NORMAL, this::diagnostic);
	}

	public void onAfterSky(@Nonnull final AfterSkyEvent event) {
		if (this.current == null)
			return;
		final LevelRenderState state = event.getLevelRenderState();
		final ProfilerFiller profiler = Profiler.get();
		profiler.push("Aurora Render");
		doRender(event.getModelViewMatrix(), state.cameraRenderState.pos, state.worldPartialTicks);
		profiler.pop();
	}

	public void diagnostic(@Nonnull final DiagnosticEvent event) {
		if (Config.CLIENT.logging.enableLogging.get()) {
			event.getLeft().add("Aurora: " + (this.current == null ? "NONE" : this.current.toString()));
			event.getRenderTimers().add(this.render);
		}
	}
}
