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

package dynamiclabs.immersivefx.environs.config;

import dynamiclabs.immersivefx.platform.event.ConfigEvent;
import dynamiclabs.immersivefx.ImmersiveFX;
import dynamiclabs.immersivefx.dsurround.DynamicSurroundings;
import dynamiclabs.immersivefx.environs.Environs;
import dynamiclabs.immersivefx.environs.effects.JetEffect;
import dynamiclabs.immersivefx.environs.effects.particles.RippleStyle;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;

public final class Config {
    public static Client CLIENT;
    public static ModConfigSpec SPEC;

    public static void build(@Nonnull final ModConfigSpec.Builder builder) {
        CLIENT = new Client(builder);
    }

    private Config() {
    }

    private static void applyConfig() {
        Environs.LOGGER.setDebug(Config.CLIENT.logging.enableLogging.get());
        Environs.LOGGER.setTraceMask(Config.CLIENT.logging.flagMask.get());
    }

    public static void onLoad(final ConfigEvent configEvent) {
        if (configEvent.getSpec() != SPEC)
            return;
        applyConfig();
        Environs.LOGGER.debug("Loaded config file %s", configEvent.getFileName());
    }

    public static void onFileChange(final ConfigEvent configEvent) {
        if (configEvent.getSpec() != SPEC)
            return;
        Environs.LOGGER.debug("Config file changed %s", configEvent.getFileName());
        applyConfig();
    }


    public static class Client {

        public final Logging logging;
        public final Biome biome;
        public final Effects effects;
        public final Aurora aurora;
        public final Fog fog;
        public final Sound sound;

        Client(@Nonnull final ModConfigSpec.Builder builder) {
            this.logging = new Logging(builder);
            this.biome = new Biome(builder);
            this.effects = new Effects(builder);
            this.aurora = new Aurora(builder);
            this.fog = new Fog(builder);
            this.sound = new Sound(builder);
        }

        public static class Logging {

            public final BooleanValue enableLogging;
            public final IntValue flagMask;

            Logging(@Nonnull final ModConfigSpec.Builder builder) {
                builder.comment("Defines how logging will behave")
                        .translation("environs.cfg.logging")
                        .push("Logging Options");

                this.enableLogging = builder
                        .comment("Enables/disables debug logging of the mod")
                        .translation("environs.cfg.logging.EnableDebug")
                        .define("Debug Logging", false);

                this.flagMask = builder
                        .comment("Bitmask for toggling various debug traces")
                        .translation("environs.cfg.logging.FlagMask")
                        .defineInRange("Debug Flag Mask", 0, 0, Integer.MAX_VALUE);

                builder.pop();
            }
        }

        public static class Biome {

            public final IntValue worldSealevelOverride;
            public final ModConfigSpec.ConfigValue<List<? extends String>> biomeSoundBlacklist;

            Biome(@Nonnull final ModConfigSpec.Builder builder) {
                builder.comment("Options for controlling biome sound/effects")
                        .translation("environs.cfg.biomes")
                        .push("Biome Options");

                this.worldSealevelOverride = builder
                        .comment("Sealevel to set for Overworld (0 use default for World)")
                        .translation("environs.cfg.biomes.Sealevel")
                        .defineInRange("Overworld Sealevel Override", 0, 0, 256);

                this.biomeSoundBlacklist = builder
                        .comment("Dimension IDs where biome sounds will not be played")
                        .translation("environs.cfg.biomes.DimBlacklist")
                        .defineList("Dimension Blacklist", List::of, () -> "", s -> s instanceof String);

                builder.pop();
            }
        }

        public static class Effects {

            public final BooleanValue enableFireFlies;
            public final BooleanValue enableSteamJets;
            public final BooleanValue enableFireJets;
            public final BooleanValue enableBubbleJets;
            public final BooleanValue enableDustJets;
            public final BooleanValue enableFountainJets;
            public final BooleanValue disableUnderwaterParticles;
            public final BooleanValue enableWaterfalls;
            public final IntValue waterfallCutoff;
            public final BooleanValue enableWaterRipples;
            public final ModConfigSpec.EnumValue<RippleStyle> waterRippleStyle;

            Effects(@Nonnull final ModConfigSpec.Builder builder) {
                builder.comment("Options for controlling various effects")
                        .translation("environs.cfg.effects")
                        .push("Effect Options");

                this.enableFireFlies = builder
                        .worldRestart()
                        .comment("Enable/disable Firefly effect around plants")
                        .translation("environs.cfg.effects.Fireflies")
                        .define("Fireflies", true);

                this.enableSteamJets = builder
                        .worldRestart()
                        .comment("Enable/disable Steam Jets where lava meets water")
                        .translation("environs.cfg.effects.Steam")
                        .define("Steam Jets", true);

                this.enableFireJets = builder
                        .worldRestart()
                        .comment("Enable/disable Fire Jets in lava")
                        .translation("environs.cfg.effects.Fire")
                        .define("Fire Jets", true);

                this.enableBubbleJets = builder
                        .worldRestart()
                        .comment("Enable/disable Bubble Jets under water")
                        .translation("environs.cfg.effects.Bubble")
                        .define("Bubble Jets", true);

                this.enableDustJets = builder
                        .worldRestart()
                        .comment("Enable/disable Dust Motes dropping from under blocks")
                        .translation("environs.cfg.effects.Dust")
                        .define("Dust Jets", true);

                this.enableFountainJets = builder
                        .worldRestart()
                        .comment("Enable/disable Fountain Jets spraying")
                        .translation("environs.cfg.effects.Fountain")
                        .define("Fountain Jets", true);

                this.enableWaterfalls = builder
                        .worldRestart()
                        .comment("Enable/disable Water Splash effects when water spills down")
                        .translation("environs.cfg.effects.Splash")
                        .define("Waterfall Splash", true);

                this.waterfallCutoff = builder
                        .comment("Limit sounds to waterfall strengths above this value")
                        .translation("environs.cfg.effects.WaterfallCutoff")
                        .defineInRange("Waterfall Strength Cutoff", 0, 0, JetEffect.MAX_STRENGTH);


                this.disableUnderwaterParticles = builder
                        .worldRestart()
                        .comment("Enable/disable Minecraft's Underwater particle effect")
                        .translation("environs.cfg.effects.Underwater")
                        .define("Disable Underwater Particles", false);

                this.enableWaterRipples = builder
                        .comment("Enable/disable Water Ripple effects for rain and water splash")
                        .translation("environs.cfg.effects.WaterRipple")
                        .define("Water Ripples", true);

                // The default validator chokes on this enum for some reason
                this.waterRippleStyle = builder
                        .comment("The render style of water ripples")
                        .translation("environs.cfg.effects.WaterRippleStyle")
                        .defineEnum("Water Ripple Style", RippleStyle.PIXELATED_CIRCLE);

                builder.pop();
            }

            // Reach over and grab from SoundControl
            public int get_effectRange() {
                return dynamiclabs.immersivefx.sndctrl.config.Config.CLIENT.effects.effectRange.get();
            }

        }

        public static class Aurora {

            public final BooleanValue auroraEnabled;
            public final IntValue maxBands;

            Aurora(@Nonnull final ModConfigSpec.Builder builder) {
                builder.comment("Options for controlling various effects")
                        .push("Effect Options");

                this.auroraEnabled = builder
                        .comment("Enable/disable Aurora processing")
                        .translation("environs.cfg.aurora.Enable")
                        .define("Auroras", true);

                this.maxBands = builder
                        .comment("Cap the maximum bands that will be rendered")
                        .translation("environs.cfg.aurora.MaxBands")
                        .defineInRange("Maximum Bands", 3, 0, 3);

                builder.pop();
            }
        }

        public static class Fog {

            public final BooleanValue enableFog;
            public final BooleanValue enableBiomeFog;
            public final BooleanValue enableElevationHaze;
            public final BooleanValue enableMorningFog;
            public final BooleanValue enableBedrockFog;
            public final BooleanValue enableWeatherFog;
            public final IntValue morningFogChance;
            public final IntValue minFogDistance;

            Fog(@Nonnull final ModConfigSpec.Builder builder) {
                builder.comment("Options that control the various fog effects in the client")
                        .translation("environs.cfg.fog")
                        .push("Fog Options");

                this.enableFog = builder
                        .comment("Enable/disable fog processing")
                        .translation("environs.cfg.fog.Enable")
                        .define("Enable Fog Processing", true);

                this.enableBiomeFog = builder
                        .comment("Enable biome specific fog density and color")
                        .translation("environs.cfg.fog.Biome")
                        .define("Biome Fog", true);

                this.enableElevationHaze = builder
                        .comment("Higher the player elevation the more haze that is experienced")
                        .translation("environs.cfg.fog.Haze")
                        .define("Elevation Haze", true);

                this.enableMorningFog = builder
                        .comment("Show morning fog that eventually burns off")
                        .translation("environs.cfg.fog.Morning")
                        .define("Morning Fog", true);

                this.enableBedrockFog = builder
                        .comment("Increase fog at bedrock layers")
                        .translation("environs.cfg.fog.Bedrock")
                        .define("Bedrock Fog", true);

                this.enableWeatherFog = builder
                        .comment("Increase fog based on the strength of rain")
                        .translation("environs.cfg.fog.Weather")
                        .define("Weather Fog", true);

                this.morningFogChance = builder
                        .comment("Percentage chance morning fog will occur")
                        .translation("environs.cfg.fog.MorningChance")
                        .defineInRange("Morning Fog Chance Percentage", 80, 1, 100);

                this.minFogDistance = builder
                        .comment("Fog never ends closer than this percentage of your render distance")
                        .translation("environs.cfg.fog.MinDistance")
                        .defineInRange("Minimum Fog Distance", 70, 0, 100);

                builder.pop();
            }
        }

        public static class Sound {

            public final IntValue biomeSoundVolume;
            public final IntValue spotSoundVolume;
            public final IntValue waterfallSoundVolume;
            public final BooleanValue occludeWaterfall;
            public final BooleanValue enable3DAmbience;
            public final IntValue ambienceDirectionality;

            Sound(@Nonnull final ModConfigSpec.Builder builder) {
                builder.comment("Options for defining sound behavior")
                        .translation("environs.cfg.sound")
                        .push("Sound Options");

                this.biomeSoundVolume = builder
                        .comment("Scaling factor to apply to biome sounds")
                        .translation("environs.cfg.sound.BiomeVolume")
                        .defineInRange("Biome Sound Volume", 100, 0, 100);

                this.spotSoundVolume = builder
                        .comment("Scaling factor to apply to spot sounds")
                        .translation("environs.cfg.sound.SpotVolume")
                        .defineInRange("Spot Sound Volume", 100, 0, 100);

                this.waterfallSoundVolume = builder
                        .comment("Scaling factor to apply to waterfall sounds")
                        .translation("environs.cfg.sound.WaterfallVolume")
                        .defineInRange("Waterfall Volume", 100, 0, 100);

                this.occludeWaterfall = builder
                        .comment("Occlude WATERFALL sounds")
                        .translation("environs.cfg.sound.OccludeWaterfall")
                        .define("Occlude WATERFALL sounds", false);

                this.enable3DAmbience = builder
                        .comment("Play biome ambience loops in 3D: several mono emitters around you that pan when you turn and get louder towards the biome they come from. Off = classic non-positional stereo loops")
                        .translation("environs.cfg.sound.3DAmbience")
                        .define("3D Biome Ambience", true);

                this.ambienceDirectionality = builder
                        .comment("How directional the 3D biome ambience is: 0 = always evenly all around you, 100 = only from the directions the biome is in")
                        .translation("environs.cfg.sound.AmbienceDirectionality")
                        .defineInRange("3D Ambience Directionality", 75, 0, 100);

                builder.pop();
            }
        }
    }
}
