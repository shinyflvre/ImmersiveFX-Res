/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
/*
 *  Dynamic Surroundings: Mob Effects
 *  Copyright (C) 2019  OreCruncher
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package dynamiclabs.immersivefx.mobeffects.misc;

import dynamiclabs.immersivefx.platform.event.ClientConnectionEvent;
import dynamiclabs.immersivefx.platform.event.PlaySoundEvent;
import dynamiclabs.immersivefx.ImmersiveFX;
import dynamiclabs.immersivefx.lib.GameUtils;
import dynamiclabs.immersivefx.mobeffects.MobEffects;
import dynamiclabs.immersivefx.mobeffects.effects.EntityFootprintEffect;
import dynamiclabs.immersivefx.mobeffects.library.Constants;
import dynamiclabs.immersivefx.mobeffects.library.EffectLibrary;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;

public final class StepSoundHandler {

    private static final double RANGE_XZ = 1.5D;
    private static final double RANGE_Y = 2.0D;

    @Nullable
    private static Set<Identifier> stepSounds;

    private StepSoundHandler() {
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    private static Set<Identifier> getStepSounds() {
        Set<Identifier> result = stepSounds;
        if (result == null) {
            result = new ObjectOpenHashSet<>();
            for (final Field f : SoundType.class.getFields()) {
                if (Modifier.isStatic(f.getModifiers()) && SoundType.class.isAssignableFrom(f.getType())) {
                    try {
                        addStepSound(result, (SoundType) f.get(null));
                    } catch (@Nonnull final Throwable ignore) {
                    }
                }
            }
            for (final BlockState state : Block.BLOCK_STATE_REGISTRY) {
                try {
                    addStepSound(result, state.getSoundType());
                } catch (@Nonnull final Throwable ignore) {
                }
            }
            stepSounds = result;
        }
        return result;
    }

    private static void addStepSound(@Nonnull final Set<Identifier> set, @Nullable final SoundType type) {
        if (type != null) {
            final SoundEvent step = type.getStepSound();
            if (step != null)
                set.add(step.location());
        }
    }

    private static boolean isEntitySound(@Nonnull final SoundInstance sound) {
        final SoundSource source = sound.getSource();
        return !sound.isRelative() && (source == SoundSource.PLAYERS || source == SoundSource.HOSTILE || source == SoundSource.NEUTRAL);
    }

    public static void onPlaySound(@Nonnull final PlaySoundEvent event) {
        final SoundInstance sound = event.getSound();
        if (sound == null || !isEntitySound(sound))
            return;
        if (Constants.FOOTSTEPS.getVolumeScale() <= 0)
            return;
        if (!getStepSounds().contains(sound.getIdentifier()))
            return;

        final ClientLevel level = GameUtils.getWorld();
        final Player player = GameUtils.getPlayer();
        if (level == null || player == null)
            return;

        final double x = sound.getX();
        final double y = sound.getY();
        final double z = sound.getZ();
        final AABB box = new AABB(x - RANGE_XZ, y - RANGE_Y, z - RANGE_XZ, x + RANGE_XZ, y + RANGE_Y, z + RANGE_XZ);
        final List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class, box, e -> e != player && e.isAlive());

        LivingEntity closest = null;
        double best = Double.MAX_VALUE;
        for (final LivingEntity e : candidates) {
            final double d = e.distanceToSqr(x, y, z);
            if (d < best) {
                best = d;
                closest = e;
            }
        }

        if (closest != null && isHandled(closest, player)) {
            event.setSound(null);
        }
    }

    private static boolean isHandled(@Nonnull final LivingEntity entity, @Nonnull final Player player) {
        if (!EffectLibrary.hasEffect(entity, EntityFootprintEffect.FACTORY.getName()))
            return false;
        final int range = dynamiclabs.immersivefx.sndctrl.config.Config.CLIENT.effects.effectRange.get();
        return entity.distanceToSqr(player) <= (double) range * range;
    }

    public static void onLogout(@Nonnull final ClientConnectionEvent event) {
        stepSounds = null;
    }
}
