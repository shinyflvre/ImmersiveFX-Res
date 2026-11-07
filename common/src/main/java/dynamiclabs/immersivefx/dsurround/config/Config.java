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

package dynamiclabs.immersivefx.dsurround.config;

import dynamiclabs.immersivefx.platform.event.ConfigEvent;
import dynamiclabs.immersivefx.ImmersiveFX;
import dynamiclabs.immersivefx.dsurround.DynamicSurroundings;
import dynamiclabs.immersivefx.dsurround.huds.lightlevel.LightLevelHUD;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.io.File;

public final class Config {
    public static Client CLIENT;
    public static ModConfigSpec SPEC;

    public static void build(@Nonnull final ModConfigSpec.Builder builder) {
        CLIENT = new Client(builder);
    }

    private Config() {
    }

    private static void applyConfig() {
        DynamicSurroundings.LOGGER.setDebug(Config.CLIENT.logging.enableLogging.get());
        DynamicSurroundings.LOGGER.setTraceMask(Config.CLIENT.logging.flagMask.get());
    }

    public static void onLoad(final ConfigEvent configEvent) {
        if (configEvent.getSpec() != SPEC)
            return;
        applyConfig();
        DynamicSurroundings.LOGGER.debug("Loaded config file %s", configEvent.getFileName());
    }

    public static void onFileChange(final ConfigEvent configEvent) {
        if (configEvent.getSpec() != SPEC)
            return;
        DynamicSurroundings.LOGGER.debug("Config file changed %s", configEvent.getFileName());
        applyConfig();
    }


    public static class Flags {
        public static int ALLOW_LIGHTLEVEL_HUD = 0x01;
        public static int ALLOW_CHUNK_BORDER_HUD = 0x02;
    }

    public static class Client {

        public final Logging logging;
        public final LightLevel lightLevel;

        Client(@Nonnull final ModConfigSpec.Builder builder) {
            this.logging = new Logging(builder);
            this.lightLevel = new LightLevel(builder);
        }

        public static class Logging {

            public final BooleanValue enableLogging;
            public final BooleanValue onlineVersionCheck;
            public final IntValue flagMask;

            Logging(@Nonnull final ModConfigSpec.Builder builder) {
                builder.comment("Defines how Sound Control logging will behave")
                        .push("Logging Options");

                this.enableLogging = builder
                        .comment("Enables/disables debug logging of the mod")
                        .define("Debug Logging", false);

                this.onlineVersionCheck = builder
                        .comment("Enables/disables display of version check information")
                        .define("Online Version Check Result", true);

                this.flagMask = builder
                        .comment("Bitmask for toggling various debug traces")
                        .defineInRange("Debug Flag Mask", 0, 0, Integer.MAX_VALUE);

                builder.pop();
            }
        }

        public static class LightLevel {

            public final ModConfigSpec.EnumValue<LightLevelHUD.ColorSet> colorSet;
            public final ModConfigSpec.EnumValue<LightLevelHUD.Mode> mode;
            public final IntValue range;
            public final BooleanValue hideSafe;
            public final IntValue lightSpawnThreshold;

            LightLevel(@Nonnull final ModConfigSpec.Builder builder) {
                builder.comment("Options for configuring the Light Level HUD")
                        .push("Light Level HUD Options");

                this.colorSet = builder
                        .comment("Coloring style to use for numbering")
                        .defineEnum("Color Set", LightLevelHUD.ColorSet.BRIGHT);

                this.mode = builder
                        .comment("The type of light level to display")
                        .defineEnum("Display Mode", LightLevelHUD.Mode.BLOCK);

                this.range = builder
                        .comment("Block range for light level analysis")
                        .defineInRange("Block Range", 16, 8, 64);

                this.hideSafe = builder
                        .comment("Do not show light level for blocks where mobs will not spawn")
                        .define("Hide Safe Blocks", false);

                this.lightSpawnThreshold = builder
                        .comment("Light level at which mobs can spawn")
                        .defineInRange("Mob Spawn Light Threshold", 7, 0, 15);

                builder.pop();
            }
        }
    }
}
