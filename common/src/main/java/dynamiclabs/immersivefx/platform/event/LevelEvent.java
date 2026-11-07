/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.platform.event;

import net.minecraft.world.level.LevelAccessor;

public record LevelEvent(LevelAccessor getLevel) {
}
