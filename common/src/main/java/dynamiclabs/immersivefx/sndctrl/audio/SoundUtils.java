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

package dynamiclabs.immersivefx.sndctrl.audio;

import com.google.common.base.MoreObjects;
import dynamiclabs.immersivefx.lib.GameUtils;
import dynamiclabs.immersivefx.lib.compat.ModEnvironment;
import dynamiclabs.immersivefx.lib.logging.IModLog;
import dynamiclabs.immersivefx.sndctrl.SoundControl;
import dynamiclabs.immersivefx.sndctrl.api.sound.ISoundInstance;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import com.mojang.blaze3d.audio.Library;
import dynamiclabs.immersivefx.sndctrl.mixins.AccessorLibrary;
import dynamiclabs.immersivefx.sndctrl.mixins.AccessorSoundEngine;
import dynamiclabs.immersivefx.sndctrl.mixins.AccessorSoundManager;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.openal.*;
import org.lwjgl.system.MemoryStack;
import dynamiclabs.immersivefx.sndctrl.config.Config;
import dynamiclabs.immersivefx.sndctrl.audio.handlers.SoundFXProcessor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.IntBuffer;
import java.util.Map;
import java.util.Objects;

/**
 * Helper class that obtains information about the current sound processing environment
 * from sources that are not directly obtainable.
 */
public final class SoundUtils {
    private static final IModLog LOGGER = SoundControl.LOGGER.createChild(SoundUtils.class);

    private static final String[] HRTF_STATUS = {
            "ALC_HRTF_DISABLED_SOFT",
            "ALC_HRTF_ENABLED_SOFT",
            "ALC_HRTF_DENIED_SOFT",
            "ALC_HRTF_REQUIRED_SOFT",
            "ALC_HRTF_HEADPHONES_DETECTED_SOFT",
            "ALC_HRTF_UNSUPPORTED_FORMAT_SOFT"
    };

    private static int MAX_SOUNDS = 0;
    private static int SOUND_LIMIT = 255; // 0;
    private static final Map<String, SoundSource> categoryMapper;

    // Since the pieces of the Minecraft sound system are effectively singletons, cache these values for reuse.
    private static final Map<SoundInstance, ChannelAccess.ChannelHandle> playing;
    private static final Map<SoundInstance, Integer> delayed;

    private static boolean enhancedSounds = false;

    static {
        categoryMapper = new Object2ObjectOpenHashMap<>();
        for (final SoundSource sc : SoundSource.values())
            categoryMapper.put(sc.getName(), sc);

        final SoundEngine engine = ((AccessorSoundManager) GameUtils.getSoundHander()).sndctrl_getSoundEngine();
        playing = ((AccessorSoundEngine) engine).sndctrl_getInstanceToChannel();
        delayed = ((AccessorSoundEngine) engine).sndctrl_getQueuedSounds();
    }

    private SoundUtils() {
    }

    public static int getMaxSounds() {
        return MAX_SOUNDS;
    }

    public static boolean hasRoom() {
        return getTotalPlaying() < SOUND_LIMIT;
    }

    static int getTotalPlaying() {
        return playing.size() + delayed.size();
    }

    @Nonnull
    static Map<SoundInstance, ChannelAccess.ChannelHandle> getPlayingSounds() {
        return playing;
    }

    @Nonnull
    static Map<SoundInstance, Integer> getDelayedSounds() {
        return delayed;
    }

    @Nullable
    public static SoundSource getSoundCategory(@Nonnull final String name) {
        return categoryMapper.get(Objects.requireNonNull(name));
    }

    public static float getMasterGain() {
        return GameUtils.getGameSettings().getSoundSourceVolume(SoundSource.MASTER);
    }

    /**
     * Checks to see if a sound will be blocked from playing because the volume would be too low to hear.  This is
     * a best guess based on coarse information available.
     *
     * @param sound Sound instance to check for a volume block
     * @return true of the sound would be volume blocked; false otherwise
     */
    public static boolean isSoundVolumeBlocked(@Nonnull final SoundInstance sound) {
        Objects.requireNonNull(sound);
        return getMasterGain() <= 0F || (!sound.canStartSilent() && sound.getVolume() <= 0F);
    }

    /**
     * Determines if a sound is in range of a listener based on the sounds attenuation distance.
     *
     * @param listener Location of the listener
     * @param sound    The sound that is to be evaluated
     * @param pad      Additional distance to add when evaluating
     * @return true if the sound is within the attenuation distance; false otherwise
     */
    public static boolean inRange(@Nonnull final Vec3 listener, @Nonnull final SoundInstance sound, final int pad) {
        if (sound.isRelative() || sound.getAttenuation() == SoundInstance.Attenuation.NONE)
            return true;
        final Sound s = sound.getSound();
        if (s == null)
            return true;
        int distSq = s.getAttenuationDistance() + pad;
        distSq *= distSq;
        return listener.distanceToSqr(sound.getX(), sound.getY(), sound.getZ()) < distSq;
    }

    public static boolean inRange(@Nonnull final Vec3 listener, @Nonnull final SoundInstance sound) {
        return inRange(listener, sound, 0);
    }

    /**
     * Provides a debug string for the specified sound object.
     *
     * @param sound Sound instance to provide a debug string for
     * @return Debug string
     */
    @Nonnull
    public static String debugString(@Nullable final SoundInstance sound) {

        if (sound == null)
            return "null";

        if (sound instanceof ISoundInstance)
            return sound.toString();

        final Sound s = sound.getSound();

        //@formatter:off
        return MoreObjects.toStringHelper(sound)
                .addValue(sound.getIdentifier().toString())
                .addValue(sound.getSource().toString())
                .addValue(sound.getAttenuation().toString())
                .add("v", sound.getVolume())
                .add("p", sound.getPitch())
                .add("x", sound.getX())
                .add("y", sound.getY())
                .add("z", sound.getZ())
                .add("distance", s != null ? s.getAttenuationDistance() : 0)
                .add("streaming", s != null && s.shouldStream())
                .add("global", sound.isRelative())
                .toString();
        //@formatter:on
    }

    @Nonnull
    public static IntBuffer configureContextAttributes(@Nonnull final MemoryStack stack, @Nonnull final IntBuffer attributes, final long device) {
        enhancedSounds = false;
        try {
            if (!doEnhancedSounds())
                return attributes;

            final ALCCapabilities deviceCaps = ALC.createCapabilities(device);
            if (!deviceCaps.ALC_EXT_EFX) {
                LOGGER.warn("EFX audio extensions not available for the current sound device!");
                return attributes;
            }

            LOGGER.info("Enhanced sounds are enabled.  Will perform sound engine reconfiguration.");
            enhancedSounds = true;

            final IntBuffer result = stack.callocInt(attributes.remaining() + 2);
            for (int i = attributes.position(); i + 1 < attributes.limit(); i += 2) {
                final int key = attributes.get(i);
                if (key == 0)
                    break;
                if (key == EXTEfx.ALC_MAX_AUXILIARY_SENDS)
                    continue;
                result.put(key).put(attributes.get(i + 1));
            }
            result.put(EXTEfx.ALC_MAX_AUXILIARY_SENDS).put(4);
            return result.put(0).flip();
        } catch (@Nonnull final Throwable t) {
            enhancedSounds = false;
            LOGGER.warn(t.getMessage());
            return attributes;
        }
    }

    public static void initialize(@Nonnull final Library soundSystem) {

        try {

            final long device = ((AccessorLibrary) soundSystem).sndctrl_getCurrentDevice();

            boolean hasFX = false;
            if (enhancedSounds) {
                final ALCCapabilities deviceCaps = ALC.createCapabilities(device);
                hasFX = deviceCaps.ALC_EXT_EFX;

                if (!hasFX) {
                    LOGGER.warn("EFX audio extensions not available for the current sound device!");
                } else {
                    LOGGER.info("OpenAL context reports %d auxiliary sends", ALC10.alcGetInteger(device, EXTEfx.ALC_MAX_AUXILIARY_SENDS));

                    // If HRTF is available enable if configured to do so
                    if (deviceCaps.ALC_SOFT_HRTF) {
                        int status = ALC10.alcGetInteger(device, SOFTHRTF.ALC_HRTF_STATUS_SOFT);
                        LOGGER.info("HRTF status report before configuration: %s", hrtfStatus(status));
                        if (status == SOFTHRTF.ALC_HRTF_DISABLED_SOFT && Config.CLIENT.sound.enableHRTF.get()) {
                            final boolean result = SOFTHRTF.alcResetDeviceSOFT(device, new int[]{SOFTHRTF.ALC_HRTF_SOFT, ALC10.ALC_TRUE, EXTEfx.ALC_MAX_AUXILIARY_SENDS, 4, 0});
                            if (result) {
                                status = ALC10.alcGetInteger(device, SOFTHRTF.ALC_HRTF_STATUS_SOFT);
                                LOGGER.warn("After configuration OpenAL reports HRTF status %s", hrtfStatus(status));
                            } else {
                                LOGGER.warn("Unable to set HRTF feature in OpenAL");
                            }
                        } else {
                            LOGGER.info("HRTF is already configured or Dynamic Surroundings is not configured to enable");
                        }
                    }
                }
            }

            // Calculate the number of source slots available
            MAX_SOUNDS = ALC11.alcGetInteger(device, ALC11.ALC_MONO_SOURCES);
            SOUND_LIMIT = MAX_SOUNDS - 10;

            // Do this last because it is dependent on the sound calculations
            if (hasFX)
                SoundFXProcessor.initialize();

            final String vendor = AL10.alGetString(AL10.AL_VENDOR);
            final String version = AL10.alGetString(AL10.AL_VERSION);
            final String renderer = AL10.alGetString(AL10.AL_RENDERER);
            final String extensions = AL10.alGetString(AL10.AL_EXTENSIONS);

            LOGGER.info("Vendor: %s", vendor);
            LOGGER.info("Version: %s", version);
            LOGGER.info("Renderer: %s", renderer);
            LOGGER.info("Extensions: %s", extensions);

        } catch (@Nonnull final Throwable t) {
            LOGGER.warn(t.getMessage());
            LOGGER.warn("OpenAL special effects for sounds will not be available");
        }

        try {
            NaturalPanning.onDeviceReady(((AccessorLibrary) soundSystem).sndctrl_getCurrentDevice());
        } catch (@Nonnull final Throwable t) {
            LOGGER.warn("3D Panning Softness unavailable: %s", t.getMessage());
        }
    }

    @Nonnull
    private static String hrtfStatus(final int status) {
        return status >= 0 && status < HRTF_STATUS.length ? HRTF_STATUS[status] : Integer.toString(status);
    }

    private static final String COMPAT_FAILURE_MESSAGE = "===============================================================================================\n" +
            "%s is installed and can cause conflicts with Dynamic Surroundings.\n" +
            "Dynamic Surroundings enhanced sound processing will be disabled.\n" +
            "===============================================================================================";

    private static boolean doEnhancedSounds() {
        if (ModEnvironment.SoundFilters.isLoaded()) {
            LOGGER.warn(COMPAT_FAILURE_MESSAGE, "Sound Filters");
            return false;
        } else if (ModEnvironment.SoundPhysics.isLoaded()) {
            LOGGER.warn(COMPAT_FAILURE_MESSAGE, "SoundPhysics");
            return false;
        } else if (!Config.CLIENT.sound.enableEnhancedSounds.get()) {
            LOGGER.warn("Enhanced sounds are not enabled.  No fancy sounds for you!");
            return false;
        }

        return true;
    }

    public static void deinitialize(@Nonnull final Library soundSystem) {
        SoundFXProcessor.deinitialize();
    }

}
