/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.platform.event;

import net.minecraft.client.Minecraft;

public final class ClientTickEvents {
    private ClientTickEvents() {
    }

    public static final EventHook<Minecraft> START = new EventHook<>("client tick start");
    public static final EventHook<Minecraft> END = new EventHook<>("client tick end");
}
