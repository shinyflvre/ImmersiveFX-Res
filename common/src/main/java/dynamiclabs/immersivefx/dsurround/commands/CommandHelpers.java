/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
/*
 * Dynamic Surroundings
 * Copyright (C) 2020  OreCruncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package dynamiclabs.immersivefx.dsurround.commands;

import java.util.concurrent.CompletableFuture;
import dynamiclabs.immersivefx.platform.event.ClientCommandRegistrar;
import dynamiclabs.immersivefx.ImmersiveFX;
import dynamiclabs.immersivefx.dsurround.DynamicSurroundings;
import dynamiclabs.immersivefx.dsurround.commands.dump.DumpCommand;
import net.minecraft.client.Minecraft;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

public class CommandHelpers {
    private CommandHelpers() {

    }

    public static void registerCommands(@Nonnull final ClientCommandRegistrar event) {
        event.register(DumpCommand.INSTANCE);
    }

    public static void scheduleOnClientThread(Runnable runnable) {
        CompletableFuture.runAsync(runnable, Minecraft.getInstance());
    }

    public static void sendSuccess(@Nonnull final Consumer<Component> source, @Nonnull final String command, @Nonnull String operation, @Nonnull String target) {
        final String key = String.format("command.dsurround.%s.success", command);
        source.accept(Component.translatable(key, operation, target));
    }

    public static void sendFailure(@Nonnull final Consumer<Component> source, @Nonnull final String command) {
        final String key = String.format("command.dsurround.%s.failure", command);
        source.accept(Component.translatable(key));
    }
}
