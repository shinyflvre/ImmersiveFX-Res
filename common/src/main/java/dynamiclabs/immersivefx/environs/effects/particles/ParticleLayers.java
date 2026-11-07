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

import dynamiclabs.immersivefx.dsurround.mixins.RenderPipelinesAccessor;
import dynamiclabs.immersivefx.platform.event.ParticleGroupRegistrar;
import dynamiclabs.immersivefx.platform.event.RenderPipelineRegistrar;
import dynamiclabs.immersivefx.ImmersiveFX;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import dynamiclabs.immersivefx.environs.Environs;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.QuadParticleGroup;
import net.minecraft.client.renderer.texture.TextureAtlas;

import javax.annotation.Nonnull;
import java.util.EnumMap;
import java.util.Map;

public final class ParticleLayers {

    public static final RenderPipeline SOFT_PARTICLE_PIPELINE = RenderPipeline.builder(RenderPipelinesAccessor.immersivefx$getParticleSnippet())
            .withLocation(Identifier.fromNamespaceAndPath(Environs.MOD_ID, "pipeline/soft_particle"))
            .withFragmentShader(Identifier.fromNamespaceAndPath(Environs.MOD_ID, "core/soft_particle"))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .build();

    private static final Map<RippleStyle, SingleQuadParticle.Layer> RIPPLES = new EnumMap<>(RippleStyle.class);

    static {
        for (final RippleStyle style : RippleStyle.values())
            RIPPLES.put(style, new SingleQuadParticle.Layer(true, style.getTexture(), SOFT_PARTICLE_PIPELINE, RenderPipelines.OIT_PARTICLE));
    }

    public static final SingleQuadParticle.Layer SPRAY = new SingleQuadParticle.Layer(
            false, Identifier.fromNamespaceAndPath(Environs.MOD_ID, "textures/particles/rainsplash.png"), RenderPipelines.OPAQUE_PARTICLE);

    public static final SingleQuadParticle.Layer FIREFLY = new SingleQuadParticle.Layer(
            true, TextureAtlas.LOCATION_PARTICLES, SOFT_PARTICLE_PIPELINE, RenderPipelines.OIT_PARTICLE);

    public static final ParticleRenderType GROUP = new ParticleRenderType("ENVIRONS_MOTES", "EM");

    private ParticleLayers() {
    }

    @Nonnull
    public static SingleQuadParticle.Layer ripple(@Nonnull final RippleStyle style) {
        return RIPPLES.get(style);
    }

    public static void registerParticleGroups(final ParticleGroupRegistrar event) {
        event.register(GROUP, engine -> new QuadParticleGroup(engine, GROUP));
    }

    public static void registerPipelines(final RenderPipelineRegistrar event) {
        event.registerPipeline(SOFT_PARTICLE_PIPELINE);
    }
}
