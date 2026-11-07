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

package dynamiclabs.immersivefx.environs.library;

import net.minecraft.util.datafix.fixes.BiomeFix;
import net.minecraft.util.datafix.fixes.CavesAndCliffsRenames;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LegacyBiomeIds {

    private static final Pattern QUOTED_ID = Pattern.compile("(['\"])(minecraft:[a-z0-9_./-]+)\1");
    private static final Map<String, String> RENAMES = new HashMap<>();
    private static final Map<String, String> CACHE = new ConcurrentHashMap<>();

    static {
        for (final Map.Entry<String, String> e : BiomeFix.BIOMES.entrySet())
            RENAMES.put(e.getKey(), e.getValue());
        RENAMES.put("minecraft:deep_warm_ocean", "minecraft:warm_ocean");
        for (final Map.Entry<String, String> e : RENAMES.entrySet())
            e.setValue(CavesAndCliffsRenames.RENAMES.getOrDefault(e.getValue(), e.getValue()));
        RENAMES.putAll(CavesAndCliffsRenames.RENAMES);
    }

    private LegacyBiomeIds() {
    }

    @Nonnull
    public static String map(@Nonnull final String id) {
        return RENAMES.getOrDefault(id, id);
    }

    @Nonnull
    public static String rewrite(@Nonnull final String conditions) {
        if (conditions.indexOf(':') < 0)
            return conditions;
        return CACHE.computeIfAbsent(conditions, c -> {
            final Matcher m = QUOTED_ID.matcher(c);
            final StringBuilder sb = new StringBuilder();
            while (m.find()) {
                final String q = m.group(1);
                m.appendReplacement(sb, Matcher.quoteReplacement(q + map(m.group(2)) + q));
            }
            m.appendTail(sb);
            return sb.toString();
        });
    }
}
