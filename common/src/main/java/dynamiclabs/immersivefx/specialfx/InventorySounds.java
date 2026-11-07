/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.specialfx;

import dynamiclabs.immersivefx.platform.event.ScreenOpeningEvent;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;

public final class InventorySounds {
	private static final float MIN_PITCH = 0.9F;
	private static final float MAX_PITCH = 1.1F;
	private static final float MIN_VOLUME = 0.60F;
	private static final float MAX_VOLUME = 0.75F;

	private InventorySounds() {
	}

	private static boolean isInventory(final Screen screen) {
		return screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen;
	}

	public static void onScreenOpening(final ScreenOpeningEvent event) {
		if (SpecialFxConfig.OPTIONS == null || Minecraft.getInstance().player == null)
			return;
		if (!isInventory(event.getNewScreen()) || isInventory(event.getCurrentScreen()))
			return;
		final float factor = SpecialFxConfig.OPTIONS.inventoryOpen.factor();
		if (factor <= 0F)
			return;
		final ThreadLocalRandom random = ThreadLocalRandom.current();
		final float pitch = MIN_PITCH + random.nextFloat() * (MAX_PITCH - MIN_PITCH);
		final float volume = MIN_VOLUME + random.nextFloat() * (MAX_VOLUME - MIN_VOLUME);
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ImmersivefxRegistry.sound("inventory_open"), pitch, volume * factor));
	}
}
