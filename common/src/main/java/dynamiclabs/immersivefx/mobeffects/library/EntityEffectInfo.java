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

package dynamiclabs.immersivefx.mobeffects.library;

import dynamiclabs.immersivefx.mobeffects.MobEffects;
import dynamiclabs.immersivefx.mobeffects.library.config.EntityConfig;
import net.minecraft.resources.Identifier;
import dynamiclabs.immersivefx.sndctrl.api.acoustics.Library;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public class EntityEffectInfo {

	public final Set<Identifier> effects = new HashSet<>(6);
	public final String variator;

	public EntityEffectInfo() {
		this.variator = "default";
	}

	public EntityEffectInfo(@Nonnull final EntityConfig ec) {
		final String[] effects = ec.effects.split(",");
		for (final String e : effects) {
			final Identifier r = Library.resolveResource(MobEffects.MOD_ID, e);
			this.effects.add(r);
		}
		this.variator = ec.variator;
	}

	@Override
	public String toString() {
		if (!this.effects.isEmpty()) {
			return this.effects.toString() + "; variator=" + this.variator;
		}
		return "<NONE>";
	}
}
