/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
/*
 * Dynamic Surroundings]
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

package dynamiclabs.immersivefx.lib;

import dynamiclabs.immersivefx.platform.event.DebugEntryRegistrar;
import net.minecraft.client.Minecraft;
import dynamiclabs.immersivefx.ImmersiveFX;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.components.debug.DebugScreenProfile;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import dynamiclabs.immersivefx.dsurround.DynamicSurroundings;
import dynamiclabs.immersivefx.lib.events.DiagnosticEvent;
import dynamiclabs.immersivefx.lib.math.LoggingTimerEMA;
import dynamiclabs.immersivefx.lib.math.MathStuff;
import dynamiclabs.immersivefx.lib.math.TimerEMA;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public final class Diagnostics {

    public static final Identifier LEFT_ENTRY = Identifier.fromNamespaceAndPath(DynamicSurroundings.MOD_ID, "diagnostics_left");
    public static final Identifier RIGHT_ENTRY = Identifier.fromNamespaceAndPath(DynamicSurroundings.MOD_ID, "diagnostics_right");

    private static final TimerEMA clientTick = new TimerEMA("Client Tick");
    private static final TimerEMA lastTick = new TimerEMA("Last Tick");
    private static final LoggingTimerEMA diagnostics = new LoggingTimerEMA("Diagnostics");
    private static long lastTickMark = -1;
    private static long timeMark = 0;
    private static float tps = 0;

    private static DiagnosticEvent lastEvent = new DiagnosticEvent();

    public static void startTick(@Nonnull final Minecraft event) {
        timeMark = System.nanoTime();
        if (lastTickMark != -1) {
            lastTick.update(timeMark - lastTickMark);
            tps = MathStuff.clamp((float) (50F / lastTick.getMSecs() * 20F), 0F, 20F);
        }
        lastTickMark = timeMark;
    }

    public static void endTick(@Nonnull final Minecraft event) {
        final long delta = System.nanoTime() - timeMark;
        clientTick.update(delta);
    }

    public static void onClientTick(@Nonnull final Minecraft event) {
        if (GameUtils.displayDebug()) {
            diagnostics.begin();
            final DiagnosticEvent evt = new DiagnosticEvent();
            evt.getRight().add("");
            evt.getRight().add(ChatFormatting.LIGHT_PURPLE + clientTick.toString());
            evt.getRight().add(ChatFormatting.LIGHT_PURPLE + lastTick.toString());
            evt.getRight().add(ChatFormatting.LIGHT_PURPLE + diagnostics.toString());
            evt.getRight().add(ChatFormatting.LIGHT_PURPLE + String.format("TPS:%7.3fms", tps));
            DiagnosticEvent.BUS.post(evt);
            lastEvent = evt;

            if (!lastEvent.getTimers().isEmpty()) {
                for (final TimerEMA timer : lastEvent.getTimers())
                    evt.getRight().add(ChatFormatting.GREEN + timer.toString());
            }

            if (!lastEvent.getRenderTimers().isEmpty()) {
                for (final TimerEMA timer : lastEvent.getRenderTimers())
                    evt.getRight().add(ChatFormatting.AQUA + timer.toString());
            }
            diagnostics.end();
        }
    }

    public static void onRegisterDebugEntries(@Nonnull final DebugEntryRegistrar event) {
        event.register(LEFT_ENTRY, new LinesEntry(LEFT_ENTRY, true));
        event.register(RIGHT_ENTRY, new LinesEntry(RIGHT_ENTRY, false));
        event.includeInProfile(LEFT_ENTRY, DebugScreenProfile.DEFAULT, DebugScreenEntryStatus.IN_OVERLAY);
        event.includeInProfile(RIGHT_ENTRY, DebugScreenProfile.DEFAULT, DebugScreenEntryStatus.IN_OVERLAY);
    }

    private static final class LinesEntry implements DebugScreenEntry {
        private final Identifier group;
        private final boolean left;

        LinesEntry(@Nonnull final Identifier group, final boolean left) {
            this.group = group;
            this.left = left;
        }

        @Override
        public void display(@Nonnull final DebugScreenDisplayer displayer, @Nullable final Level serverOrClientLevel, @Nullable final LevelChunk clientChunk, @Nullable final LevelChunk serverChunk) {
            final Collection<String> lines = this.left ? lastEvent.getLeft() : lastEvent.getRight();
            if (!lines.isEmpty())
                displayer.addToGroup(this.group, lines);
        }

        @Override
        public boolean isAllowed(final boolean reducedDebugInfo) {
            return true;
        }
    }
}
