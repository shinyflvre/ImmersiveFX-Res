/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.platform.event;

import javax.annotation.Nullable;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.world.level.material.FogType;

public record RenderFogEvent(@Nullable FogEnvironment getEnvironment, FogType getType, Camera getCamera, float getPartialTick,
                             FogData getFogData) {
}
