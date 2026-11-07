/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
package dynamiclabs.immersivefx.specialfx;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;

public class BloodSounds {

	private static final long ECHO_WINDOW_TICKS = 20L;

	private static final Int2LongOpenHashMap RECENT_LOCAL_HITS = new Int2LongOpenHashMap();

	public static void onLocalAttack(final LivingEntity target, final DamageSource source) {
		if (!target.level().isClientSide() || !(source.getEntity() instanceof LocalPlayer))
			return;
		if (executeSplatter(target.level(), target.getX(), target.getY(), target.getZ(), source.getEntity()))
			RECENT_LOCAL_HITS.put(target.getId(), target.level().getGameTime());
	}

	public static void onDamageEvent(final LivingEntity target, final DamageSource source) {
		if (!target.level().isClientSide())
			return;
		final Entity attacker = source.getEntity();
		if (attacker instanceof LocalPlayer && RECENT_LOCAL_HITS.containsKey(target.getId())) {
			final long playedAt = RECENT_LOCAL_HITS.remove(target.getId());
			final long age = target.level().getGameTime() - playedAt;
			if (age >= 0 && age <= ECHO_WINDOW_TICKS)
				return;
		}
		executeSplatter(target.level(), target.getX(), target.getY(), target.getZ(), attacker);
	}

	public static boolean executeSplatter(final LevelAccessor world, final double x, final double y, final double z, final Entity sourceentity) {
		if (RECENT_LOCAL_HITS.size() > 64)
			RECENT_LOCAL_HITS.clear();
		final ItemStack weapon = (sourceentity instanceof LivingEntity living) ? living.getMainHandItem() : ItemStack.EMPTY;
		if (weapon.is(ItemTags.SWORDS) || weapon.is(ItemTags.AXES) || weapon.is(ItemTags.PICKAXES)) {
			ImmersivefxRegistry.playSound(SpecialFxConfig.OPTIONS.bloodSplatter, world, x, y, z, ImmersivefxRegistry.sound("splatter"), SoundSource.NEUTRAL, 1.0F, 1.0F);
			return true;
		}
		return false;
	}
}
