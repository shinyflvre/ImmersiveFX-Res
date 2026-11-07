/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx;

import dynamiclabs.immersivefx.lib.events.BlockUpdateEvent;
import dynamiclabs.immersivefx.lib.events.DiagnosticEvent;
import dynamiclabs.immersivefx.platform.event.ClientEvents;
import dynamiclabs.immersivefx.platform.event.ClientTickEvents;
import dynamiclabs.immersivefx.platform.event.ConfigEvents;
import dynamiclabs.immersivefx.platform.event.EntityEvents;
import dynamiclabs.immersivefx.platform.event.LevelEvents;
import dynamiclabs.immersivefx.platform.event.Priority;
import dynamiclabs.immersivefx.platform.event.RegistrationEvents;
import dynamiclabs.immersivefx.platform.event.RenderEvents;
import dynamiclabs.immersivefx.platform.event.SoundEngineEvents;
import dynamiclabs.immersivefx.sndctrl.events.BlockInspectionEvent;
import dynamiclabs.immersivefx.sndctrl.events.EntityInspectionEvent;

final class ClientEventSubscribers {

    private ClientEventSubscribers() {
    }

    static void register() {
        RegistrationEvents.CLIENT_COMMANDS.register(Priority.NORMAL, dynamiclabs.immersivefx.dsurround.commands.CommandHelpers::registerCommands);

        ConfigEvents.LOADING.register(Priority.NORMAL, dynamiclabs.immersivefx.dsurround.config.Config::onLoad);
        ConfigEvents.RELOADING.register(Priority.NORMAL, dynamiclabs.immersivefx.dsurround.config.Config::onFileChange);

        RegistrationEvents.KEY_MAPPINGS.register(Priority.NORMAL, dynamiclabs.immersivefx.dsurround.gui.Keys::register);
        ClientTickEvents.END.register(Priority.NORMAL, dynamiclabs.immersivefx.dsurround.gui.Keys::keyPressed);

        EntityEvents.PLAYER_TICK_START.register(Priority.NORMAL, dynamiclabs.immersivefx.dsurround.huds.lightlevel.LightLevelHUD::doTick);
        RenderEvents.EXTRACT_LEVEL_RENDER_STATE.register(Priority.NORMAL, dynamiclabs.immersivefx.dsurround.huds.lightlevel.LightLevelHUD::captureFrustum);
        RenderEvents.SUBMIT_CUSTOM_GEOMETRY.register(Priority.NORMAL, dynamiclabs.immersivefx.dsurround.huds.lightlevel.LightLevelHUD::render);

        ConfigEvents.LOADING.register(Priority.NORMAL, dynamiclabs.immersivefx.environs.config.Config::onLoad);
        ConfigEvents.RELOADING.register(Priority.NORMAL, dynamiclabs.immersivefx.environs.config.Config::onFileChange);

        DiagnosticEvent.BUS.register(Priority.NORMAL, dynamiclabs.immersivefx.environs.effects.particles.Collections::diagnostics);

        RegistrationEvents.PARTICLE_GROUPS.register(Priority.NORMAL, dynamiclabs.immersivefx.environs.effects.particles.ParticleLayers::registerParticleGroups);
        RegistrationEvents.RENDER_PIPELINES.register(Priority.NORMAL, dynamiclabs.immersivefx.environs.effects.particles.ParticleLayers::registerPipelines);

        DiagnosticEvent.BUS.register(Priority.NORMAL, dynamiclabs.immersivefx.environs.handlers.Manager::diagnosticEvent);
        ClientTickEvents.START.register(Priority.HIGH, dynamiclabs.immersivefx.environs.handlers.Manager::clientTick);

        RegistrationEvents.RENDER_PIPELINES.register(Priority.NORMAL, dynamiclabs.immersivefx.environs.shaders.aurora.AuroraRenderType::registerPipelines);

        ClientTickEvents.START.register(Priority.HIGHEST, dynamiclabs.immersivefx.lib.Diagnostics::startTick);
        ClientTickEvents.END.register(Priority.LOWEST, dynamiclabs.immersivefx.lib.Diagnostics::endTick);
        ClientTickEvents.END.register(Priority.LOW, dynamiclabs.immersivefx.lib.Diagnostics::onClientTick);
        RegistrationEvents.DEBUG_ENTRIES.register(Priority.NORMAL, dynamiclabs.immersivefx.lib.Diagnostics::onRegisterDebugEntries);

        LevelEvents.TAGS_UPDATED.register(Priority.NORMAL, e -> {
            if (e.clientPacket())
                dynamiclabs.immersivefx.lib.MaterialUtils.onTagsUpdated(e);
        });

        ClientTickEvents.START.register(Priority.HIGHEST, dynamiclabs.immersivefx.lib.TickCounter::onClientTick);

        LevelEvents.TAGS_UPDATED.register(Priority.NORMAL, e -> {
            if (e.clientPacket())
                dynamiclabs.immersivefx.lib.biomes.BiomeDictionary.onTagsUpdated(e);
        });

        EntityEvents.ENTITY_TICK.register(Priority.NORMAL, dynamiclabs.immersivefx.lib.effects.EntityEffectHandler::onLivingUpdate);
        EntityEvents.JOIN_LEVEL.register(Priority.HIGHEST, dynamiclabs.immersivefx.lib.effects.EntityEffectHandler::onEntityJoin);
        ClientTickEvents.END.register(Priority.NORMAL, dynamiclabs.immersivefx.lib.effects.EntityEffectHandler::onClientTick);
        DiagnosticEvent.BUS.register(Priority.NORMAL, dynamiclabs.immersivefx.lib.effects.EntityEffectHandler::onDiagnostics);

        LevelEvents.UNLOAD.register(Priority.NORMAL, dynamiclabs.immersivefx.lib.effects.entity.CapabilityEntityFXData.EventHandler::onLevelUnload);
        ClientEvents.LOGGING_OUT.register(Priority.NORMAL, dynamiclabs.immersivefx.lib.effects.entity.CapabilityEntityFXData.EventHandler::onLoggingOut);

        ClientEvents.LOGGING_IN.register(Priority.NORMAL, dynamiclabs.immersivefx.lib.fml.ClientLoginChecks::onLogin);

        RegistrationEvents.PARTICLE_GROUPS.register(Priority.NORMAL, dynamiclabs.immersivefx.lib.particles.CollectionManager::onRegisterParticleGroups);
        LevelEvents.UNLOAD.register(Priority.NORMAL, dynamiclabs.immersivefx.lib.particles.CollectionManager::onWorldUnload);
        DiagnosticEvent.BUS.register(Priority.NORMAL, dynamiclabs.immersivefx.lib.particles.CollectionManager::diagnostics);

        EntityEvents.JOIN_LEVEL.register(Priority.NORMAL, dynamiclabs.immersivefx.lib.service.ModuleServiceManager::entityJoinWorld);
        LevelEvents.TAGS_UPDATED.register(Priority.NORMAL, e -> {
            if (e.clientPacket())
                dynamiclabs.immersivefx.lib.service.ModuleServiceManager.onLoad(e);
        });
        ClientEvents.LOGGING_OUT.register(Priority.NORMAL, dynamiclabs.immersivefx.lib.service.ModuleServiceManager::onStop);

        ClientTickEvents.START.register(Priority.LOW, dynamiclabs.immersivefx.lib.world.ClientBlockUpdateHandler::onClientTickStart);
        ClientTickEvents.END.register(Priority.LOW, dynamiclabs.immersivefx.lib.world.ClientBlockUpdateHandler::onClientTickEnd);
        LevelEvents.LOAD.register(Priority.LOW, dynamiclabs.immersivefx.lib.world.ClientBlockUpdateHandler::onWorldLoad);
        LevelEvents.UNLOAD.register(Priority.LOW, dynamiclabs.immersivefx.lib.world.ClientBlockUpdateHandler::onWorldUnload);
        DiagnosticEvent.BUS.register(Priority.NORMAL, dynamiclabs.immersivefx.lib.world.ClientBlockUpdateHandler::onDiagnostics);

        ConfigEvents.LOADING.register(Priority.NORMAL, dynamiclabs.immersivefx.mobeffects.config.Config::onLoad);
        ConfigEvents.RELOADING.register(Priority.NORMAL, dynamiclabs.immersivefx.mobeffects.config.Config::onFileChange);

        EntityEvents.RIGHT_CLICK_BLOCK.register(Priority.NORMAL, dynamiclabs.immersivefx.mobeffects.effects.EntitySwingEffect::onRightClick);

        SoundEngineEvents.PLAY_SOUND.register(Priority.HIGH, dynamiclabs.immersivefx.mobeffects.library.EffectLibrary::soundPlay);

        BlockInspectionEvent.BUS.register(Priority.NORMAL, dynamiclabs.immersivefx.mobeffects.library.FootstepLibrary::onInspectionEvent);

        EntityInspectionEvent.BUS.register(Priority.HIGH, dynamiclabs.immersivefx.mobeffects.misc.EntityInspectionHandler::handler);

        ClientTickEvents.END.register(Priority.HIGH, dynamiclabs.immersivefx.mobeffects.misc.HidePotionParticlesHandler::handler);

        SoundEngineEvents.PLAY_SOUND.register(Priority.HIGH, dynamiclabs.immersivefx.mobeffects.misc.StepSoundHandler::onPlaySound);
        ClientEvents.LOGGING_OUT.register(Priority.NORMAL, dynamiclabs.immersivefx.mobeffects.misc.StepSoundHandler::onLogout);

        ClientTickEvents.START.register(Priority.NORMAL, dynamiclabs.immersivefx.sndctrl.audio.handlers.MusicFader::onTick);

        SoundEngineEvents.PLAY_SOUND.register(Priority.LOW, dynamiclabs.immersivefx.sndctrl.audio.handlers.PlayerSounds2D::onPlaySound);

        ClientEvents.SCREEN_OPENING.register(Priority.NORMAL, dynamiclabs.immersivefx.sndctrl.audio.handlers.SoundEventHandling::onGuiOpen);

        ConfigEvents.LOADING.register(Priority.LOWEST, dynamiclabs.immersivefx.sndctrl.audio.handlers.SoundProcessor::onConfigLoad);
        ConfigEvents.RELOADING.register(Priority.LOWEST, dynamiclabs.immersivefx.sndctrl.audio.handlers.SoundProcessor::onConfigChange);

        ClientTickEvents.END.register(Priority.NORMAL, dynamiclabs.immersivefx.sndctrl.client.Inspector::onClientTick);
        BlockInspectionEvent.BUS.register(Priority.HIGH, dynamiclabs.immersivefx.sndctrl.client.Inspector::onBlockInspectionEvent);
        DiagnosticEvent.BUS.register(Priority.HIGH, dynamiclabs.immersivefx.sndctrl.client.Inspector::onGatherText);

        ConfigEvents.LOADING.register(Priority.NORMAL, dynamiclabs.immersivefx.sndctrl.config.Config::onLoad);
        ConfigEvents.RELOADING.register(Priority.NORMAL, dynamiclabs.immersivefx.sndctrl.config.Config::onFileChange);

        RegistrationEvents.KEY_MAPPINGS.register(Priority.NORMAL, dynamiclabs.immersivefx.sndctrl.gui.Keys::onRegisterKeyMappings);
        ClientEvents.KEY_INPUT.register(Priority.NORMAL, dynamiclabs.immersivefx.sndctrl.gui.Keys::keyPressed);

        LevelEvents.LOAD.register(Priority.HIGHEST, dynamiclabs.immersivefx.sndctrl.misc.ClientWorldPatcher::onWorldLoad);

        ClientTickEvents.END.register(Priority.NORMAL, dynamiclabs.immersivefx.specialfx.BedSound::onClientTick);

        ClientEvents.CHAT_SEND.register(Priority.LOWEST, dynamiclabs.immersivefx.specialfx.ChatSound::onChatSent);
        ClientEvents.SCREEN_OPENING.register(Priority.NORMAL, dynamiclabs.immersivefx.specialfx.InventorySounds::onScreenOpening);
        ClientEvents.CHAT_RECEIVED.register(Priority.LOWEST, dynamiclabs.immersivefx.specialfx.ChatSound::onChatReceived);

        EntityEvents.ITEM_CRAFTED.register(Priority.NORMAL, dynamiclabs.immersivefx.specialfx.CraftSounds::onItemCrafted);

        EntityEvents.PLAYER_TICK_END.register(Priority.NORMAL, dynamiclabs.immersivefx.specialfx.HungerSound::onPlayerTick);

        LevelEvents.LOAD.register(Priority.NORMAL, dynamiclabs.immersivefx.specialfx.MusicTriggersID::onWorldLoad);

        EntityEvents.RIGHT_CLICK_BLOCK.register(Priority.NORMAL, dynamiclabs.immersivefx.specialfx.RightClickSounds::onRightClickBlock);

        EntityEvents.ITEM_SMELTED.register(Priority.NORMAL, dynamiclabs.immersivefx.specialfx.SmeltSounds::onItemSmelted);

        EntityEvents.JOIN_LEVEL.register(Priority.NORMAL, dynamiclabs.immersivefx.specialfx.SpawnSound::onEntitySpawned);
    }
}
