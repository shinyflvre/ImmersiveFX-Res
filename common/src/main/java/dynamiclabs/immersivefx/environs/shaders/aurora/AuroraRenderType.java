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

import dynamiclabs.immersivefx.dsurround.mixins.RenderPipelinesAccessor;
import dynamiclabs.immersivefx.platform.event.RenderPipelineRegistrar;
import dynamiclabs.immersivefx.ImmersiveFX;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.BlendFactor;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.UniformType;
import dynamiclabs.immersivefx.environs.Environs;
import dynamiclabs.immersivefx.environs.shaders.ShaderPrograms;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class AuroraRenderType {

    public static final BindGroupLayout AURORA_INFO = BindGroupLayout.builder()
            .withUniform(ShaderPrograms.AURORA.getUniformBlock(), UniformType.UNIFORM_BUFFER)
            .build();

    private static final BlendFunction AURORA_TRANSPARENCY = new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE, BlendFactor.ONE, BlendFactor.ZERO);

    public static final RenderPipeline QUAD = RenderPipeline.builder(RenderPipelinesAccessor.immersivefx$getGlobalsSnippet())
            .withBindGroupLayout(BindGroupLayouts.PROJECTION)
            .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
            .withBindGroupLayout(AURORA_INFO)
            .withLocation(Identifier.fromNamespaceAndPath(Environs.MOD_ID, "pipeline/aurora"))
            .withVertexShader(ShaderPrograms.AURORA.getVertex())
            .withFragmentShader(ShaderPrograms.AURORA.getFragment())
            .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX)
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .withColorTargetState(new ColorTargetState(AURORA_TRANSPARENCY))
            .withCull(false)
            .build();

    private AuroraRenderType() {
    }

    public static void registerPipelines(final RenderPipelineRegistrar event) {
        event.registerPipeline(QUAD);
    }
}
