/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.fabric.mixins;

import dynamiclabs.immersivefx.platform.event.EntityEvents;
import dynamiclabs.immersivefx.platform.event.PlayerTickEvent;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class MixinPlayer {

    @Inject(method = "tick", at = @At("HEAD"))
    private void ifx$tickStart(final CallbackInfo ci) {
        if (EntityEvents.PLAYER_TICK_START.hasListeners())
            EntityEvents.PLAYER_TICK_START.post(new PlayerTickEvent((Player) (Object) this));
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void ifx$tickEnd(final CallbackInfo ci) {
        if (EntityEvents.PLAYER_TICK_END.hasListeners())
            EntityEvents.PLAYER_TICK_END.post(new PlayerTickEvent((Player) (Object) this));
    }
}
