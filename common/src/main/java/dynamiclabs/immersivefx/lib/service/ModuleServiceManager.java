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

package dynamiclabs.immersivefx.lib.service;

import dynamiclabs.immersivefx.platform.event.ClientConnectionEvent;
import dynamiclabs.immersivefx.platform.event.EntityJoinLevelEvent;
import dynamiclabs.immersivefx.platform.event.TagsUpdatedEvent;
import dynamiclabs.immersivefx.ImmersiveFX;
import dynamiclabs.immersivefx.dsurround.DynamicSurroundings;
import dynamiclabs.immersivefx.lib.GameUtils;
import dynamiclabs.immersivefx.lib.Singleton;
import dynamiclabs.immersivefx.lib.fml.ForgeUtils;
import dynamiclabs.immersivefx.lib.logging.IModLog;
import dynamiclabs.immersivefx.lib.resource.ResourceUtils;
import dynamiclabs.immersivefx.lib.tags.TagUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import dynamiclabs.immersivefx.lib.collections.ObjectArray;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class ModuleServiceManager implements ResourceManagerReloadListener {

    private static final IModLog LOGGER = DynamicSurroundings.LOGGER.createChild(ModuleServiceManager.class);

    private static final Singleton<ModuleServiceManager> instance = new Singleton<>(ModuleServiceManager::new);

    private final ObjectArray<IModuleService> services = new ObjectArray<>();
    private boolean playerJoined = false;
    private boolean customTagsEventFired = false;
    private boolean vanillaTagsEventFired = false;
    private boolean reloadFired = false;

    private ModuleServiceManager() {
        final ResourceManager resourceManager = GameUtils.getMC().getResourceManager();
        ((ReloadableResourceManager) resourceManager).registerReloadListener(this);
    }

    public static ModuleServiceManager instance() {
        return instance.get();
    }

    /**
     * Adds the service instance to the service manager.
     *
     * @param svc Service to add
     */
    public void add(@Nonnull final IModuleService svc) {
        services.add(svc);
    }

    /**
     * Instructs configured services to reload configuration
     */
    private void reload() {
        ForgeUtils.getEnabledResourcePacks().forEach(p -> {
            LOGGER.debug("Resource pack '%s'", p.getId());
            LOGGER.debug("+  %s", p.getTitle().getString());
            LOGGER.debug("+  %s", p.getDescription().getString());
        });
        performAction("reload", IModuleService::reload);
        this.services.forEach(IModuleService::log);
    }

    @Override
    public void onResourceManagerReload(@Nonnull final ResourceManager resourceManager) {
        reportStatus("Received Resource reload callback");
        ResourceUtils.clearCache();
        this.reload();
    }

    private boolean readyForReload() {
        return this.playerJoined && (this.vanillaTagsEventFired || this.customTagsEventFired);
    }

    private void reportStatus(@Nonnull final String msg) {
        final String txt = String.format("%s (playerJoined: %b, reloadFired: %b, customTagsEventFired: %b, vanillaTagsEventFired: %b)",
                msg,
                this.playerJoined,
                this.reloadFired,
                this.customTagsEventFired,
                this.vanillaTagsEventFired);
        LOGGER.info(txt);
    }

    private void reloadIfReady() {
        if (!this.reloadFired && readyForReload()) {
            this.reloadFired = true;
            this.reload();
        }
    }

    private void clearReloadState() {
        reportStatus("Clearing reload state");
        this.playerJoined = false;
        this.reloadFired = false;
        this.customTagsEventFired = false;
        this.vanillaTagsEventFired = false;
    }

    public static void entityJoinWorld(@Nonnull final EntityJoinLevelEvent event) {
        final Player player = GameUtils.getPlayer();
        if (player != null && event.getLevel().isClientSide() && event.getEntity() == player) {
            instance().joinWorld(event);
        }
    }

    private void joinWorld(@Nonnull final EntityJoinLevelEvent ignore) {
        this.playerJoined = true;
        reportStatus("EntityJoinWorldEvent fired");
        this.reloadIfReady();
    }

    public static void onLoad(@Nonnull final TagsUpdatedEvent event) {
        instance().load(event);
    }

    private void load(@Nonnull final TagsUpdatedEvent event) {
        this.vanillaTagsEventFired = true;
        this.customTagsEventFired = true;
        TagUtils.setTagManager(event.getRegistries());
        reportStatus("TagsUpdatedEvent.ClientPacketReceived fired");
        this.reloadIfReady();
    }

    /**
     * Causes the service stop phase to be invoked when a player logs out
     *
     * @param event Event that is raised
     */
    public static void onStop(@Nonnull final ClientConnectionEvent event) {
        instance().stop();
    }

    private void stop() {
        performAction("stop", IModuleService::stop);
        TagUtils.clearTagManager();
        this.clearReloadState();
    }

    private void performAction(@Nonnull final String actionName, @Nonnull final Consumer<IModuleService> action) {

        LOGGER.info("Starting action '%s'", actionName);

        long start = System.nanoTime();

        final List<String> results = this.services.stream().map(svc -> {
            long begin = System.nanoTime();
            action.accept(svc);
            long duration = System.nanoTime() - begin;
            return String.format("Action '%s::%s' took %dmsecs", svc.name(), actionName, (long) (duration / 1000000D));
        }).collect(Collectors.toList());

        long duration = System.nanoTime() - start;
        results.forEach(LOGGER::debug);

        LOGGER.info("Overall Action '%s' took %dmsecs", actionName, (long) (duration / 1000000D));
    }
}
