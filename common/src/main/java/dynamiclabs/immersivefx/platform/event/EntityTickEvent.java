/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.platform.event;

import net.minecraft.world.entity.Entity;

public record EntityTickEvent(Entity getEntity) {
}
