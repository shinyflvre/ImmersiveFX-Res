/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.fabric.mixins;

import dynamiclabs.immersivefx.platform.event.EntityEvents;
import dynamiclabs.immersivefx.platform.event.EntityJoinLevelEvent;
import dynamiclabs.immersivefx.platform.event.EntityTickEvent;
import dynamiclabs.immersivefx.platform.event.LevelEvent;
import dynamiclabs.immersivefx.platform.event.LevelEvents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class MixinClientLevel {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void ifx$load(final CallbackInfo ci) {
        if (LevelEvents.LOAD.hasListeners())
            LevelEvents.LOAD.post(new LevelEvent((ClientLevel) (Object) this));
    }

    @Inject(method = "addEntity", at = @At("HEAD"))
    private void ifx$joinLevel(final Entity entity, final CallbackInfo ci) {
        if (EntityEvents.JOIN_LEVEL.hasListeners())
            EntityEvents.JOIN_LEVEL.post(new EntityJoinLevelEvent(entity, (ClientLevel) (Object) this, false));
    }

    @Inject(method = "tickNonPassenger", at = @At("HEAD"))
    private void ifx$entityTick(final Entity entity, final CallbackInfo ci) {
        if (EntityEvents.ENTITY_TICK.hasListeners())
            EntityEvents.ENTITY_TICK.post(new EntityTickEvent(entity));
    }
}
