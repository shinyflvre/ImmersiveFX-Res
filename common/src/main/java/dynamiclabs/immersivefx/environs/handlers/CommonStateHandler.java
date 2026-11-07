/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
/*
 *  Dynamic Surroundings
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

import dynamiclabs.immersivefx.platform.event.Priority;
import com.google.common.collect.Streams;
import dynamiclabs.immersivefx.environs.library.BiomeLibrary;
import dynamiclabs.immersivefx.environs.library.DimensionLibrary;
import dynamiclabs.immersivefx.environs.scanner.CeilingCoverage;
import dynamiclabs.immersivefx.lib.DayCycle;
import dynamiclabs.immersivefx.lib.GameUtils;
import dynamiclabs.immersivefx.lib.TickCounter;
import dynamiclabs.immersivefx.lib.WorldUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import dynamiclabs.immersivefx.environs.library.DimensionInfo;
import dynamiclabs.immersivefx.environs.Environs;
import dynamiclabs.immersivefx.environs.config.Config;
import dynamiclabs.immersivefx.environs.handlers.scripts.ConditionEvaluator;
import dynamiclabs.immersivefx.lib.*;
import dynamiclabs.immersivefx.lib.events.DiagnosticEvent;
import dynamiclabs.immersivefx.lib.resource.ResourceUtils;
import dynamiclabs.immersivefx.lib.seasons.Season;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class CommonStateHandler extends HandlerBase {

    private static final double VILLAGE_RANGE = 64 * 64;

    private final static List<String> scripts;

    static {
        final String script_lines = ResourceUtils.readResource(Identifier.fromNamespaceAndPath(Environs.MOD_ID, "misc/script_debug.txt"));
        assert script_lines != null;
        scripts = Pattern.compile("\\r?\\n")
                .splitAsStream(script_lines)
                .map(String::trim)
                .filter(s -> !(StringUtil.isNullOrEmpty(s) || s.startsWith("//")))
                .collect(Collectors.toList());
    }

    protected final CeilingCoverage ceilingCoverage = new CeilingCoverage();

    CommonStateHandler() {
        super("Common State");
    }

    @Override
    public void process(@Nonnull final Player player) {

        final long currentTick = TickCounter.getTickCount();
        final CommonState data = CommonState.getData();
        final Level world = player.level();

        ceilingCoverage.tick();

        data.clock.update(world);
        data.season = Season.getSeason(world);

        data.playerBiome = BiomeLibrary.getPlayerBiome(player, false);
        data.truePlayerBiome = BiomeLibrary.getPlayerBiome(player, true);
        data.dimInfo = DimensionLibrary.getData(world);
        data.dimensionName = data.dimInfo.getName().toString();
        data.playerPosition = player.blockPosition();
        data.playerEyePosition = player.getEyePosition(1F);
        data.dayCycle = DayCycle.getCycle(world);
        data.inside = ceilingCoverage.isReallyInside();
        data.biomeTemperature = WorldUtils.getTemperatureAt(world, data.playerPosition);

        data.isUnderground = data.playerBiome == BiomeLibrary.UNDERGROUND_INFO;
        data.isInSpace = data.playerBiome == BiomeLibrary.OUTERSPACE_INFO;
        data.isInClouds = data.playerBiome == BiomeLibrary.CLOUDS_INFO;

        final int blockLight = world.getBrightness(LightLayer.BLOCK, data.playerPosition);
        final int skyLight = world.getBrightness(LightLayer.SKY, data.playerPosition) - world.getRawBrightness(data.playerPosition, 0);
        data.lightLevel = Math.max(blockLight, skyLight);

        // Only check once a second
        if (currentTick % 20 == 0) {
            // Only for surface worlds.  Other types of worlds are interpreted as not having villages.
            if (DimensionInfo.isNatural(world)) {
                data.isInVillage = findBell(world, data.playerEyePosition);

                // If a bell is found, look for a villager within range
                if (data.isInVillage) {
                    final Optional<Entity> entity = Streams.stream(GameUtils.getWorld().entitiesForRendering())
                            .filter(e -> e instanceof Villager)
                            .filter(e -> e.distanceToSqr(data.playerEyePosition.x, data.playerEyePosition.y, data.playerEyePosition.z) <= VILLAGE_RANGE)
                            .findAny();
                    data.isInVillage = entity.isPresent();
                }
            } else {
                data.isInVillage = false;
            }
        }

        // Resets cached script variables so they are updated
        ConditionEvaluator.INSTANCE.tick();
    }

    private static boolean findBell(@Nonnull final Level world, @Nonnull final net.minecraft.world.phys.Vec3 eyes) {
        final int range = (int) Math.ceil(Math.sqrt(VILLAGE_RANGE));
        final int minCX = ((int) Math.floor(eyes.x) - range) >> 4;
        final int maxCX = ((int) Math.floor(eyes.x) + range) >> 4;
        final int minCZ = ((int) Math.floor(eyes.z) - range) >> 4;
        final int maxCZ = ((int) Math.floor(eyes.z) + range) >> 4;
        for (int cx = minCX; cx <= maxCX; cx++)
            for (int cz = minCZ; cz <= maxCZ; cz++) {
                if (!world.hasChunk(cx, cz))
                    continue;
                final LevelChunk chunk = world.getChunk(cx, cz);
                for (final BlockEntity te : chunk.getBlockEntities().values()) {
                    if (te instanceof BellBlockEntity
                            && te.getBlockPos().distToCenterSqr(eyes.x, eyes.y, eyes.z) <= VILLAGE_RANGE)
                        return true;
                }
            }
        return false;
    }

    @Override
    public void onDisconnect() {
        CommonState.reset();
    }

    @Override
    protected void registerEvents() {
        listen(DiagnosticEvent.BUS, Priority.HIGH, this::diagnostics);
    }

    public void diagnostics(@Nonnull final DiagnosticEvent event) {
        if (Config.CLIENT.logging.enableLogging.get()) {
            event.addLeft(ChatFormatting.YELLOW + CommonState.getData().clock.getFormattedTime());

            for (final String s : scripts) {
                final String result = ConditionEvaluator.INSTANCE.eval(s).toString();
                event.getLeft().add(ChatFormatting.DARK_AQUA + result);
            }
        }
    }
}
