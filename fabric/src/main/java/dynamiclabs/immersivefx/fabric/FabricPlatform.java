/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import dynamiclabs.immersivefx.ImmersiveFX;
import dynamiclabs.immersivefx.fabric.mixins.KeyMappingAccessor;
import dynamiclabs.immersivefx.platform.IPlatform;
import dynamiclabs.immersivefx.platform.StreamingSoundInstance;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.block.v1.FabricBlock;
import net.fabricmc.fabric.api.client.sound.v1.FabricSoundInstance;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class FabricPlatform implements IPlatform {

    public FabricPlatform() {
    }

    @Override
    @Nonnull
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(@Nonnull final String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    @Nonnull
    public List<String> getModIds() {
        return FabricLoader.getInstance().getAllMods()
                .stream()
                .map(c -> c.getMetadata().getId())
                .distinct()
                .toList();
    }

    @Override
    @Nonnull
    public Optional<ModInfo> getModInfo(@Nonnull final String modId) {
        return FabricLoader.getInstance().getModContainer(modId)
                .map(ModContainer::getMetadata)
                .map(m -> new ModInfo(m.getId(), m.getName(), m.getVersion().getFriendlyString()));
    }

    @Override
    @Nullable
    public String getLoaderVersion() {
        return getModInfo("fabricloader").map(ModInfo::version).orElse(null);
    }

    @Override
    @Nonnull
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    @Nonnull
    public Optional<UpdateStatus> getOutdatedStatus(@Nonnull final String modId) {
        return Optional.empty();
    }

    @Override
    public void registerClientConfig(@Nonnull final ModConfigSpec spec, @Nonnull final String fileName) {
        ConfigRegistry.INSTANCE.register(ImmersiveFX.MOD_ID, ModConfig.Type.CLIENT, spec, fileName);
    }

    @Override
    public void setKeyModifierAndCode(@Nonnull final KeyMapping key, @Nonnull final KeyModifier modifier, @Nonnull final InputConstants.Key code) {
        ((KeyMappingAccessor) key).ifx$setDefaultKey(code);
        key.setKey(code);
        FabricKeyModifiers.set(key, modifier);
        KeyMapping.resetMapping();
    }

    @Override
    public boolean isLivingOnLadder(@Nonnull final BlockState state, @Nonnull final Level level, @Nonnull final BlockPos pos, @Nonnull final LivingEntity entity) {
        if (entity instanceof Player && entity.isSpectator())
            return false;
        if (state.is(BlockTags.CLIMBABLE))
            return true;
        if (state.getBlock() instanceof TrapDoorBlock && state.getValue(TrapDoorBlock.OPEN)) {
            final BlockState below = level.getBlockState(pos.below());
            return below.is(Blocks.LADDER) && below.getValue(LadderBlock.FACING) == state.getValue(TrapDoorBlock.FACING);
        }
        return false;
    }

    @Override
    @Nullable
    public String getBlockAppearanceApi() {
        return "Fabric API FabricBlock#getAppearance";
    }

    @Override
    public boolean hasCustomAppearance(@Nonnull final Block block) {
        try {
            return block.getClass().getMethod("getAppearance", BlockState.class, BlockAndLightGetter.class, BlockPos.class,
                    Direction.class, BlockState.class, BlockPos.class).getDeclaringClass() != FabricBlock.class;
        } catch (@Nonnull final Throwable t) {
            return false;
        }
    }

    @Override
    @Nonnull
    public BlockState getAppearance(@Nonnull final BlockState state, @Nonnull final BlockAndLightGetter level, @Nonnull final BlockPos pos, @Nonnull final Direction side) {
        return state.getAppearance(level, pos, side, null, null);
    }

    @Override
    @Nonnull
    public CompletableFuture<AudioStream> getStream(@Nonnull final SoundInstance instance, @Nonnull final SoundBufferLibrary soundBuffers, @Nonnull final Sound sound, final boolean looping) {
        if (instance instanceof StreamingSoundInstance streaming)
            return streaming.getStream(soundBuffers, sound, looping);
        @SuppressWarnings("unchecked") final CompletableFuture<AudioStream> stream =
                (CompletableFuture<AudioStream>) ((FabricSoundInstance) instance).getAudioStream(soundBuffers, sound.getPath(), looping);
        return stream;
    }

    @Override
    @Nonnull
    public Climate getClimateSettings(@Nonnull final Biome biome) {
        return ClimateAccess.get(biome);
    }

    @Override
    @Nonnull
    public SoundType getSoundType(@Nonnull final BlockState state, @Nonnull final LevelReader level, @Nonnull final BlockPos pos, @Nullable final Entity entity) {
        return state.getSoundType();
    }

    @Override
    public boolean collisionExtendsVertically(@Nonnull final BlockState state, @Nonnull final BlockGetter level, @Nonnull final BlockPos pos, @Nonnull final Entity entity) {
        return state.is(BlockTags.FENCES) || state.is(BlockTags.WALLS) || state.getBlock() instanceof FenceGateBlock;
    }

    @Override
    @Nonnull
    public ItemStack getCloneItemStack(@Nonnull final BlockState state, @Nonnull final LevelReader level, @Nonnull final BlockPos pos, @Nullable final Player player) {
        return state.getCloneItemStack(level, pos, false);
    }

    @Override
    @Nonnull
    public ItemAttributeModifiers getAttributeModifiers(@Nonnull final ItemStack stack) {
        return stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
    }

    private static final class ClimateAccess {
        private static final MethodHandle SETTINGS;
        private static final MethodHandle HAS_PRECIPITATION;
        private static final MethodHandle TEMPERATURE;
        private static final MethodHandle TEMPERATURE_MODIFIER;
        private static final MethodHandle DOWNFALL;

        static {
            MethodHandle settings = null, precipitation = null, temperature = null, modifier = null, downfall = null;
            try {
                final MethodHandles.Lookup lookup = MethodHandles.lookup();
                final Field field = Biome.class.getDeclaredField("climateSettings");
                field.setAccessible(true);
                settings = lookup.unreflectGetter(field);
                final Class<?> record = field.getType();
                precipitation = accessor(lookup, record, "hasPrecipitation");
                temperature = accessor(lookup, record, "temperature");
                modifier = accessor(lookup, record, "temperatureModifier");
                downfall = accessor(lookup, record, "downfall");
            } catch (@Nonnull final Throwable t) {
                ImmersiveFXFabric.LOGGER.error("Cannot access Biome#climateSettings, using the public biome getters", t);
                settings = null;
            }
            SETTINGS = settings;
            HAS_PRECIPITATION = precipitation;
            TEMPERATURE = temperature;
            TEMPERATURE_MODIFIER = modifier;
            DOWNFALL = downfall;
        }

        private static MethodHandle accessor(@Nonnull final MethodHandles.Lookup lookup, @Nonnull final Class<?> record, @Nonnull final String name) throws ReflectiveOperationException {
            final Method method = record.getDeclaredMethod(name);
            method.setAccessible(true);
            return lookup.unreflect(method);
        }

        @Nonnull
        static Climate get(@Nonnull final Biome biome) {
            if (SETTINGS != null) {
                try {
                    final Object c = SETTINGS.invoke(biome);
                    return new Climate((boolean) HAS_PRECIPITATION.invoke(c), (float) TEMPERATURE.invoke(c),
                            (Biome.TemperatureModifier) TEMPERATURE_MODIFIER.invoke(c), (float) DOWNFALL.invoke(c));
                } catch (@Nonnull final Throwable ignored) {
                }
            }
            return new Climate(biome.hasPrecipitation(), biome.getBaseTemperature(), Biome.TemperatureModifier.NONE, 0.5F);
        }
    }
}
