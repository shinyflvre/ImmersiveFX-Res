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

package dynamiclabs.immersivefx.environs.shaders;

import com.google.common.collect.ImmutableList;
import dynamiclabs.immersivefx.environs.Environs;
import net.minecraft.resources.Identifier;

import javax.annotation.Nonnull;
import java.util.Collection;

public enum ShaderPrograms {

    AURORA(
            "core/aurora",
            "core/aurora",
            "AuroraInfo",
            ImmutableList.of(
                    "time",
                    "resolution",
                    "topColor",
                    "middleColor",
                    "bottomColor",
                    "alpha"
            ));

    private final Identifier vertex;
    private final Identifier fragment;
    private final String uniformBlock;
    private final Collection<String> uniforms;

    ShaderPrograms(@Nonnull final String vert, @Nonnull final String frag, @Nonnull final String uniformBlock, @Nonnull final Collection<String> uniforms) {
        this.vertex = Identifier.fromNamespaceAndPath(Environs.MOD_ID, vert);
        this.fragment = Identifier.fromNamespaceAndPath(Environs.MOD_ID, frag);
        this.uniformBlock = uniformBlock;
        this.uniforms = uniforms;
    }

    @Nonnull
    public Identifier getVertex() {
        return this.vertex;
    }

    @Nonnull
    public Identifier getFragment() {
        return this.fragment;
    }

    @Nonnull
    public String getShaderName() {
        return this.name();
    }

    @Nonnull
    public String getUniformBlock() {
        return this.uniformBlock;
    }

    @Nonnull
    public Collection<String> getUniforms() {
        return this.uniforms;
    }
}
