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

package dynamiclabs.immersivefx.mobeffects.misc;

import dynamiclabs.immersivefx.ImmersiveFX;
import dynamiclabs.immersivefx.lib.effects.EntityEffectManager;
import dynamiclabs.immersivefx.mobeffects.MobEffects;
import dynamiclabs.immersivefx.sndctrl.api.effects.IEntityEffectManager;
import dynamiclabs.immersivefx.sndctrl.events.EntityInspectionEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.WeakHashMap;

public class EntityInspectionHandler {

    private static final Map<Entity, IEntityEffectManager> MANAGERS = new WeakHashMap<>();

    public static void track(@Nonnull final IEntityEffectManager manager) {
        MANAGERS.put(manager.getEntity(), manager);
    }

    public static void handler(@Nonnull final EntityInspectionEvent event) {
        final IEntityEffectManager mgr = MANAGERS.get(event.entity);
        if (mgr != null && mgr.isActive()) {
            event.data.add(ChatFormatting.DARK_AQUA + "<Effects>");
            if (mgr instanceof EntityEffectManager manager)
                event.data.addAll(manager.getAttachedEffects());
        }
    }

}
