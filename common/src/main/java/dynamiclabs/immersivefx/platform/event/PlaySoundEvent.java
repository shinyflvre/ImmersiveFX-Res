/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.platform.event;

import javax.annotation.Nullable;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;

public abstract class PlaySoundEvent {

    @Nullable
    public abstract SoundEngine getEngine();

    public abstract SoundInstance getOriginalSound();

    @Nullable
    public abstract SoundInstance getSound();

    public abstract void setSound(@Nullable SoundInstance sound);
}
