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

package dynamiclabs.immersivefx.mobeffects.library;

import dynamiclabs.immersivefx.ImmersiveFX;
import com.google.gson.reflect.TypeToken;
import dynamiclabs.immersivefx.dsurround.DynamicSurroundings;
import dynamiclabs.immersivefx.lib.SoundTypeUtils;
import dynamiclabs.immersivefx.lib.blockstate.BlockStateMatcher;
import dynamiclabs.immersivefx.lib.blockstate.BlockStateMatcherMap;
import dynamiclabs.immersivefx.lib.blockstate.BlockStateParser;
import dynamiclabs.immersivefx.lib.logging.IModLog;
import dynamiclabs.immersivefx.lib.resource.IResourceAccessor;
import dynamiclabs.immersivefx.lib.resource.ResourceUtils;
import dynamiclabs.immersivefx.lib.service.IModuleService;
import dynamiclabs.immersivefx.lib.service.ModuleServiceManager;
import dynamiclabs.immersivefx.lib.validation.MapValidator;
import dynamiclabs.immersivefx.lib.validation.Validators;
import dynamiclabs.immersivefx.mobeffects.MobEffects;
import dynamiclabs.immersivefx.mobeffects.config.Config;
import dynamiclabs.immersivefx.mobeffects.footsteps.Generator;
import dynamiclabs.immersivefx.mobeffects.footsteps.GeneratorQP;
import dynamiclabs.immersivefx.mobeffects.footsteps.Substrate;
import dynamiclabs.immersivefx.mobeffects.footsteps.Variator;
import dynamiclabs.immersivefx.mobeffects.library.config.FootstepConfig;
import dynamiclabs.immersivefx.mobeffects.library.config.VariatorConfig;
import dynamiclabs.immersivefx.mobeffects.misc.IMixinFootstepData;
import dynamiclabs.immersivefx.sndctrl.api.acoustics.IAcoustic;
import dynamiclabs.immersivefx.sndctrl.api.acoustics.Library;
import dynamiclabs.immersivefx.sndctrl.audio.acoustic.NullAcoustic;
import dynamiclabs.immersivefx.sndctrl.events.BlockInspectionEvent;
import dynamiclabs.immersivefx.sndctrl.library.Primitives;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class FootstepLibrary {

    private static final String TEXT_FOOTSTEPS = ChatFormatting.DARK_PURPLE + "<Footsteps>";
    private static final Map<Substrate, BlockAcousticMap> substrateMap = new EnumMap<>(Substrate.class);
    private static final List<SoundType> FOOTPRINT_SOUND_PROFILE =
            Arrays.asList(
                    SoundType.SAND,
                    SoundType.GRAVEL,
                    SoundType.SLIME_BLOCK,
                    SoundType.SNOW
            );

    private static final Set<Block> FOOTPRINT_BLOCKS = new ReferenceOpenHashSet<>();
    private static final List<TagKey<Block>> FOOTPRINT_TAGS = new ArrayList<>();

    private static final Map<String, String> TAG_ALIASES = new Object2ObjectOpenHashMap<>();
    private static final Map<String, String> BLOCK_ALIASES = new Object2ObjectOpenHashMap<>();

    private static final BlockStateMatcherMap<Boolean> FOOTPRINT_STATES = new BlockStateMatcherMap<>();
    private static final Map<String, List<MacroEntry>> macros = new Object2ObjectOpenHashMap<>();
    private static final Map<String, Variator> variators = new Object2ObjectOpenHashMap<>();
    private static final IModLog LOGGER = MobEffects.LOGGER.createChild(FootstepLibrary.class);
    private static Variator defaultVariator;
    private static Variator childVariator;
    private static Variator playerVariator;
    private static Variator playerQuadrupedVariator;

    private static IAcoustic SPLASH;
    private static IAcoustic SWIM;
    private static IAcoustic WATERLOGGED;

    static {

        // Initialize our substrate maps
        for (final Substrate s : Substrate.values())
            substrateMap.put(s, new BlockAcousticMap());
        substrateMap.put(Substrate.NORMAL, new BlockAcousticMap(FootstepLibrary::primitiveResolver));

        FOOTPRINT_BLOCKS.add(Blocks.CLAY);
        FOOTPRINT_TAGS.add(BlockTags.DIRT);
        FOOTPRINT_TAGS.add(BlockTags.MUD);
        FOOTPRINT_BLOCKS.add(Blocks.PODZOL);
        FOOTPRINT_BLOCKS.add(Blocks.FARMLAND);
        FOOTPRINT_BLOCKS.add(Blocks.DIRT_PATH);
        FOOTPRINT_BLOCKS.add(Blocks.SOUL_SOIL);
        FOOTPRINT_TAGS.add(BlockTags.GRASS_BLOCKS);
        FOOTPRINT_BLOCKS.add(Blocks.GRASS_BLOCK);
        FOOTPRINT_BLOCKS.add(Blocks.MYCELIUM);
        FOOTPRINT_BLOCKS.add(Blocks.SPONGE);
        FOOTPRINT_BLOCKS.add(Blocks.WET_SPONGE);
        FOOTPRINT_TAGS.add(BlockTags.SAND);
        FOOTPRINT_TAGS.add(BlockTags.CONCRETE_POWDERS);
        FOOTPRINT_BLOCKS.add(Blocks.GRAVEL);
        FOOTPRINT_BLOCKS.add(Blocks.SUSPICIOUS_GRAVEL);
        FOOTPRINT_BLOCKS.add(Blocks.SOUL_SAND);
        FOOTPRINT_TAGS.add(BlockTags.SNOW);
        FOOTPRINT_BLOCKS.add(Blocks.CAKE);
        FOOTPRINT_TAGS.add(BlockTags.CANDLE_CAKES);

        TAG_ALIASES.put("minecraft:carpets", "minecraft:wool_carpets");
        TAG_ALIASES.put("minecraft:tall_flowers", "c:flowers/tall");
        TAG_ALIASES.put("forge:glass", "c:glass_blocks");
        TAG_ALIASES.put("forge:stained_glass", "c:glass_blocks");
        TAG_ALIASES.put("forge:stained_glass_panes", "c:glass_panes");
        TAG_ALIASES.put("forge:stone", "c:stones");
        TAG_ALIASES.put("forge:cobblestone", "c:cobblestones");
        TAG_ALIASES.put("forge:dirt", "minecraft:dirt");
        TAG_ALIASES.put("forge:sand", "c:sands");
        TAG_ALIASES.put("forge:sandstone", "c:sandstone/blocks");
        TAG_ALIASES.put("forge:gravel", "c:gravels");
        TAG_ALIASES.put("forge:obsidian", "c:obsidians");

        BLOCK_ALIASES.put("minecraft:grass", "minecraft:short_grass");
        BLOCK_ALIASES.put("minecraft:grass_path", "minecraft:dirt_path");
        BLOCK_ALIASES.put("minecraft:chain", "minecraft:iron_chain");

        final MacroEntry MESSY = new MacroEntry("messy", "messy_ground");
        final MacroEntry NOT_EMITTER = new MacroEntry(null, "not_emitter");

        List<MacroEntry> entries = new ArrayList<>(3);
        entries.add(NOT_EMITTER);
        entries.add(MESSY);
        entries.add(new MacroEntry("foliage", "straw"));
        macros.put("#sapling", entries);
        macros.put("#reed", entries);

        entries = new ArrayList<>(3);
        entries.add(new MacroEntry(null, "leaves"));
        entries.add(MESSY);
        entries.add(new MacroEntry("foliage", "brush"));
        macros.put("#plant", entries);

        entries = new ArrayList<>(3);
        entries.add(NOT_EMITTER);
        entries.add(MESSY);
        entries.add(new MacroEntry("foliage", "brush"));
        macros.put("#tallplant", entries);

        entries = new ArrayList<>(3);
        entries.add(new MacroEntry(null, "leaves"));
        entries.add(MESSY);
        entries.add(new MacroEntry("foliage", "brush_straw_transition"));
        macros.put("#bush", entries);

        entries = new ArrayList<>(3);
        entries.add(new MacroEntry(null, "not_emitter"));
        entries.add(MESSY);
        entries.add(new MacroEntry("foliage", "straw"));
        macros.put("#deadbush", entries);

        entries = new ArrayList<>(2);
        entries.add(NOT_EMITTER);
        entries.add(new MacroEntry("bigger", "bluntwood"));
        macros.put("#woodfence", entries);

        entries = new ArrayList<>(2);
        entries.add(NOT_EMITTER);
        entries.add(new MacroEntry("bigger", "brickstone"));
        macros.put("#stonewall", entries);

        entries = new ArrayList<>(2);
        entries.add(NOT_EMITTER);
        entries.add(new MacroEntry("bigger", "sandstone"));
        macros.put("#sandstonewall", entries);

        entries = new ArrayList<>(2);
        entries.add(NOT_EMITTER);
        entries.add(new MacroEntry("foliage", "rails"));
        macros.put("#rail", entries);

        entries = new ArrayList<>(3);
        entries.add(new MacroEntry(null, "straw"));
        entries.add(MESSY);
        entries.add(new MacroEntry("foliage", "straw"));
        macros.put("#vine", entries);

        entries = new ArrayList<>(3);
        entries.add(new MacroEntry(null, "nether_vines"));
        entries.add(MESSY);
        entries.add(new MacroEntry("foliage", "nether_vines"));
        macros.put("#nethervines", entries);

        entries = new ArrayList<>(2);
        entries.add(NOT_EMITTER);
        entries.add(new MacroEntry("carpet", "rug"));
        macros.put("#moss", entries);

        entries = new ArrayList<>(10);
        entries.add(NOT_EMITTER);
        entries.add(MESSY);
        entries.add(new MacroEntry("age", "0", "foliage", "not_emitter"));
        entries.add(new MacroEntry("age", "1", "foliage", "not_emitter"));
        entries.add(new MacroEntry("age", "2", "foliage", "brush"));
        entries.add(new MacroEntry("age", "3", "foliage", "brush"));
        entries.add(new MacroEntry("age", "4", "foliage", "brush_straw_transition"));
        entries.add(new MacroEntry("age", "5", "foliage", "brush_straw_transition"));
        entries.add(new MacroEntry("age", "6", "foliage", "straw"));
        entries.add(new MacroEntry("age", "7", "foliage", "straw"));
        macros.put("#wheat", entries);

        entries = new ArrayList<>(10);
        entries.add(NOT_EMITTER);
        entries.add(MESSY);
        entries.add(new MacroEntry("age", "0", "foliage", "not_emitter"));
        entries.add(new MacroEntry("age", "1", "foliage", "not_emitter"));
        entries.add(new MacroEntry("age", "2", "foliage", "not_emitter"));
        entries.add(new MacroEntry("age", "3", "foliage", "not_emitter"));
        entries.add(new MacroEntry("age", "4", "foliage", "brush"));
        entries.add(new MacroEntry("age", "5", "foliage", "brush"));
        entries.add(new MacroEntry("age", "6", "foliage", "brush"));
        entries.add(new MacroEntry("age", "7", "foliage", "brush"));
        macros.put("#crop", entries);

        entries = new ArrayList<>(6);
        entries.add(NOT_EMITTER);
        entries.add(MESSY);
        entries.add(new MacroEntry("age", "0", "foliage", "not_emitter"));
        entries.add(new MacroEntry("age", "1", "foliage", "not_emitter"));
        entries.add(new MacroEntry("age", "2", "foliage", "brush"));
        entries.add(new MacroEntry("age", "3", "foliage", "brush"));
        macros.put("#beets", entries);
    }

    private FootstepLibrary() {

    }

    static void initialize() {
        ModuleServiceManager.instance().add(new FootstepLibraryService());
    }

    static void initFromConfig(@Nonnull final FootstepConfig mod) {

        // Load up configured primitives.  These will overwrite existing ones.
        for (final Map.Entry<String, String> kvp : mod.primitives.entrySet()) {
            if (SoundTypeUtils.getSoundType(kvp.getKey()) == null) {
                LOGGER.warn("'%s' is not a known SoundType; adding and praying...", kvp.getKey());
            }
            final Identifier loc = Primitives.createFootstepResource(kvp.getKey());
            Library.resolve(loc, kvp.getValue(), true);
        }

        // Apply acoustics based on configured tagging
        for (final Map.Entry<String, String> kvp : mod.blockTags.entrySet()) {
            registerTag(kvp.getKey(), kvp.getValue());
        }

        // Now do the regular block stuff
        for (final Map.Entry<String, String> kvp : mod.footsteps.entrySet()) {
            register(kvp.getKey(), kvp.getValue());
        }

        // Register special blocks for footprints
        for (final String print : mod.footprints) {
            final BlockStateMatcher matcher = BlockStateMatcher.create(renameLegacyBlock(print));
            if (matcher != BlockStateMatcher.AIR)
                FOOTPRINT_STATES.put(matcher, true);
        }
    }

    @Nonnull
    private static Stream<BlockState> allBlockStates() {
        return StreamSupport.stream(Block.BLOCK_STATE_REGISTRY.spliterator(), false);
    }

    /**
     * This is pretty heavy - it will blow out all blockstates to get their values
     */
    public static Stream<String> dump() {
        return allBlockStates().map(state -> {
            try {
                final StringBuilder builder = new StringBuilder();
                builder.append(BlockStateMatcher.create(state).toString()).append(" -> [");
                IAcoustic[] cached = getCachedAcoustics(state);
                if (cached != null) {
                    for (final Substrate sub : Substrate.values()) {
                        IAcoustic acoustic = cached[sub.ordinal()];
                        builder.append(sub.name()).append("=");
                        if (acoustic != null)
                            builder.append(acoustic.toString());
                        else
                            builder.append("NOT SET");
                        builder.append(", ");
                    }
                } else {
                    builder.append("No acoustics defined");
                }
                builder.append("]");
                return builder.toString();
            } catch (@Nonnull final Throwable ignore) {
            }
            return "ERROR";
        }).sorted();
    }

    public static void onInspectionEvent(@Nonnull final BlockInspectionEvent evt) {
        evt.data.add(TEXT_FOOTSTEPS);
        collectData(evt.state, evt.data);
    }

    @Nonnull
    public static IAcoustic getRainSplashAcoustic() {
        if (SPLASH == null)
            SPLASH = Library.resolve(Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, "waterfine"));
        return SPLASH;
    }

    @Nonnull
    public static IAcoustic getSwimAcoustic() {
        if (SWIM == null)
            SWIM = Library.resolve(Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, "_swim"));
        return SWIM;
    }

    public static IAcoustic getWaterLoggedAcoustic() {
        if (WATERLOGGED == null)
            WATERLOGGED = Library.resolve(Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, "_waterlogged"));
        return WATERLOGGED;
    }

    @Nonnull
    private static Variator getVariator(@Nonnull final String varName) {
        return variators.getOrDefault(varName, defaultVariator);
    }

    @Nonnull
    public static IAcoustic getBlockAcoustics(@Nonnull final BlockState state) {
        return getBlockAcoustics(state, Substrate.NORMAL);
    }

    @Nonnull
    public static IAcoustic getBlockAcoustics(@Nonnull final BlockState state, @Nonnull final Substrate substrate) {
        // Walking an edge of a block can produce this
        if (state.isAir())
            return Constants.NOT_EMITTER;
        // Get our cached entries
        IAcoustic[] cached = ((IMixinFootstepData) state).getAcoustics();
        if (cached == null) {
            ((IMixinFootstepData) state).setAcoustics(cached = new IAcoustic[Substrate.values().length]);
        }
        IAcoustic result = cached[substrate.ordinal()];
        if (result == null) {
            result = cached[substrate.ordinal()] = substrateMap.get(substrate).getBlockAcoustics(state);
        }
        return result;
    }

    private static IAcoustic[] getCachedAcoustics(@Nonnull final BlockState state) {
        IAcoustic[] cached = ((IMixinFootstepData) state).getAcoustics();
        if (cached == null) {
            ((IMixinFootstepData) state).setAcoustics(cached = new IAcoustic[Substrate.values().length]);
        }
        for (final Substrate sub : Substrate.values()) {
            IAcoustic result = cached[sub.ordinal()];
            if (result == null) {
                cached[sub.ordinal()] = substrateMap.get(sub).getBlockAcoustics(state);
            }
        }
        return cached;
    }

    private static void put(@Nonnull final BlockStateMatcher info, @Nullable final String substrate,
                            @Nonnull final String acousticList) {

        final IAcoustic acoustics = Library.resolve(
                MobEffects.MOD_ID,
                acousticList,
                r -> {
                    if (r.getPath().equals("not_emitter"))
                        return Constants.NOT_EMITTER;
                    if (r.getPath().equals("messy_ground"))
                        return Constants.MESSY_GROUND;
                    return null;
                });

        substrateMap.get(Substrate.get(substrate)).put(info, acoustics);
    }

    @Nonnull
    private static String renameLegacyBlock(@Nonnull final String key) {
        if (key.startsWith("minecraft:cauldron[level="))
            return "minecraft:water_cauldron" + key.substring("minecraft:cauldron".length());
        int end = key.length();
        final int bracket = key.indexOf('[');
        if (bracket >= 0)
            end = bracket;
        final int plus = key.indexOf('+');
        if (plus >= 0 && plus < end)
            end = plus;
        final String id = key.substring(0, end);
        final String renamed = BLOCK_ALIASES.get(id);
        return renamed == null ? key : renamed + key.substring(end);
    }

    private static void register0(@Nonnull String key, @Nonnull final String acousticList) {

        key = renameLegacyBlock(key);
        final Optional<BlockStateParser.ParseResult> parseResult = BlockStateParser.parseBlockState(key);
        if (parseResult.isPresent()) {
            final BlockStateParser.ParseResult name = parseResult.get();
            final BlockStateMatcher matcher = BlockStateMatcher.create(name);
            if (matcher.isEmpty()) {
                LOGGER.warn("Unable to identify block state '%s'", key);
            } else {
                final String substrate = name.getExtras();
                put(matcher, substrate, acousticList);
            }
        } else {
            LOGGER.warn("Malformed key in blockMap '%s'", key);
        }
    }

    @Nullable
    private static HolderSet.Named<Block> findBlockTag(@Nonnull final String tagName) {
        final Identifier id = Identifier.tryParse(tagName);
        if (id == null)
            return null;
        final Optional<HolderSet.Named<Block>> tag = BuiltInRegistries.BLOCK.get(TagKey.create(Registries.BLOCK, id));
        if (tag.isPresent())
            return tag.get();

        String alias = TAG_ALIASES.get(tagName);
        if (alias == null && "forge".equals(id.getNamespace()))
            alias = "c:" + id.getPath();
        if (alias != null) {
            final Identifier aliasId = Identifier.tryParse(alias);
            if (aliasId != null) {
                LOGGER.debug("Block tag '%s' is known as '%s' now", tagName, alias);
                return BuiltInRegistries.BLOCK.get(TagKey.create(Registries.BLOCK, aliasId)).orElse(null);
            }
        }
        return null;
    }

    private static void registerTag(@Nonnull String tagName, @Nonnull final String acousticList) {
        String substrate = null;
        final int idx = tagName.indexOf('+');

        if (idx >= 0) {
            substrate = tagName.substring(idx + 1);
            tagName = tagName.substring(0, idx);
        }

        final HolderSet.Named<Block> blockTag = findBlockTag(tagName);
        if (blockTag != null) {
            if (blockTag.size() == 0) {
                LOGGER.debug("No blocks associated with tag '%s'", tagName);
            } else {
                for (final Holder<Block> b : blockTag) {
                    String blockName = BuiltInRegistries.BLOCK.getKey(b.value()).toString();
                    if (substrate != null)
                        blockName = blockName + "+" + substrate;
                    register(blockName, acousticList);
                }
            }
        } else {
            LOGGER.debug("Unable to identify block tag '%s'", tagName);
        }
    }

    private static void register(@Nonnull final String key, @Nonnull final String acousticList) {
        if (acousticList.startsWith("#")) {
            final List<MacroEntry> macro = macros.get(acousticList);
            if (macro != null) {
                macro.stream()
                        .map(m -> m.expand(key))
                        .forEach(t -> register0(t.getLeft(), t.getRight()));
            } else {
                LOGGER.debug("Unknown macro '%s'", acousticList);
            }
        } else {
            register0(key, acousticList);
        }
    }

    @Nonnull
    public static Generator createGenerator(@Nonnull final LivingEntity entity) {
        Variator var;
        if (entity.isBaby()) {
            var = childVariator;
        } else if (entity instanceof Player) {
            var = Config.CLIENT.footsteps.footstepsAsQuadruped.get() ? playerQuadrupedVariator : playerVariator;
        } else {
            var = getVariator(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString());
        }

        return var.QUADRUPED ? new GeneratorQP(var) : new Generator(var);
    }

    private static void collectData(@Nonnull final BlockState state, @Nonnull final List<String> data) {

        final int s = data.size();
        final IAcoustic temp = getBlockAcoustics(state);
        if (temp != Constants.EMPTY)
            data.add(temp.toString());

        for (final Map.Entry<Substrate, BlockAcousticMap> e : substrateMap.entrySet()) {
            final IAcoustic acoustics = e.getValue().getBlockAcoustics(state);
            if (acoustics != Constants.EMPTY)
                data.add(e.getKey() + ":" + acoustics.toString());
        }

        if (s == data.size()) {
            data.add("** NONE **");
        }
    }

    public static boolean isLiquid(@Nonnull final BlockState state) {
        return state.liquid() || state.is(Blocks.BUBBLE_COLUMN);
    }

    @SuppressWarnings("deprecation")
    public static boolean blocksMovement(@Nonnull final BlockState state) {
        return state.isSolid() && !state.is(Blocks.COBWEB) && !state.is(Blocks.BAMBOO_SAPLING);
    }

    @Nonnull
    private static IAcoustic primitiveResolver(@Nonnull final BlockState state) {
        // If the state does not block movement or is liquid, like grass and plants, then it is not an
        // emitter.  Otherwise we get strange effects when edge walking on blocks with a plant
        // to the side.
        if (!blocksMovement(state) || isLiquid(state))
            return Constants.NOT_EMITTER;
        final IAcoustic acoustic = Primitives.getFootstepAcoustic(state);
        return acoustic == NullAcoustic.INSTANCE ? Constants.NOT_EMITTER : acoustic;
    }

    private static boolean isFootprintBlock(@Nonnull final BlockState state) {
        if (FOOTPRINT_BLOCKS.contains(state.getBlock()))
            return true;
        for (final TagKey<Block> tag : FOOTPRINT_TAGS)
            if (state.is(tag))
                return true;
        return false;
    }

    @SuppressWarnings("deprecation")
    public static boolean hasFootprint(@Nonnull final BlockState state) {
        Boolean result = ((IMixinFootstepData) state).hasFootprint();
        if (result != null)
            return result;

        result = FOOTPRINT_STATES.get(state);
        if (result == null) {
            result = isFootprintBlock(state) || FOOTPRINT_SOUND_PROFILE.contains(state.getSoundType());
        }
        ((IMixinFootstepData) state).setHasFootprint(result);
        return result;
    }

    private static class MacroEntry {
        public final String propertyName;
        public final String propertyValue;
        public final String substrate;
        public final String value;

        public MacroEntry(@Nullable final String substrate, @Nonnull final String value) {
            this(null, null, substrate, value);
        }

        public MacroEntry(@Nullable final String propertyName, @Nullable final String propertyValue,
                          @Nullable final String substrate, @Nonnull final String value) {
            this.propertyName = propertyName;
            this.propertyValue = propertyValue;
            this.substrate = substrate;
            this.value = value;
        }

        @Nonnull
        public Pair<String, String> expand(@Nonnull final String base) {
            final StringBuilder builder = new StringBuilder();
            builder.append(base);
            if (this.propertyName != null) {
                builder.append('[');
                builder.append(this.propertyName).append('=').append(this.propertyValue);
                builder.append(']');
            }

            if (this.substrate != null) {
                builder.append('+').append(this.substrate);
            }

            return Pair.of(builder.toString(), this.value);
        }
    }

    private static class FootstepLibraryService implements IModuleService {

        private static final Type variatorType = TypeToken.getParameterized(Map.class, String.class, VariatorConfig.class).getType();

        static {
            Validators.registerValidator(variatorType, new MapValidator<String, VariatorConfig>());
        }

        @Override
        public String name() {
            return "FootstepLibrary";
        }

        @Override
        public void start() {

            Collection<IResourceAccessor> configs = ResourceUtils.findConfigs(DynamicSurroundings.MOD_ID, DynamicSurroundings.DATA_PATH, "variators.json");

            IResourceAccessor.process(configs, accessor -> {
                final Map<String, VariatorConfig> cfg = accessor.as(variatorType);
                for (final Map.Entry<String, VariatorConfig> kvp : cfg.entrySet()) {
                    variators.put(kvp.getKey(), new Variator(kvp.getValue()));
                }
            });

            defaultVariator = getVariator("default");
            childVariator = getVariator("child");
            playerVariator = getVariator(Config.CLIENT.footsteps.firstPersonFootstepCadence.get() ? "player_slow" : "player");
            playerQuadrupedVariator = getVariator(Config.CLIENT.footsteps.firstPersonFootstepCadence.get() ? "quadruped_slow" : "quadruped");

            configs = ResourceUtils.findConfigs(DynamicSurroundings.MOD_ID, DynamicSurroundings.DATA_PATH, "footsteps.json");

            IResourceAccessor.process(configs, accessor -> initFromConfig(accessor.as(FootstepConfig.class)));

            substrateMap.forEach((key, value) -> value.trim());
        }

        @Override
        public void log() {
            if (Config.CLIENT.logging.enableLogging.get()) {
                LOGGER.info("Registered Variators");
                LOGGER.info("====================");

                for (final String v : variators.keySet()) {
                    LOGGER.info(v);
                }
            }
        }

        @Override
        public void stop() {
            variators.clear();
            FOOTPRINT_STATES.clear();

            for (final BlockAcousticMap m : substrateMap.values())
                m.clear();

            allBlockStates().forEach(state -> {
                ((IMixinFootstepData) state).setAcoustics(null);
                ((IMixinFootstepData) state).resetFootprint();
            });
        }
    }
}
