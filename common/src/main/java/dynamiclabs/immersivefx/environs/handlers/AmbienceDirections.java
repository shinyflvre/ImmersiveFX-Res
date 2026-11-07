/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
/*
 *  Dynamic Surroundings: Environs
 *  Copyright (C) 2020  OreCruncher
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package dynamiclabs.immersivefx.environs.handlers;

import dynamiclabs.immersivefx.environs.library.BiomeInfo;
import dynamiclabs.immersivefx.environs.library.BiomeUtil;
import dynamiclabs.immersivefx.lib.biomes.BiomeUtilities;
import dynamiclabs.immersivefx.sndctrl.api.acoustics.IAcoustic;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

final class AmbienceDirections {

    static final int SECTORS = 8;
    static final int EMITTERS = 4;

    private static final int[] RADII = {8, 16, 24};
    private static final float[] RADIUS_WEIGHT = {1F, 0.7F, 0.45F};
    private static final float[] SECTOR_SPREAD = {-15F, 0F, 15F};

    static final float AMBIENCE_GAIN = 1.41F;

    private static final float[][] LOBE = new float[EMITTERS][SECTORS];

    static {
        for (int e = 0; e < EMITTERS; e++)
            for (int s = 0; s < SECTORS; s++)
                LOBE[e][s] = (float) Math.max(0D, Math.cos(sectorAngle(s) - emitterAngle(e)));
    }

    private final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
    private final Reference2ObjectOpenHashMap<IAcoustic, float[]> sectors = new Reference2ObjectOpenHashMap<>(8);
    private final Reference2ObjectOpenHashMap<BiomeInfo, Collection<IAcoustic>> matches = new Reference2ObjectOpenHashMap<>(8);

    static double sectorAngle(final int sector) {
        return sector * (Math.PI * 2D / SECTORS);
    }

    static double emitterAngle(final int emitter) {
        return emitter * (Math.PI * 2D / EMITTERS);
    }

    void clear() {
        this.sectors.clear();
        this.matches.clear();
    }

    void survey() {
        clear();

        final BiomeInfo playerBiome = CommonState.getPlayerBiome();
        if (playerBiome == null || playerBiome.isFake())
            return;

        final BlockPos center = CommonState.getPlayerPosition();
        final double cx = center.getX() + 0.5D;
        final double cz = center.getZ() + 0.5D;

        for (int s = 0; s < SECTORS; s++) {
            final double sectorAngle = sectorAngle(s);
            for (final float spread : SECTOR_SPREAD) {
                final double angle = sectorAngle + Math.toRadians(spread);
                final double cos = Math.cos(angle);
                final double sin = Math.sin(angle);
                for (int r = 0; r < RADII.length; r++) {
                    this.mutable.set(Mth.floor(cx + cos * RADII[r]), center.getY(), Mth.floor(cz + sin * RADII[r]));
                    final Biome biome = BiomeUtilities.getClientBiome(this.mutable);
                    if (biome == null)
                        continue;
                    final BiomeInfo info = BiomeUtil.getBiomeData(biome);
                    Collection<IAcoustic> acoustics = this.matches.get(info);
                    if (acoustics == null) {
                        acoustics = info.findSoundMatches();
                        this.matches.put(info, acoustics);
                    }
                    for (final IAcoustic acoustic : acoustics) {
                        float[] weights = this.sectors.get(acoustic);
                        if (weights == null) {
                            weights = new float[SECTORS];
                            this.sectors.put(acoustic, weights);
                        }
                        weights[s] += RADIUS_WEIGHT[r];
                    }
                }
            }
        }
    }

    @Nullable
    float[] getSectorWeights(@Nonnull final IAcoustic acoustic) {
        return this.sectors.get(acoustic);
    }

    @Nonnull
    static float[] computeGains(@Nullable final float[] sectorWeights, final float directionality) {
        final float[] raw = new float[EMITTERS];
        float total = 0F;
        if (sectorWeights != null) {
            for (int e = 0; e < EMITTERS; e++) {
                float v = 0F;
                for (int s = 0; s < SECTORS; s++)
                    v += sectorWeights[s] * LOBE[e][s];
                raw[e] = v;
                total += v;
            }
        }

        final float d = Mth.clamp(directionality, 0F, 1F);
        final float even = 1F / EMITTERS;
        final float[] gains = new float[EMITTERS];
        for (int e = 0; e < EMITTERS; e++) {
            final float share = total > 0F ? (1F - d) * even + d * (raw[e] / total) : even;
            gains[e] = (float) Math.sqrt(share) * AMBIENCE_GAIN;
        }
        return gains;
    }
}
