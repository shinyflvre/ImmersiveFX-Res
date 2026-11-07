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

package dynamiclabs.immersivefx.sndctrl.misc;

import dynamiclabs.immersivefx.platform.event.LevelEvent;
import dynamiclabs.immersivefx.ImmersiveFX;
import dynamiclabs.immersivefx.sndctrl.SoundControl;
import dynamiclabs.immersivefx.sndctrl.mixins.AccessorLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;
import dynamiclabs.immersivefx.sndctrl.config.Config;

import javax.annotation.Nonnull;

public final class ClientWorldPatcher {
    private ClientWorldPatcher() {
    }

    public static void onWorldLoad(@Nonnull final LevelEvent event) {
        if (Config.CLIENT.effects.fixupRandoms.get()) {
            final LevelAccessor world = event.getLevel();
            if (world.isClientSide() && world instanceof Level) {
                ((AccessorLevel) world).sndctrl_setRandom(new SingleThreadedRandomSource(RandomSupport.generateUniqueSeed()));
            }
        }
    }
}
