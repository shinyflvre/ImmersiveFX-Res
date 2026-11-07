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

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.util.ARGB;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class MoteRenderState extends QuadParticleRenderState {

    private final Map<SingleQuadParticle.Layer, Recorder> layers = new LinkedHashMap<>();
    private int vertexCount;

    @Nonnull
    public VertexConsumer begin(@Nonnull final SingleQuadParticle.Layer layer) {
        return this.layers.computeIfAbsent(layer, l -> new Recorder(this));
    }

    @Override
    public void clear() {
        super.clear();
        this.layers.values().forEach(Recorder::reset);
        this.vertexCount = 0;
    }

    @Override
    public boolean isEmpty() {
        return this.vertexCount == 0;
    }

    @Override
    public Set<SingleQuadParticle.Layer> layers() {
        return this.layers.keySet();
    }

    @Override
    public void buildLayer(@Nonnull final SingleQuadParticle.Layer layer, @Nonnull final VertexConsumer bufferBuilder) {
        final Recorder r = this.layers.get(layer);
        if (r != null)
            r.replay(bufferBuilder);
    }

    @Override
    public void submit(@Nonnull final SubmitNodeCollector submitNodeCollector, @Nonnull final CameraRenderState camera) {
        if (!isEmpty())
            submitNodeCollector.submitQuadParticleGroup(this);
    }

    private static final class Recorder implements VertexConsumer {
        private static final int FLOATS = 5;
        private static final int INTS = 2;

        private final MoteRenderState owner;
        private float[] floats = new float[256 * FLOATS];
        private int[] ints = new int[256 * INTS];
        private int count;

        Recorder(@Nonnull final MoteRenderState owner) {
            this.owner = owner;
        }

        void reset() {
            this.count = 0;
        }

        void replay(@Nonnull final VertexConsumer builder) {
            final int complete = this.count - (this.count % 4);
            for (int i = 0; i < complete; i++) {
                final int f = i * FLOATS;
                final int n = i * INTS;
                builder.addVertex(this.floats[f], this.floats[f + 1], this.floats[f + 2])
                        .setUv(this.floats[f + 3], this.floats[f + 4])
                        .setColor(this.ints[n])
                        .setLight(this.ints[n + 1]);
            }
        }

        private int current() {
            return this.count - 1;
        }

        @Override
        public VertexConsumer addVertex(final float x, final float y, final float z) {
            if (this.count * FLOATS >= this.floats.length) {
                this.floats = Arrays.copyOf(this.floats, this.floats.length * 2);
                this.ints = Arrays.copyOf(this.ints, this.ints.length * 2);
            }
            final int f = this.count * FLOATS;
            this.floats[f] = x;
            this.floats[f + 1] = y;
            this.floats[f + 2] = z;
            this.floats[f + 3] = 0;
            this.floats[f + 4] = 0;
            final int n = this.count * INTS;
            this.ints[n] = -1;
            this.ints[n + 1] = 0xF000F0;
            this.count++;
            this.owner.vertexCount++;
            return this;
        }

        @Override
        public VertexConsumer setColor(final int r, final int g, final int b, final int a) {
            return setColor(ARGB.color(a, r, g, b));
        }

        @Override
        public VertexConsumer setColor(final int color) {
            if (this.count > 0)
                this.ints[current() * INTS] = color;
            return this;
        }

        @Override
        public VertexConsumer setUv(final float u, final float v) {
            if (this.count > 0) {
                final int f = current() * FLOATS;
                this.floats[f + 3] = u;
                this.floats[f + 4] = v;
            }
            return this;
        }

        @Override
        public VertexConsumer setUv1(final int u, final int v) {
            return this;
        }

        @Override
        public VertexConsumer setUv2(final int u, final int v) {
            if (this.count > 0)
                this.ints[current() * INTS + 1] = (u & 0xFFFF) | ((v & 0xFFFF) << 16);
            return this;
        }

        @Override
        public VertexConsumer setUv3(final float u, final float v) {
            return this;
        }

        @Override
        public VertexConsumer setNormal(final float x, final float y, final float z) {
            return this;
        }

        @Override
        public VertexConsumer setLineWidth(final float width) {
            return this;
        }
    }
}
