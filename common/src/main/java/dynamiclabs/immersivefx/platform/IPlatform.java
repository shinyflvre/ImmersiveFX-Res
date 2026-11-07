/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.platform;

import com.mojang.blaze3d.platform.InputConstants;
import java.nio.file.Path;
import java.util.List;
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
import net.neoforged.neoforge.common.ModConfigSpec;

public interface IPlatform {

    @Nonnull
    String getPlatformName();

    boolean isModLoaded(@Nonnull String modId);

    @Nonnull
    List<String> getModIds();

    @Nonnull
    Optional<ModInfo> getModInfo(@Nonnull String modId);

    @Nullable
    String getLoaderVersion();

    @Nonnull
    Path getConfigDir();

    @Nonnull
    Optional<UpdateStatus> getOutdatedStatus(@Nonnull String modId);

    void registerClientConfig(@Nonnull ModConfigSpec spec, @Nonnull String fileName);

    void setKeyModifierAndCode(@Nonnull KeyMapping key, @Nonnull KeyModifier modifier, @Nonnull InputConstants.Key code);

    boolean isLivingOnLadder(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull LivingEntity entity);

    @Nullable
    String getBlockAppearanceApi();

    boolean hasCustomAppearance(@Nonnull Block block);

    @Nonnull
    BlockState getAppearance(@Nonnull BlockState state, @Nonnull BlockAndLightGetter level, @Nonnull BlockPos pos, @Nonnull Direction side);

    @Nonnull
    CompletableFuture<AudioStream> getStream(@Nonnull SoundInstance instance, @Nonnull SoundBufferLibrary soundBuffers, @Nonnull Sound sound, boolean looping);

    @Nonnull
    Climate getClimateSettings(@Nonnull Biome biome);

    @Nonnull
    SoundType getSoundType(@Nonnull BlockState state, @Nonnull LevelReader level, @Nonnull BlockPos pos, @Nullable Entity entity);

    boolean collisionExtendsVertically(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull Entity entity);

    @Nonnull
    ItemStack getCloneItemStack(@Nonnull BlockState state, @Nonnull LevelReader level, @Nonnull BlockPos pos, @Nullable Player player);

    @Nonnull
    ItemAttributeModifiers getAttributeModifiers(@Nonnull ItemStack stack);

    record Climate(boolean hasPrecipitation, float temperature, Biome.TemperatureModifier temperatureModifier, float downfall) {
    }

    record ModInfo(String modId, String displayName, String version) {
    }

    record UpdateStatus(@Nullable String targetVersion, @Nullable String url) {
    }

    enum KeyModifier {
        NONE,
        CONTROL,
        SHIFT,
        ALT
    }
}
