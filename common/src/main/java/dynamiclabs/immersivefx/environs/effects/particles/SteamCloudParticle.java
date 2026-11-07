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

import dynamiclabs.immersivefx.lib.random.XorShiftRandom;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.Random;

public class SteamCloudParticle extends SingleQuadParticle {

    private static final Random RANDOM = XorShiftRandom.current();

    private final SpriteSet sprites;

    public SteamCloudParticle(Level world, double x, double y, double z, double dY) {
        this((ClientLevel) world, x, y, z, dY, ParticleSprites.get(ParticleTypes.CLOUD));
    }

    private SteamCloudParticle(ClientLevel world, double x, double y, double z, double dY, SpriteSet sprites) {
        super(world, x, y, z, RANDOM.nextGaussian() * 0.02D, dY,
                RANDOM.nextGaussian() * 0.02D, sprites.first());

        this.sprites = sprites;

        this.xd *= 0.1F;
        this.yd *= 0.1F;
        this.zd *= 0.1F;
        this.yd += dY;
        float f1 = 1.0F - (float) (Math.random() * (double) 0.3F);
        this.rCol = f1;
        this.gCol = f1;
        this.bCol = f1;
        this.quadSize *= 1.875F;
        int i = (int) (8.0D / (Math.random() * 0.8D + 0.3D));
        this.lifetime = (int) Math.max((float) i * 2.5F, 1.0F);
        this.hasPhysics = false;
        this.setSpriteFromAge(this.sprites);
    }

    @Nonnull
    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    @Override
    public float getQuadSize(float partialTick) {
        return this.quadSize * Mth.clamp(((float) this.age + partialTick) / (float) this.lifetime * 32.0F, 0.0F, 1.0F);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.setSpriteFromAge(this.sprites);
            this.move(this.xd, this.yd, this.zd);
            this.xd *= 0.96F;
            this.yd *= 0.96F;
            this.zd *= 0.96F;
            if (this.onGround) {
                this.xd *= 0.7F;
                this.zd *= 0.7F;
            }
        }
    }
}
