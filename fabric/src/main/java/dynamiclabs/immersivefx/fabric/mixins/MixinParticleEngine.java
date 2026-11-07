/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.fabric.mixins;

import dynamiclabs.immersivefx.fabric.FabricRegistrations;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.ParticlesRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleEngine.class)
public class MixinParticleEngine {

    @Shadow
    @Final
    private Map<ParticleRenderType, ParticleGroup<?>> particles;

    @Inject(method = "createParticleGroup", at = @At("HEAD"), cancellable = true)
    private void ifx$createParticleGroup(final ParticleRenderType type, final CallbackInfoReturnable<ParticleGroup<?>> cir) {
        final Function<ParticleEngine, ParticleGroup<?>> factory = FabricRegistrations.particleGroupFactory(type);
        if (factory != null)
            cir.setReturnValue(factory.apply((ParticleEngine) (Object) this));
    }

    @Inject(method = "extract", at = @At("TAIL"))
    private void ifx$extractGroups(final ParticlesRenderState particlesRenderState, final Frustum frustum, final Camera camera, final float partialTickTime,
                                   final CallbackInfo ci) {
        for (final ParticleRenderType type : FabricRegistrations.particleGroupOrder()) {
            final ParticleGroup<?> group = this.particles.get(type);
            if (group != null && !group.isEmpty())
                particlesRenderState.add(group.extractRenderState(frustum, camera, partialTickTime));
        }
    }
}
