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

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import java.util.Optional;

public abstract class MotionMoteParticle extends MoteParticle {

    protected double moteGravity = 0.06D;

    protected MotionMoteParticle(@Nonnull final BlockGetter world, final double x, final double y, final double z,
                                 final double dX, final double dY, final double dZ, @Nonnull final TextureAtlasSprite sprite) {
        super(world, x, y, z, sprite);
        this.xd = dX;
        this.yd = dY;
        this.zd = dZ;
    }

    @Nonnull
    protected Optional<CollisionResult> detectCollision() {
        final BlockState state = this.world.getBlockState(this.position);

        // Air does not collide
        if (state.isAir())
            return Optional.empty();

        // Check fluid state because the particle could have landed in fluid
        final FluidState fluid = state.getFluidState();
        if (!fluid.isEmpty()) {
            // Potential of collision with a liquid
            final double height = fluid.getHeight(this.world, this.position) + this.position.getY();
            if (height >= this.y) {
                // Hit the surface of liquid
                return Optional.of(new CollisionResult(this.world, new Vec3(this.x, height, this.z), state, false, fluid));
            }
        }

        if (state.isSolid()) {
            final VoxelShape shape = state.getCollisionShape(this.world, this.position, CollisionContext.empty());
            if (!shape.isEmpty()) {
                final double height = shape.max(Direction.Axis.Y) + this.position.getY();
                if (height >= this.y) {
                    // Have a collision
                    return Optional.of(new CollisionResult(this.world, new Vec3(this.x, height, this.z), state, true, null));
                }
            }
        }

        return Optional.empty();
    }

    protected void handleCollision(@Nonnull final CollisionResult collision) {
        remove();
    }

    @Override
    protected void update() {
        this.yd -= this.moteGravity;
        this.x += this.xd;
        this.y += this.yd;
        this.z += this.zd;
        this.position.set(this.x, this.y, this.z);

        final Optional<CollisionResult> result = detectCollision();
        if (result.isPresent()) {
            handleCollision(result.get());
        } else {
            this.xd *= 0.9800000190734863D;
            this.yd *= 0.9800000190734863D;
            this.zd *= 0.9800000190734863D;
        }
    }
}
