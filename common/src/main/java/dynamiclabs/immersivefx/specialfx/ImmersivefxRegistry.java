/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
package dynamiclabs.immersivefx.specialfx;

import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ImmersivefxRegistry {

	private static final String[] SOUND_IDS = {
			"heartbeat", "splatter", "crafting", "smelting", "throwitem", "spawnmob", "hunger", "bed", "wakeup",
			"click_anvil", "click_crafting_table", "click_grind_stone", "click_oven", "click_upgrade", "click_loom", "chat",
			"ore_break", "stone_break", "typing", "inventory_open"
	};

	public static final Map<Identifier, SoundEvent> sounds;

	static {
		final Map<Identifier, SoundEvent> map = new HashMap<>();
		for (final String id : SOUND_IDS) {
			final Identifier location = Identifier.fromNamespaceAndPath(Immersivefx.MOD_ID, id);
			map.put(location, SoundEvent.createVariableRangeEvent(location));
		}
		sounds = Collections.unmodifiableMap(map);
	}

	private ImmersivefxRegistry() {
	}

	public static SoundEvent sound(final String path) {
		final Identifier location = Identifier.fromNamespaceAndPath(Immersivefx.MOD_ID, path);
		final SoundEvent event = sounds.get(location);
		return event != null ? event : SoundEvent.createVariableRangeEvent(location);
	}

	public static void playSound(final SpecialFxConfig.Effect effect, final LevelAccessor world, final double x, final double y,
								 final double z, final SoundEvent sound, final SoundSource source, final float volume, final float pitch) {
		final float factor = effect.factor();
		if (factor > 0F)
			playSound(world, x, y, z, sound, source, volume * factor, pitch);
	}

	public static void playSound(final LevelAccessor world, final double x, final double y, final double z, final SoundEvent sound,
								 final SoundSource source, final float volume, final float pitch) {
		if (!(world instanceof Level level) || !level.isClientSide())
			return;
		level.playLocalSound(x, y, z, sound, source, volume, pitch, false);
	}
}
