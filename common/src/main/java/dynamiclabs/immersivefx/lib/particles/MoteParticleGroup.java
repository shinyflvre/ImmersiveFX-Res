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

package dynamiclabs.immersivefx.lib.particles;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;

import javax.annotation.Nonnull;

public final class MoteParticleGroup extends ParticleGroup<ParticleCollection> {

    public static final ParticleRenderType GROUP = new ParticleRenderType("IMMERSIVEFX_MOTES", "IFX");

    private final MoteRenderState renderState = new MoteRenderState();

    public MoteParticleGroup(@Nonnull final ParticleEngine engine) {
        super(engine);
    }

    @Override
    @Nonnull
    public ParticleGroupRenderState extractRenderState(@Nonnull final Frustum frustum, @Nonnull final Camera camera, final float partialTickTime) {
        FrustumHelper.setFrustum(frustum);
        for (final ParticleCollection collection : this.particles) {
            try {
                collection.extract(this.renderState, frustum, camera, partialTickTime);
            } catch (@Nonnull final Throwable throwable) {
                final CrashReport report = CrashReport.forThrowable(throwable, "Rendering Particle");
                final CrashReportCategory category = report.addCategory("Particle being rendered");
                category.setDetail("Particle", collection::toString);
                category.setDetail("Particle Type", collection.getRenderType()::toString);
                throw new ReportedException(report);
            }
        }
        return this.renderState;
    }
}
