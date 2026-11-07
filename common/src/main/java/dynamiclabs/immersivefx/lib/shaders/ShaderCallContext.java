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

import com.mojang.blaze3d.buffers.Std140Builder;
import dynamiclabs.immersivefx.lib.Lib;
import dynamiclabs.immersivefx.lib.gui.Color;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public final class ShaderCallContext {

    private final ShaderProgram program;
    private final Map<String, Object> values = new HashMap<>();

    ShaderCallContext(@Nonnull final ShaderProgram program) {
        this.program = program;
    }

    public void set(@Nonnull final String uniform, final float value) {
        put(uniform, new float[]{value});
    }

    public void set(@Nonnull final String uniform, final float v1, final float v2) {
        put(uniform, new float[]{v1, v2});
    }

    public void set(@Nonnull final String uniform, @Nonnull final Color color) {
        set(uniform, color, 1.0F);
    }

    public void set(@Nonnull final String uniform, @Nonnull final Color color, final float alpha) {
        put(uniform, new float[]{color.red(), color.green(), color.blue(), alpha});
    }

    public void set(@Nonnull final String uniform, @Nonnull final int... values) {
        put(uniform, values.clone());
    }

    private void put(@Nonnull final String uniform, @Nonnull final Object value) {
        if (getUniform(uniform) < 0) {
            Lib.LOGGER.warn("Cannot locate uniform '%s' for shader '%s'", uniform, this.program.getName());
            return;
        }
        this.values.put(uniform, value);
    }

    public void writeStd140(@Nonnull final Std140Builder builder) {
        for (final String name : this.program.getUniformNames()) {
            final Object v = this.values.get(name);
            if (v instanceof float[] f) {
                switch (f.length) {
                    case 1 -> builder.putFloat(f[0]);
                    case 2 -> builder.putVec2(f[0], f[1]);
                    case 3 -> builder.putVec3(f[0], f[1], f[2]);
                    default -> builder.putVec4(f[0], f[1], f[2], f[3]);
                }
            } else if (v instanceof int[] i) {
                if (i.length == 1) {
                    builder.putInt(i[0]);
                } else {
                    for (final int e : i)
                        builder.putIVec4(e, 0, 0, 0);
                }
            } else {
                builder.putFloat(0F);
            }
        }
    }

    private int getUniform(@Nonnull final String name) {
        return this.program.getUniform(name);
    }
}
