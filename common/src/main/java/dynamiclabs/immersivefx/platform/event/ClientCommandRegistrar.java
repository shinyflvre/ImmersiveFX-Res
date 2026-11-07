/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.platform.event;

import net.minecraft.commands.CommandBuildContext;

public interface ClientCommandRegistrar {
    CommandBuildContext getBuildContext();

    void register(ClientCommand command);
}
