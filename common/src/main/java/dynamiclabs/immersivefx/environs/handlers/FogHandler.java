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
import dynamiclabs.immersivefx.platform.event.RenderFogEvent;
import dynamiclabs.immersivefx.lib.math.LoggingTimerEMA;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FogType;
import dynamiclabs.immersivefx.environs.config.Config;
import dynamiclabs.immersivefx.environs.fog.*;
import dynamiclabs.immersivefx.lib.compat.ModEnvironment;
import dynamiclabs.immersivefx.lib.events.DiagnosticEvent;

import javax.annotation.Nonnull;

public class FogHandler extends HandlerBase {

    protected final LoggingTimerEMA render = new LoggingTimerEMA("Render Fog");

    protected HolisticFogRangeCalculator fogRange = new HolisticFogRangeCalculator();

    public FogHandler() {
        super("Fog Handler");
    }

    public static boolean doFog() {
        return !ModEnvironment.ImmersiveVFX.isLoaded() && Config.CLIENT.fog.enableFog.get() && CommonState.getDimensionInfo().hasFog();
    }

    @Override
    public void process(@Nonnull final Player player) {
        if (doFog()) {
            this.fogRange.tick();
        }
    }

    @Override
    protected void registerEvents() {
        listen(RenderEvents.RENDER_FOG, Priority.LOWEST, this::fogRenderEvent);
        listen(DiagnosticEvent.BUS, Priority.NORMAL, this::diagnostics);
    }

    public void fogRenderEvent(@Nonnull final RenderFogEvent event) {
        if (event.getType() == FogType.ATMOSPHERIC && doFog()) {
            final ProfilerFiller profiler = Profiler.get();
            profiler.push("Environs Fog Render");
            this.render.begin();
            final FogResult result = this.fogRange.calculate(event);
            final FogData data = event.getFogData();
            final float minEnd = data.renderDistanceEnd * Config.CLIENT.fog.minFogDistance.get() / 100F;
            final float end = Math.max(Math.min(data.renderDistanceEnd, result.getEnd()), minEnd);
            final float start = Math.min(Math.max(Math.min(data.renderDistanceStart, result.getStart()), minEnd * 0.5F), end);
            data.renderDistanceStart = start;
            data.renderDistanceEnd = end;
            this.render.end();
            profiler.pop();
        }
    }

    public void diagnostics(final DiagnosticEvent event) {
        if (Config.CLIENT.logging.enableLogging.get()) {
            if (doFog()) {
                event.getLeft().add("Fog Range: " + this.fogRange.toString());
                event.addRenderTimer(this.render);
            } else
                event.getLeft().add("FOG: IGNORED");
        }
    }

    @Override
    public void onConnect() {
        this.fogRange = new HolisticFogRangeCalculator();
        this.fogRange.add(new BiomeFogRangeCalculator());
        this.fogRange.add(new HazeFogRangeCalculator());
        this.fogRange.add(new MorningFogRangeCalculator());
        this.fogRange.add(new BedrockFogRangeCalculator());
        this.fogRange.add(new WeatherFogRangeCalculator());

//			this.fogRange
//					.add(new FixedFogRangeCalculator(this.theme.getMinFogDistance(), this.theme.getMaxFogDistance()));

    }
}
