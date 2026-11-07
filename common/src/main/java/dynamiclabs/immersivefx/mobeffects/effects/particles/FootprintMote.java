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

package dynamiclabs.immersivefx.mobeffects.effects.particles;

import dynamiclabs.immersivefx.dsurround.mixins.RenderPipelinesAccessor;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import dynamiclabs.immersivefx.lib.WorldUtils;
import dynamiclabs.immersivefx.lib.math.MathStuff;
import dynamiclabs.immersivefx.mobeffects.MobEffects;
import dynamiclabs.immersivefx.mobeffects.footsteps.FootprintStyle;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Quaternionf;

import javax.annotation.Nonnull;

public class FootprintMote extends SingleQuadParticle {

    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, "textures/particles/footprint.png");

    public static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelinesAccessor.immersivefx$getParticleSnippet())
            .withLocation(Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, "pipeline/footprint"))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
            .withCull(false)
            .build();

    private static final SingleQuadParticle.Layer LAYER = new SingleQuadParticle.Layer(true, TEXTURE, PIPELINE, RenderPipelines.OIT_PARTICLE);

    // Texture properties of the print
    private static final float TEXEL_WIDTH = 1F / 8F;
    private static final float TEXEL_PRINT_WIDTH = TEXEL_WIDTH / 2F;

    // Basic layout of the footprint
    private static final float WIDTH = 0.125F;

    // Micro Y adjuster to avoid z-fighting when rendering
    // multiple overlapping prints.
    private static float zFighter = 0F;

    protected final boolean isSnowLayer;
    protected final BlockPos position;
    protected final BlockPos downPos;

    protected final float texU1, texU2;
    protected final float texV1, texV2;
    protected final float scale;

    protected final Quaternionf orientation;
    protected final float halfX;
    protected final float halfZ;

    public FootprintMote(@Nonnull final FootprintStyle style, @Nonnull final ClientLevel world, final double x,
                         final double y, final double z, final float rotation, final float scale, final boolean isRight) {
        super(world, x, y, z, null);

        this.lifetime = 200;
        this.hasPhysics = false;
        this.gravity = 0F;

        if (++zFighter > 20)
            zFighter = 1;

        this.position = BlockPos.containing(x, y, z);
        final BlockState state = world.getBlockState(this.position);
        this.isSnowLayer = state.getBlock() == Blocks.SNOW;

        this.setPos(x, y + zFighter * 0.001F, z);
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        // Make sure that the down position is calculated from the display position!
        final float fraction = (float) (y - (int) y);
        if (this.isSnowLayer || fraction <= 0.0625F) {
            this.downPos = BlockPos.containing(this.x, this.y, this.z).below();
        } else {
            this.downPos = this.position.immutable();
        }

        float u1 = style.ordinal() * TEXEL_WIDTH + 1 / 256F;
        if (isRight)
            u1 += TEXEL_PRINT_WIDTH;
        this.texU1 = u1;
        this.texU2 = u1 + TEXEL_PRINT_WIDTH;
        this.texV1 = 0F;
        this.texV2 = 1F;
        this.scale = scale;

        final float theRotation = MathStuff.toRadians(-rotation + 180);
        final float cos = MathStuff.cos(theRotation);
        final float sin = MathStuff.sin(theRotation);
        final Matrix3f basis = new Matrix3f(
                cos, 0F, -sin,
                -sin, 0F, -cos,
                0F, 1F, 0F);
        this.orientation = new Quaternionf().setFromNormalized(basis);
        this.halfX = sin * WIDTH * scale;
        this.halfZ = cos * WIDTH * scale;
    }

    /**
     * Footprints age faster when raining
     */
    protected boolean advanceAge() {
        if (this.level.isRaining())
            this.age += (WorldUtils.getRainStrength(this.level, 1F) * 100F) / 25;
        return this.age++ >= this.lifetime;
    }

    @Override
    public void tick() {
        // The mote reached it's life expectancy
        if (advanceAge()) {
            remove();
            return;
        }

        if (this.isSnowLayer && this.level.getBlockState(this.position).getBlock() != Blocks.SNOW) {
            remove();
        } else {
            final BlockState state = this.level.getBlockState(this.downPos);
            if (!state.isSolid())
                remove();
        }
    }

    @Override
    @Nonnull
    protected SingleQuadParticle.Layer getLayer() {
        return LAYER;
    }

    @Override
    public float getQuadSize(final float partialTicks) {
        return WIDTH * this.scale;
    }

    @Override
    public void extract(@Nonnull final QuadParticleRenderState state, @Nonnull final Camera camera, final float partialTicks) {

        float f = (this.age + partialTicks) / ((float) this.lifetime + 1);
        f *= f;
        final float alpha = MathStuff.clamp1(1.0F - f) * 0.4F;
        final int color = ARGB.colorFromFloat(alpha, 1F, 1F, 1F);
        final int light = this.getLightCoords(partialTicks);

        final Vec3 cam = camera.position();
        final float x = (float) (this.x - cam.x());
        final float y = (float) (this.y - cam.y());
        final float z = (float) (this.z - cam.z());
        final float size = getQuadSize(partialTicks);
        final float vMid = (this.texV1 + this.texV2) / 2F;

        state.add(LAYER, x + this.halfX, y, z + this.halfZ,
                this.orientation.x, this.orientation.y, this.orientation.z, this.orientation.w,
                size, this.texU1, this.texU2, vMid, this.texV2, color, light);
        state.add(LAYER, x - this.halfX, y, z - this.halfZ,
                this.orientation.x, this.orientation.y, this.orientation.z, this.orientation.w,
                size, this.texU1, this.texU2, this.texV1, vMid, color, light);
    }

}
