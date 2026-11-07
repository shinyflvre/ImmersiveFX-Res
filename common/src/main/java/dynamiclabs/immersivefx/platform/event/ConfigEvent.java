/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.platform.event;

import net.neoforged.fml.config.IConfigSpec;

public record ConfigEvent(IConfigSpec getSpec, String getFileName) {
}
