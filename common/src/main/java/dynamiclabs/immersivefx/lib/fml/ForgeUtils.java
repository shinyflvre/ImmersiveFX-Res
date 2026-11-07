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

package dynamiclabs.immersivefx.lib.fml;

import dynamiclabs.immersivefx.platform.Platform;
import dynamiclabs.immersivefx.platform.IPlatform;
import dynamiclabs.immersivefx.lib.GameUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class ForgeUtils {
    private ForgeUtils() {

    }

    @Nonnull
    public static Optional<IPlatform.ModInfo> getModInfo(@Nonnull final String modId) {
        return Platform.get().getModInfo(modId);
    }

    @Nonnull
    public static String getModDisplayName(@Nonnull final String modId) {
        if ("minecraft".equalsIgnoreCase(modId))
            return "Minecraft";
        return getModInfo(modId).map(IPlatform.ModInfo::displayName).orElse("UNKNOWN");
    }

    @Nonnull
    public static String getModDisplayName(@Nonnull final Identifier resource) {
        Objects.requireNonNull(resource);
        return getModDisplayName(resource.getNamespace());
    }

    @Nullable
    public static String getForgeVersion() {
        return Platform.get().getLoaderVersion();
    }

    @Nonnull
    public static List<String> getModIdList() {
        List<String> result = new ArrayList<>(Platform.get().getModIds());

        // Make sure minecraft is the first element.  This is done to ensure that any baseline configs are applied
        // first so that other configs can properly override.
        result.remove("minecraft");
        result.add(0, "minecraft");

        return result;
    }

    @Nonnull
    public static Collection<Pack> getEnabledResourcePacks() {
        return GameUtils.getMC().getResourcePackRepository().getSelectedPacks();
    }

    @Nonnull
    public static List<String> getResourcePackIdList() {
        return getLoadedResourcePacks()
                .stream()
                .flatMap(e -> e.getNamespaces(PackType.CLIENT_RESOURCES).stream())
                .collect(Collectors.toList());
    }

    @Nonnull
    public static List<PackResources> getLoadedResourcePacks() {
        return GameUtils.getMC().getResourceManager().listPacks().collect(Collectors.toList());
    }

    @Nullable
    public static RegistryAccess getClientRegistryAccess() {
        final Minecraft mc = GameUtils.getMC();
        if (mc.level != null)
            return mc.level.registryAccess();
        final ClientPacketListener connection = mc.getConnection();
        return connection != null ? connection.registryAccess() : null;
    }

    @Nonnull
    public static Optional<Registry<Biome>> getBiomeRegistry() {
        final RegistryAccess access = getClientRegistryAccess();
        return access == null ? Optional.empty() : access.lookup(Registries.BIOME);
    }

    @Nonnull
    public static Collection<Biome> getBiomes() {
        return getBiomeRegistry().map(r -> r.stream().collect(Collectors.toList())).orElse(Collections.emptyList());
    }

    @Nonnull
    public static Collection<Holder.Reference<Biome>> getBiomeHolders() {
        return getBiomeRegistry().map(r -> r.listElements().collect(Collectors.toList())).orElse(Collections.emptyList());
    }

    @Nonnull
    public static Collection<BlockState> getBlockStates() {
        return StreamSupport.stream(BuiltInRegistries.BLOCK.spliterator(), false)
                .map(block -> block.getStateDefinition().getPossibleStates())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public static boolean isModLoaded(@Nonnull final String mod) {
        if (dynamiclabs.immersivefx.ImmersiveFX.PART_IDS.contains(mod.toLowerCase()))
            return true;
        return Platform.get().isModLoaded(mod.toLowerCase());
    }
}
