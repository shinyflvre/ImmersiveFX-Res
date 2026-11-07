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

package dynamiclabs.immersivefx.sndctrl.gui;

import dynamiclabs.immersivefx.platform.Platform;
import dynamiclabs.immersivefx.platform.IPlatform;
import dynamiclabs.immersivefx.platform.event.KeyInputEvent;
import dynamiclabs.immersivefx.platform.event.KeyMappingRegistrar;
import dynamiclabs.immersivefx.ImmersiveFX;
import dynamiclabs.immersivefx.lib.GameUtils;
import dynamiclabs.immersivefx.menu.MenuOpener;
import dynamiclabs.immersivefx.sndctrl.SoundControl;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.resources.Identifier;

import javax.annotation.Nonnull;

public class Keys {

    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath("dsurround", "controls"));

    private static KeyMapping quickVolumeGui;
    private static KeyMapping soundConfigGui;

    public static void register() {
        if (quickVolumeGui != null)
            return;

        quickVolumeGui = new KeyMapping(
                "sndctrl.text.quickvolumemenu.open",
                InputConstants.UNKNOWN.getValue(),
                CATEGORY);
        Platform.get().setKeyModifierAndCode(quickVolumeGui, IPlatform.KeyModifier.CONTROL, InputConstants.getKey("key.keyboard.v"));

        soundConfigGui = new KeyMapping(
                "sndctrl.text.soundconfig.open",
                InputConstants.UNKNOWN.getValue(),
                CATEGORY);
        Platform.get().setKeyModifierAndCode(soundConfigGui, IPlatform.KeyModifier.CONTROL, InputConstants.getKey("key.keyboard.i"));
    }

    public static void onRegisterKeyMappings(@Nonnull final KeyMappingRegistrar event) {
        register();
        try {
            event.registerCategory(CATEGORY);
        } catch (@Nonnull final IllegalArgumentException ignored) {
        }
        event.register(quickVolumeGui);
        event.register(soundConfigGui);
    }

    public static void keyPressed(@Nonnull final KeyInputEvent event) {
        if (quickVolumeGui == null)
            return;
        final Minecraft mc = GameUtils.getMC();
        if (mc.gui.screen() == null && GameUtils.getPlayer() != null) {
            if (quickVolumeGui.consumeClick()) {
                mc.gui.setScreen(new QuickVolumeScreen());
            } else if (soundConfigGui.consumeClick()) {
                MenuOpener.openFromKey(mc);
            }
        }
    }
}
