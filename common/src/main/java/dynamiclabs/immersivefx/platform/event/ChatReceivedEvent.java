/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.platform.event;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;

public record ChatReceivedEvent(@Nullable ChatType.Bound getBoundChatType, Component getMessage, UUID getSender) {
}
