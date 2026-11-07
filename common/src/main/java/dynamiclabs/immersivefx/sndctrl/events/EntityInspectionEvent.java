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

package dynamiclabs.immersivefx.sndctrl.events;

import net.minecraft.world.entity.Entity;
import dynamiclabs.immersivefx.platform.event.EventHook;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class EntityInspectionEvent {

    public static final EventHook<EntityInspectionEvent> BUS = new EventHook<>("EntityInspectionEvent");

    public final List<String> data = new ArrayList<>();
    public final Entity entity;

    public EntityInspectionEvent(@Nonnull final Entity entity) {
        this.entity = entity;
    }
}
