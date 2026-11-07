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

package dynamiclabs.immersivefx.environs.library;

import com.google.common.collect.ImmutableList;
import com.google.gson.reflect.TypeToken;
import dynamiclabs.immersivefx.environs.library.config.AcousticConfig;
import dynamiclabs.immersivefx.environs.library.config.BlockConfig;
import dynamiclabs.immersivefx.environs.library.config.EffectConfig;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import org.apache.commons.lang3.StringUtils;
import dynamiclabs.immersivefx.dsurround.DynamicSurroundings;
import dynamiclabs.immersivefx.environs.Environs;
import dynamiclabs.immersivefx.environs.effects.BlockEffectType;
import dynamiclabs.immersivefx.environs.library.config.*;
import dynamiclabs.immersivefx.lib.blockstate.BlockStateMatcher;
import dynamiclabs.immersivefx.lib.blockstate.BlockStateMatcherMap;
import dynamiclabs.immersivefx.lib.fml.ForgeUtils;
import dynamiclabs.immersivefx.lib.logging.IModLog;
import dynamiclabs.immersivefx.lib.resource.IResourceAccessor;
import dynamiclabs.immersivefx.lib.resource.ResourceUtils;
import dynamiclabs.immersivefx.lib.service.ModuleServiceManager;
import dynamiclabs.immersivefx.lib.service.IModuleService;
import dynamiclabs.immersivefx.lib.validation.ListValidator;
import dynamiclabs.immersivefx.lib.validation.Validators;
import dynamiclabs.immersivefx.sndctrl.api.acoustics.IAcoustic;
import dynamiclabs.immersivefx.sndctrl.api.acoustics.Library;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class BlockStateLibrary {

    private static final String TAG_SPECIFIER = "#";

    private static final IModLog LOGGER = Environs.LOGGER.createChild(BlockStateLibrary.class);
    private static final BlockStateMatcherMap<BlockStateData> registry = new BlockStateMatcherMap<>();

    private BlockStateLibrary() {

    }

    static void initialize() {
        ModuleServiceManager.instance().add(new BlockStateLibraryService());
    }

    static void initFromConfig(@Nonnull final List<BlockConfig> config) {
        config.forEach(BlockStateLibrary::register);
    }

    @Nonnull
    static BlockStateData get(@Nonnull final BlockState state) {
        BlockStateData profile = registry.get(state);
        return profile == null ? BlockStateData.DEFAULT : profile;
    }

    @Nonnull
    private static BlockStateData getOrCreateProfile(@Nonnull final BlockStateMatcher info) {
        BlockStateData profile = registry.get(info);
        if (profile == null) {
            profile = new BlockStateData();
            registry.put(info, profile);
        }

        return profile;
    }

    private static void register(@Nonnull final BlockConfig entry) {
        if (entry.blocks.isEmpty())
            return;

        for (final String blockName : entry.blocks) {
            final Collection<BlockStateMatcher> list = expand(blockName);

            for (final BlockStateMatcher blockInfo : list) {
                final BlockStateData blockData = getOrCreateProfile(blockInfo);

                // Reset of a block clears all registry
                if (entry.soundReset != null && entry.soundReset)
                    blockData.clearSounds();
                if (entry.effectReset != null && entry.effectReset)
                    blockData.clearEffects();

                if (entry.chance != null)
                    blockData.setChance(entry.chance);

                for (final AcousticConfig sr : entry.acoustics) {
                    if (sr.acoustic != null) {
                        final Identifier res = Library.resolveResource(Environs.MOD_ID, sr.acoustic);
                        final IAcoustic acoustic = Library.resolve(res, sr.acoustic);
                        final int weight = sr.weight;
                        final WeightedAcousticEntry acousticEntry = new WeightedAcousticEntry(acoustic, sr.conditions, weight);
                        blockData.addSound(acousticEntry);
                    }
                }

                for (final EffectConfig e : entry.effects) {
                    if (StringUtils.isEmpty(e.effect))
                        continue;
                    final BlockEffectType type = BlockEffectType.get(e.effect);
                    if (type == BlockEffectType.UNKNOWN) {
                        LOGGER.warn("Unknown block effect type in configuration: [%s]", e.effect);
                    } else if (type.isEnabled()) {
                        final int chance = e.chance != null ? e.chance : 100;
                        type.getInstance(chance).ifPresent(
                                be -> {
                                    if (e.conditions != null)
                                        be.setConditions(e.conditions);
                                    blockData.addEffect(be);
                                });
                    }
                }
            }
        }
    }

    private static Collection<BlockStateMatcher> expand(@Nonnull final String blockName) {
        if (blockName.startsWith(TAG_SPECIFIER)) {
            final String tagName = blockName.substring(1);
            final Identifier tagId = Identifier.tryParse(tagName);
            final Optional<HolderSet.Named<Block>> tag = tagId == null ? Optional.empty() : BuiltInRegistries.BLOCK.get(TagKey.create(Registries.BLOCK, tagId));
            if (tag.isPresent()) {
                return tag.get().stream().map(h -> BlockStateMatcher.create(h.value())).filter(m -> !m.isEmpty()).collect(Collectors.toList());
            }
            LOGGER.debug("Unknown block tag '%s' in Block specification", tagName);
        } else {
            final BlockStateMatcher matcher = BlockStateMatcher.create(blockName);
            if (!matcher.isEmpty()) {
                return ImmutableList.of(matcher);
            }
            LOGGER.debug("Unknown block name '%s' in Block Specification", blockName);
        }
        return ImmutableList.of();
    }

    private static class BlockStateLibraryService implements IModuleService
    {
        private static final Type blockType = TypeToken.getParameterized(List.class, BlockConfig.class).getType();

        static {
            Validators.registerValidator(blockType, new ListValidator<BlockConfig>());
        }

        @Override
        public String name() {
            return "BlockStateLibrary";
        }

        @Override
        public void start() {
            final Collection<IResourceAccessor> configs = ResourceUtils.findConfigs(DynamicSurroundings.MOD_ID, DynamicSurroundings.DATA_PATH, "blocks.json");

            IResourceAccessor.process(configs, accessor -> initFromConfig(accessor.as(blockType)));

            ForgeUtils.getBlockStates().forEach(BlockStateUtil::getData);
            ForgeUtils.getBlockStates().stream().map(BlockStateUtil::getData).forEach(BlockStateData::trim);

            BlockStateUtil.setData(Blocks.AIR.defaultBlockState(), BlockStateData.DEFAULT);
            BlockStateUtil.setData(Blocks.CAVE_AIR.defaultBlockState(), BlockStateData.DEFAULT);
            BlockStateUtil.setData(Blocks.VOID_AIR.defaultBlockState(), BlockStateData.DEFAULT);
        }

        @Override
        public void log() {
            LOGGER.info("%d block states processed, %d registry entries", ForgeUtils.getBlockStates().size(), registry.size());
        }

        @Override
        public void stop() {
            registry.clear();
            ForgeUtils.getBlockStates().forEach(state -> BlockStateUtil.setData(state, null));
        }
    }
}
