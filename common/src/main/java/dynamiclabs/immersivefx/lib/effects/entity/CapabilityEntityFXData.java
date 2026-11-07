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

package dynamiclabs.immersivefx.lib.effects.entity;

import dynamiclabs.immersivefx.platform.event.ClientConnectionEvent;
import dynamiclabs.immersivefx.platform.event.LevelEvent;
import dynamiclabs.immersivefx.ImmersiveFX;
import dynamiclabs.immersivefx.sndctrl.SoundControl;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

public class CapabilityEntityFXData {

	public static final Identifier CAPABILITY_ID = Identifier.fromNamespaceAndPath(SoundControl.MOD_ID, "entityfx");

	private static final Map<Entity, IEntityFX> DATA = new WeakHashMap<>();

	public static void register() {
	}

	@Nonnull
	public static Optional<IEntityFX> get(@Nullable final Entity entity) {
		if (!(entity instanceof LivingEntity))
			return Optional.empty();
		IEntityFX data = DATA.get(entity);
		if (data == null) {
			final Level world = entity.level();
			if (world == null || !world.isClientSide())
				return Optional.empty();
			data = new EntityFXData();
			DATA.put(entity, data);
		}
		return Optional.of(data);
	}

	@Nonnull
	public static Optional<IEntityFX> getIfPresent(@Nullable final Entity entity) {
		return entity == null ? Optional.empty() : Optional.ofNullable(DATA.get(entity));
	}

	public static void clearAll() {
		for (final IEntityFX fx : DATA.values())
			fx.clear();
		DATA.clear();
	}

	public static class EventHandler {

		public static void onLevelUnload(@Nonnull final LevelEvent event) {
			if (event.getLevel().isClientSide())
				clearAll();
		}

		public static void onLoggingOut(@Nonnull final ClientConnectionEvent event) {
			clearAll();
		}
	}
}
