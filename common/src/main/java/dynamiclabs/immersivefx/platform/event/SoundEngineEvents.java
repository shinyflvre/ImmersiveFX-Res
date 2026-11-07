/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.platform.event;

public final class SoundEngineEvents {
    private SoundEngineEvents() {
    }

    public static final EventHook<PlaySoundEvent> PLAY_SOUND = new EventHook<>("play sound");
    public static final EventHook<SoundSourceEvent> SOURCE_STARTED = new EventHook<>("sound source started");
}
