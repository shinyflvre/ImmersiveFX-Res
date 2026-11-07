/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
package dynamiclabs.immersivefx.specialfx;

import dynamiclabs.immersivefx.platform.event.PlayerTickEvent;
import dynamiclabs.immersivefx.ImmersiveFX;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;

public class HungerSound {

	public static void onPlayerTick(final PlayerTickEvent event) {
		final Player entity = event.getEntity();
		if (!(entity instanceof LocalPlayer) || !entity.level().isClientSide())
			return;
		executeHungerSound(entity.level(), entity);
	}

	public static void executeHungerSound(final LevelAccessor world, final Entity entity) {
		if (((entity instanceof Player player) ? player.getFoodData().getFoodLevel() : 0) < 8) {
			if (Math.random() < 0.005) {
				ImmersivefxRegistry.playSound(SpecialFxConfig.OPTIONS.hunger, world, entity.getX(), entity.getY(), entity.getZ(), ImmersivefxRegistry.sound("hunger"),
						SoundSource.PLAYERS, 1.0F, 1.0F);
			}
		}
	}
}
