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

package dynamiclabs.immersivefx.dsurround.gui;

import dynamiclabs.immersivefx.platform.event.KeyMappingRegistrar;
import dynamiclabs.immersivefx.ImmersiveFX;
import com.mojang.blaze3d.platform.InputConstants;
import dynamiclabs.immersivefx.dsurround.DynamicSurroundings;
import dynamiclabs.immersivefx.dsurround.config.Config;
import dynamiclabs.immersivefx.dsurround.huds.lightlevel.LightLevelHUD;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.resources.Identifier;

public class Keys {

    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(
            Identifier.fromNamespaceAndPath(DynamicSurroundings.MOD_ID, "controls"));

    private static KeyMapping lightLevelHUD;
    private static KeyMapping chunkBorders;

    public static void register(final KeyMappingRegistrar event) {
        final int flags = Config.CLIENT.logging.flagMask.get();
        final boolean lightLevel = (flags & Config.Flags.ALLOW_LIGHTLEVEL_HUD) != 0;
        final boolean chunks = (flags & Config.Flags.ALLOW_CHUNK_BORDER_HUD) != 0;
        if (!lightLevel && !chunks)
            return;

        try {
            event.registerCategory(CATEGORY);
        } catch (final IllegalArgumentException ignored) {
        }

        if (lightLevel) {
            lightLevelHUD = new KeyMapping(
                    "dsurround.text.lightlevel.toggle",
                    InputConstants.UNKNOWN.getValue(),
                    CATEGORY);
            event.register(lightLevelHUD);
        }

        if (chunks) {
            chunkBorders = new KeyMapping(
                    "dsurround.text.chunkborders.toggle",
                    InputConstants.UNKNOWN.getValue(),
                    CATEGORY);
            event.register(chunkBorders);
        }
    }

    public static void keyPressed(final Minecraft event) {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.gui.screen() == null && mc.player != null) {
            while (lightLevelHUD != null && lightLevelHUD.consumeClick()) {
                LightLevelHUD.toggleDisplay();
            }

            while (chunkBorders != null && chunkBorders.consumeClick()) {
                mc.debugEntries.toggleStatus(DebugScreenEntries.CHUNK_BORDERS);
            }
        }
    }
}
