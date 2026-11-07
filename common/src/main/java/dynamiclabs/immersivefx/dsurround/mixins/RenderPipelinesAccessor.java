/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.dsurround.mixins;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderPipelines.class)
public interface RenderPipelinesAccessor {

    @Accessor("GLOBALS_SNIPPET")
    static RenderPipeline.Snippet immersivefx$getGlobalsSnippet() {
        throw new AssertionError();
    }

    @Accessor("PARTICLE_SNIPPET")
    static RenderPipeline.Snippet immersivefx$getParticleSnippet() {
        throw new AssertionError();
    }
}
