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

package dynamiclabs.immersivefx.dsurround.commands.dump;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dynamiclabs.immersivefx.dsurround.DynamicSurroundings;
import dynamiclabs.immersivefx.dsurround.commands.CommandHelpers;
import dynamiclabs.immersivefx.environs.library.DimensionLibrary;
import dynamiclabs.immersivefx.lib.tags.TagUtils;
import dynamiclabs.immersivefx.mobeffects.library.FootstepLibrary;
import dynamiclabs.immersivefx.sndctrl.library.AcousticLibrary;
import dynamiclabs.immersivefx.platform.event.ClientCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.PrintStream;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Command for dumping out various things about the state of the mod.  These commands are only available single player
 * and execute on the main client thread.
 */
public class DumpCommand implements ClientCommand {
    public static final DumpCommand INSTANCE = new DumpCommand();

    private DumpCommand() {
    }

    @Override
    @Nonnull
    public <S> LiteralArgumentBuilder<S> build(@Nonnull final Feedback<S> feedback) {
        return LiteralArgumentBuilder.<S>literal("dsdump")
                .then(dump("acoustics", "dump.acoustics", AcousticLibrary::dump, feedback))
                .then(dump("audioeffects", "dump.audioeffects", DumpCommand::tbd, feedback))
                .then(dump("blocks", "dump.blocks", DumpCommand::tbd, feedback))
                .then(dump("blocktags", "dump.blocktags", TagUtils::dumpBlockTags, feedback))
                .then(dump("biomes", "dump.biomes", DumpCommand::tbd, feedback))
                .then(dump("items", "dump.items", DumpCommand::tbd, feedback))
                .then(dump("footsteps", "dump.footsteps", FootstepLibrary::dump, feedback))
                .then(dump("mobeffects", "dump.mobeffects", DumpCommand::tbd, feedback))
                .then(dump("blockstates", "dump.blockstates", DumpCommand::tbd, feedback))
                .then(dump("dimensions", "dump.dimensions", DimensionLibrary::dump, feedback));
    }

    @Nonnull
    private static <S> LiteralArgumentBuilder<S> dump(@Nonnull final String name, @Nonnull final String cmdString,
                                                      @Nonnull final Supplier<Stream<String>> supplier, @Nonnull final Feedback<S> feedback) {
        return LiteralArgumentBuilder.<S>literal(name).executes((CommandContext<S> cmd) -> {
            final S source = cmd.getSource();
            return handle(message -> feedback.send(source, message), cmdString, supplier);
        });
    }

    private static int handle(@Nonnull final Consumer<Component> source, @Nonnull final String cmdString, @Nonnull final Supplier<Stream<String>> supplier) {
        try {
            if (Minecraft.getInstance().hasSingleplayerServer()) {
                final String operation = cmdString.substring(5);
                final String fileName = operation + ".txt";
                File target = new File(DynamicSurroundings.DUMP_PATH, fileName);

                CommandHelpers.scheduleOnClientThread(() -> {
                    try (PrintStream out = new PrintStream(target)) {
                        final Stream<String> strm = supplier.get();
                        strm.forEach(out::println);
                        out.flush();
                    } catch (@Nonnull final Throwable t) {
                        DynamicSurroundings.LOGGER.error(t, "Error writing dump file '%s'", target.toString());
                    }
                });
                CommandHelpers.sendSuccess(source, "dump", operation, target.toString());
            } else {
                CommandHelpers.sendFailure(source, "dump");
            }
        } catch (@Nonnull final Throwable t) {
            CommandHelpers.sendFailure(source, "dump");
        }
        return 0;
    }

    private static Stream<String> tbd() {
        final List<String> result = ImmutableList.of("Not hooked up");
        return result.stream();
    }

}
