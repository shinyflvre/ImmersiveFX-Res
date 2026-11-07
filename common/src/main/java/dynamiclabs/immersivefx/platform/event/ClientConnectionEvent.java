/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.platform.event;

import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;

public record ClientConnectionEvent(@Nullable LocalPlayer getPlayer, @Nullable MultiPlayerGameMode getMultiPlayerGameMode,
                                    @Nullable Connection getConnection) {
}
