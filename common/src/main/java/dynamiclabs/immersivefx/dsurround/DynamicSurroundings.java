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

package dynamiclabs.immersivefx.dsurround;

import dynamiclabs.immersivefx.dsurround.config.Config;
import dynamiclabs.immersivefx.lib.fml.ClientLoginChecks;
import dynamiclabs.immersivefx.lib.fml.ConfigUtils;
import dynamiclabs.immersivefx.lib.fml.ForgeUtils;
import dynamiclabs.immersivefx.lib.fml.UpdateChecker;
import dynamiclabs.immersivefx.lib.logging.ModLog;
import dynamiclabs.immersivefx.platform.event.ClientLifecycleEvents;
import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class DynamicSurroundings {

    /**
     * ID of the mod
     */
    public static final String MOD_ID = "dsurround";

    /**
     * Logging instance for trace
     */
    public static final ModLog LOGGER = new ModLog(DynamicSurroundings.class);

    /**
     * Path to the mod's configuration directory
     */
    public static final Path CONFIG_PATH = ConfigUtils.getConfigPath(MOD_ID);

    /**
     * Path to the external config data cache for user customization
     */
    public static final File DATA_PATH = Paths.get(CONFIG_PATH.toString(), "configs").toFile();

    /**
     * Path to the external folder for dumping data
     */
    public static final File DUMP_PATH = Paths.get(CONFIG_PATH.toString(), "dumps").toFile();

    public DynamicSurroundings() {

        ClientLifecycleEvents.CLIENT_SETUP.register(this::clientSetup);
        ClientLifecycleEvents.LOAD_COMPLETE.register(this::setupComplete);

        // Create additional data paths if needed
        createPath(DATA_PATH);
        createPath(DUMP_PATH);
    }


    private static void createPath(@Nonnull final File path) {
        if (!path.exists()) {
            try {
                path.mkdirs();
            } catch (@Nonnull final Throwable t) {
                LOGGER.error(t, "Unable to create data path %s", path.toString());
            }
        }
    }

    private void clientSetup(@Nonnull final Minecraft mc) {
        if (Config.CLIENT.logging.onlineVersionCheck.get())
            ClientLoginChecks.register(new UpdateChecker(dynamiclabs.immersivefx.ImmersiveFX.MOD_ID));
    }

    private void setupComplete(@Nonnull final Minecraft mc) {
        if (LOGGER.isDebugging()) {
            // Dump out the mod list
            LOGGER.info("Loaded Mods");
            LOGGER.info("===========");
            ForgeUtils.getModIdList().forEach(l -> LOGGER.info("Mod '%s' detected", l));

            LOGGER.info("Resource Packs");
            LOGGER.info("==============");
            ForgeUtils.getEnabledResourcePacks().forEach(l -> LOGGER.info("%s (%s)", l.getTitle().getString(), l.getDescription().getString()));
        }
    }

}
