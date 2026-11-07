/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
package dynamiclabs.immersivefx.specialfx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Immersivefx {
	public static final String MOD_ID = "immersivefx";
	public static final Logger LOGGER = LogManager.getLogger(Immersivefx.class);

	public Immersivefx() {
		LOGGER.debug("ImmersiveFX SpecialFX: {} sound events", ImmersivefxRegistry.sounds.size());
	}
}
