/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.dsurround.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dynamiclabs.immersivefx.specialfx.BlockBreakSounds;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelEventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelEventHandler.class)
public abstract class MixinLevelEventHandler {

    @Shadow
    @Final
    private ClientLevel level;

    @Unique
    private boolean immersivefx$muteBreakSound;

    @Inject(method = "levelEvent", at = @At("HEAD"))
    private void immersivefx$blockBroken(final int eventType, final BlockPos pos, final int data, final CallbackInfo ci) {
        this.immersivefx$muteBreakSound = eventType == LevelEvent.PARTICLES_AND_SOUND_DESTROY_BLOCK
                && BlockBreakSounds.onBlockBroken(this.level, pos, Block.stateById(data));
    }

    @WrapOperation(method = "levelEvent", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/ClientLevel;playLocalSound(Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZ)V"))
    private void immersivefx$vanillaBreakSound(final ClientLevel level, final BlockPos pos, final SoundEvent sound, final SoundSource source,
                                               final float volume, final float pitch, final boolean distanceDelay, final Operation<Void> original) {
        if (!this.immersivefx$muteBreakSound)
            original.call(level, pos, sound, source, volume, pitch, distanceDelay);
    }
}
