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

import dynamiclabs.immersivefx.platform.event.RenderFogEvent;

import javax.annotation.Nonnull;

public final class FogResult {

    public static final float DEFAULT_PLANE_SCALE = 0.75F;

    private float start;
    private float end;

    public FogResult() {
        this(0, 0);
    }

    public FogResult(final float distance, final float scale) {
        this.set(distance, scale);
    }

    public FogResult(@Nonnull final RenderFogEvent event) {
        this.set(event);
    }

    public static float renderDistance(@Nonnull final RenderFogEvent event) {
        return event.getFogData().renderDistanceEnd;
    }

    public void setScaled(final float distance, final float scale) {
        this.start = distance * scale;
        this.end = distance;
    }

    public void set(final float start, final float end) {
        this.start = start;
        this.end = end;
    }

    public void set(@Nonnull final RenderFogEvent event) {
        this.setScaled(FogResult.renderDistance(event), DEFAULT_PLANE_SCALE);
    }

    public float getStart() {
        return this.start;
    }

    public float getEnd() {
        return this.end;
    }

    public boolean isValid(@Nonnull final RenderFogEvent event) {
        return this.end > this.start;
    }

    @Override
    public String toString() {
        return String.format("[start: %f, end: %f]", this.start, this.end);
    }

}
