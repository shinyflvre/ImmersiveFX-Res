/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
/*
 * Dynamic Surroundings: Sound Control
 * Copyright (C) 2019  OreCruncher
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

package dynamiclabs.immersivefx.lib;

import net.minecraft.client.Options;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.CameraType;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class GameUtils {
    private GameUtils() {

    }

    // Client methods
    @Nullable
    public static Player getPlayer() {
        return getMC().player;
    }

    @Nullable
    public static ClientLevel getWorld() {
        return getMC().level;
    }

    @Nonnull
    public static Minecraft getMC() {
        return Minecraft.getInstance();
    }

    @Nonnull
    public static Options getGameSettings() {
        return getMC().options;
    }

    public static boolean displayDebug() {
        return getMC().getDebugOverlay().showDebugScreen();
    }

    @Nonnull
    public static SoundManager getSoundHander() {
        return getMC().getSoundManager();
    }

    public static boolean isInGame() {
        return getWorld() != null && getPlayer() != null;
    }

    public static boolean isThirdPersonView() {
        return getGameSettings().getCameraType() != CameraType.FIRST_PERSON;
    }

    public static boolean isFirstPersonView() {
        return getGameSettings().getCameraType() == CameraType.FIRST_PERSON;
    }
}
