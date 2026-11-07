/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.platform.event;

public final class EntityEvents {
    private EntityEvents() {
    }

    public static final EventHook<EntityTickEvent> ENTITY_TICK = new EventHook<>("entity tick");
    public static final EventHook<PlayerTickEvent> PLAYER_TICK_START = new EventHook<>("player tick start");
    public static final EventHook<PlayerTickEvent> PLAYER_TICK_END = new EventHook<>("player tick end");
    public static final EventHook<EntityJoinLevelEvent> JOIN_LEVEL = new EventHook<>("entity join level");
    public static final EventHook<ItemResultEvent> ITEM_CRAFTED = new EventHook<>("item crafted");
    public static final EventHook<ItemResultEvent> ITEM_SMELTED = new EventHook<>("item smelted");
    public static final EventHook<RightClickBlockEvent> RIGHT_CLICK_BLOCK = new EventHook<>("right click block");
}
