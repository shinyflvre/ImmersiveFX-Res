/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.platform.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public record EntityJoinLevelEvent(Entity getEntity, Level getLevel, boolean loadedFromDisk) {
}
