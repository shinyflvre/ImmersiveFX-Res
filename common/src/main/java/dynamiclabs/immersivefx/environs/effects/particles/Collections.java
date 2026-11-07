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

package dynamiclabs.immersivefx.environs.effects.particles;

import dynamiclabs.immersivefx.ImmersiveFX;
import dynamiclabs.immersivefx.environs.Environs;
import dynamiclabs.immersivefx.environs.config.Config;
import dynamiclabs.immersivefx.lib.GameUtils;
import dynamiclabs.immersivefx.lib.events.DiagnosticEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.BlockGetter;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public final class Collections {

    private static final MoteCollection theRipples = new MoteCollection("Rain Ripples");
    private static final MoteCollection theSprays = new MoteCollection("Water Spray");
    private static final MoteCollection theFireFlies = new MoteCollection("Fireflies");

    private Collections() {

    }

    public static void addWaterRipple(@Nonnull final BlockGetter world, final double x, final double y,
                                      final double z) {
        if (theRipples.canFit()) {
            theRipples.add(new MoteWaterRipple(world, x, y, z));
        }
    }

    public static boolean addWaterSpray(@Nonnull final BlockGetter world, final double x, final double y,
                                        final double z, final double dX, final double dY, final double dZ) {
        if (theSprays.canFit()) {
            theSprays.add(new MoteWaterSpray(world, x, y, z, dX, dY, dZ));
            return true;
        }
        return false;
    }

    public static boolean canFitWaterSpray() {
        return theSprays.canFit();
    }

    public static void addRainSplash(@Nonnull final BlockGetter world, final double x, final double y,
                                     final double z) {
        if (theSprays.canFit()) {
            theSprays.add(new MoteRainSplash(world, x, y, z));
        }
    }

    public static void addFireFly(@Nonnull final BlockGetter world, final double x, final double y, final double z) {
        if (theFireFlies.canFit()) {
            theFireFlies.add(new MoteFireFly(world, x, y, z));
        }
    }

    public static void diagnostics(@Nonnull final DiagnosticEvent event) {
        if (Config.CLIENT.logging.enableLogging.get()) {
            event.getLeft().add(ChatFormatting.AQUA + theRipples.toString());
            event.getLeft().add(ChatFormatting.AQUA + theSprays.toString());
            event.getLeft().add(ChatFormatting.AQUA + theFireFlies.toString());
        }
    }

    private static final class MoteCollection {

        private static final int MAX_PARTICLES = 4000;

        private final String name;
        private final List<MoteParticle> motes = new ArrayList<>();

        MoteCollection(@Nonnull final String name) {
            this.name = name;
        }

        private ClientLevel prunedLevel;
        private long prunedTime = Long.MIN_VALUE;

        private int prune() {
            final ClientLevel level = GameUtils.getWorld();
            if (level == null) {
                this.motes.clear();
                this.prunedLevel = null;
                return 0;
            }
            if (level != this.prunedLevel || level.getGameTime() != this.prunedTime) {
                this.motes.removeIf(m -> !m.isLive(level));
                this.prunedLevel = level;
                this.prunedTime = level.getGameTime();
            }
            return this.motes.size();
        }

        boolean canFit() {
            return GameUtils.getWorld() != null && prune() < MAX_PARTICLES;
        }

        void add(@Nonnull final MoteParticle mote) {
            this.motes.add(mote);
            GameUtils.getMC().particleEngine.add(mote);
        }

        @Override
        public String toString() {
            return this.name + '=' + prune();
        }
    }
}
