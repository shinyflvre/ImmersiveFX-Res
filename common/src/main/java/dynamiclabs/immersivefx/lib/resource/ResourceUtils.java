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

package dynamiclabs.immersivefx.lib.resource;

import dynamiclabs.immersivefx.lib.Lib;
import dynamiclabs.immersivefx.lib.fml.ForgeUtils;
import dynamiclabs.immersivefx.lib.GameUtils;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.PackType;
import net.minecraft.resources.Identifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.*;

public final class ResourceUtils {
    private ResourceUtils() {

    }

    private static final String MANIFEST_FILE = "manifest.json";

    private static Collection<String> cachedNamespaces = null;

    /**
     * Clears any cached namespace information
     */
    public static void  clearCache() {
        cachedNamespaces = null;
    }

    /**
     * Scans the local disk as well as resource packs and JARs locating and creating accessors for the config file
     * in question.  Configs on disk have priority over resource packs and JARs, and 3rd party jars have priority
     * over the provided mod ID.  These resources are read from CLIENT resources (assets) rather than SERVER
     * resources (data).
     * @param modId Mod ID that is considered the least in priority
     * @param root Location on disk where external configs can be cached
     * @param config The config file that is of interest
     * @return A collection of resource accessors that match the config criteria
     */
    public static Collection<IResourceAccessor> findConfigs(@Nonnull final String modId, @Nonnull final File root, @Nonnull final String config) {

        final String specialConfigFolder = modId + "_configs";
        final String resourceContainer = specialConfigFolder + "/%s";
        final String parentModConfigs = modId + "/configs";

        final Map<Identifier, IResourceAccessor> locations = new HashMap<>();
        final Collection<PackResources> packs = ForgeUtils.getLoadedResourcePacks();

        // Cache the namespaces so we don't do discovery unnecessarily.
        final Collection<String> namespaceList = discoverNamespaces(resourceContainer);

        // Look for resources in resource packs.  Mod resources will be exposed by an internal Forge resource pack
        // so we do not need to scan JARs directly.  The collection returned is already sorted so that the first
        // entries in the collection are lower priority that those further in the collection.  The result is that
        // higher priority resource packs will replace data from lower priority packs if there is an overlap.
        for (final PackResources rp : packs) {
            final Set<String> namespaces = rp.getNamespaces(PackType.CLIENT_RESOURCES);
            for (final String mod : namespaceList) {
                if (namespaces.contains(mod)) {
                    final String container = String.format(resourceContainer, config);
                    final Identifier location = Identifier.fromNamespaceAndPath(mod, config);
                    IResourceAccessor accessor = IResourceAccessor.createPackResource(
                            rp,
                            location,
                            Identifier.fromNamespaceAndPath(mod, container));
                    if (accessor.exists()) {
                        // Need to make sure we use namespace:config as the location since it can be overridden
                        // by subsequent configs.
                        final Identifier loc = Identifier.fromNamespaceAndPath(mod, config);
                        locations.put(loc, accessor);
                    }
                }
            }
        }

        // Scan resources from external configuration sources as well as default configs found in the parent JAR.
        // Data from external configs will replace any existing entries in the location dictionary since they are
        // considered highest priority.  Those from the parent JAR will only be applied if no other configs have
        // been loaded for the mod in question.
        for (final String mod : namespaceList) {
            Identifier location = Identifier.fromNamespaceAndPath(mod, config);
            IResourceAccessor accessor = IResourceAccessor.createExternalResource(root, location);
            if (accessor.exists()) {
                locations.put(location, accessor);
                continue;
            }

            accessor = IResourceAccessor.createJarResource(parentModConfigs, location);
            if (accessor.exists())
                locations.put(location, accessor);
        }

        return locations.values();
    }

    /**
     * Scans resource packs locating sound.json configurations.
     * @return Collection of accessors to retrieve sound.json configurations.
     */
    public static Collection<IResourceAccessor> findSounds() {
        final List<IResourceAccessor> results = new ArrayList<>();
        final Collection<PackResources> packs = ForgeUtils.getLoadedResourcePacks();
        if (!packs.isEmpty()) {
            collectSounds(packs, results);
            return results;
        }

        final List<PackResources> opened = new ArrayList<>();
        try {
            for (final Pack pack : GameUtils.getMC().getResourcePackRepository().getSelectedPacks())
                pack.open().forEach(opened::add);
            collectSounds(opened, results);
        } finally {
            for (final PackResources rp : opened) {
                try {
                    rp.close();
                } catch (final Throwable ignore) {
                }
            }
        }
        return results;
    }

    private static void collectSounds(@Nonnull final Collection<PackResources> packs, @Nonnull final List<IResourceAccessor> results) {
        for (final PackResources rp : packs) {
            final Set<String> embeddedNamespaces = rp.getNamespaces(PackType.CLIENT_RESOURCES);
            for (final String ns : embeddedNamespaces) {
                final Identifier location = Identifier.fromNamespaceAndPath(ns, "sounds.json");
                final IResourceAccessor accessor = IResourceAccessor.createPackResource(rp, location, location);
                if (accessor.exists()) {
                    results.add(accessor);
                }
            }
        }
    }

    private static Collection<String> discoverNamespaces(@Nonnull final String resourceContainer) {

        if (cachedNamespaces != null)
            return cachedNamespaces;

        final String container = String.format(resourceContainer, MANIFEST_FILE);

        // Initial namespace list is based on the currently loaded mod list
        final List<String> namespaces = new ArrayList<>(ForgeUtils.getModIdList());

        int insertAt = namespaces.indexOf("minecraft") + 1;
        for (final String part : dynamiclabs.immersivefx.ImmersiveFX.PART_IDS) {
            if (!namespaces.contains(part))
                namespaces.add(insertAt++, part);
        }

        // Resource packs are a bit of a challenge.  There are two different ways that a pack can provide resource
        // information to us:
        //
        // 1. The pack contains resource info specific to another mod in the mod pack.  These resources should not be
        //    processed unless the mod is present in the pack.  The constructed Forge resource pack is an example of
        //    this style.
        //
        // 2. The pack itself has it's own namespace, and it is providing resources that are not associated with any
        //    other mod present.  We identify these namespaces by the presence of a manifest file.
        //
        final Collection<PackResources> packs = ForgeUtils.getLoadedResourcePacks();

        for (final PackResources rp : packs) {
            final Set<String> embeddedNamespaces = rp.getNamespaces(PackType.CLIENT_RESOURCES);
            for (final String ns : embeddedNamespaces) {
                if (!namespaces.contains(ns)) {
                    final Identifier location = Identifier.fromNamespaceAndPath(ns, container);
                    final IResourceAccessor accessor = IResourceAccessor.createPackResource(rp, location, location);
                    if (accessor.exists()) {
                        final Manifest manifest = accessor.as(Manifest.class);
                        if (manifest != null) {
                            Lib.LOGGER.info("Resource Pack namespace detected: %s", manifest.getName());
                            namespaces.add(ns);
                        }
                    }
                }
            }
        }

        cachedNamespaces = namespaces;
        return cachedNamespaces;
    }

    /**
     * Obtains the string content of the resource at the specified asset location with the JAR
     * @param location The resource to load
     * @return The content of the specified resource, or null if not found
     */
    @Nullable
    public static String readResource(@Nonnull final Identifier location) {
        return readResource("", location);
    }

    /**
     * Obtains the string content of the resource at the specified asset location with the JAR
     * @param root Location is relative to this root in the JAR
     * @param location The resource to load
     * @return The content of the specified resource, or null if not found
     */
    @Nullable
    public static String readResource(@Nonnull final String root, @Nonnull final Identifier location) {
        final IResourceAccessor accessor = IResourceAccessor.createJarResource(root, location);
        return accessor.asString();
    }
}
