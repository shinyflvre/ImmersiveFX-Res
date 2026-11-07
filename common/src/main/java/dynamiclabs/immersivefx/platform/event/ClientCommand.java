/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.platform.event;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.network.chat.Component;

public interface ClientCommand {

    <S> LiteralArgumentBuilder<S> build(Feedback<S> feedback);

    @FunctionalInterface
    interface Feedback<S> {
        void send(S source, Component message);
    }
}
