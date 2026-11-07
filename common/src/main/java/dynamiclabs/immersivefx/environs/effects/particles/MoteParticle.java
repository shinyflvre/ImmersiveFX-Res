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

import dynamiclabs.immersivefx.lib.GameUtils;
import dynamiclabs.immersivefx.lib.random.XorShiftRandom;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.Random;

public abstract class MoteParticle extends SingleQuadParticle {

    protected static final Random RANDOM = XorShiftRandom.current();

    protected final BlockGetter world;
    protected final BlockPos.MutableBlockPos position = new BlockPos.MutableBlockPos();

    long lastTicked;

    protected MoteParticle(@Nonnull final BlockGetter world, final double x, final double y, final double z, @Nonnull final TextureAtlasSprite sprite) {
        super(GameUtils.getWorld(), x, y, z, sprite);
        this.world = world;
        this.hasPhysics = false;
        this.gravity = 0F;
        this.xd = this.yd = this.zd = 0;
        this.lifetime = 0;
        this.position.set(x, y, z);
        this.lastTicked = this.level.getGameTime();
        configureColor();
    }

    public void configureColor() {
        this.rCol = this.gCol = this.bCol = this.alpha = 1F;
    }

    @Nonnull
    public Vec3 getPosition() {
        return new Vec3(this.x, this.y, this.z);
    }

    @Override
    public ParticleRenderType getGroup() {
        return ParticleLayers.GROUP;
    }

    @Override
    public final void tick() {
        this.lastTicked = this.level.getGameTime();
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            remove();
            return;
        }
        update();
        if (isAlive()) {
            setPos(this.x, this.y, this.z);
            this.position.set(this.x, this.y, this.z);
        }
    }

    protected void update() {
    }

    boolean isLive(@Nonnull final ClientLevel current) {
        return isAlive() && this.level == current && current.getGameTime() - this.lastTicked <= 5;
    }
}
