/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
package dynamiclabs.immersivefx.specialfx;

import dynamiclabs.immersivefx.platform.event.ItemResultEvent;
import dynamiclabs.immersivefx.ImmersiveFX;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;

public class SmeltSounds {

	public static void onItemSmelted(final ItemResultEvent event) {
		final Player entity = event.getEntity();
		if (!entity.level().isClientSide())
			return;
		executeSmeltSound(entity.level(), entity.getX(), entity.getY(), entity.getZ());
	}

	public static void executeSmeltSound(final LevelAccessor world, final double x, final double y, final double z) {
		ImmersivefxRegistry.playSound(SpecialFxConfig.OPTIONS.smelting, world, x, y, z, ImmersivefxRegistry.sound("smelting"), SoundSource.BLOCKS, 0.65F, 1.0F);
	}
}
