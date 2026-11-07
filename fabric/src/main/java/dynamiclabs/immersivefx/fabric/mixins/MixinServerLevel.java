/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.fabric.mixins;

import dynamiclabs.immersivefx.platform.event.EntityEvents;
import dynamiclabs.immersivefx.platform.event.EntityJoinLevelEvent;
import dynamiclabs.immersivefx.platform.event.EntityTickEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class MixinServerLevel {

    @Inject(method = "addPlayer", at = @At("HEAD"))
    private void ifx$joinLevel(final ServerPlayer player, final CallbackInfo ci) {
        if (EntityEvents.JOIN_LEVEL.hasListeners())
            EntityEvents.JOIN_LEVEL.post(new EntityJoinLevelEvent(player, (ServerLevel) (Object) this, false));
    }

    @Inject(method = "tickNonPassenger", at = @At("HEAD"))
    private void ifx$entityTick(final Entity entity, final CallbackInfo ci) {
        if (EntityEvents.ENTITY_TICK.hasListeners())
            EntityEvents.ENTITY_TICK.post(new EntityTickEvent(entity));
    }
}
