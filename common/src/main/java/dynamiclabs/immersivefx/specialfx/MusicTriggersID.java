/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
package dynamiclabs.immersivefx.specialfx;

import dynamiclabs.immersivefx.platform.Platform;
import dynamiclabs.immersivefx.platform.event.LevelEvent;
import dynamiclabs.immersivefx.ImmersiveFX;
import net.minecraft.world.level.LevelAccessor;

public class MusicTriggersID {

	public static void onWorldLoad(final LevelEvent event) {
		if (event.getLevel() != null && !event.getLevel().isClientSide())
			return;
		CheckForMusicTriggers(event.getLevel());
	}

	public static void CheckForMusicTriggers(final LevelAccessor world) {
		if (world == null) {
			Immersivefx.LOGGER.warn("ImmersiveFX: Failed to load checks for MusicTriggers.");
			return;
		}
		if (Platform.get().isModLoaded("musictriggers")) {
			Immersivefx.LOGGER.warn(
					"ImmersiveFX: You are using MusicTriggers. Normally there should be no problems. If you should have problems use ingame CTRL + I and mute the sounds that overlap.");
		}
	}
}
