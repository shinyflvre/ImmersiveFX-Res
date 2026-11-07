/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
/*
 * Dynamic Surroundings
 * Copyright (C) 2020  OreCruncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package dynamiclabs.immersivefx.specialfx;

import dynamiclabs.immersivefx.platform.Platform;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.cubemob.MagmaCube;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BloodParticles
{
	@SuppressWarnings("unused")
	private static Logger log = LogManager.getLogger(BloodParticles.class);

	// @formatter:off
	private static final ParticleOptions
			ZOMBIFIED_PIG_PARTICLE_DATA = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DYED_TERRACOTTA.pick(DyeColor.LIME).defaultBlockState()),
			PHANTOM_PARTICLE_DATA = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STAINED_GLASS.pick(DyeColor.GRAY).defaultBlockState()),
			BLAZE_PARTICLE_DATA = ParticleTypes.FLAME, SLIME_PARTICLE_DATA = ParticleTypes.ITEM_SLIME,
			MAGMA_CUBE_PARTICLE_DATA = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MAGMA_BLOCK.defaultBlockState()),
			ENDER_PARTICLE_DATA = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.CONCRETE.pick(DyeColor.PURPLE).defaultBlockState()),
			LAVA_PARTICLE_DATA = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.LAVA.defaultBlockState()),
			CREEPER_PARTICLE_DATA = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DYED_TERRACOTTA.pick(DyeColor.GREEN).defaultBlockState()),
			DEFAULT_PARTICLE_DATA = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.REDSTONE_BLOCK.defaultBlockState());
	// @formatter:on

	public static void onClientAttack(final LivingEntity entity, final DamageSource source, final float amount)
	{
		if (!isValidDamageSource(source))
			return;

		if (!entity.level().isClientSide())
			return;

		// compute number of particles to spawn based on damage dealt

		int numParticles;

		switch (source.getMsgId())
		{
		case "arrow":
			numParticles = 1 + ((int) (amount * 3));
			break;
		case "trident":
			numParticles = 1 + ((int) (amount * 3));
			break;
		case "player":
			if (source.getEntity() instanceof Player player)
			{
				ItemStack weapon = player.getMainHandItem();
				if (!weapon.isEmpty())
				{
					numParticles = 1 + ((int) ((amount + itemAttackDamage(weapon)) * 2));
					break;
				}
			}
			// fallthrough to default case
		default:
			numParticles = defaultParticleCount(amount);
		}

		spawn(entity, numParticles);
	}

	public static void onClientHurtStatus(final LivingEntity entity)
	{
		if (!entity.level().isClientSide())
			return;
		spawn(entity, defaultParticleCount(0.0F));
	}

	private static int defaultParticleCount(final float amount)
	{
		return amount == 0.0f ? 20 : 5 + ((int) (amount * 15));
	}

	private static float itemAttackDamage(final ItemStack weapon)
	{
		if (weapon.is(ItemTags.SWORDS) || weapon.is(ItemTags.AXES) || weapon.is(ItemTags.PICKAXES)
				|| weapon.is(ItemTags.SHOVELS) || weapon.is(ItemTags.HOES))
		{
			return (float) Platform.get().getAttributeModifiers(weapon).compute(Attributes.ATTACK_DAMAGE, 0.0, EquipmentSlot.MAINHAND);
		}
		return 1.0f;
	}

	private static void spawn(final LivingEntity entity, final int requestedParticles)
	{
		final SpecialFxConfig.Options options = SpecialFxConfig.OPTIONS;
		if (!options.enabled.get() || !options.bloodParticles.get())
			return;
		final int numParticles = Math.round(requestedParticles * options.bloodParticleAmount.get() / 100F);
		if (numParticles <= 0)
			return;

		// Select which particle to use based on the entity

		ParticleOptions particleData;

		final String path = EntityType.getKey(entity.getType()).getPath();

		if (entity instanceof AbstractSkeleton || entity instanceof SkeletonHorse)
			return; // skeletons have no blood
		else if (entity instanceof ZombifiedPiglin || entity instanceof Zoglin || path.contains("skeleton"))
			particleData = ZOMBIFIED_PIG_PARTICLE_DATA;
		else if (entity instanceof Phantom)
			particleData = PHANTOM_PARTICLE_DATA;
		else if (entity instanceof Blaze)
			particleData = BLAZE_PARTICLE_DATA;
		else if (entity instanceof MagmaCube)
			particleData = MAGMA_CUBE_PARTICLE_DATA;
		else if (entity instanceof Slime)
			particleData = SLIME_PARTICLE_DATA;
		else if (entity instanceof Creeper)
			particleData = CREEPER_PARTICLE_DATA;
		else if (entity instanceof Enderman || entity instanceof EnderDragon || entity instanceof Endermite)
			particleData = ENDER_PARTICLE_DATA;
		else if (path.equals("lava_monster"))
			particleData = LAVA_PARTICLE_DATA;
		else
			particleData = DEFAULT_PARTICLE_DATA;

		// compute particle speed and location, other misc variables

		final Level level = entity.level();

		Vec3 pos = entity.position();

		double x = pos.x;
		double y = pos.y + entity.getBbHeight() / 1.5;
		double z = pos.z;

		for (int i = 0; i < numParticles; i++)
		{
			level.addParticle(particleData, x, y, z, 0, 0, 0);
		}
	}

	private static boolean isValidDamageSource(DamageSource source)
	{
		String damageType = source.getMsgId();
		return (source.is(DamageTypes.FALL) || source.is(DamageTypes.GENERIC) || source.is(DamageTypeTags.IS_PROJECTILE)
				|| damageType.equalsIgnoreCase("player") || damageType.equalsIgnoreCase("mob") || damageType.equalsIgnoreCase("thorns"));
	}

	@SuppressWarnings("unused")
	private static String damageSourceToString(DamageSource source)
	{
		if (source == null)
		{
			return null;
		}
		else
		{
			StringBuilder sb = new StringBuilder();
			sb.append(source.getMsgId()).append(" {");

			if (source.getFoodExhaustion() != 0)
				sb.append(" hunger damage = ").append(source.getFoodExhaustion()).append(',');

			if (source.is(DamageTypeTags.IS_FIRE))
				sb.append(" fire damage,");

			if (source.is(DamageTypeTags.IS_PROJECTILE))
				sb.append(" projectile,");

			if (source.scalesWithDifficulty())
				sb.append(" difficulty scaled,");

			if (source.is(DamageTypeTags.WITCH_RESISTANT_TO))
				sb.append(" magic,");

			if (source.is(DamageTypeTags.IS_EXPLOSION))
				sb.append(" explosion,");

			if (source.is(DamageTypeTags.BYPASSES_ENCHANTMENTS))
				sb.append(" absolute damage,");

			if (source.is(DamageTypeTags.BYPASSES_ARMOR))
				sb.append(" unblockable,");

			if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY))
				sb.append(" can harm creative players,");

			if (source.getDirectEntity() != null)
				sb.append(" immediate source = ").append(EntityType.getKey(source.getDirectEntity().getType())).append(',');

			if (source.getEntity() != null)
				sb.append(" true source = ").append(EntityType.getKey(source.getEntity().getType())).append(',');

			if (sb.charAt(sb.length() - 1) == ',')
				sb.setCharAt(sb.length() - 1, ' ');

			sb.append('}');
			return sb.toString();
		}
	}

}
