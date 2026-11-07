/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
/*
 *  Dynamic Surroundings
 *  Copyright (C) 2020  OreCruncher
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package dynamiclabs.immersivefx.mobeffects.effects.particles;

import dynamiclabs.immersivefx.lib.GameUtils;
import dynamiclabs.immersivefx.mobeffects.footsteps.FootprintStyle;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public final class Collections {

    private Collections() {

    }

    public static void addFootprint(@Nonnull final FootprintStyle style, @Nonnull final Level world,
                                    final Vec3 loc, final float rot, final float scale, final boolean isRight) {
        if (world instanceof ClientLevel clientLevel && GameUtils.getMC().level == clientLevel) {
            GameUtils.getMC().particleEngine.add(new FootprintMote(style, clientLevel, loc.x, loc.y, loc.z, rot, scale, isRight));
        }
    }
}
