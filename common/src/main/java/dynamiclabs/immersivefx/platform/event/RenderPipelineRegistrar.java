/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.platform.event;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;

public interface RenderPipelineRegistrar {
    void registerPipeline(RenderPipeline pipeline);
}
