/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
package dynamiclabs.immersivefx.specialfx;

import dynamiclabs.immersivefx.ImmersiveFX;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;

public class BedSound {

	private static final Map<Player, Boolean> SLEEPING = new WeakHashMap<>();
	private static ClientLevel lastLevel;

	public static void onClientTick(final Minecraft event) {
		final ClientLevel level = Minecraft.getInstance().level;
		if (level != lastLevel) {
			SLEEPING.clear();
			lastLevel = level;
		}
		if (level == null)
			return;
		for (final AbstractClientPlayer player : level.players()) {
			final boolean sleeping = player.isSleeping();
			final Boolean wasSleeping = SLEEPING.put(player, sleeping);
			if (sleeping && wasSleeping != null && !wasSleeping)
				executeBedSound(level, player);
		}
	}

	public static void executeBedSound(final LevelAccessor world, final Entity entity) {
		ImmersivefxRegistry.playSound(SpecialFxConfig.OPTIONS.bed, world, entity.getX(), entity.getY(), entity.getZ(), ImmersivefxRegistry.sound("bed"),
				SoundSource.PLAYERS, 1.0F, 1.0F);
	}
}
