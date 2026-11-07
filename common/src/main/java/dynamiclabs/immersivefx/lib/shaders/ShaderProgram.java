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

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class ShaderProgram {

    private final String name;
    private final RenderPipeline pipeline;
    private final List<String> uniformNames = new ArrayList<>();
    private final Object2IntOpenHashMap<String> uniforms = new Object2IntOpenHashMap<>();

    ShaderProgram(@Nonnull final String name, @Nonnull final RenderPipeline pipeline, @Nonnull final Collection<String> uniforms) {
        this.name = name;
        this.pipeline = pipeline;
        this.uniforms.defaultReturnValue(-1);
        for (final String u : uniforms) {
            this.uniforms.put(u, this.uniformNames.size());
            this.uniformNames.add(u);
        }
    }

    @Nonnull
    RenderPipeline getPipeline() {
        return this.pipeline;
    }

    @Nonnull
    List<String> getUniformNames() {
        return this.uniformNames;
    }

    int getUniform(@Nonnull final String uniform) {
        return this.uniforms.getInt(uniform);
    }

    @Nonnull
    String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return String.format("%s [%s]", this.name, this.pipeline.getLocation());
    }
}
