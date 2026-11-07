/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.platform.event;

import java.util.function.Function;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;

public interface ParticleGroupRegistrar {
    void register(ParticleRenderType group, Function<ParticleEngine, ParticleGroup<?>> factory);
}
