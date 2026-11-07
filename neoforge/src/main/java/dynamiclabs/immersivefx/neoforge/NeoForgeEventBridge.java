/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.neoforge;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import dynamiclabs.immersivefx.platform.event.AfterSkyEvent;
import dynamiclabs.immersivefx.platform.event.ChatReceivedEvent;
import dynamiclabs.immersivefx.platform.event.ChatSendEvent;
import dynamiclabs.immersivefx.platform.event.ClientCommand;
import dynamiclabs.immersivefx.platform.event.ClientCommandRegistrar;
import dynamiclabs.immersivefx.platform.event.ClientConnectionEvent;
import dynamiclabs.immersivefx.platform.event.ClientEvents;
import dynamiclabs.immersivefx.platform.event.ClientLifecycleEvents;
import dynamiclabs.immersivefx.platform.event.ClientTickEvents;
import dynamiclabs.immersivefx.platform.event.ConfigEvent;
import dynamiclabs.immersivefx.platform.event.ConfigEvents;
import dynamiclabs.immersivefx.platform.event.DebugEntryRegistrar;
import dynamiclabs.immersivefx.platform.event.EntityEvents;
import dynamiclabs.immersivefx.platform.event.EventHook;
import dynamiclabs.immersivefx.platform.event.ItemResultEvent;
import dynamiclabs.immersivefx.platform.event.KeyInputEvent;
import dynamiclabs.immersivefx.platform.event.KeyMappingRegistrar;
import dynamiclabs.immersivefx.platform.event.LevelEvents;
import dynamiclabs.immersivefx.platform.event.ParticleGroupRegistrar;
import dynamiclabs.immersivefx.platform.event.ParticleProviderRegistrar;
import dynamiclabs.immersivefx.platform.event.Priority;
import dynamiclabs.immersivefx.platform.event.RegistrationEvents;
import dynamiclabs.immersivefx.platform.event.RenderEvents;
import dynamiclabs.immersivefx.platform.event.RenderFogEvent;
import dynamiclabs.immersivefx.platform.event.RightClickBlockEvent;
import dynamiclabs.immersivefx.platform.event.ScreenOpeningEvent;
import dynamiclabs.immersivefx.platform.event.SoundEngineEvents;
import dynamiclabs.immersivefx.platform.event.SoundSourceEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.components.debug.DebugScreenProfile;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.neoforge.client.event.ClientChatEvent;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterDebugEntriesEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleGroupsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundSourceEvent;
import net.neoforged.neoforge.client.event.sound.PlayStreamingSourceEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

final class NeoForgeEventBridge {

    private NeoForgeEventBridge() {
    }

    static void register(@Nonnull final IEventBus modBus) {
        final IEventBus game = NeoForge.EVENT_BUS;

        bridge(modBus, FMLClientSetupEvent.class, ClientLifecycleEvents.CLIENT_SETUP, e -> Minecraft.getInstance());
        bridge(modBus, InterModEnqueueEvent.class, ClientLifecycleEvents.REGISTER_INTEGRATIONS, e -> Minecraft.getInstance());
        bridge(modBus, FMLLoadCompleteEvent.class, ClientLifecycleEvents.LOAD_COMPLETE, e -> Minecraft.getInstance());
        bridge(modBus, ModConfigEvent.Loading.class, ConfigEvents.LOADING, e -> new ConfigEvent(e.getConfig().getSpec(), e.getConfig().getFileName()));
        bridge(modBus, ModConfigEvent.Reloading.class, ConfigEvents.RELOADING, e -> new ConfigEvent(e.getConfig().getSpec(), e.getConfig().getFileName()));

        bridge(modBus, RegisterRenderPipelinesEvent.class, RegistrationEvents.RENDER_PIPELINES, e -> (RenderPipeline pipeline) -> e.registerPipeline(pipeline));
        bridge(modBus, RegisterParticleProvidersEvent.class, RegistrationEvents.PARTICLE_PROVIDERS, NeoForgeEventBridge::particleProviders);
        bridge(modBus, RegisterParticleGroupsEvent.class, RegistrationEvents.PARTICLE_GROUPS, e -> (ParticleRenderType group, Function<ParticleEngine, ParticleGroup<?>> factory) -> e.register(group, factory));
        bridge(modBus, RegisterKeyMappingsEvent.class, RegistrationEvents.KEY_MAPPINGS, NeoForgeEventBridge::keyMappings);
        bridge(modBus, RegisterDebugEntriesEvent.class, RegistrationEvents.DEBUG_ENTRIES, NeoForgeEventBridge::debugEntries);
        bridge(game, RegisterClientCommandsEvent.class, RegistrationEvents.CLIENT_COMMANDS, NeoForgeEventBridge::clientCommands);

        bridge(game, ClientTickEvent.Pre.class, ClientTickEvents.START, e -> Minecraft.getInstance());
        bridge(game, ClientTickEvent.Post.class, ClientTickEvents.END, e -> Minecraft.getInstance());
        bridge(game, PlayerTickEvent.Pre.class, EntityEvents.PLAYER_TICK_START, e -> new dynamiclabs.immersivefx.platform.event.PlayerTickEvent(e.getEntity()));
        bridge(game, PlayerTickEvent.Post.class, EntityEvents.PLAYER_TICK_END, e -> new dynamiclabs.immersivefx.platform.event.PlayerTickEvent(e.getEntity()));
        bridge(game, EntityTickEvent.Pre.class, EntityEvents.ENTITY_TICK, true, e -> new dynamiclabs.immersivefx.platform.event.EntityTickEvent(e.getEntity()));

        bridge(game, EntityJoinLevelEvent.class, EntityEvents.JOIN_LEVEL, e -> new dynamiclabs.immersivefx.platform.event.EntityJoinLevelEvent(e.getEntity(), e.getLevel(), e.loadedFromDisk()));
        bridge(game, PlayerEvent.ItemCraftedEvent.class, EntityEvents.ITEM_CRAFTED, e -> new ItemResultEvent(e.getEntity(), e.getCrafting()));
        bridge(game, PlayerEvent.ItemSmeltedEvent.class, EntityEvents.ITEM_SMELTED, e -> new ItemResultEvent(e.getEntity(), e.getSmelting()));
        bridge(game, PlayerInteractEvent.RightClickBlock.class, EntityEvents.RIGHT_CLICK_BLOCK, e -> new RightClickBlockEvent(e.getEntity(), e.getLevel(), e.getHand(), e.getPos(), e.getHitVec()));

        bridge(game, LevelEvent.Load.class, LevelEvents.LOAD, e -> new dynamiclabs.immersivefx.platform.event.LevelEvent(e.getLevel()));
        bridge(game, LevelEvent.Unload.class, LevelEvents.UNLOAD, e -> new dynamiclabs.immersivefx.platform.event.LevelEvent(e.getLevel()));
        bridge(game, TagsUpdatedEvent.class, LevelEvents.TAGS_UPDATED, e -> new dynamiclabs.immersivefx.platform.event.TagsUpdatedEvent(e.getRegistries(), e instanceof TagsUpdatedEvent.ClientPacketReceived));

        bridge(game, ClientPlayerNetworkEvent.LoggingIn.class, ClientEvents.LOGGING_IN, e -> new ClientConnectionEvent(e.getPlayer(), e.getMultiPlayerGameMode(), e.getConnection()));
        bridge(game, ClientPlayerNetworkEvent.LoggingOut.class, ClientEvents.LOGGING_OUT, e -> new ClientConnectionEvent(e.getPlayer(), e.getMultiPlayerGameMode(), e.getConnection()));
        bridge(game, ScreenEvent.Opening.class, ClientEvents.SCREEN_OPENING, e -> new ScreenOpeningEvent(e.getCurrentScreen(), e.getNewScreen()));
        bridge(game, ClientChatEvent.class, ClientEvents.CHAT_SEND, e -> new ChatSendEvent(e.getMessage()));
        bridge(game, ClientChatReceivedEvent.Player.class, ClientEvents.CHAT_RECEIVED, e -> new ChatReceivedEvent(e.getBoundChatType(), e.getMessage(), e.getSender()));
        bridge(game, InputEvent.Key.class, ClientEvents.KEY_INPUT, e -> new KeyInputEvent(e.getKey(), e.getKeycode(), e.getAction(), e.getModifiers()));

        bridge(game, PlaySoundEvent.class, SoundEngineEvents.PLAY_SOUND, PlaySound::new);
        bridge(game, PlaySoundSourceEvent.class, SoundEngineEvents.SOURCE_STARTED, e -> new SoundSourceEvent(e.getEngine(), e.getSound(), e.getChannel(), false));
        bridge(game, PlayStreamingSourceEvent.class, SoundEngineEvents.SOURCE_STARTED, e -> new SoundSourceEvent(e.getEngine(), e.getSound(), e.getChannel(), true));

        bridge(game, ViewportEvent.RenderFog.class, RenderEvents.RENDER_FOG, e -> new RenderFogEvent(e.getEnvironment(), e.getType(), e.getCamera(), e.getPartialTick(), e.getFogData()));
        bridge(game, RenderLevelStageEvent.AfterSky.class, RenderEvents.AFTER_SKY, e -> new AfterSkyEvent(e.getLevelRenderState(), e.getPoseStack(), e.getModelViewMatrix()));
        bridge(game, ExtractLevelRenderStateEvent.class, RenderEvents.EXTRACT_LEVEL_RENDER_STATE, e -> new dynamiclabs.immersivefx.platform.event.ExtractLevelRenderStateEvent(e.getLevel(), e.getCamera(), e.getFrustum(), e.getRenderState()));
        bridge(game, SubmitCustomGeometryEvent.class, RenderEvents.SUBMIT_CUSTOM_GEOMETRY, e -> new dynamiclabs.immersivefx.platform.event.SubmitCustomGeometryEvent(e.getLevelRenderState(), e.getSubmitNodeCollector(), e.getPoseStack()));
    }

    private static <N extends Event, E> void bridge(@Nonnull final IEventBus bus, @Nonnull final Class<N> type, @Nonnull final EventHook<E> hook,
                                                    @Nonnull final Function<N, E> adapter) {
        bridge(bus, type, hook, false, adapter);
    }

    private static <N extends Event, E> void bridge(@Nonnull final IEventBus bus, @Nonnull final Class<N> type, @Nonnull final EventHook<E> hook,
                                                    final boolean receiveCanceled, @Nonnull final Function<N, E> adapter) {
        for (final Priority priority : Priority.values()) {
            bus.addListener(EventPriority.valueOf(priority.name()), receiveCanceled, type, event -> {
                if (hook.hasListeners(priority))
                    hook.post(priority, adapter.apply(event));
            });
        }
    }

    @Nonnull
    private static ParticleProviderRegistrar particleProviders(@Nonnull final RegisterParticleProvidersEvent event) {
        return new ParticleProviderRegistrar() {
            @Override
            public <T extends ParticleOptions> void registerSpecial(final ParticleType<T> type, final ParticleProvider<T> provider) {
                event.registerSpecial(type, provider);
            }

            @Override
            public <T extends ParticleOptions> void registerSpriteSet(final ParticleType<T> type, final Function<SpriteSet, ParticleProvider<T>> registration) {
                event.registerSpriteSet(type, registration::apply);
            }
        };
    }

    @Nonnull
    private static KeyMappingRegistrar keyMappings(@Nonnull final RegisterKeyMappingsEvent event) {
        return new KeyMappingRegistrar() {
            @Override
            public void registerCategory(final KeyMapping.Category category) {
                event.registerCategory(category);
            }

            @Override
            public void register(final KeyMapping key) {
                event.register(key);
            }
        };
    }

    @Nonnull
    private static DebugEntryRegistrar debugEntries(@Nonnull final RegisterDebugEntriesEvent event) {
        return new DebugEntryRegistrar() {
            @Override
            public void register(final Identifier id, final DebugScreenEntry entry) {
                event.register(id, entry);
            }

            @Override
            public void includeInProfile(final Identifier id, final DebugScreenProfile profile, final DebugScreenEntryStatus status) {
                event.includeInProfile(id, profile, status);
            }
        };
    }

    @Nonnull
    private static ClientCommandRegistrar clientCommands(@Nonnull final RegisterClientCommandsEvent event) {
        return new ClientCommandRegistrar() {
            @Override
            public CommandBuildContext getBuildContext() {
                return event.getBuildContext();
            }

            @Override
            public void register(final ClientCommand command) {
                event.getDispatcher().register(command.<CommandSourceStack>build((source, message) -> source.sendSuccess(() -> message, true)));
            }
        };
    }

    private static final class PlaySound extends dynamiclabs.immersivefx.platform.event.PlaySoundEvent {
        private final PlaySoundEvent event;

        PlaySound(@Nonnull final PlaySoundEvent event) {
            this.event = event;
        }

        @Override
        @Nullable
        public SoundEngine getEngine() {
            return this.event.getEngine();
        }

        @Override
        public SoundInstance getOriginalSound() {
            return this.event.getOriginalSound();
        }

        @Override
        @Nullable
        public SoundInstance getSound() {
            return this.event.getSound();
        }

        @Override
        public void setSound(@Nullable final SoundInstance sound) {
            this.event.setSound(sound);
        }
    }
}
