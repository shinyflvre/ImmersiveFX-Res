/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
package dynamiclabs.immersivefx.specialfx;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;

public class DropItemSounds {

	private static long lastPlayedTick = Long.MIN_VALUE;

	public static void onLocalDrop(final LocalPlayer player) {
		final long now = player.level().getGameTime();
		if (now == lastPlayedTick)
			return;
		lastPlayedTick = now;
		executeDropItemSound(player.level(), player);
	}

	public static void executeDropItemSound(final LevelAccessor world, final Entity entity) {
		ImmersivefxRegistry.playSound(SpecialFxConfig.OPTIONS.itemDrop, world, entity.getX(), entity.getY(), entity.getZ(), ImmersivefxRegistry.sound("throwitem"),
				SoundSource.BLOCKS, 1.0F, 1.0F);
	}
}
