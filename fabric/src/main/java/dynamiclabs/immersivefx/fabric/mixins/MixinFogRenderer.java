/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.fabric.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import dynamiclabs.immersivefx.platform.event.RenderEvents;
import dynamiclabs.immersivefx.platform.event.RenderFogEvent;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public class MixinFogRenderer {

    @Shadow
    @Final
    private static List<FogEnvironment> FOG_ENVIRONMENTS;

    @Inject(method = "setupFog", at = @At("RETURN"))
    private void ifx$renderFog(final Camera camera, final int renderDistanceInChunks, final DeltaTracker deltaTracker, final float darkenWorldAmount,
                               final ClientLevel level, final CallbackInfoReturnable<FogData> cir,
                               @Local final FogType fogType, @Local final Entity entity) {
        if (!RenderEvents.RENDER_FOG.hasListeners())
            return;
        FogEnvironment chosen = null;
        for (final FogEnvironment environment : FOG_ENVIRONMENTS) {
            if (environment.isApplicable(fogType, entity)) {
                chosen = environment;
                break;
            }
        }
        RenderEvents.RENDER_FOG.post(new RenderFogEvent(chosen, fogType, camera, deltaTracker.getGameTimeDeltaPartialTick(false), cir.getReturnValue()));
    }
}
