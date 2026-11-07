/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
/*
 * Dynamic Surroundings: Sound Control
 * Copyright (C) 2019  OreCruncher
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

import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;

import javax.annotation.Nonnull;

public class ParticleRenderType {

    public static final ParticleRenderType PARTICLE_SHEET_TRANSLUCENT = new ParticleRenderType(TextureAtlas.LOCATION_PARTICLES, true);

    public static final ParticleRenderType PARTICLE_SHEET_OPAQUE = new ParticleRenderType(TextureAtlas.LOCATION_PARTICLES, false);

    private final Identifier texture;
    private final boolean translucent;
    private SingleQuadParticle.Layer layer;

    public ParticleRenderType(@Nonnull final Identifier texture) {
        this(texture, true);
    }

    public ParticleRenderType(@Nonnull final Identifier texture, final boolean translucent) {
        this.texture = texture;
        this.translucent = translucent;
    }

    protected Identifier getTexture() {
        return this.texture;
    }

    public boolean isTranslucent() {
        return this.translucent;
    }

    @Nonnull
    public SingleQuadParticle.Layer getLayer() {
        final Identifier tex = getTexture();
        SingleQuadParticle.Layer l = this.layer;
        if (l == null || !l.textureAtlasLocation().equals(tex)) {
            l = this.translucent
                    ? new SingleQuadParticle.Layer(true, tex, RenderPipelines.TRANSLUCENT_PARTICLE, RenderPipelines.OIT_PARTICLE)
                    : new SingleQuadParticle.Layer(false, tex, RenderPipelines.OPAQUE_PARTICLE);
            this.layer = l;
        }
        return l;
    }

    @Override
    public String toString() {
        return getTexture().toString();
    }
}
