/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.fabric;

import com.mojang.blaze3d.audio.Channel;
import dynamiclabs.immersivefx.ImmersiveFX;
import dynamiclabs.immersivefx.platform.event.ChatReceivedEvent;
import dynamiclabs.immersivefx.platform.event.ChatSendEvent;
import dynamiclabs.immersivefx.platform.event.ClientEvents;
import dynamiclabs.immersivefx.platform.event.ClientLifecycleEvents;
import dynamiclabs.immersivefx.platform.event.ClientTickEvents;
import dynamiclabs.immersivefx.platform.event.ConfigEvent;
import dynamiclabs.immersivefx.platform.event.ConfigEvents;
import dynamiclabs.immersivefx.platform.event.EntityEvents;
import dynamiclabs.immersivefx.platform.event.EventHook;
import dynamiclabs.immersivefx.platform.event.LevelEvent;
import dynamiclabs.immersivefx.platform.event.LevelEvents;
import dynamiclabs.immersivefx.platform.event.PlaySoundEvent;
import dynamiclabs.immersivefx.platform.event.RightClickBlockEvent;
import dynamiclabs.immersivefx.platform.event.SoundEngineEvents;
import dynamiclabs.immersivefx.platform.event.SoundSourceEvent;
import dynamiclabs.immersivefx.platform.event.TagsUpdatedEvent;
import fuzs.forgeconfigapiport.fabric.api.v5.ModConfigEvents;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStarted;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionResult;
import net.neoforged.fml.config.ModConfig;

public final class FabricEventBridge {

    private static List<ConfigEvent> pendingConfigLoading = new ArrayList<>();

    private FabricEventBridge() {
    }

    static void register() {
        ModConfigEvents.loading(ImmersiveFX.MOD_ID).register(config -> {
            final ConfigEvent event = configEvent(config);
            synchronized (FabricEventBridge.class) {
                if (pendingConfigLoading != null) {
                    pendingConfigLoading.add(event);
                    return;
                }
            }
            ConfigEvents.LOADING.post(event);
        });
        ModConfigEvents.reloading(ImmersiveFX.MOD_ID).register(config -> ConfigEvents.RELOADING.post(configEvent(config)));

        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.CLIENT_STARTED.register((ClientStarted) mc -> {
            ClientLifecycleEvents.CLIENT_SETUP.post(mc);
            ClientLifecycleEvents.REGISTER_INTEGRATIONS.post(mc);
            ClientLifecycleEvents.LOAD_COMPLETE.post(mc);
        });

        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.START_CLIENT_TICK.register(ClientTickEvents.START::post);
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.END::post);

        ServerLevelEvents.LOAD.register((server, level) -> {
            if (LevelEvents.LOAD.hasListeners())
                LevelEvents.LOAD.post(new LevelEvent(level));
        });
        ServerLevelEvents.UNLOAD.register((server, level) -> {
            if (LevelEvents.UNLOAD.hasListeners())
                LevelEvents.UNLOAD.post(new LevelEvent(level));
        });
        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> LevelEvents.TAGS_UPDATED.post(new TagsUpdatedEvent(registries, client)));

        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            if (EntityEvents.RIGHT_CLICK_BLOCK.hasListeners())
                EntityEvents.RIGHT_CLICK_BLOCK.post(new RightClickBlockEvent(player, level, hand, hit.getBlockPos(), hit));
            return InteractionResult.PASS;
        });

        ClientSendMessageEvents.CHAT.register(message -> ClientEvents.CHAT_SEND.post(new ChatSendEvent(message)));
        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, boundChatType, timestamp) ->
                ClientEvents.CHAT_RECEIVED.post(new ChatReceivedEvent(boundChatType, message, sender != null ? sender.id() : Util.NIL_UUID)));
    }

    static void flushConfigLoading() {
        final List<ConfigEvent> pending;
        synchronized (FabricEventBridge.class) {
            pending = pendingConfigLoading;
            pendingConfigLoading = null;
        }
        if (pending != null)
            pending.forEach(ConfigEvents.LOADING::post);
    }

    @Nonnull
    private static ConfigEvent configEvent(@Nonnull final ModConfig config) {
        return new ConfigEvent(config.getSpec(), config.getFileName());
    }

    @Nullable
    public static SoundInstance playSound(@Nonnull final SoundEngine engine, @Nonnull final SoundInstance sound) {
        if (!SoundEngineEvents.PLAY_SOUND.hasListeners())
            return sound;
        final FabricPlaySoundEvent event = new FabricPlaySoundEvent(engine, sound);
        SoundEngineEvents.PLAY_SOUND.post(event);
        return event.getSound();
    }

    public static void sourceStarted(@Nonnull final SoundEngine engine, @Nonnull final SoundInstance sound, @Nonnull final Channel channel, final boolean streaming) {
        if (SoundEngineEvents.SOURCE_STARTED.hasListeners())
            SoundEngineEvents.SOURCE_STARTED.post(new SoundSourceEvent(engine, sound, channel, streaming));
    }

    private static final class FabricPlaySoundEvent extends PlaySoundEvent {
        private final SoundEngine engine;
        private final SoundInstance original;
        @Nullable
        private SoundInstance sound;

        FabricPlaySoundEvent(@Nonnull final SoundEngine engine, @Nonnull final SoundInstance sound) {
            this.engine = engine;
            this.original = sound;
            this.sound = sound;
        }

        @Override
        public SoundEngine getEngine() {
            return this.engine;
        }

        @Override
        public SoundInstance getOriginalSound() {
            return this.original;
        }

        @Override
        @Nullable
        public SoundInstance getSound() {
            return this.sound;
        }

        @Override
        public void setSound(@Nullable final SoundInstance sound) {
            this.sound = sound;
        }
    }
}
