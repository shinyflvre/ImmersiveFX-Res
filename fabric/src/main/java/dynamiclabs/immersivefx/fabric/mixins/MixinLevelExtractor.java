/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.fabric.mixins;

import dynamiclabs.immersivefx.platform.event.ExtractLevelRenderStateEvent;
import dynamiclabs.immersivefx.platform.event.RenderEvents;
import javax.annotation.Nullable;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelExtractor.class)
public class MixinLevelExtractor {

    @Shadow
    @Nullable
    private ClientLevel level;

    @Shadow
    @Final
    private LevelRenderState levelRenderState;

    @Inject(method = "extract", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/extract/LevelExtractor;extractGizmos()V", shift = At.Shift.AFTER))
    private void ifx$extract(final DeltaTracker deltaTracker, final Camera camera, final float worldPartialTicks, final CallbackInfo ci) {
        if (this.level != null && RenderEvents.EXTRACT_LEVEL_RENDER_STATE.hasListeners())
            RenderEvents.EXTRACT_LEVEL_RENDER_STATE.post(new ExtractLevelRenderStateEvent(this.level, camera, camera.getCullFrustum(), this.levelRenderState));
    }
}
