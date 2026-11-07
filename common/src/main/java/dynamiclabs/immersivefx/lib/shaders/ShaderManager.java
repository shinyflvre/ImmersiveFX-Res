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

package dynamiclabs.immersivefx.lib.shaders;

import dynamiclabs.immersivefx.platform.event.RenderPipelineRegistrar;
import dynamiclabs.immersivefx.dsurround.mixins.RenderPipelinesAccessor;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.UniformType;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ShaderManager<T extends Enum<T> & IShaderResourceProvider> {

	private final Class<T> clazz;
	private final EnumMap<T, ShaderProgram> programs;
	private final Supplier<Boolean> supportCheck;
	private final Function<T, RenderPipeline> pipelineFactory;

	public ShaderManager(@Nonnull final Class<T> clazz) {
		this(clazz, () -> true);
	}

	public ShaderManager(@Nonnull final Class<T> clazz, @Nonnull final Supplier<Boolean> supportCheck) {
		this(clazz, supportCheck, ShaderManager::defaultPipeline);
	}

	public ShaderManager(@Nonnull final Class<T> clazz, @Nonnull final Supplier<Boolean> supportCheck, @Nonnull final Function<T, RenderPipeline> pipelineFactory) {
		Objects.requireNonNull(clazz);
		this.clazz = clazz;
		this.programs = new EnumMap<>(clazz);
		this.supportCheck = supportCheck;
		this.pipelineFactory = pipelineFactory;

		// Validate the entries provide sane info
		for (final T shader : clazz.getEnumConstants()) {
			final String shaderName = shader.name();
			Objects.requireNonNull(shader.getVertex(), String.format("%s provided null for vertex shader", shaderName));
			Objects.requireNonNull(shader.getFragment(), String.format("%s provided null for fragment shader", shaderName));
		}
	}

	public boolean supported() {
		return this.supportCheck.get();
	}

	@Nonnull
	public RenderPipeline getPipeline(@Nonnull final T shader) {
		return getProgram(shader).getPipeline();
	}

	@Nonnull
	private ShaderProgram getProgram(@Nonnull final T shader) {
		return this.programs.computeIfAbsent(shader, s -> new ShaderProgram(s.getShaderName(), this.pipelineFactory.apply(s), s.getUniforms()));
	}

	public void registerPipelines(@Nonnull final RenderPipelineRegistrar event) {
		for (final T shader : this.clazz.getEnumConstants())
			event.registerPipeline(getPipeline(shader));
	}

	@Nullable
	public ShaderCallContext useShader(@Nonnull final T shader, @Nullable final Consumer<ShaderCallContext> callback) {
		Objects.requireNonNull(shader);
		if (!supported())
			return null;
		final ShaderCallContext context = new ShaderCallContext(getProgram(shader));
		if (callback != null)
			callback.accept(context);
		return context;
	}

	@SuppressWarnings("unused")
	public void useShader(@Nonnull final T shader) {
		useShader(shader, null);
	}

	public void releaseShader() {
	}

	public void initShaders() {
		if (!supported())
			return;
		for (final T shader : this.clazz.getEnumConstants())
			getProgram(shader);
	}

	@Nonnull
	private static <T extends Enum<T> & IShaderResourceProvider> RenderPipeline defaultPipeline(@Nonnull final T shader) {
		final Identifier vertex = shader.getVertex();
		final BindGroupLayout uniforms = BindGroupLayout.builder()
				.withUniform(shader.getUniformBlock(), UniformType.UNIFORM_BUFFER)
				.build();
		return RenderPipeline.builder(RenderPipelinesAccessor.immersivefx$getGlobalsSnippet())
				.withBindGroupLayout(BindGroupLayouts.PROJECTION)
				.withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
				.withBindGroupLayout(uniforms)
				.withLocation(Identifier.fromNamespaceAndPath(vertex.getNamespace(), "pipeline/" + shader.getShaderName().toLowerCase(Locale.ROOT)))
				.withVertexShader(vertex)
				.withFragmentShader(shader.getFragment())
				.withVertexBinding(0, DefaultVertexFormat.POSITION_TEX)
				.withPrimitiveTopology(PrimitiveTopology.QUADS)
				.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
				.withCull(false)
				.build();
	}
}
