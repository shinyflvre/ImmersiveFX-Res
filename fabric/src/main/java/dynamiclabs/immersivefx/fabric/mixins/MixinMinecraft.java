/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.fabric.mixins;

import dynamiclabs.immersivefx.platform.event.ClientConnectionEvent;
import dynamiclabs.immersivefx.platform.event.ClientEvents;
import dynamiclabs.immersivefx.platform.event.LevelEvent;
import dynamiclabs.immersivefx.platform.event.LevelEvents;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Shadow
    @Nullable
    public ClientLevel level;

    @Shadow
    @Nullable
    public MultiPlayerGameMode gameMode;

    @Shadow
    @Nullable
    public LocalPlayer player;

    @Inject(method = "setLevel", at = @At("HEAD"))
    private void ifx$unloadOnLevelChange(final ClientLevel newLevel, final CallbackInfo ci) {
        ifx$unloadLevel();
    }

    @Inject(method = {"disconnect(Lnet/minecraft/client/gui/screens/Screen;ZZ)V", "clearClientLevel"},
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;level:Lnet/minecraft/client/multiplayer/ClientLevel;", opcode = Opcodes.PUTFIELD))
    private void ifx$unloadOnDisconnect(final CallbackInfo ci) {
        ifx$unloadLevel();
    }

    @Unique
    private void ifx$unloadLevel() {
        if (this.level != null && LevelEvents.UNLOAD.hasListeners())
            LevelEvents.UNLOAD.post(new LevelEvent(this.level));
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;ZZ)V",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;gameMode:Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;", opcode = Opcodes.PUTFIELD))
    private void ifx$loggingOut(final CallbackInfo ci) {
        if (ClientEvents.LOGGING_OUT.hasListeners()) {
            final LocalPlayer p = this.player;
            ClientEvents.LOGGING_OUT.post(new ClientConnectionEvent(p, this.gameMode, p != null && p.connection != null ? p.connection.getConnection() : null));
        }
    }
}
