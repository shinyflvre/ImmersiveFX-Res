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

package dynamiclabs.immersivefx.lib.tags;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class TagUtils {

    private TagUtils() {

    }

    private static RegistryAccess registries;

    public static void setTagManager(@Nonnull final RegistryAccess manager) {
        registries = manager;
    }

    public static void clearTagManager() {
        registries = null;
    }

    @Nullable
    public static HolderSet.Named<Block> getBlockTag(@Nonnull final String name) {
        final Identifier id = Identifier.tryParse(name);
        return id == null ? null : getBlockTag(id);
    }

    @Nullable
    public static HolderSet.Named<Block> getBlockTag(@Nonnull final Identifier res) {
        if (registries == null)
            return null;
        return BuiltInRegistries.BLOCK.get(TagKey.create(Registries.BLOCK, res)).orElse(null);
    }

    @Nullable
    public static Collection<Block> getBlockTagValues(@Nonnull final String name) {
        final HolderSet.Named<Block> tag = getBlockTag(name);
        if (tag == null)
            return null;
        return tag.stream().map(Holder::value).collect(Collectors.toList());
    }

    public static Stream<String> dumpBlockTags() {
        if (registries == null)
            return ImmutableList.<String>of().stream();

        final List<String> result = BuiltInRegistries.BLOCK.getTags().map(tag -> {
            final StringBuilder builder = new StringBuilder();
            builder.append(tag.key().location()).append(" -> ");
            final String text = tag.stream()
                    .map(h -> h.unwrapKey().map(k -> k.identifier().toString()).orElse("<unregistered>"))
                    .collect(Collectors.joining(","));
            builder.append(text);
            return builder.toString();
        }).sorted().collect(Collectors.toList());
        return result.stream();
    }
}
