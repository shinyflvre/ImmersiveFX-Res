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

package dynamiclabs.immersivefx.environs.fog;

import dynamiclabs.immersivefx.environs.handlers.CommonState;
import dynamiclabs.immersivefx.environs.library.DimensionInfo;
import dynamiclabs.immersivefx.lib.GameUtils;
import net.minecraft.world.phys.Vec3;
import dynamiclabs.immersivefx.platform.event.RenderFogEvent;
import dynamiclabs.immersivefx.environs.config.Config;

import javax.annotation.Nonnull;

/**
 * Calculates the fog ranges based on player elevation as compared to the
 * dimensions cloud height.
 */
public class HazeFogRangeCalculator extends VanillaFogRangeCalculator {

    protected static final int BAND_OFFSETS = 15;
    protected static final int BAND_CORE_SIZE = 10;
    protected static final float IMPACT_FAR = 0.6F;
    protected static final float IMPACT_NEAR = 0.95F;

    protected final FogResult cached = new FogResult();

    public HazeFogRangeCalculator() {
        super("HazeFogRangeCalculator");
    }

    @Override
    public boolean enabled() {
        return Config.CLIENT.fog.enableElevationHaze.get();
    }

    @Override
    @Nonnull
    public FogResult calculate(@Nonnull final RenderFogEvent event) {
        final DimensionInfo di = CommonState.getDimensionInfo();
        if (di.hasHaze()) {
            final float lowY = di.getCloudHeight() - BAND_OFFSETS;
            final float highY = di.getCloudHeight() + BAND_OFFSETS + BAND_CORE_SIZE;

            // Calculate the players Y. If it's in the band range calculate the fog
            // parameters
            final Vec3 eyes = GameUtils.getPlayer().getEyePosition(event.getPartialTick());
            if (eyes.y > lowY && eyes.y < highY) {
                final float coreLowY = lowY + BAND_OFFSETS;
                final float coreHighY = coreLowY + BAND_CORE_SIZE;

                float scaleFar = IMPACT_FAR;
                float scaleNear = IMPACT_NEAR;
                if (eyes.y < coreLowY) {
                    final float factor = (float) ((eyes.y - lowY) / BAND_OFFSETS);
                    scaleFar *= factor;
                    scaleNear *= factor;
                } else if (eyes.y > coreHighY) {
                    final float factor = (float) ((highY - eyes.y) / BAND_OFFSETS);
                    scaleFar *= factor;
                    scaleNear *= factor;
                }

                final float end = FogResult.renderDistance(event) * (1F - scaleFar);
                final float start = FogResult.renderDistance(event) * (1F - scaleNear);
                this.cached.set(start, end);
                return this.cached;
            }
        }

        this.cached.set(event);
        return this.cached;
    }

}
