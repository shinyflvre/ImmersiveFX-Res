/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.platform.event;

public final class LevelEvents {
    private LevelEvents() {
    }

    public static final EventHook<LevelEvent> LOAD = new EventHook<>("level load");
    public static final EventHook<LevelEvent> UNLOAD = new EventHook<>("level unload");
    public static final EventHook<TagsUpdatedEvent> TAGS_UPDATED = new EventHook<>("tags updated");
}
