/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.platform.event;

import net.minecraft.client.Minecraft;

public final class ClientLifecycleEvents {
    private ClientLifecycleEvents() {
    }

    public static final EventHook<Minecraft> CLIENT_SETUP = new EventHook<>("client setup");
    public static final EventHook<Minecraft> REGISTER_INTEGRATIONS = new EventHook<>("register integrations");
    public static final EventHook<Minecraft> LOAD_COMPLETE = new EventHook<>("load complete");
}
