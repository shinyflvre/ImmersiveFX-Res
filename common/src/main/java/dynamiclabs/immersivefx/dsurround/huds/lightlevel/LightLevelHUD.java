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

package dynamiclabs.immersivefx.dsurround.huds.lightlevel;

import dynamiclabs.immersivefx.platform.event.ExtractLevelRenderStateEvent;
import dynamiclabs.immersivefx.platform.event.PlayerTickEvent;
import dynamiclabs.immersivefx.platform.event.SubmitCustomGeometryEvent;
import dynamiclabs.immersivefx.ImmersiveFX;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dynamiclabs.immersivefx.dsurround.DynamicSurroundings;
import dynamiclabs.immersivefx.dsurround.config.Config;
import dynamiclabs.immersivefx.lib.collections.ObjectArray;
import dynamiclabs.immersivefx.lib.gui.Color;
import dynamiclabs.immersivefx.lib.gui.ColorPalette;
import dynamiclabs.immersivefx.lib.particles.FrustumHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Quaternionf;

import javax.annotation.Nonnull;

public final class LightLevelHUD {

    private static Font font;

    public enum Mode {
        BLOCK,
        SKY,
        BLOCK_SKY
    }

    public enum ColorSet {

        BRIGHT(ColorPalette.MC_GREEN, ColorPalette.MC_YELLOW, ColorPalette.MC_RED, ColorPalette.MC_DARKAQUA),
        DARK(ColorPalette.MC_DARKGREEN, ColorPalette.MC_GOLD, ColorPalette.MC_DARKRED, ColorPalette.MC_DARKBLUE);

        public final Color safe;
        public final Color caution;
        public final Color hazard;
        public final Color noSpawn;

        ColorSet(@Nonnull final Color safe, @Nonnull final Color caution, @Nonnull final Color hazard,
                         @Nonnull final Color noSpawn) {
            this.safe = safe;
            this.caution = caution;
            this.hazard = hazard;
            this.noSpawn = noSpawn;
        }
    }

    private static final class LightCoord {
        public int x;
        public double y;
        public int z;
        public int lightLevel;
        public int color;
    }

    private static boolean showHUD = false;

    private static final int ALLOCATION_SIZE = 2048;
    private static final ObjectArray<LightCoord> lightLevels = new ObjectArray<>(ALLOCATION_SIZE);
    private static final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
    private static int nextCoord = 0;
    private static long tickCount = 0;

    private static final String[] lightLevelText = new String[16];
    private static final FormattedCharSequence[] lightLevelSequence = new FormattedCharSequence[16];
    private static final int[] margins = new int[16];

    static {
        for (int i = 0; i < ALLOCATION_SIZE; i++)
            lightLevels.add(new LightCoord());

        for (int i = 0; i < 16; i++) {
            lightLevelText[i] = String.valueOf(i);
            lightLevelSequence[i] = FormattedCharSequence.forward(lightLevelText[i], Style.EMPTY);
        }
    }

    private static LightCoord nextCoord() {
        if (nextCoord == lightLevels.size())
            lightLevels.add(new LightCoord());
        return lightLevels.get(nextCoord++);
    }

    protected static boolean inFrustum(final double x, final double y, final double z) {
        return FrustumHelper.isLocationInFrustum(new Vec3(x, y, z));
    }

    @SuppressWarnings("deprecation")
    protected static boolean renderLightLevel(@Nonnull final BlockState state, @Nonnull final BlockState below) {
        return !state.isSolid() && !state.liquid() && below.isSolid();
    }

    protected static float heightAdjustment(@Nonnull final BlockState state, @Nonnull final BlockState below,
                                            @Nonnull final BlockPos pos) {
        final ClientLevel world = Minecraft.getInstance().level;
        if (state.isAir()) {
            final VoxelShape shape = below.getCollisionShape(world, pos.below());
            return shape.isEmpty() ? 0 : (float) shape.max(Direction.Axis.Y) - 1;
        }

        final VoxelShape shape = below.getCollisionShape(world, pos);
        if (shape.isEmpty())
            return 0F;
        final float adjust = (float) (shape.max(Direction.Axis.Y));
        return state.getBlock() == Blocks.SNOW ? adjust + 0.125F : adjust;
    }

    protected static void updateLightInfo(@Nonnull final Vec3 position) {

        final Font fr = Minecraft.getInstance().font;

        if (fr != font) {
            font = fr;
            for (int i = 0; i < 16; i++)
                margins[i] = -(font.width(lightLevelText[i]) + 1) / 2;
        }

        nextCoord = 0;

        final ClientLevel world = Minecraft.getInstance().level;
        if (world == null)
            return;

        final ColorSet colors = Config.CLIENT.lightLevel.colorSet.get();
        final Mode displayMode = Config.CLIENT.lightLevel.mode.get();
        final int skyLightSub = world.getSkyDarken();
        final int rangeXZ = Config.CLIENT.lightLevel.range.get() * 2 + 1;
        final int rangeY = Config.CLIENT.lightLevel.range.get() + 1;
        final int originX = Mth.floor(position.x) - (rangeXZ / 2);
        final int originZ = Mth.floor(position.z) - (rangeXZ / 2);
        final int originY = Mth.floor(position.y) - (rangeY - 3);

        for (int dX = 0; dX < rangeXZ; dX++)
            for (int dZ = 0; dZ < rangeXZ; dZ++) {

                final int trueX = originX + dX;
                final int trueZ = originZ + dZ;

                BlockState lastState = null;

                for (int dY = 0; dY < rangeY; dY++) {

                    final int trueY = originY + dY;

                    if (trueY < world.getMinY() + 1 || !inFrustum(trueX, trueY, trueZ))
                        continue;

                    final BlockPos pos = new BlockPos(trueX, trueY, trueZ);
                    final BlockState state = world.getBlockState(pos);

                    if (lastState == null)
                        lastState = world.getBlockState(pos.below());

                    if (renderLightLevel(state, lastState)) {
                        mutable.set(trueX, trueY, trueZ);

                        final boolean mobSpawn = lastState.isValidSpawn(world, mutable, EntityTypes.ZOMBIE);

                        if (mobSpawn || !Config.CLIENT.lightLevel.hideSafe.get()) {
                            final int blockLight = world.getBrightness(LightLayer.BLOCK, mutable);
                            final int skyLight = world.getBrightness(LightLayer.SKY, mutable) - skyLightSub;
                            final int effective = Math.max(blockLight, skyLight);

                            final int result;
                            if (displayMode == Mode.BLOCK_SKY) {
                                result = effective;
                            } else if (displayMode == Mode.BLOCK)  {
                                result = blockLight;
                            } else {
                                result = skyLight;
                            }

                            Color color = colors.safe;
                            if (!mobSpawn) {
                                color = colors.noSpawn;
                            } else if (blockLight <= Config.CLIENT.lightLevel.lightSpawnThreshold.get()) {
                                if (effective > Config.CLIENT.lightLevel.lightSpawnThreshold.get())
                                    color = colors.caution;
                                else
                                    color = colors.hazard;
                            }

                            if (!(color == colors.safe && Config.CLIENT.lightLevel.hideSafe.get())) {
                                final LightCoord coord = nextCoord();
                                coord.x = trueX;
                                coord.y = trueY + heightAdjustment(state, lastState, mutable) + 0.002D;
                                coord.z = trueZ;
                                coord.lightLevel = Mth.clamp(result, 0, 15);
                                coord.color = color.rgbWithAlpha(254);
                            }
                        }
                    }

                    lastState = state;
                }
            }
    }

    public static void toggleDisplay() {
        showHUD = !showHUD;
        DynamicSurroundings.LOGGER.info("Light Level HUD: %s", Boolean.toString(showHUD));
    }

    public static void doTick(@Nonnull final PlayerTickEvent event) {

        final Player player = event.getEntity();
        if (player == null || player.level() == null || !player.level().isClientSide())
            return;

        if (player != Minecraft.getInstance().player)
            return;
        tickCount++;

        if (!showHUD || Minecraft.getInstance().isPaused())
            return;

        if (tickCount % 4 != 0)
            return;

        updateLightInfo(player.position());
    }

    public static void captureFrustum(@Nonnull final ExtractLevelRenderStateEvent event) {
        FrustumHelper.setFrustum(event.getFrustum());
    }

    public static void render(@Nonnull final SubmitCustomGeometryEvent event) {
        if (!showHUD || nextCoord == 0 || font == null)
            return;

        final Player player = Minecraft.getInstance().player;
        if (player == null)
            return;

        drawStringRender(event.getPoseStack(), event.getSubmitNodeCollector(), event.getLevelRenderState().cameraRenderState.pos, player);
    }

    private static void drawStringRender(@Nonnull final PoseStack matrixStack, @Nonnull final SubmitNodeCollector collector,
                                         @Nonnull final Vec3 view, @Nonnull final Player player) {

        final boolean thirdPerson = !Minecraft.getInstance().options.getCameraType().isFirstPerson();
        Direction playerFacing = player.getDirection();
        if (thirdPerson)
            playerFacing = playerFacing.getOpposite();
        if (playerFacing == Direction.SOUTH || playerFacing == Direction.NORTH)
            playerFacing = playerFacing.getOpposite();
        final float rotationAngle = playerFacing.getOpposite().toYRot();

        final Quaternionf rotY = Axis.YP.rotationDegrees(rotationAngle);
        final Quaternionf rotX = Axis.XP.rotationDegrees(90);

        final int yAdjust = -(font.lineHeight / 2);
        final float scale = 0.07F;

        for (int i = 0; i < nextCoord; i++) {
            final LightCoord coord = lightLevels.get(i);
            final int margin = margins[coord.lightLevel];

            matrixStack.pushPose();
            matrixStack.translate(coord.x + 0.5D - view.x, coord.y - view.y, coord.z + 0.5D - view.z);
            matrixStack.rotate(rotY);
            matrixStack.rotate(rotX);
            matrixStack.scale(-scale, -scale, scale);

            collector.submitText(
                    matrixStack,
                    margin,
                    yAdjust,
                    lightLevelSequence[coord.lightLevel],
                    false,
                    Font.DisplayMode.NORMAL,
                    LightCoordsUtil.FULL_BRIGHT,
                    coord.color,
                    0,
                    0);

            matrixStack.popPose();
        }
    }
}
