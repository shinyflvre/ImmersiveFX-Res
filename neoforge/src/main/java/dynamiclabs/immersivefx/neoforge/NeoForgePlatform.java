/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.neoforge;

import com.mojang.blaze3d.platform.InputConstants;
import dynamiclabs.immersivefx.ImmersiveFX;
import dynamiclabs.immersivefx.platform.IPlatform;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.VersionChecker;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import net.neoforged.neoforgespi.language.IModInfo;

public final class NeoForgePlatform implements IPlatform {

    private static volatile ModContainer container;

    public NeoForgePlatform() {
    }

    static void setContainer(@Nonnull final ModContainer modContainer) {
        container = Objects.requireNonNull(modContainer);
    }

    @Nonnull
    private static ModContainer container() {
        ModContainer result = container;
        if (result == null)
            result = ModList.get().getModContainerById(ImmersiveFX.MOD_ID).orElseThrow();
        return result;
    }

    @Override
    @Nonnull
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(@Nonnull final String modId) {
        final ModList list = ModList.get();
        return list != null && list.isLoaded(modId);
    }

    @Override
    @Nonnull
    public List<String> getModIds() {
        return ModList.get().getModFiles()
                .stream()
                .flatMap(e -> e.getMods().stream())
                .map(IModInfo::getModId)
                .distinct()
                .toList();
    }

    @Override
    @Nonnull
    public Optional<ModInfo> getModInfo(@Nonnull final String modId) {
        return modInfo(modId).map(i -> new ModInfo(i.getModId(), i.getDisplayName(), i.getVersion().toString()));
    }

    @Nonnull
    private static Optional<IModInfo> modInfo(@Nonnull final String modId) {
        return ModList.get().getModContainerById(modId).map(ModContainer::getModInfo);
    }

    @Override
    @Nullable
    public String getLoaderVersion() {
        return modInfo("neoforge").map(i -> i.getVersion().toString()).orElse(null);
    }

    @Override
    @Nonnull
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    @Nonnull
    public Optional<UpdateStatus> getOutdatedStatus(@Nonnull final String modId) {
        final Optional<IModInfo> mod = modInfo(modId);
        if (mod.isEmpty())
            return Optional.empty();
        final VersionChecker.CheckResult result = VersionChecker.getResult(mod.get());
        if (result == null || result.status() != VersionChecker.Status.OUTDATED)
            return Optional.empty();
        return Optional.of(new UpdateStatus(result.target() != null ? result.target().toString() : null, result.url()));
    }

    @Override
    public void registerClientConfig(@Nonnull final ModConfigSpec spec, @Nonnull final String fileName) {
        container().registerConfig(ModConfig.Type.CLIENT, spec, fileName);
    }

    @Override
    public void setKeyModifierAndCode(@Nonnull final KeyMapping key, @Nonnull final KeyModifier modifier, @Nonnull final InputConstants.Key code) {
        final net.neoforged.neoforge.client.settings.KeyModifier neo = switch (modifier) {
            case NONE -> net.neoforged.neoforge.client.settings.KeyModifier.NONE;
            case CONTROL -> net.neoforged.neoforge.client.settings.KeyModifier.CONTROL;
            case SHIFT -> net.neoforged.neoforge.client.settings.KeyModifier.SHIFT;
            case ALT -> net.neoforged.neoforge.client.settings.KeyModifier.ALT;
        };
        key.setKeyModifierAndCode(neo, code);
    }

    @Override
    public boolean isLivingOnLadder(@Nonnull final BlockState state, @Nonnull final Level level, @Nonnull final BlockPos pos, @Nonnull final LivingEntity entity) {
        return CommonHooks.isLivingOnLadder(state, level, pos, entity).isPresent();
    }

    @Override
    @Nullable
    public String getBlockAppearanceApi() {
        return "NeoForge IBlockExtension#getAppearance";
    }

    @Override
    public boolean hasCustomAppearance(@Nonnull final Block block) {
        try {
            return block.getClass().getMethod("getAppearance", BlockState.class, BlockAndLightGetter.class, BlockPos.class,
                    Direction.class, BlockState.class, BlockPos.class).getDeclaringClass() != IBlockExtension.class;
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
        return instance.getStream(soundBuffers, sound, looping);
    }

    @Override
    @Nonnull
    public Climate getClimateSettings(@Nonnull final Biome biome) {
        final Biome.ClimateSettings c = biome.getModifiedClimateSettings();
        return new Climate(c.hasPrecipitation(), c.temperature(), c.temperatureModifier(), c.downfall());
    }

    @Override
    @Nonnull
    public SoundType getSoundType(@Nonnull final BlockState state, @Nonnull final LevelReader level, @Nonnull final BlockPos pos, @Nullable final Entity entity) {
        return state.getSoundType(level, pos, entity);
    }

    @Override
    public boolean collisionExtendsVertically(@Nonnull final BlockState state, @Nonnull final BlockGetter level, @Nonnull final BlockPos pos, @Nonnull final Entity entity) {
        return state.collisionExtendsVertically(level, pos, entity);
    }

    @Override
    @Nonnull
    public ItemStack getCloneItemStack(@Nonnull final BlockState state, @Nonnull final LevelReader level, @Nonnull final BlockPos pos, @Nullable final Player player) {
        return state.getBlock().getCloneItemStack(level, pos, state, false, player);
    }

    @Override
    @Nonnull
    public ItemAttributeModifiers getAttributeModifiers(@Nonnull final ItemStack stack) {
        return stack.getAttributeModifiers();
    }
}
