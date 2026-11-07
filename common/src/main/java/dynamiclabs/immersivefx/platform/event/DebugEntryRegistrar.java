/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.platform.event;

import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.components.debug.DebugScreenProfile;
import net.minecraft.resources.Identifier;

public interface DebugEntryRegistrar {
    void register(Identifier id, DebugScreenEntry entry);

    void includeInProfile(Identifier id, DebugScreenProfile profile, DebugScreenEntryStatus status);
}
