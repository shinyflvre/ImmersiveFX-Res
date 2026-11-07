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

package dynamiclabs.immersivefx.lib;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.shapes.CollisionContext;
import dynamiclabs.immersivefx.lib.compat.ModEnvironment;
import dynamiclabs.immersivefx.lib.reflection.ReflectionHelper;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

@SuppressWarnings("unused")
public final class WorldUtils {

    /**
     * Temperatures LESS than this value are considered cold temperatures.
     */
    public static final float COLD_THRESHOLD = 0.2F;

    /**
     * Temperatures LESS than this value are considered cold enough for snow.
     */
    public static final float SNOW_THRESHOLD = 0.15F;

    /**
     * SereneSeasons support to obtain the temperature at a specific block location.
     */
    private interface ITemperatureHandler {
        float getTemp(@Nonnull final Level world, @Nonnull final BlockPos pos);
    }

    /**
     * Weather support to obtain current rain/thunder strength client side
     */
    private interface IWeatherStrength {
        float getStrength(@Nonnull final Level world, final float partialTicks);
    }

    /**
     * Weather support to determine if an aspect of weather is occuring.
     */
    private interface IWeatherAspect {
        boolean isOccuring(@Nonnull final Level world);
    }

    private static final ITemperatureHandler TEMP;
    private static final IWeatherStrength RAIN_STRENGTH;
    private static final IWeatherStrength THUNDER_STRENGTH;
    private static final IWeatherAspect RAIN_OCCURING;
    private static final IWeatherAspect THUNDER_OCCURING;

    private static final Method BIOME_TEMPERATURE = ReflectionHelper.resolveMethod(Biome.class,
            new String[]{"getTemperature", "getHeightAdjustedTemperature"}, BlockPos.class, int.class);

    private static final Field FLAT_WORLD = ReflectionHelper.resolveField(ClientLevel.ClientLevelData.class, "isFlat");

    static {
        ITemperatureHandler TEMP1 = WorldUtils::getVanillaTemperature;
        if (ModEnvironment.SereneSeasons.isLoaded()) {
            ITemperatureHandler hooked = null;
            try {
                final Class<?> clazz = Class.forName("sereneseasons.season.SeasonHooks");
                Method holderMethod = null;
                Method biomeMethod = null;
                for (final Method m : clazz.getMethods()) {
                    if (!"getBiomeTemperature".equals(m.getName()) || m.getParameterCount() != 3)
                        continue;
                    final Class<?>[] p = m.getParameterTypes();
                    if (!p[0].isAssignableFrom(Level.class) || p[2] != BlockPos.class)
                        continue;
                    if (p[1] == Holder.class)
                        holderMethod = m;
                    else if (p[1] == Biome.class)
                        biomeMethod = m;
                }
                if (holderMethod != null) {
                    final Method method = holderMethod;
                    hooked = (world, pos) -> {
                        try {
                            return ((Number) method.invoke(null, world, world.getBiome(pos), pos)).floatValue();
                        } catch (@Nonnull final Throwable t) {
                            return getVanillaTemperature(world, pos);
                        }
                    };
                } else if (biomeMethod != null) {
                    final Method method = biomeMethod;
                    hooked = (world, pos) -> {
                        try {
                            return ((Number) method.invoke(null, world, world.getBiome(pos).value(), pos)).floatValue();
                        } catch (@Nonnull final Throwable t) {
                            return getVanillaTemperature(world, pos);
                        }
                    };
                }
            } catch (@Nonnull final Throwable ignore) {
            }
            if (hooked != null) {
                TEMP1 = hooked;
                Lib.LOGGER.info("Hooked SereneSeasons getBiomeTemperature()");
            } else {
                Lib.LOGGER.warn("Unable to hook SereneSeasons getBiomeTemperature()!");
            }
        }
        TEMP = TEMP1;

        // Place holder for future
        RAIN_STRENGTH = Level::getRainLevel;
        RAIN_OCCURING = Level::isRaining;
        THUNDER_STRENGTH = Level::getThunderLevel;
        THUNDER_OCCURING = Level::isThundering;
    }

    private WorldUtils() {

    }

    public static float getVanillaTemperature(@Nonnull final LevelReader world, @Nonnull final BlockPos pos) {
        final Biome biome = world.getBiome(pos).value();
        return getBiomeTemperature(biome, pos, world.getSeaLevel());
    }

    public static float getBiomeTemperature(@Nonnull final Biome biome, @Nonnull final BlockPos pos, final int seaLevel) {
        if (BIOME_TEMPERATURE != null) {
            try {
                return (float) BIOME_TEMPERATURE.invoke(biome, pos, seaLevel);
            } catch (@Nonnull final Throwable ignore) {
            }
        }
        return biome.getBaseTemperature();
    }

    @Nonnull
    public static BlockPos getTopSolidOrLiquidBlock(@Nonnull final LevelReader world, @Nonnull final BlockPos pos) {
        return new BlockPos(pos.getX(), world.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ()), pos.getZ());
    }

    /**
     * Gets the temperature value for the given BlockPos in the world.
     */
    public static float getTemperatureAt(@Nonnull final Level world, @Nonnull final BlockPos pos) {
        return TEMP.getTemp(world, pos);
    }

    /**
     * Determines if the temperature value is considered a cold temperature.
     */
    public static boolean isColdTemperature(final float temp) {
        return temp < COLD_THRESHOLD;
    }

    /**
     * Determines if the temperature value is considered cold enough for snow.
     */
    public static boolean isSnowTemperature(final float temp) {
        return temp < SNOW_THRESHOLD;
    }

    /**
     * Determines if the side of the block at the specified position is considered solid.
     */
    public static boolean isSolid(@Nonnull final BlockGetter world, @Nonnull final BlockPos pos, @Nonnull final Direction dir) {
        final BlockState state = world.getBlockState(pos);
        return Block.isFaceFull(state.getCollisionShape(world, pos, CollisionContext.empty()), dir);
    }

    /**
     * Determines if the top side of the block at the specified position is considered solid.
     */
    public static boolean isTopSolid(@Nonnull final BlockGetter world, @Nonnull final BlockPos pos) {
        return isSolid(world, pos, Direction.UP);
    }

    /**
     * Determines if the block at the specified location is solid.
     */
    public static boolean isBlockSolid(@Nonnull final BlockGetter world, @Nonnull final BlockPos pos) {
        final BlockState state = world.getBlockState(pos);
        return state.isSolid();
    }

    /**
     * Determines if the block at the specified location is an air block.
     */
    public static boolean isAirBlock(@Nonnull final BlockGetter world, @Nonnull final BlockPos pos) {
        return isAirBlock(world.getBlockState(pos));
    }

    public static boolean isAirBlock(@Nonnull final BlockState state) {
        return state.isAir();
    }

    /**
     * Gets the precipitation currently falling at the specified location.  It takes into account temperature and the
     * like.
     */
    public static Biome.Precipitation getCurrentPrecipitationAt(@Nonnull final LevelReader world, @Nonnull final BlockPos pos) {
        if (!(world instanceof Level) || !isRaining((Level) world)) {
            // Not currently raining
            return Biome.Precipitation.NONE;
        }

        final Biome biome = world.getBiome(pos).value();

        // If the biome has no rain...
        if (!biome.hasPrecipitation())
            return Biome.Precipitation.NONE;

        // Is there a block above that is blocking the rainfall?
        final BlockPos p = getPrecipitationHeight(world, pos);
        if (p.getY() > pos.getY()) {
            return Biome.Precipitation.NONE;
        }

        // Use the temperature of the biome to get whether it is raining or snowing
        final float temp = getTemperatureAt((Level) world, pos);
        return isSnowTemperature(temp) ? Biome.Precipitation.SNOW : Biome.Precipitation.RAIN;
    }

    public static float getRainStrength(@Nonnull final Level world, final float partialTicks) {
        return RAIN_STRENGTH.getStrength(world, partialTicks);
    }

    public static float getThunderStrength(@Nonnull final Level world, final float partialTicks) {
        return THUNDER_STRENGTH.getStrength(world, partialTicks);
    }

    public static boolean isRaining(@Nonnull final Level world) {
        return RAIN_OCCURING.isOccuring(world);
    }

    public static boolean isThundering(@Nonnull final Level world) {
        return THUNDER_OCCURING.isOccuring(world);
    }

    @Nonnull
    public static BlockPos getPrecipitationHeight(@Nonnull final LevelReader world, @Nonnull final BlockPos pos) {
        return world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos);
    }

    public static boolean hasVoidParticles(@Nonnull final Level world) {
        return world.dimensionType().hasSkyLight();
    }

    public static boolean isSuperFlat(@Nonnull final Level world) {
        final LevelData info = world.getLevelData();
        if (info instanceof ClientLevel.ClientLevelData && FLAT_WORLD != null) {
            try {
                return FLAT_WORLD.getBoolean(info);
            } catch (@Nonnull final Throwable ignore) {
            }
        }
        return false;
    }
}
