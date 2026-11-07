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

package dynamiclabs.immersivefx.sndctrl.audio.handlers;

import dynamiclabs.immersivefx.platform.event.ConfigEvent;
import dynamiclabs.immersivefx.ImmersiveFX;
import dynamiclabs.immersivefx.lib.TickCounter;
import dynamiclabs.immersivefx.lib.math.MathStuff;
import dynamiclabs.immersivefx.sndctrl.SoundControl;
import dynamiclabs.immersivefx.sndctrl.audio.SoundUtils;
import dynamiclabs.immersivefx.sndctrl.config.Config;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.Identifier;
import dynamiclabs.immersivefx.platform.event.PlaySoundEvent;
import dynamiclabs.immersivefx.platform.event.Priority;
import dynamiclabs.immersivefx.platform.event.SoundEngineEvents;
import dynamiclabs.immersivefx.sndctrl.library.IndividualSoundConfig;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public final class SoundProcessor {

    private static final float MIN_SOUNDFACTOR = 0F;
    private static final float MAX_SOUNDFACTOR = 4F;
    private static final float DEFAULT_SOUNDFACTOR = 1F;

    private static final Set<Identifier> blockedSounds = new ObjectOpenHashSet<>(32);
    private static final Object2LongOpenHashMap<Identifier> soundCull = new Object2LongOpenHashMap<>(32);
    private static final Object2FloatOpenHashMap<Identifier> volumeControl = new Object2FloatOpenHashMap<>(32);
    private static int cullInterval = 20;

    static {
        volumeControl.defaultReturnValue(DEFAULT_SOUNDFACTOR);
        SoundEngineEvents.PLAY_SOUND.register(Priority.HIGHEST, SoundProcessor::soundPlay);
    }

    private SoundProcessor() {
    }

    public static void onConfigLoad(@Nonnull final ConfigEvent configEvent) {
        applyConfig();
    }

    public static void onConfigChange(@Nonnull final ConfigEvent configEvent) {
        applyConfig();
    }

    public static void applyConfig() {
        soundCull.clear();
        blockedSounds.clear();
        volumeControl.clear();

        cullInterval = Config.CLIENT.sound.cullInterval.get();

        final List<IndividualSoundConfig> configs = Config.CLIENT.sound.individualSounds.get()
                .stream()
                .map(IndividualSoundConfig::createFrom)
                .filter(Objects::nonNull)
                .filter(cfg -> !cfg.isDefault())
                .collect(Collectors.toList());

        for (final IndividualSoundConfig cfg : configs) {
            if (cfg.isBlocked())
                blockedSounds.add(cfg.getLocation());
            if (cfg.isCulled())
                soundCull.put(cfg.getLocation(), -cullInterval);
            if (cfg.getVolumeScaleInt() != 100)
                volumeControl.put(cfg.getLocation(), MathStuff.clamp(cfg.getVolumeScale(), MIN_SOUNDFACTOR, MAX_SOUNDFACTOR));
        }
    }

    public static boolean isSoundBlocked(@Nonnull final Identifier sound) {
        return blockedSounds.contains(Objects.requireNonNull(sound));
    }

    public static float getVolumeScale(@Nonnull final Identifier sound) {
        return volumeControl.getFloat(Objects.requireNonNull(sound));
    }

    public static float getVolumeScale(@Nonnull final SoundInstance sound) {
        return getVolumeScale(Objects.requireNonNull(sound).getIdentifier());
    }

    private static boolean isSoundCulledLogical(@Nonnull final Identifier sound) {
        if (cullInterval > 0) {
            // Get the last time the sound was seen
            final long lastOccurance = soundCull.getLong(Objects.requireNonNull(sound));
            if (lastOccurance != 0) {
                final long currentTick = TickCounter.getTickCount();
                if ((currentTick - lastOccurance) < cullInterval) {
                    return true;
                } else {
                    // Set when it happened and fall through for remapping and stuff
                    soundCull.put(sound, currentTick);
                }
            }
        }
        return false;
    }

    private static boolean blockSoundProcess(@Nonnull final Identifier res) {
        return isSoundBlocked(res) || isSoundCulledLogical(res);
    }

    // Event handler for sound plays - hooked in static class initializer
    private static void soundPlay(@Nonnull final PlaySoundEvent e) {
        // If there is no sound assigned, or if there is no more room in the play lists kill it
        final SoundInstance theSound = e.getSound();
        if (theSound == null || !SoundUtils.hasRoom()) {
            e.setSound(null);
            return;
        }

        // Don't mess with our config sound instances from the config menu
        if (MusicFader.isConfigSoundInstance(theSound))
            return;

        // Check to see if we need to block sound processing
        final Identifier soundResource = theSound.getIdentifier();
        if (blockSoundProcess(soundResource)) {
            e.setSound(null);
        }
    }

}
