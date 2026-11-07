/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.fabric.mixins;

import java.util.Map;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.components.debug.DebugScreenProfile;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DebugScreenEntries.class)
public interface DebugScreenEntriesAccessor {

    @Accessor("PROFILES")
    @Mutable
    static void ifx$setProfiles(final Map<DebugScreenProfile, Map<Identifier, DebugScreenEntryStatus>> profiles) {
        throw new AssertionError();
    }
}
