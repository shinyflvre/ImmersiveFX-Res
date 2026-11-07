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

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import dynamiclabs.immersivefx.environs.shaders.ShaderPrograms;
import dynamiclabs.immersivefx.lib.gui.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Consumer;

public final class AuroraRenderer {

    private static final int UBO_SIZE = new Std140SizeCalculator()
            .putVec4().putVec4().putVec4().putVec2().putFloat().putFloat().get();

    @Nullable
    private static GpuBuffer vertexBuffer;
    @Nullable
    private static MappableRingBuffer uniforms;

    private AuroraRenderer() {
    }

    public record Parameters(float time, float width, float height, @Nonnull Color top, @Nonnull Color middle,
                             @Nonnull Color bottom, float alpha) {
    }

    public static void draw(@Nonnull final Matrix4fc modelView, final int quadCount, @Nonnull final Parameters params,
                            @Nonnull final Consumer<BufferBuilder> generator) {
        if (quadCount <= 0)
            return;

        final GpuDevice device = RenderSystem.getDevice();
        final int indexCount;

        try (ByteBufferBuilder bytes = ByteBufferBuilder.exactlySized(quadCount * 4 * DefaultVertexFormat.POSITION_TEX.getVertexSize())) {
            final BufferBuilder builder = new BufferBuilder(bytes, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX);
            generator.accept(builder);
            try (MeshData mesh = builder.build()) {
                if (mesh == null)
                    return;
                indexCount = mesh.drawState().indexCount();
                final int size = mesh.vertexBuffer().remaining();
                if (vertexBuffer == null || vertexBuffer.size() < size) {
                    if (vertexBuffer != null)
                        vertexBuffer.close();
                    vertexBuffer = device.createBuffer(() -> "Environs aurora vertices", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST, size);
                }
                device.createCommandEncoder().writeToBuffer(vertexBuffer.slice(), mesh.vertexBuffer());
            }
        }

        if (uniforms == null)
            uniforms = new MappableRingBuffer(() -> "Environs aurora UBO", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE, UBO_SIZE);

        final GpuBuffer ubo = uniforms.currentBuffer();
        try (GpuBufferSlice.MappedView view = ubo.map(false, true)) {
            Std140Builder.intoBuffer(view.data())
                    .putVec4(params.top().red(), params.top().green(), params.top().blue(), 1F)
                    .putVec4(params.middle().red(), params.middle().green(), params.middle().blue(), 1F)
                    .putVec4(params.bottom().red(), params.bottom().green(), params.bottom().blue(), 1F)
                    .putVec2(params.width(), params.height())
                    .putFloat(params.time())
                    .putFloat(params.alpha());
        }

        final RenderSystem.AutoStorageIndexBuffer quadIndices = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
        final GpuBuffer indexBuffer = quadIndices.getBuffer(indexCount);
        final RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
        final GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().writeTransform(new Matrix4f(modelView));

        try (RenderPass pass = device.createCommandEncoder()
                .createRenderPass(() -> "Environs aurora", target.getColorTextureView(), Optional.empty(), target.getDepthTextureView(), OptionalDouble.empty())) {
            pass.setPipeline(RenderSystem.getCompiledPipeline(AuroraRenderType.QUAD));
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", transforms);
            pass.setUniform(ShaderPrograms.AURORA.getUniformBlock(), ubo);
            pass.setVertexBuffer(0, vertexBuffer.slice());
            pass.setIndexBuffer(indexBuffer, quadIndices.type());
            pass.drawIndexed(indexCount, 1, 0, 0, 0);
        }

        uniforms.rotate();
    }

    public static void close() {
        if (vertexBuffer != null) {
            vertexBuffer.close();
            vertexBuffer = null;
        }
        if (uniforms != null) {
            uniforms.close();
            uniforms = null;
        }
    }
}
