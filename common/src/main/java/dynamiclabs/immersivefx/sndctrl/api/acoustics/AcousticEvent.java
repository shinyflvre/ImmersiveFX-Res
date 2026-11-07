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

package dynamiclabs.immersivefx.sndctrl.api.acoustics;

import com.google.common.base.MoreObjects;
import dynamiclabs.immersivefx.sndctrl.SoundControl;
import net.minecraft.resources.Identifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public final class AcousticEvent {

    private static final Map<Identifier, AcousticEvent> mapping = new HashMap<>();
    @Nonnull
    public static final AcousticEvent NONE = new AcousticEvent(Identifier.fromNamespaceAndPath(SoundControl.MOD_ID, "none"), null).register();
    private final Identifier name;
    private final AcousticEvent transition;

    public AcousticEvent(@Nonnull final Identifier name, @Nullable final AcousticEvent transition) {
        this.name = name;
        this.transition = transition;
    }

    @Nullable
    public static AcousticEvent getEvent(@Nonnull final Identifier name) {
        return mapping.get(name);
    }

    @Nonnull
    public Identifier getName() {
        return this.name;
    }

    public boolean canTransition() {
        return this.transition != null;
    }

    @Nullable
    public AcousticEvent getTransition() {
        return this.transition;
    }

    @Nonnull
    public AcousticEvent register() {
        mapping.put(this.name, this);
        return this;
    }

    @Override
    public String toString() {
        final MoreObjects.ToStringHelper builder = MoreObjects.toStringHelper(this).addValue(this.name.toString());
        if (this.transition != null)
            builder.add("transition", this.transition.getName());
        return builder.toString();
    }

}
