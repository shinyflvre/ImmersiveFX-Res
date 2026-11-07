/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
package dynamiclabs.immersivefx.specialfx;

import dynamiclabs.immersivefx.platform.event.EntityJoinLevelEvent;
import dynamiclabs.immersivefx.ImmersiveFX;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

public class SpawnSound {

	public static void onEntitySpawned(final EntityJoinLevelEvent event) {
		if (!event.getLevel().isClientSide())
			return;
		final Entity entity = event.getEntity();
		executeSpawnSound(event.getLevel(), entity.getX(), entity.getY(), entity.getZ(), entity);
	}

	public static void executeSpawnSound(final LevelAccessor world, final double x, final double y, final double z, final Entity entity) {
		if (entity instanceof Monster || entity instanceof Animal) {
			ImmersivefxRegistry.playSound(SpecialFxConfig.OPTIONS.mobSpawn, world, x, y, z, ImmersivefxRegistry.sound("spawnmob"), SoundSource.NEUTRAL, 1.0F, 1.0F);
			if (world instanceof Level level && level.isClientSide() && SpecialFxConfig.OPTIONS.enabled.get() && SpecialFxConfig.OPTIONS.mobSpawnSmoke.get()) {
				addParticles(level, ParticleTypes.SMOKE, x, y, z, 10, 0.03, 0.5, 0.03, 0.01);
				addParticles(level, ParticleTypes.LARGE_SMOKE, x, y, z, 6, 0.03, 0.5, 0.03, 0.01);
				addParticles(level, ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y, z, 3, 0.03, 0.5, 0.03, 0.01);
			}
		}
	}

	private static void addParticles(final Level level, final ParticleOptions particle, final double x, final double y, final double z,
									 final int count, final double xDist, final double yDist, final double zDist, final double speed) {
		final RandomSource random = level.getRandom();
		for (int i = 0; i < count; i++) {
			final double px = x + random.nextGaussian() * xDist;
			final double py = y + random.nextGaussian() * yDist;
			final double pz = z + random.nextGaussian() * zDist;
			level.addParticle(particle, px, py, pz, random.nextGaussian() * speed, random.nextGaussian() * speed, random.nextGaussian() * speed);
		}
	}
}
