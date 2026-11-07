/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.platform.event;

import javax.annotation.Nullable;
import net.minecraft.client.gui.screens.Screen;

public record ScreenOpeningEvent(@Nullable Screen getCurrentScreen, Screen getNewScreen) {
}
