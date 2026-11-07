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

package dynamiclabs.immersivefx.lib.effects;

import dynamiclabs.immersivefx.platform.event.EntityJoinLevelEvent;
import dynamiclabs.immersivefx.platform.event.EntityTickEvent;
import net.minecraft.client.Minecraft;
import dynamiclabs.immersivefx.ImmersiveFX;
import dynamiclabs.immersivefx.lib.GameUtils;
import dynamiclabs.immersivefx.lib.Lib;
import dynamiclabs.immersivefx.lib.math.LoggingTimerEMA;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import dynamiclabs.immersivefx.dsurround.DynamicSurroundings;
import dynamiclabs.immersivefx.lib.collections.ObjectArray;
import dynamiclabs.immersivefx.lib.events.DiagnosticEvent;
import dynamiclabs.immersivefx.sndctrl.config.Config;
import dynamiclabs.immersivefx.lib.effects.entity.CapabilityEntityFXData;
import dynamiclabs.immersivefx.lib.effects.entity.IEntityFX;
import dynamiclabs.immersivefx.sndctrl.api.effects.AbstractEntityEffect;
import dynamiclabs.immersivefx.sndctrl.library.EntityEffectLibrary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Central repository for a collection of IEntityEffectFactory instances and the
 * IFactoryFilters associated with them. Typically there will be a single
 * instance of the EntityEffectLibrary for a project, but multiples can be
 * created based on the circumstances.
 */
public final class EntityEffectHandler {

    private static final LoggingTimerEMA timer = new LoggingTimerEMA("Entity Effect Update");
    private static long nanos;

    private EntityEffectHandler() {
    }

    public static void initialize() {
        // Noop to cause class initializers to fire.
    }

    @Nullable
    public static EntityEffectManager getManager(@Nonnull final LivingEntity entity) {
        return CapabilityEntityFXData.getIfPresent(entity).map(IEntityFX::get).orElse(null);
    }

    /**
     * Creates an EntityEffectHandler for the specified Entity. The IEffects
     * attached to the EntityEffectHandler is determined by an IFactoryFitler. An
     * EntityEffectHandler will always be created.
     *
     * @param entity The subject Entity for which an EntityEffectHandler is created
     * @return An EntityEffectHandler for the Entity
     */
    @Nonnull
    private static Optional<EntityEffectManager> create(@Nonnull final LivingEntity entity) {
        final ObjectArray<AbstractEntityEffect> effectToApply = EntityEffectLibrary.getEffects(entity);
        final EntityEffectManager result;
        if (effectToApply.size() > 0) {
            result = new EntityEffectManager(entity, effectToApply);
        } else {
            result = new EntityEffectManager(entity);
        }

        return Optional.of(result);
    }

    public static void onLivingUpdate(@Nonnull final EntityTickEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity))
            return;
        try {
            if (entity.level().isClientSide() && GameUtils.getPlayer() != null) {

                final ProfilerFiller profiler = Profiler.get();
                profiler.push("MobEffects Living Update");
                final long start = System.nanoTime();

                CapabilityEntityFXData.get(entity).ifPresent(cap -> {
                    final int range = Config.CLIENT.effects.effectRange.get();
                    final int effectDistSq = range * range;
                    final boolean inRange = entity.distanceToSqr(GameUtils.getPlayer()) <= effectDistSq;
                    final EntityEffectManager mgr = cap.get();
                    if (mgr != null && !inRange) {
                        cap.clear();
                    } else if (mgr == null && inRange && entity.isAlive()) {
                        cap.set(create(entity).get());
                    } else if (mgr != null) {
                        mgr.update();
                    }
                });

                nanos += System.nanoTime() - start;
                profiler.pop();
            }
        } catch(@Nonnull final Throwable t) {
            Lib.LOGGER.error(t, "Error ticking entity %s!", entity);
        }
    }

    private static void clearHandlers() {
        if (GameUtils.getWorld() == null)
            return;
        final Iterable<Entity> entities = GameUtils.getWorld().entitiesForRendering();
        for (final Entity e : entities) {
            CapabilityEntityFXData.getIfPresent(e).ifPresent(IEntityFX::clear);
        }
    }

    /**
     * Check if the player joining the world is the one sitting at the keyboard. If
     * so we need to wipe out the existing handler list because the dimension
     * changed.
     */
    public static void onEntityJoin(@Nonnull final EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            if (GameUtils.getPlayer() == event.getEntity())
                clearHandlers();
        }
    }

    public static void onClientTick(@Nonnull final Minecraft event) {
        timer.update(nanos);
        nanos = 0;
    }

    public static void onDiagnostics(@Nonnull final DiagnosticEvent event) {
        if (Config.CLIENT.logging.enableLogging.get())
            event.getRenderTimers().add(timer);
    }

}
