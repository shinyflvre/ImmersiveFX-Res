/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.platform.event;

public interface Cancellable {
    boolean isCanceled();

    void setCanceled(boolean canceled);
}
