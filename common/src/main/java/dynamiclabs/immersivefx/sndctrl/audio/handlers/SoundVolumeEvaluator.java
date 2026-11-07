/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
/*
 * Dynamic Surroundings: Sound Control
 * Copyright (C) 2019  OreCruncher
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

import com.google.common.base.Preconditions;
import dynamiclabs.immersivefx.lib.GameUtils;
import dynamiclabs.immersivefx.lib.Utilities;
import dynamiclabs.immersivefx.lib.collections.ObjectArray;
import dynamiclabs.immersivefx.lib.math.MathStuff;
import dynamiclabs.immersivefx.sndctrl.api.sound.Category;
import dynamiclabs.immersivefx.sndctrl.api.sound.ISoundCategory;
import dynamiclabs.immersivefx.sndctrl.api.sound.ISoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Function;

/**
 * Handler that calculates an effective volume for a given sound based on
 * various factors, including any user configured sound scaling.
 */
public final class SoundVolumeEvaluator {

    // Callbacks from other mods where volume can be scaled.  Goal is to get mods to use this callback rather than
    // replacing the sound during sound play.
    private static final ObjectArray<Function<SoundInstance, Float>> volumeScaleCallbacks = new ObjectArray<>();

    private SoundVolumeEvaluator() {
    }

    public static void register(@Nonnull final Function<SoundInstance, Float> callback) {
        volumeScaleCallbacks.add(callback);
    }

    private static float getVolumeScaleFromMods(@Nonnull final SoundInstance sound) {
        float result = SoundProcessor.getVolumeScale(sound);
        for (final Function<SoundInstance, Float> callback : volumeScaleCallbacks) {
            try {
                result = MathStuff.min(result, callback.apply(sound));
                if (result == 0F)
                    break;
            } catch (@Nonnull final Throwable ignore) {
            }
        }

        return MathStuff.clamp(result, 0, 4F);
    }

    private static float getCategoryVolumeScale(@Nonnull final SoundInstance sound) {
        final Optional<ISoundInstance> si = Utilities.safeCast(sound, ISoundInstance.class);
        if (si.isPresent()) {
            final ISoundCategory sc = si.get().getSoundCategory();
            return sc == Category.MASTER ? 1F : sc.getVolumeScale();
        }

        // Master category already controlled by master gain so ignore
        final SoundSource category = sound.getSource();
        return category == SoundSource.MASTER ? 1F : GameUtils.getGameSettings().getSoundSourceVolume(category);
    }

    /**
     * This guy is hooked by a Mixin to replace getClampedVolume() in Minecraft code.
     */
    public static float getClampedVolume(@Nonnull final SoundInstance sound) {
        // No special handling on CONFIG sounds
        if (sound instanceof ISoundInstance) {
            final ISoundCategory sc = ((ISoundInstance) sound).getSoundCategory();
            if (sc == Category.CONFIG)
                return sound.getVolume();
        }

        Preconditions.checkNotNull(sound);
        float volume = getVolumeScaleFromMods(sound)
                * getCategoryVolumeScale(sound)
                * sound.getVolume();
        return MathStuff.clamp1(volume);
    }

}
