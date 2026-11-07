/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.platform.event;

public final class ClientEvents {
    private ClientEvents() {
    }

    public static final EventHook<ClientConnectionEvent> LOGGING_IN = new EventHook<>("logging in");
    public static final EventHook<ClientConnectionEvent> LOGGING_OUT = new EventHook<>("logging out");
    public static final EventHook<ScreenOpeningEvent> SCREEN_OPENING = new EventHook<>("screen opening");
    public static final EventHook<ChatSendEvent> CHAT_SEND = new EventHook<>("chat send");
    public static final EventHook<ChatReceivedEvent> CHAT_RECEIVED = new EventHook<>("player chat received");
    public static final EventHook<KeyInputEvent> KEY_INPUT = new EventHook<>("key input");
}
