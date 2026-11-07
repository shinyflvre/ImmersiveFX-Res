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

package dynamiclabs.immersivefx.sndctrl;

import dynamiclabs.immersivefx.lib.effects.EntityEffectHandler;
import dynamiclabs.immersivefx.lib.logging.ModLog;
import dynamiclabs.immersivefx.platform.event.ClientLifecycleEvents;
import net.minecraft.client.Minecraft;
import dynamiclabs.immersivefx.sndctrl.api.IMC;
import dynamiclabs.immersivefx.sndctrl.audio.AudioEngine;
import dynamiclabs.immersivefx.sndctrl.config.Config;
import dynamiclabs.immersivefx.sndctrl.gui.Keys;
import dynamiclabs.immersivefx.sndctrl.library.AcousticLibrary;
import dynamiclabs.immersivefx.sndctrl.library.AudioEffectLibrary;
import dynamiclabs.immersivefx.sndctrl.library.EntityEffectLibrary;
import dynamiclabs.immersivefx.sndctrl.library.SoundLibrary;

import javax.annotation.Nonnull;

public final class SoundControl {

    /**
     * ID of the mod
     */
    public static final String MOD_ID = "sndctrl";
    /**
     * Logging instance for trace
     */
    public static final ModLog LOGGER = new ModLog(SoundControl.class);

    public SoundControl() {

        // Various event bus registrations
        ClientLifecycleEvents.CLIENT_SETUP.register(this::clientSetup);
        ClientLifecycleEvents.LOAD_COMPLETE.register(this::setupComplete);
    }

    private void clientSetup(@Nonnull final Minecraft mc) {
        Keys.register();

        AudioEngine.initialize();
        EntityEffectLibrary.initialize();
        EntityEffectHandler.initialize();
    }

    private void setupComplete(@Nonnull final Minecraft mc) {
        // Mod initialization and IMC processing should have completed by now.  Do any further baking.
        AudioEffectLibrary.initialize();
        EntityEffectLibrary.complete();

        // Callback initialization where the acoustic library is concerned.  Only way to serialize access because
        // of the new Forge parallel loading.
        IMC.processCompletions();

        // Initialize after.  Reason is that a mod could override a regular sound with a complex
        // acoustic, so we only want to create a SimpleAcoustic if it does not exist in the map.
        SoundLibrary.initialize();
        AcousticLibrary.initialize();
    }

}
