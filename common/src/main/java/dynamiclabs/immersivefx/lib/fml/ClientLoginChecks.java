/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
/*
 * Dynamic Surroundings
 * Copyright (C) 2020  OreCruncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package dynamiclabs.immersivefx.lib.fml;

import dynamiclabs.immersivefx.platform.event.ClientConnectionEvent;
import dynamiclabs.immersivefx.ImmersiveFX;
import dynamiclabs.immersivefx.lib.Lib;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import dynamiclabs.immersivefx.dsurround.DynamicSurroundings;
import dynamiclabs.immersivefx.lib.collections.ObjectArray;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Simple framework to register callbacks for whenever a player logs into a session.  These checks can be a
 * check for updated versions of a mod, or other checks about runtime that the player may need to know about (like
 * mod conflicts).
 */
public class ClientLoginChecks {

    private static final ObjectArray<ICallbackHandler> handlers = new ObjectArray<>();

    public static void register(@Nonnull final ICallbackHandler handler) {
        handlers.add(handler);
    }

    public static void onLogin(@Nonnull final ClientConnectionEvent event) {
        final LocalPlayer player = event.getPlayer();
        if (player != null) {
            Lib.LOGGER.info("Player login: %s", event.getPlayer().getName().getString());

            for (final ICallbackHandler callback : handlers) {
                final Component msg = callback.onClientLogin(event.getPlayer());
                if (msg != null)
                    event.getPlayer().sendSystemMessage(msg);
            }
        }
    }

    @FunctionalInterface
    public interface ICallbackHandler {
        /**
         * Invoked by the login process.  The result, if provided, will be sent to the local chat window to
         * inform the player.
         *
         * @param player The instance of the player that logged in
         * @return Text message to display to the player, if any
         */
        @Nullable
        Component onClientLogin(@Nonnull final LocalPlayer player);
    }
}
