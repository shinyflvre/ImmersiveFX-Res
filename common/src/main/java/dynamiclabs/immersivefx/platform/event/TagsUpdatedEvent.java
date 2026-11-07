/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.platform.event;

import net.minecraft.core.RegistryAccess;

public record TagsUpdatedEvent(RegistryAccess getRegistries, boolean clientPacket) {
}
