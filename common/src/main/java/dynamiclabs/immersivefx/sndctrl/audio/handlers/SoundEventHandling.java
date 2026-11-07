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

import dynamiclabs.immersivefx.platform.event.ScreenOpeningEvent;
import dynamiclabs.immersivefx.ImmersiveFX;
import dynamiclabs.immersivefx.lib.GameUtils;
import dynamiclabs.immersivefx.lib.logging.IModLog;
import dynamiclabs.immersivefx.lib.random.XorShiftRandom;
import dynamiclabs.immersivefx.sndctrl.SoundControl;
import dynamiclabs.immersivefx.sndctrl.api.acoustics.IAcoustic;
import dynamiclabs.immersivefx.sndctrl.api.acoustics.IAcousticFactory;
import dynamiclabs.immersivefx.sndctrl.api.sound.Category;
import dynamiclabs.immersivefx.sndctrl.api.sound.ISoundInstance;
import dynamiclabs.immersivefx.sndctrl.audio.AudioEngine;
import dynamiclabs.immersivefx.sndctrl.audio.PlayerCenteredSoundInstance;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.resources.Identifier;
import org.apache.commons.lang3.StringUtils;
import dynamiclabs.immersivefx.sndctrl.config.Config;
import dynamiclabs.immersivefx.sndctrl.audio.*;
import dynamiclabs.immersivefx.sndctrl.library.Primitives;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

public final class SoundEventHandling {
    private static final IModLog LOGGER = SoundControl.LOGGER.createChild(SoundEventHandling.class);
    private static boolean hasPlayed = false;

    private SoundEventHandling() {
    }

    public static void onGuiOpen(@Nonnull final ScreenOpeningEvent event) {
        if (!hasPlayed && event.getNewScreen() instanceof TitleScreen) {

            hasPlayed = true;

            final List<String> possibles = Config.CLIENT.sound.startupSoundList.get()
                    .stream()
                    .map(StringUtils::trim)
                    .filter(s -> s.length() > 0)
                    .collect(Collectors.toList());

            if (possibles.size() == 0)
                return;

            final String res;
            if (possibles.size() == 1) {
                res = possibles.get(0);
            } else {
                res = possibles.get(XorShiftRandom.current().nextInt(possibles.size()));
            }

            final Identifier rl = Identifier.parse(res);
            final IAcoustic acoustic = Primitives.getSound(rl, Category.MASTER);
            IAcousticFactory factory = acoustic.getFactory();
            if (factory != null) {
                final ISoundInstance instance = new PlayerCenteredSoundInstance(acoustic.getFactory().createSound(), Category.MASTER);
                GameUtils.getMC().execute(() -> {
                    try {
                        AudioEngine.play(instance);
                    } catch (@Nonnull final Throwable t) {
                        LOGGER.error(t, "Error executing startup sound '%s'", rl.toString());
                    }
                });
            }
        }
    }
}
