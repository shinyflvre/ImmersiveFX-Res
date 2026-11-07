/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.specialfx;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;

public final class BlockBreakSounds {
	private static final TagKey<Block> ORES = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "ores"));
	private static final TagKey<Block> STONES = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "stones"));
	private static final TagKey<Block> COBBLESTONES = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "cobblestones"));

	private BlockBreakSounds() {
	}

	public static boolean onBlockBroken(final ClientLevel level, final BlockPos pos, final BlockState state) {
		if (SpecialFxConfig.OPTIONS == null || state.isAir())
			return false;
		final SpecialFxConfig.Effect effect;
		final BooleanValue replace;
		final String sound;
		if (state.is(ORES)) {
			effect = SpecialFxConfig.OPTIONS.oreBreak;
			replace = SpecialFxConfig.OPTIONS.oreBreakReplace;
			sound = "ore_break";
		} else if (state.is(STONES) || state.is(COBBLESTONES) || state.getSoundType() == SoundType.STONE) {
			effect = SpecialFxConfig.OPTIONS.stoneBreak;
			replace = SpecialFxConfig.OPTIONS.stoneBreakReplace;
			sound = "stone_break";
		} else {
			return false;
		}
		final float factor = effect.factor();
		if (factor <= 0F)
			return false;
		level.playLocalSound(pos, ImmersivefxRegistry.sound(sound), SoundSource.BLOCKS, factor, 1.0F, false);
		return replace.get();
	}
}
