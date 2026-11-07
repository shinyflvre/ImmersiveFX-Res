/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.platform.event;

public final class RegistrationEvents {
    private RegistrationEvents() {
    }

    public static final EventHook<RenderPipelineRegistrar> RENDER_PIPELINES = new EventHook<>("register render pipelines");
    public static final EventHook<ParticleProviderRegistrar> PARTICLE_PROVIDERS = new EventHook<>("register particle providers");
    public static final EventHook<ParticleGroupRegistrar> PARTICLE_GROUPS = new EventHook<>("register particle groups");
    public static final EventHook<KeyMappingRegistrar> KEY_MAPPINGS = new EventHook<>("register key mappings");
    public static final EventHook<ClientCommandRegistrar> CLIENT_COMMANDS = new EventHook<>("register client commands");
    public static final EventHook<DebugEntryRegistrar> DEBUG_ENTRIES = new EventHook<>("register debug entries");
}
