/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.fabric;

import com.mojang.logging.LogUtils;
import dynamiclabs.immersivefx.ImmersiveFX;
import dynamiclabs.immersivefx.platform.IPlatform;
import dynamiclabs.immersivefx.platform.Platform;
import fuzs.forgeconfigapiport.fabric.api.v5.client.ConfigScreenFactoryRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import org.slf4j.Logger;

public final class ImmersiveFXFabric implements ClientModInitializer {

    static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitializeClient() {
        FabricEventBridge.register();
        ImmersiveFX.init();
        FabricEventBridge.flushConfigLoading();
        FabricRegistrations.registerAll();
        ConfigScreenFactoryRegistry.INSTANCE.register(ImmersiveFX.MOD_ID, ConfigurationScreen::new);
        LOGGER.info("ImmersiveFX {} initialized on Fabric", Platform.get().getModInfo(ImmersiveFX.MOD_ID).map(IPlatform.ModInfo::version).orElse("?"));
    }

}
