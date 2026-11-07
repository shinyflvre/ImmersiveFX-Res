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

import dynamiclabs.immersivefx.lib.gui.Color;
import dynamiclabs.immersivefx.lib.gui.ColorPalette;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.BlockGetter;

import javax.annotation.Nonnull;

public class MoteFireFly extends MotionMoteParticle {

    private static final float XZ_MOTION_DELTA = 0.2F;
    private static final float Y_MOTION_DELTA = XZ_MOTION_DELTA / 2.0F;
    private static final float ACCELERATION = 0.004F;

    private static final int FULL_BRIGHT = 15728880;

    protected final SpriteSet sprites;
    protected final float baseAirFriction = 0.91F;
    protected final double xAcceleration;
    protected final double yAcceleration;
    protected final double zAcceleration;
    protected float particleScale;

    private boolean doRender;

    public MoteFireFly(@Nonnull final BlockGetter world, final double x, final double y, final double z) {
        this(world, x, y, z, ParticleSprites.get(ParticleTypes.FIREWORK));
    }

    private MoteFireFly(@Nonnull final BlockGetter world, final double x, final double y, final double z, @Nonnull final SpriteSet sprites) {
        super(world, x, y, z, 0, 0, 0, sprites.first());
        this.sprites = sprites;
        this.particleScale = (RANDOM.nextFloat() * 0.5F + 0.5F) * 2.0F;

        this.xd = RANDOM.nextGaussian() * XZ_MOTION_DELTA;
        this.zd = RANDOM.nextGaussian() * XZ_MOTION_DELTA;
        this.yd = RANDOM.nextGaussian() * Y_MOTION_DELTA;

        this.xAcceleration = RANDOM.nextGaussian() * ACCELERATION;
        this.yAcceleration = RANDOM.nextGaussian() / 2.0D * ACCELERATION;
        this.zAcceleration = RANDOM.nextGaussian() * ACCELERATION;

        this.moteGravity = 0D;

        this.particleScale *= 0.75F * 0.25F * 0.1F;
        this.lifetime = 120 + RANDOM.nextInt(12);

        setColor(ColorPalette.MC_YELLOW);
        this.doRender = true;
        setSpriteFromAge(this.sprites);
    }

    private void setColor(@Nonnull final Color color) {
        this.rCol = color.red();
        this.gCol = color.green();
        this.bCol = color.blue();
        this.alpha = 0.99F;
    }

    @Override
    protected void handleCollision(@Nonnull final CollisionResult collision) {
        this.xd *= 0.699999988079071D;
        this.zd *= 0.699999988079071D;
    }

    @Override
    protected void update() {
        this.yd += this.yAcceleration;
        this.xd += this.xAcceleration;
        this.zd += this.zAcceleration;
        this.xd *= this.baseAirFriction;
        this.yd *= this.baseAirFriction;
        this.zd *= this.baseAirFriction;

        super.update();

        if (isAlive()) {
            if (this.age > this.lifetime / 2) {
                final float raw = (int) ((1.0F - ((float) this.age - (float) (this.lifetime / 2)) / this.lifetime) * 254);
                this.alpha = (((int) (raw * 255F)) & 0xFF) / 255F;
            }
            setSpriteFromAge(this.sprites);
        }

        this.doRender = this.age < this.lifetime / 3 || (this.age + this.lifetime) / 3 % 2 == 0;
    }

    @Override
    public int getLightCoords(final float partialTick) {
        return FULL_BRIGHT;
    }

    @Nonnull
    @Override
    protected Layer getLayer() {
        return ParticleLayers.FIREFLY;
    }

    @Override
    public float getQuadSize(final float partialTick) {
        return this.particleScale;
    }

    @Override
    public void extract(@Nonnull final QuadParticleRenderState state, @Nonnull final Camera camera, final float partialTick) {
        if (this.doRender)
            super.extract(state, camera, partialTick);
    }
}
