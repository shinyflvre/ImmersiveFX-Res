/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
package dynamiclabs.immersivefx.specialfx;

import dynamiclabs.immersivefx.platform.event.RightClickBlockEvent;
import dynamiclabs.immersivefx.ImmersiveFX;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class RightClickSounds {

	public static void onRightClickBlock(final RightClickBlockEvent event) {
		if (!event.getLevel().isClientSide())
			return;
		final Player entity = event.getEntity();
		if (event.getHand() != entity.getUsedItemHand()) {
			return;
		}
		final BlockPos pos = event.getPos();
		executeRightClickSounds(event.getLevel(), pos);
	}

	public static void executeRightClickSounds(final LevelAccessor world, final BlockPos pos) {
		final Block block = world.getBlockState(pos).getBlock();
		final String sound;
		if (block == Blocks.CRAFTING_TABLE) {
			sound = "click_crafting_table";
		} else if (block == Blocks.FURNACE || block == Blocks.BLAST_FURNACE || block == Blocks.SMOKER) {
			sound = "click_oven";
		} else if (block == Blocks.ENCHANTING_TABLE || block == Blocks.LOOM || block == Blocks.CARTOGRAPHY_TABLE) {
			sound = "click_loom";
		} else if (block == Blocks.STONECUTTER || block == Blocks.GRINDSTONE) {
			sound = "click_grind_stone";
		} else if (block == Blocks.SMITHING_TABLE) {
			sound = "click_upgrade";
		} else if (block == Blocks.ANVIL || block == Blocks.CHIPPED_ANVIL || block == Blocks.DAMAGED_ANVIL) {
			sound = "click_anvil";
		} else {
			return;
		}
		final SoundEvent event = ImmersivefxRegistry.sound(sound);
		ImmersivefxRegistry.playSound(SpecialFxConfig.OPTIONS.workstationClicks, world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, event,
				SoundSource.PLAYERS, 1.0F, 1.0F);
	}
}
