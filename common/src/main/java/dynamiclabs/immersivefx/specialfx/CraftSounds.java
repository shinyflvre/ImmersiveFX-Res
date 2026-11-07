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

public class CraftSounds {

	public static void onItemCrafted(final ItemResultEvent event) {
		final Player entity = event.getEntity();
		if (!entity.level().isClientSide())
			return;
		executeCraftSound(entity.level(), entity.getX(), entity.getY(), entity.getZ());
	}

	public static void executeCraftSound(final LevelAccessor world, final double x, final double y, final double z) {
		ImmersivefxRegistry.playSound(SpecialFxConfig.OPTIONS.crafting, world, x, y, z, ImmersivefxRegistry.sound("crafting"), SoundSource.BLOCKS, 0.5F, 1.0F);
	}
}
