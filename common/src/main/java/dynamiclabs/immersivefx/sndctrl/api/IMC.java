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

package dynamiclabs.immersivefx.sndctrl.api;

import dynamiclabs.immersivefx.lib.collections.ObjectArray;
import dynamiclabs.immersivefx.lib.logging.IModLog;
import dynamiclabs.immersivefx.sndctrl.SoundControl;
import dynamiclabs.immersivefx.sndctrl.api.acoustics.AcousticEvent;
import dynamiclabs.immersivefx.sndctrl.api.sound.ISoundCategory;
import dynamiclabs.immersivefx.sndctrl.api.effects.IEntityEffectFactoryHandler;
import dynamiclabs.immersivefx.sndctrl.api.sound.Category;
import dynamiclabs.immersivefx.sndctrl.library.EntityEffectLibrary;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public final class IMC {

    private static final IModLog LOGGER = SoundControl.LOGGER.createChild(IMC.class);
    private static final ObjectArray<Runnable> callbacks = new ObjectArray<>(4);

    private IMC() {

    }

    private static <T> void handle(@Nonnull final String method, @Nonnull final T value, @Nonnull final Consumer<T> handler) {
        LOGGER.debug("Processing registration '%s'", method);
        try {
            handler.accept(value);
        } catch (@Nonnull final Throwable t) {
            LOGGER.error(t, "Error processing registration '%s'", method);
        }
    }

    /**
     * Adds an AcousticEvent to the system so that it is recognized by the compiler
     *
     * @param event The Acoustic Event to register
     */
    public static void registerAcousticEvent(@Nonnull final AcousticEvent... event) {
        for (final AcousticEvent e : event)
            handle("REGISTER_ACOUSTIC_EVENT", e, AcousticEvent::register);
    }

    /**
     * Adds a Sound Category to the system so that it is recognized by the compiler
     *
     * @param category Sound Category to register
     */
    public static void registerSoundCategory(@Nonnull final ISoundCategory... category) {
        for (final ISoundCategory c : category)
            handle("REGISTER_SOUND_CATEGORY", c, Category::register);
    }

    /**
     * Register an EffectFactoryHandler for the entity effect system.
     *
     * @param handler Effect handler to register
     */
    public static void registerEffectFactoryHandler(@Nonnull final IEntityEffectFactoryHandler... handler) {
        for (final IEntityEffectFactoryHandler h : handler)
            handle("REGISTER_EFFECT_FACTORY_HANDLER", h, EntityEffectLibrary::register);
    }

    /**
     * Register a callback method to be invoked during completion processing.  Call may come back on a separate thread.
     * This method is useful to prevent concurrent access by mods during setup when they need to access the acoustic
     * library and such.
     *
     * @param callback  Callback to invoke on completion
     */
    public static void registerCompletionCallback(@Nonnull final Runnable... callback) {
        for (final Runnable r : callback)
            handle("REGISTER_COMPLETION_CALLBACK", r, callbacks::add);
    }

    /**
     * Called by the startup routine to process any callbacks that were posted.  Not to be called by other mods!
     */
    public static void processCompletions() {
        for (final Runnable r : callbacks) {
            try {
                r.run();
            } catch (@Nonnull final Throwable t) {
                LOGGER.error(t, "Error executing completion processing routine");
            }
        }
        callbacks.clear();
    }

}
