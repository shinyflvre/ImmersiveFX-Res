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

package dynamiclabs.immersivefx.sndctrl.library;

import com.google.common.collect.ImmutableMap;
import com.google.gson.annotations.SerializedName;
import dynamiclabs.immersivefx.dsurround.DynamicSurroundings;
import dynamiclabs.immersivefx.lib.blockstate.BlockStateMatcherMap;
import dynamiclabs.immersivefx.lib.fml.ForgeUtils;
import dynamiclabs.immersivefx.lib.logging.IModLog;
import dynamiclabs.immersivefx.lib.math.MathStuff;
import dynamiclabs.immersivefx.lib.resource.IResourceAccessor;
import dynamiclabs.immersivefx.lib.resource.ResourceUtils;
import dynamiclabs.immersivefx.lib.service.IModuleService;
import dynamiclabs.immersivefx.lib.service.ModuleServiceManager;
import dynamiclabs.immersivefx.sndctrl.SoundControl;
import dynamiclabs.immersivefx.sndctrl.misc.IMixinAudioEffectData;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.Identifier;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public final class AudioEffectLibrary {

    private static final String MATERIAL_PREFIX = "+";
    private static final String TAG_PREFIX = "#";
    private static final float DEFAULT_OPAQUE_OCCLUSION = 0.5F;
    private static final float DEFAULT_TRANSLUCENT_OCCLUSION = 0.15F;
    private static final float DEFAULT_REFLECTION = 0.4F;

    private static final IModLog LOGGER = SoundControl.LOGGER.createChild(AudioEffectLibrary.class);

    // Occlusion data
    private static final Object2FloatOpenHashMap<LegacyMaterial> materialOcclusion = new Object2FloatOpenHashMap<>();
    private static final BlockStateMatcherMap<Float> blockStateOcclusionMap = new BlockStateMatcherMap<>();

    // Reflection data
    private static final Object2FloatOpenHashMap<LegacyMaterial> materialReflect = new Object2FloatOpenHashMap<>();
    private static final BlockStateMatcherMap<Float> blockStateReflectMap = new BlockStateMatcherMap<>();

    // Lowpass Data
    private static final Object2FloatOpenHashMap<Identifier> fluidCoefficient = new Object2FloatOpenHashMap<>();

    public static void initialize() {
        // Currently does nothing.  Called during startup which triggers the class init.
        ModuleServiceManager.instance().add(new AudioEffectLibraryService());
    }

    /**
     * Gets the occlusion value for the given BlockState.  This value determines how effective sound is transmitted
     * through the block.  Low values indicate more efficient transmission, and higher values indicate more absorption.
     *
     * @param state BlockState to obtain the occlusion coefficient.
     * @return The coeeficient that has been configured, or default of 1 for opaque blocks, and 0.15F for non-solid.
     */
    public static float getOcclusion(@Nonnull final BlockState state) {
        return resolve(state).occlusion;
    }

    /**
     * Gets the reflectivity value for the given BlockState.  Low values indicates sounds are absorbed by the block,
     * whereas high values indicate the sounds bounce off the block.
     *
     * @param state BlockState to obtain the reflectivity coefficient.
     * @return The coefficient that has been configured, or the default value of 0.5 if it hasn't
     */
    public static float getReflectivity(@Nonnull final BlockState state) {
        return resolve(state).reflectivity;
    }

    private static EffectData resolve(@Nonnull final BlockState state) {
        IMixinAudioEffectData accessor = (IMixinAudioEffectData)state;
        EffectData data = accessor.getData();
        if (data == null) {
            final float o = resolveOcclusion(state);
            final float r = resolveReflectivity(state);
            data = new EffectData(o, r);
            accessor.setData(data);
        }
        return data;
    }

    private static float resolveReflectivity(@Nonnull final BlockState state) {
        Float result = blockStateReflectMap.get(state);
        if (result == null || result < 0)
            result = materialReflect.getFloat(LegacyMaterial.of(state));
        return result < 0 ? DEFAULT_REFLECTION : result;
    }

    private static float resolveOcclusion(@Nonnull final BlockState state) {
        Float result = blockStateOcclusionMap.get(state);
        if (result == null || result < 0) {
            final LegacyMaterial material = LegacyMaterial.of(state);
            result = materialOcclusion.getFloat(material);
            if (result < 0) {
                result = material.isOpaque() ? DEFAULT_OPAQUE_OCCLUSION : DEFAULT_TRANSLUCENT_OCCLUSION;
            }
        }

        return result;
    }

    /**
     * Gets the low pass filter effect information for the specified resource.  Normally used for water but can
     * apply to any resource.  For example, water and lava will have a filter effect, and those would be called
     * "minecraft:water", and "minecraft:lava".
     *
     * @param res Resource to lookup
     * @return Coefficient for dampening sounds for the specified resource
     */
    public static float getFluidCoeffcient(@Nonnull final Identifier res) {
        return fluidCoefficient.getFloat(res);
    }

    private static final Map<String, String> FORGE_TAG_RENAMES = ImmutableMap.<String, String>builder()
            .put("cobblestone", "cobblestones")
            .put("gravel", "gravels")
            .put("netherrack", "netherracks")
            .put("sand", "sands")
            .put("sandstone", "sandstone/blocks")
            .put("glass", "glass_blocks")
            .put("stone", "stones")
            .put("ores", "ores")
            .build();

    private static HolderSet.Named<Block> resolveBlockTag(@Nonnull final String tagName) {
        final Identifier id = Identifier.tryParse(tagName);
        if (id == null)
            return null;
        Optional<HolderSet.Named<Block>> result = BuiltInRegistries.BLOCK.get(TagKey.create(Registries.BLOCK, id));
        if (result.isEmpty() && "forge".equals(id.getNamespace())) {
            final String path = FORGE_TAG_RENAMES.getOrDefault(id.getPath(), id.getPath());
            result = BuiltInRegistries.BLOCK.get(TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", path)));
            if (result.isEmpty())
                result = BuiltInRegistries.BLOCK.get(TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", path + "s")));
        }
        return result.orElse(null);
    }

    private static void processOcclusions(@Nonnull final EffectOptions options) {

        for (final Map.Entry<String, Float> kvp : options.occlusions.entrySet()) {
            final String name = kvp.getKey();
            if (name.startsWith(MATERIAL_PREFIX)) {
                // Material entry
                final String materialName = name.substring(1);
                final LegacyMaterial mat = LegacyMaterial.byName(materialName);
                if (mat != null) {
                    materialOcclusion.put(mat, kvp.getValue().floatValue());
                } else {
                    LOGGER.debug("Unrecognized material name: %s", materialName);
                }
            } else if (name.startsWith(TAG_PREFIX)) {
                // Tag entry
                final String tagName = name.substring(1);
                final HolderSet.Named<Block> tag = resolveBlockTag(tagName);
                if (tag != null) {
                    for (final Holder<Block> block : tag) {
                        for (final BlockState state : block.value().getStateDefinition().getPossibleStates())
                            blockStateOcclusionMap.put(state, kvp.getValue());
                    }
                } else {
                    LOGGER.debug("Unrecognized block tag: %s", tagName);
                }
            } else {
                try {
                    blockStateOcclusionMap.put(name, kvp.getValue());
                } catch (@Nonnull final Throwable t) {
                    LOGGER.error(t, "Unable to insert entry into block state map: %s", t.getMessage());
                }
            }
        }

        // Air never occludes
        blockStateOcclusionMap.put(Blocks.AIR, 0F);
        blockStateOcclusionMap.put(Blocks.CAVE_AIR, 0F);
        blockStateOcclusionMap.put(Blocks.VOID_AIR, 0F);
    }

    private static void processReflectivity(@Nonnull final EffectOptions options) {

        for (final Map.Entry<String, Float> kvp : options.reflectivity.entrySet()) {
            final String name = kvp.getKey();
            final float val = MathStuff.clamp(kvp.getValue(), 0, 2F);
            if (name.startsWith(MATERIAL_PREFIX)) {
                // Material entry
                final String materialName = name.substring(1);
                final LegacyMaterial mat = LegacyMaterial.byName(materialName);
                if (mat != null) {
                    materialReflect.put(mat, val);
                } else {
                    LOGGER.debug("Unrecognized material name: %s", materialName);
                }
            } else if (name.startsWith(TAG_PREFIX)) {
                // Tag entry
                final String tagName = name.substring(1);
                final HolderSet.Named<Block> tag = resolveBlockTag(tagName);
                if (tag != null) {
                    for (final Holder<Block> block : tag) {
                        for (final BlockState state : block.value().getStateDefinition().getPossibleStates())
                            blockStateReflectMap.put(state, val);
                    }
                } else {
                    LOGGER.debug("Unrecognized block tag: %s", tagName);
                }
            } else {
                try {
                    blockStateReflectMap.put(name, val);
                } catch (@Nonnull final Throwable t) {
                    LOGGER.error(t, "Unable to insert entry into block state map: %s", t.getMessage());
                }
            }
        }

        // Air never reflects
        blockStateReflectMap.put(Blocks.AIR, 0F);
        blockStateReflectMap.put(Blocks.CAVE_AIR, 0F);
        blockStateReflectMap.put(Blocks.VOID_AIR, 0F);
    }

    private static void processLowpass(@Nonnull final EffectOptions options) {
        for (final Map.Entry<String, Float> kvp : options.fluid.entrySet()) {
            fluidCoefficient.put(Identifier.parse(kvp.getKey()), kvp.getValue().floatValue());
        }
    }

    public static class EffectOptions {
        @SerializedName("occlusions")
        public Map<String, Float> occlusions = ImmutableMap.of();
        @SerializedName("reflectivity")
        public Map<String, Float> reflectivity = ImmutableMap.of();
        @SerializedName("fluid")
        public Map<String, Float> fluid = ImmutableMap.of();
    }

    public static class EffectData {

        public final float occlusion;
        public final float reflectivity;

        public EffectData(final float o, final float r) {
            this.occlusion = o;
            this.reflectivity = r;
        }
    }

    private static class AudioEffectLibraryService implements IModuleService {

        @Override
        public String name() {
            return "AudioEffectLibrary";
        }

        @Override
        public void start() {
            // Occlusion setup
            materialOcclusion.defaultReturnValue(-1F);
            for (final LegacyMaterial mat : LegacyMaterial.values())
                materialOcclusion.put(mat, mat.isOpaque() ? DEFAULT_OPAQUE_OCCLUSION : DEFAULT_TRANSLUCENT_OCCLUSION);
            blockStateOcclusionMap.setDefaultValue(() -> -1F);

            // Reflection setup
            materialReflect.defaultReturnValue(-1F);
            blockStateReflectMap.setDefaultValue(() -> -1F);

            // Default lowpass state is essentially normal air
            fluidCoefficient.defaultReturnValue(0);
            fluidCoefficient.put(Identifier.parse("sndctrl:default"), 0);

            final Collection<IResourceAccessor> configs = ResourceUtils.findConfigs(DynamicSurroundings.MOD_ID, DynamicSurroundings.DATA_PATH, "effects.json");

            IResourceAccessor.process(configs, accessor -> {
                final EffectOptions cfg = accessor.as(EffectOptions.class);
                // Occlusions values determine how effectively a sound gets blocked from the listener
                processOcclusions(cfg);
                // Refelectivity governs how much reverb will be generated when a sound bounces off it's surface
                processReflectivity(cfg);
                // Lowpass filter gets applied when a player head is inside the block - think fluids.
                processLowpass(cfg);
            });
        }

        @Override
        public void stop() {
            materialOcclusion.clear();
            materialReflect.clear();
            blockStateOcclusionMap.clear();
            blockStateReflectMap.clear();
            fluidCoefficient.clear();

            // Clear out cached data
            ForgeUtils.getBlockStates().forEach(state -> {
                IMixinAudioEffectData accessor = (IMixinAudioEffectData) state;
                accessor.setData(null);
            });
        }

        @Override
        public void reload() {
            stop();
            start();
        }
    }

}
