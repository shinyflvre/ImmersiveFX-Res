/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.fabric.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.vertex.PoseStack;
import dynamiclabs.immersivefx.platform.event.AfterSkyEvent;
import dynamiclabs.immersivefx.platform.event.RenderEvents;
import dynamiclabs.immersivefx.platform.event.SubmitCustomGeometryEvent;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {

    @Shadow
    @Final
    private LevelRenderState levelRenderState;

    @Shadow
    @Final
    private SubmitNodeStorage submitNodeStorage;

    @WrapOperation(method = "addSkyPass", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/framegraph/FramePass;executes(Ljava/lang/Runnable;)V"))
    private void ifx$afterSky(final FramePass pass, final Runnable task, final Operation<Void> original,
                              @Local(argsOnly = true) final CameraRenderState cameraState) {
        final LevelRenderState state = this.levelRenderState;
        original.call(pass, (Runnable) () -> {
            task.run();
            if (RenderEvents.AFTER_SKY.hasListeners())
                RenderEvents.AFTER_SKY.post(new AfterSkyEvent(state, null, cameraState.viewRotationMatrix));
        });
    }

    @Inject(method = "submitFeatures", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;finalizeGizmoCollection()V"))
    private void ifx$submitCustomGeometry(final LevelRenderState levelRenderState, final SubmitNodeCollector submitNodeCollector, final boolean renderOutline,
                                          final CallbackInfo ci, @Local final PoseStack poseStack) {
        if (RenderEvents.SUBMIT_CUSTOM_GEOMETRY.hasListeners())
            RenderEvents.SUBMIT_CUSTOM_GEOMETRY.post(new SubmitCustomGeometryEvent(levelRenderState, this.submitNodeStorage, poseStack));
    }
}
