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

package dynamiclabs.immersivefx.lib.particles;

import dynamiclabs.immersivefx.platform.event.LevelEvent;
import dynamiclabs.immersivefx.platform.event.ParticleGroupRegistrar;
import dynamiclabs.immersivefx.ImmersiveFX;
import dynamiclabs.immersivefx.lib.events.DiagnosticEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.ChatFormatting;
import dynamiclabs.immersivefx.lib.collections.ObjectArray;
import dynamiclabs.immersivefx.sndctrl.config.Config;
import dynamiclabs.immersivefx.sndctrl.SoundControl;

import javax.annotation.Nonnull;

public final class CollectionManager {
    private CollectionManager() {

    }

    private static final ObjectArray<ParticleCollectionHelper> helpers = new ObjectArray<>();

    @Nonnull
    public static IParticleCollection create(@Nonnull final String name, @Nonnull final ParticleRenderType renderType) {
        final ParticleCollectionHelper helper = new ParticleCollectionHelper(name, renderType);
        synchronized (helpers) {
            helpers.add(helper);
        }
        return helper;
    }

    public static void onRegisterParticleGroups(@Nonnull final ParticleGroupRegistrar event) {
        event.register(MoteParticleGroup.GROUP, MoteParticleGroup::new);
    }

    public static void onWorldUnload(@Nonnull final LevelEvent event) {
        if (event.getLevel() instanceof ClientLevel) {
            helpers.forEach(ParticleCollectionHelper::clear);
        }
    }

    public static void diagnostics(@Nonnull final DiagnosticEvent event) {
        if (Config.CLIENT.logging.enableLogging.get()) {
            helpers.forEach(h -> {
                event.getLeft().add(ChatFormatting.AQUA + h.toString());
                h.getRenderTimer().ifPresent(event::addRenderTimer);
                h.getTickTimer().ifPresent(event::addRenderTimer);
            });
        }
    }

}
