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

package dynamiclabs.immersivefx.sndctrl.config;

import dynamiclabs.immersivefx.platform.event.ConfigEvent;
import dynamiclabs.immersivefx.ImmersiveFX;
import com.google.common.collect.ImmutableList;
import dynamiclabs.immersivefx.dsurround.DynamicSurroundings;
import dynamiclabs.immersivefx.sndctrl.SoundControl;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
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
        SoundControl.LOGGER.setDebug(Config.CLIENT.logging.enableLogging.get());
        SoundControl.LOGGER.setTraceMask(Config.CLIENT.logging.flagMask.get());
    }

    public static void onLoad(final ConfigEvent configEvent) {
        applyConfig();
        SoundControl.LOGGER.debug("Loaded config file %s", configEvent.getFileName());
    }

    public static void onFileChange(final ConfigEvent configEvent) {
        SoundControl.LOGGER.debug("Config file changed %s", configEvent.getFileName());
        applyConfig();
    }


    public static class Flags {
        public static final int SOUND_PLAY = 0x1;
        public static final int BASIC_SOUND_PLAY = 0x2;
    }

    public static class Client {

        public static final List<String> defaultSoundConfig = ImmutableList.<String>builder()
                .add("minecraft:entity.sheep.ambient cull")
                .add("minecraft:entity.chicken.ambient cull")
                .add("minecraft:entity.cow.ambient cull")
                .add("minecraft:entity.pig.ambient cull")
                .add("minecraft:entity.llama.ambient cull")
                .add("minecraft:entity.wither.spawn cull 10")
                .add("minecraft:entity.wither.death cull 10")
                .add("minecraft:entity.ender_dragon.death cull 10")
                .build();

        public static final List<String> defaultStartupSounds = ImmutableList.<String>builder()
                .add("minecraft:entity.experience_orb.pickup")
                .add("minecraft:entity.chicken.egg")
                .add("minecraft:ambient.underwater.exit")
                .build();

        public final Sound sound;
        public final Logging logging;
        public final Effects effects;

        Client(@Nonnull final ModConfigSpec.Builder builder) {
            this.sound = new Sound(builder);
            this.effects = new Effects(builder);
            this.logging = new Logging(builder);
        }

        public static class Sound {

            public final BooleanValue enableEnhancedSounds;
            public final BooleanValue enableHRTF;
            public final BooleanValue enableOcclusionCalcs;
            public final BooleanValue enableMonoConversion;
            public final IntValue cullInterval;
            public final IntValue backgroundThreadWorkers;
            public final IntValue streamingSoundCount;
            public final BooleanValue occludeWeather;
            public final BooleanValue occludeRecords;
            public final ConfigValue<List<? extends String>> individualSounds;
            public final ConfigValue<List<? extends String>> startupSoundList;
            public final BooleanValue enable2DPlayerSounds;
            public final ConfigValue<List<? extends String>> playerSounds2D;
            public final BooleanValue loopCrossfade;
            public final IntValue loopCrossfadeLength;
            public final IntValue fadeInTime;
            public final IntValue panningSoftness;

            Sound(@Nonnull final ModConfigSpec.Builder builder) {
                builder.comment("General options for defining sound effects")
                        .translation("sndctrl.cfg.sound")
                        .push("Sound Options");

                this.enableEnhancedSounds = builder
                        .worldRestart()
                        .comment("Enable sound reverb and filtering")
                        .translation("sndctrl.cfg.sound.EnhancedSounds")
                        .define("Enable Enhanced Sounds", true);

                this.enableHRTF = builder
                        .worldRestart()
                        .comment("Enable HRTF processing in OpenAL")
                        .translation("sndctrl.cfg.sound.EnhancedHRTF")
                        .define("Enable HRTF Processing", false);

                this.enableMonoConversion = builder
                        .comment("Enable conversion of stereo sounds to mono format for spacial play")
                        .translation("sndctrl.cfg.sound.MonoConversion")
                        .define("Enable Stereo to Mono Conversion", true);

                this.enableOcclusionCalcs = builder
                        .comment("Enable sound occlusion calculations (sound muffling when positioned behind blocks)")
                        .translation("sndctrl.cfg.sound.Occlusion")
                        .define("Enable Sound Occlusion Calculations", true);

                this.streamingSoundCount = builder
                        .worldRestart()
                        .comment("Number of streaming sound channels to configure")
                        .translation("sndctrl.cfg.sound.StreamingSoundCount")
                        .defineInRange("Streaming Sound Count", 10, 8, 16);

                this.occludeWeather = builder
                        .comment("Occlude WEATHER sounds")
                        .translation("sndctrl.cfg.sound.OccludeWeather")
                        .define("Occlude WEATHER Sounds", false);

                this.occludeRecords = builder
                        .comment("Occlude RECORDS sounds")
                        .translation("sndctrl.cfg.sound.OccludeRecords")
                        .define("Occlude RECORDS Sounds", false);

                this.individualSounds = builder
                        .comment("Options to configure sounds on an individual basis")
                        .translation("sndctrl.cfg.sound.Individual")
                        .defineListAllowEmpty("Individual Sound Config", defaultSoundConfig, () -> "", s -> s instanceof String);

                this.startupSoundList = builder
                        .comment("Possible sounds to play when client reaches main game menu")
                        .translation("sndctrl.cfg.sound.StartupSounds")
                        .defineListAllowEmpty("Startup Sound List", defaultStartupSounds, () -> "", s -> s instanceof String);

                this.enable2DPlayerSounds = builder
                        .comment("Play your own water sounds (diving, swimming, splashing, water steps) in 2D: in stereo at the listener")
                        .translation("sndctrl.cfg.sound.PlayerSounds2D")
                        .define("2D Own Water Sounds", true);

                this.playerSounds2D = builder
                        .comment("Sounds that play in 2D when they come from yourself (sound ids)")
                        .translation("sndctrl.cfg.sound.PlayerSounds2DList")
                        .defineListAllowEmpty("2D Own Sound List", dynamiclabs.immersivefx.sndctrl.audio.handlers.PlayerSounds2D.DEFAULT_SOUNDS, () -> "", s -> s instanceof String);

                this.loopCrossfade = builder
                        .comment("Make looping sounds (biome ambience, waterfalls) seamless: trim the silence at the start/end of the loop file and crossfade its end into its start")
                        .translation("sndctrl.cfg.sound.LoopCrossfade")
                        .define("Loop Crossfade", true);

                this.loopCrossfadeLength = builder
                        .comment("Length of the loop crossfade in milliseconds (at most a quarter of the loop is used)")
                        .translation("sndctrl.cfg.sound.LoopCrossfadeLength")
                        .defineInRange("Loop Crossfade Length", 1500, 100, 5000);

                this.fadeInTime = builder
                        .comment("Milliseconds a positional loop (e.g. a waterfall) takes to fade in when it starts and to fade out when it stops (0 = start/stop at once)")
                        .translation("sndctrl.cfg.sound.FadeInTime")
                        .defineInRange("Fade In Time", 1500, 0, 10000);

                this.panningSoftness = builder
                        .comment("How much a sound exactly to your side is also heard on the other side, on normal stereo output (0 = hard panning as in vanilla: 100/0, 30 = 85/15, 60 = 70/30, 100 = both sides equal). Not used with HRTF/Directional Audio or surround output")
                        .translation("sndctrl.cfg.sound.PanningSoftness")
                        .defineInRange("3D Panning Softness", 30, 0, 100);

                this.cullInterval = builder
                        .comment("Ticks between culled sound events (0 to disable culling)")
                        .translation("sndctrl.cfg.sound.CullInterval")
                        .defineInRange("Sound Culling Interval", 20, 0, 6000);

                this.backgroundThreadWorkers = builder
                        .worldRestart()
                        .comment("Number of background threads to handle sound effect calculations (0 is default)")
                        .translation("sndctrl.cfg.sound.Threads")
                        .defineInRange("Background Workers", 0, 0, 8);

                builder.pop();
            }
        }

        public static class Effects {

            public final BooleanValue fixupRandoms;
            public final IntValue effectRange;

            Effects(@Nonnull final ModConfigSpec.Builder builder) {
                builder.comment("Defines parameters for special effects")
                        .translation("sndctrl.cfg.effects")
                        .push("Effect Options");

                this.fixupRandoms = builder
                        .worldRestart()
                        .comment("Replace client side Randomizers with faster versions")
                        .translation("sndctrl.cfg.effects.Randoms")
                        .define("Replace Randoms", true);

                this.effectRange = builder
                        .worldRestart()
                        .comment("Block range of entity special effect handling")
                        .translation("sndctrl.cfg.effects.BlockRange")
                        .defineInRange("Block Range", 24, 16, 64);

                builder.pop();
            }
        }

        public static class Logging {

            public final BooleanValue enableLogging;
            public final IntValue flagMask;

            Logging(@Nonnull final ModConfigSpec.Builder builder) {
                builder.comment("Defines how Sound Control logging will behave")
                        .translation("sndctrl.cfg.logging")
                        .push("Logging Options");

                this.enableLogging = builder
                        .comment("Enables/disables debug logging of the mod")
                        .translation("sndctrl.cfg.logging.EnableDebug")
                        .define("Debug Logging", false);

                this.flagMask = builder
                        .comment("Bitmask for toggling various debug traces")
                        .translation("sndctrl.cfg.logging.FlagMask")
                        .defineInRange("Debug Flag Mask", 0, 0, Integer.MAX_VALUE);

                builder.pop();
            }
        }
    }
}
