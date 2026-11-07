/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.platform.event;

public final class ConfigEvents {
    private ConfigEvents() {
    }

    public static final EventHook<ConfigEvent> LOADING = new EventHook<>("config loading");
    public static final EventHook<ConfigEvent> RELOADING = new EventHook<>("config reloading");
}
