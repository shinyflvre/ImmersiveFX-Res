/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
/*
 *  Dynamic Surroundings: Mob Effects
 *  Copyright (C) 2019  OreCruncher
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

package dynamiclabs.immersivefx.mobeffects.effects.particles;

import dynamiclabs.immersivefx.lib.math.MathStuff;
import dynamiclabs.immersivefx.lib.random.XorShiftRandom;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.Random;

public class FrostBreathParticle extends SingleQuadParticle {
    private final SpriteSet sprites;

    public FrostBreathParticle(@Nonnull final LivingEntity entity) {
        // Reuse the cloud sheet
        this(entity, ParticleUtils.getSpriteSet(ParticleTypes.CLOUD));
    }

    private FrostBreathParticle(@Nonnull final LivingEntity entity, @Nonnull final SpriteSet sprites) {
        super((ClientLevel) entity.level(), 0, 0, 0, 0.0D, 0.0D, 0.0D, sprites.first());
        this.sprites = sprites;

        final Random rand = XorShiftRandom.current();

        final Vec3 origin = ParticleUtils.getBreathOrigin(entity);
        final Vec3 trajectory = ParticleUtils.getLookTrajectory(entity);

        this.setPos(origin.x, origin.y, origin.z);
        this.xo = origin.x;
        this.yo = origin.y;
        this.zo = origin.z;

        this.xd = trajectory.x * 0.01D;
        this.yd = trajectory.y * 0.01D;
        this.zd = trajectory.z * 0.01D;

        this.setAlpha(0.2F);
        float f1 = 1.0F - (float) (rand.nextDouble() * (double) 0.3F);
        this.rCol = f1;
        this.gCol = f1;
        this.bCol = f1;
        this.quadSize *= 1.875F * (entity.isBaby() ? 0.125F : 0.25F);
        int i = (int) (8.0D / (rand.nextDouble() * 0.8D + 0.3D));
        this.lifetime = (int) MathStuff.max((float) i * 2.5F, 1.0F);
        this.hasPhysics = false;
        this.setSpriteFromAge(this.sprites);
    }

    @Override
    @Nonnull
    protected SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    @Override
    public float getQuadSize(float partialTicks) {
        return this.quadSize * MathStuff.clamp1(((float) this.age + partialTicks) / (float) this.lifetime * 32.0F);
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
