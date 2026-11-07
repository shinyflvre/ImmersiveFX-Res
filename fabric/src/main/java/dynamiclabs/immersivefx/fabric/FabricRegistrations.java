/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.fabric;

import dynamiclabs.immersivefx.fabric.mixins.DebugScreenEntriesAccessor;
import dynamiclabs.immersivefx.platform.event.ClientCommand;
import dynamiclabs.immersivefx.platform.event.ClientCommandRegistrar;
import dynamiclabs.immersivefx.platform.event.DebugEntryRegistrar;
import dynamiclabs.immersivefx.platform.event.KeyMappingRegistrar;
import dynamiclabs.immersivefx.platform.event.ParticleGroupRegistrar;
import dynamiclabs.immersivefx.platform.event.ParticleProviderRegistrar;
import dynamiclabs.immersivefx.platform.event.RegistrationEvents;
import dynamiclabs.immersivefx.platform.event.RenderPipelineRegistrar;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.components.debug.DebugScreenProfile;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.Identifier;

public final class FabricRegistrations {

    private static final Map<ParticleRenderType, Function<ParticleEngine, ParticleGroup<?>>> PARTICLE_GROUPS = new IdentityHashMap<>();
    private static final List<ParticleRenderType> PARTICLE_GROUP_ORDER = new ArrayList<>();
    private static volatile List<ParticleRenderType> particleGroupOrderView = List.of();

    private FabricRegistrations() {
    }

    static void registerAll() {
        RegistrationEvents.RENDER_PIPELINES.post((RenderPipelineRegistrar) RenderPipelines::register);
        RegistrationEvents.KEY_MAPPINGS.post(new KeyMappings());
        RegistrationEvents.PARTICLE_PROVIDERS.post(new ParticleProviders());
        RegistrationEvents.PARTICLE_GROUPS.post(FabricRegistrations::registerParticleGroup);
        registerDebugEntries();
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> RegistrationEvents.CLIENT_COMMANDS.post(new ClientCommandRegistrar() {
            @Override
            public CommandBuildContext getBuildContext() {
                return buildContext;
            }

            @Override
            public void register(final ClientCommand command) {
                dispatcher.register(command.<FabricClientCommandSource>build(FabricClientCommandSource::sendFeedback));
            }
        }));
    }

    private static synchronized void registerParticleGroup(@Nonnull final ParticleRenderType group, @Nonnull final Function<ParticleEngine, ParticleGroup<?>> factory) {
        if (PARTICLE_GROUPS.putIfAbsent(group, factory) != null)
            throw new IllegalArgumentException("Factory already registered for provided particle render type: " + group.name());
        PARTICLE_GROUP_ORDER.add(group);
        particleGroupOrderView = List.copyOf(PARTICLE_GROUP_ORDER);
    }

    @Nullable
    public static Function<ParticleEngine, ParticleGroup<?>> particleGroupFactory(@Nonnull final ParticleRenderType group) {
        return PARTICLE_GROUPS.isEmpty() ? null : PARTICLE_GROUPS.get(group);
    }

    @Nonnull
    public static List<ParticleRenderType> particleGroupOrder() {
        return particleGroupOrderView;
    }

    private static void registerDebugEntries() {
        final Map<DebugScreenProfile, Map<Identifier, DebugScreenEntryStatus>> profiles = new EnumMap<>(DebugScreenProfile.class);
        DebugScreenEntries.PROFILES.forEach((profile, entries) -> profiles.put(profile, new HashMap<>(entries)));
        final boolean[] changed = {false};
        RegistrationEvents.DEBUG_ENTRIES.post(new DebugEntryRegistrar() {
            @Override
            public void register(final Identifier id, final DebugScreenEntry entry) {
                if (DebugScreenEntries.getEntry(id) != null)
                    throw new IllegalStateException("Duplicate DebugScreenEntry registration: " + id);
                DebugScreenEntries.register(id, entry);
            }

            @Override
            public void includeInProfile(final Identifier id, final DebugScreenProfile profile, final DebugScreenEntryStatus status) {
                if (profiles.computeIfAbsent(profile, p -> new HashMap<>()).putIfAbsent(id, status) != null)
                    throw new IllegalStateException("Duplicate DebugScreenEntry " + profile.getSerializedName() + "-profile inclusion: " + id);
                changed[0] = true;
            }
        });
        if (changed[0]) {
            final Map<DebugScreenProfile, Map<Identifier, DebugScreenEntryStatus>> result = new EnumMap<>(DebugScreenProfile.class);
            profiles.forEach((profile, entries) -> result.put(profile, Map.copyOf(entries)));
            DebugScreenEntriesAccessor.ifx$setProfiles(Map.copyOf(result));
        }
    }

    private static final class KeyMappings implements KeyMappingRegistrar {
        @Override
        public void registerCategory(final KeyMapping.Category category) {
            KeyMapping.Category.register(category.id());
        }

        @Override
        public void register(final KeyMapping key) {
            KeyMappingHelper.registerKeyMapping(key);
        }
    }

    private static final class ParticleProviders implements ParticleProviderRegistrar {
        @Override
        public <T extends ParticleOptions> void registerSpecial(final ParticleType<T> type, final ParticleProvider<T> provider) {
            ParticleProviderRegistry.getInstance().register(type, provider);
        }

        @Override
        public <T extends ParticleOptions> void registerSpriteSet(final ParticleType<T> type, final Function<SpriteSet, ParticleProvider<T>> registration) {
            ParticleProviderRegistry.getInstance().register(type, (ParticleProviderRegistry.PendingParticleProvider<T>) registration::apply);
        }
    }
}
