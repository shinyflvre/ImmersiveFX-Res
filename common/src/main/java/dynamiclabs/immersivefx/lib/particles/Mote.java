/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
/*
 * Dynamic Surroundings: Sound Control
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
import dynamiclabs.immersivefx.lib.GameUtils;
import net.minecraft.client.Camera;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;

import javax.annotation.Nonnull;

public abstract class Mote implements IParticleMote {

    protected final BlockGetter world;
    protected final LevelReader lighting;

    protected boolean isAlive = true;
    protected double posX;
    protected double posY;
    protected double posZ;
    protected final BlockPos.MutableBlockPos position = new BlockPos.MutableBlockPos();

    protected int packedLighting;

    protected float red;
    protected float green;
    protected float blue;
    protected float alpha;

    public Mote(@Nonnull final BlockGetter world, final double x, final double y, final double z) {
        this.world = world;
        this.lighting = world instanceof LevelReader ? (LevelReader) world : GameUtils.getWorld();
        setPosition(x, y, z);
        configureColor();
    }

    public void setPosition(final double posX, final double posY, final double posZ) {
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.position.set(posX, posY, posZ);
    }

    @Nonnull
    public Vec3 getPosition() {
        return new Vec3(this.posX, this.posY, this.posZ);
    }

    public void configureColor() {
        this.red = this.green = this.blue = this.alpha = 1F;
    }

    @Override
    public boolean isAlive() {
        return this.isAlive;
    }

    @Override
    public void kill() {
        this.isAlive = false;
    }

    @Override
    public boolean tick() {
        if (isAlive()) {

            update();

            // The update() may have killed the mote
            if (isAlive()) {
                setPosition(this.posX, this.posY, this.posZ);
                updateBrightness();
            }
        }
        return isAlive();
    }

    protected void update() {

    }

    public void updateBrightness() {
        this.packedLighting = LightCoordsUtil.getLightCoords(this.lighting, this.position);
    }

    protected final double interpX(Camera info) {
        return info.position().x;
    }

    protected final double interpY(Camera info) {
        return info.position().y;
    }

    protected final double interpZ(Camera info) {
        return info.position().z;
    }

    protected float renderX(Camera info, final float partialTicks) {
        return (float) (this.posX - interpX(info));
    }

    protected float renderY(Camera info, final float partialTicks) {
        return (float) (this.posY - interpY(info));
    }

    protected float renderZ(Camera info, final float partialTicks) {
        return (float) (this.posZ - interpZ(info));
    }

    protected void drawVertex(final VertexConsumer buffer, final double x, final double y, final double z,
                              final float u, final float v) {
        buffer
                .addVertex((float) x, (float) y, (float) z)
                .setUv(u, v)
                .setColor(this.red, this.green, this.blue, this.alpha)
                .setLight(this.packedLighting);
    }

    public abstract void renderParticle(@Nonnull VertexConsumer buffer, @Nonnull Camera renderInfo, float partialTicks);
}