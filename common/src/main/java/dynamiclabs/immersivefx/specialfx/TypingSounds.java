/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.specialfx;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;

public final class TypingSounds {
	private static final float MIN_PITCH = 0.8F;
	private static final float MAX_PITCH = 1.0F;
	private static final float MIN_VOLUME = 0.5F;
	private static final float MAX_VOLUME = 0.7F;

	private TypingSounds() {
	}

	public static void onCharTyped() {
		if (SpecialFxConfig.OPTIONS == null)
			return;
		final float factor = SpecialFxConfig.OPTIONS.typing.factor();
		if (factor <= 0F)
			return;
		final ThreadLocalRandom random = ThreadLocalRandom.current();
		final float pitch = MIN_PITCH + random.nextFloat() * (MAX_PITCH - MIN_PITCH);
		final float volume = MIN_VOLUME + random.nextFloat() * (MAX_VOLUME - MIN_VOLUME);
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ImmersivefxRegistry.sound("typing"), pitch, volume * factor));
	}
}
