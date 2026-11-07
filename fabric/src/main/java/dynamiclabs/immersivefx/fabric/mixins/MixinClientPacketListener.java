/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.fabric.mixins;

import dynamiclabs.immersivefx.platform.event.ClientConnectionEvent;
import dynamiclabs.immersivefx.platform.event.ClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener {

    @Inject(method = "handleLogin", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;resetPos()V", shift = At.Shift.AFTER))
    private void ifx$loggingIn(final ClientboundLoginPacket packet, final CallbackInfo ci) {
        if (ClientEvents.LOGGING_IN.hasListeners()) {
            final Minecraft mc = Minecraft.getInstance();
            ClientEvents.LOGGING_IN.post(new ClientConnectionEvent(mc.player, mc.gameMode, ((ClientPacketListener) (Object) this).getConnection()));
        }
    }
}
