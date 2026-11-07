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

package dynamiclabs.immersivefx.mobeffects;

import dynamiclabs.immersivefx.lib.logging.ModLog;
import dynamiclabs.immersivefx.mobeffects.config.Config;
import dynamiclabs.immersivefx.mobeffects.effects.EntityBowEffect;
import dynamiclabs.immersivefx.mobeffects.effects.EntityBreathEffect;
import dynamiclabs.immersivefx.mobeffects.effects.EntityFootprintEffect;
import dynamiclabs.immersivefx.mobeffects.effects.EntitySwingEffect;
import dynamiclabs.immersivefx.mobeffects.effects.PlayerToolbarEffect;
import dynamiclabs.immersivefx.mobeffects.effects.particles.FootprintMote;
import dynamiclabs.immersivefx.mobeffects.library.Constants;
import dynamiclabs.immersivefx.mobeffects.library.Libraries;
import dynamiclabs.immersivefx.sndctrl.api.IMC;
import dynamiclabs.immersivefx.platform.event.ClientLifecycleEvents;
import dynamiclabs.immersivefx.platform.event.RegistrationEvents;
import dynamiclabs.immersivefx.platform.event.RenderPipelineRegistrar;
import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;

public final class MobEffects {

    /**
     * ID of the mod
     */
    public static final String MOD_ID = "mobeffects";
    /**
     * Logging instance for trace
     */
    public static final ModLog LOGGER = new ModLog(MobEffects.class);

    public MobEffects() {
        // Various event registrations
        ClientLifecycleEvents.REGISTER_INTEGRATIONS.register(this::enqueueIMC);
        RegistrationEvents.RENDER_PIPELINES.register(this::registerPipelines);
    }

    private void registerPipelines(@Nonnull final RenderPipelineRegistrar event) {
        event.registerPipeline(FootprintMote.PIPELINE);
    }

    private void enqueueIMC(final Minecraft mc) {
        // Sound Categories
        IMC.registerSoundCategory(
                Constants.FOOTSTEPS,
                Constants.TOOLBAR
        );

        // Register our AcousticEvents
        IMC.registerAcousticEvent(
                Constants.WALK,
                Constants.WANDER,
                Constants.SWIM,
                Constants.RUN,
                Constants.JUMP,
                Constants.LAND,
                Constants.CLIMB,
                Constants.CLIMB_RUN,
                Constants.DOWN,
                Constants.DOWN_RUN,
                Constants.UP,
                Constants.UP_RUN
        );

        // Register our effect handlers
        IMC.registerEffectFactoryHandler(EntityFootprintEffect.FACTORY);
        if (Config.CLIENT.effects.showBreath.get())
            IMC.registerEffectFactoryHandler(EntityBreathEffect.FACTORY);
        if (Config.CLIENT.effects.enableBowEffect.get())
            IMC.registerEffectFactoryHandler(EntityBowEffect.FACTORY);
        if (Config.CLIENT.effects.enableToolbarEffect.get())
            IMC.registerEffectFactoryHandler(PlayerToolbarEffect.FACTORY);
        if (Config.CLIENT.effects.enableSwingEffect.get())
            IMC.registerEffectFactoryHandler(EntitySwingEffect.FACTORY);

        // Callback for completions
        IMC.registerCompletionCallback(Libraries::initialize);
        IMC.registerCompletionCallback(Libraries::complete);
    }
}
