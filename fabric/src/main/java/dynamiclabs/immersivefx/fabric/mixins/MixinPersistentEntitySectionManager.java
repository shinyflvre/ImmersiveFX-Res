/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.fabric.mixins;

import dynamiclabs.immersivefx.platform.event.EntityEvents;
import dynamiclabs.immersivefx.platform.event.EntityJoinLevelEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PersistentEntitySectionManager.class)
public class MixinPersistentEntitySectionManager {

    @Inject(method = "addEntity(Lnet/minecraft/world/level/entity/EntityAccess;Z)Z", at = @At("HEAD"))
    private void ifx$joinLevel(final EntityAccess entity, final boolean loaded, final CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof Entity actual && EntityEvents.JOIN_LEVEL.hasListeners())
            EntityEvents.JOIN_LEVEL.post(new EntityJoinLevelEvent(actual, actual.level(), loaded));
    }
}
