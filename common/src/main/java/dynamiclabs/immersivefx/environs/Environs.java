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

package dynamiclabs.immersivefx.environs;

import dynamiclabs.immersivefx.environs.config.Config;
import dynamiclabs.immersivefx.environs.handlers.Manager;
import dynamiclabs.immersivefx.environs.library.Constants;
import dynamiclabs.immersivefx.environs.library.Libraries;
import dynamiclabs.immersivefx.lib.logging.ModLog;
import dynamiclabs.immersivefx.sndctrl.api.IMC;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SuspendedParticle;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import dynamiclabs.immersivefx.platform.event.ClientConnectionEvent;
import dynamiclabs.immersivefx.platform.event.ClientEvents;
import dynamiclabs.immersivefx.platform.event.ClientLifecycleEvents;
import dynamiclabs.immersivefx.platform.event.ParticleProviderRegistrar;
import dynamiclabs.immersivefx.platform.event.Priority;
import dynamiclabs.immersivefx.platform.event.RegistrationEvents;
import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;

public final class Environs {

    /**
     * ID of the mod
     */
    public static final String MOD_ID = "environs";
    /**
     * Logging instance for trace
     */
    public static final ModLog LOGGER = new ModLog(Environs.class);

    public Environs() {
        // Various event registrations
        ClientLifecycleEvents.REGISTER_INTEGRATIONS.register(this::enqueueIMC);
        RegistrationEvents.PARTICLE_PROVIDERS.register(this::registerParticleProviders);
        ClientEvents.LOGGING_IN.register(Priority.LOWEST, this::clientConnect);
        ClientEvents.LOGGING_OUT.register(Priority.LOWEST, this::clientDisconnect);
    }

    private void registerParticleProviders(@Nonnull final ParticleProviderRegistrar event) {
        event.registerSpriteSet(ParticleTypes.UNDERWATER, sprites -> {
            final ParticleProvider<SimpleParticleType> vanilla = new SuspendedParticle.UnderwaterProvider(sprites);
            return (options, level, x, y, z, xAux, yAux, zAux, random) -> {
                if (Config.CLIENT.effects.disableUnderwaterParticles.get())
                    return null;
                return vanilla.createParticle(options, level, x, y, z, xAux, yAux, zAux, random);
            };
        });
    }

    private void enqueueIMC(final Minecraft mc) {
        IMC.registerSoundCategory(Constants.BIOMES);
        IMC.registerSoundCategory(Constants.SPOT_SOUNDS);
        IMC.registerSoundCategory(Constants.WATERFALL);

        IMC.registerCompletionCallback(Libraries::initialize);
        IMC.registerCompletionCallback(Libraries::complete);
    }

    private void clientConnect(@Nonnull final ClientConnectionEvent event) {
        Manager.connect();
    }

    private void clientDisconnect(@Nonnull final ClientConnectionEvent event) {
        Manager.disconnect();
    }

}
