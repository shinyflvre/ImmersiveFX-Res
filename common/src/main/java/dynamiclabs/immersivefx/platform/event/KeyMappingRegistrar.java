/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.platform.event;

import net.minecraft.client.KeyMapping;

public interface KeyMappingRegistrar {
    void registerCategory(KeyMapping.Category category);

    void register(KeyMapping key);
}
