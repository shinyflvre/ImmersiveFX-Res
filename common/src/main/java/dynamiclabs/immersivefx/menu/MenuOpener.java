/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.menu;

import com.mojang.logging.LogUtils;
import dynamiclabs.immersivefx.platform.Platform;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.slf4j.Logger;

public final class MenuOpener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String VFX_MOD = "immersivevfx";
    private static final String VFX_SCREEN = "com.shinyflvres.immersivevfx.config.ImmersiveVFXScreen";

    private MenuOpener() {
    }

    public static void openFromKey(final Minecraft minecraft) {
        minecraft.gui.setScreen(vfxScreen(null) != null ? new MenuChooserScreen() : new ImmersiveFXScreen(null));
    }

    static @Nullable Screen vfxScreen(final @Nullable Screen parent) {
        if (!Platform.get().isModLoaded(VFX_MOD))
            return null;
        try {
            return (Screen) Class.forName(VFX_SCREEN).getConstructor(Screen.class).newInstance(parent);
        } catch (final ReflectiveOperationException | LinkageError e) {
            LOGGER.warn("Could not create the ImmersiveVFX settings screen", e);
            return null;
        }
    }
}
