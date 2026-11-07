/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.neoforge;

import dynamiclabs.immersivefx.ImmersiveFX;
import dynamiclabs.immersivefx.lib.config.ConfigGui;
import javax.annotation.Nonnull;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(value = ImmersiveFX.MOD_ID, dist = Dist.CLIENT)
public final class ImmersiveFXNeoForge {

    public ImmersiveFXNeoForge(@Nonnull final IEventBus modBus, @Nonnull final ModContainer container) {
        NeoForgePlatform.setContainer(container);
        NeoForgeEventBridge.register(modBus);
        ImmersiveFX.init();
        ConfigGui.registerConfigGui(container);
    }
}
