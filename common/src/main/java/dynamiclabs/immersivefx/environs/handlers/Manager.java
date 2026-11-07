/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
/*
 *  Dynamic Surroundings: Environs
 *  Copyright (C) 2020  OreCruncher
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package dynamiclabs.immersivefx.environs.handlers;

import dynamiclabs.immersivefx.ImmersiveFX;
import dynamiclabs.immersivefx.lib.GameUtils;
import dynamiclabs.immersivefx.lib.TickCounter;
import dynamiclabs.immersivefx.lib.events.DiagnosticEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import dynamiclabs.immersivefx.environs.config.Config;
import dynamiclabs.immersivefx.environs.Environs;
import dynamiclabs.immersivefx.lib.collections.ObjectArray;
import dynamiclabs.immersivefx.lib.logging.IModLog;

import javax.annotation.Nonnull;

public class Manager {

    private static final IModLog LOGGER = Environs.LOGGER.createChild(Manager.class);

    private static final Manager instance_ = new Manager();
    private static boolean isConnected = false;

    public static Manager instance() {
        return instance_;
    }

    private final ObjectArray<HandlerBase> effectHandlers = new ObjectArray<>();

    private Manager() {
        init();
    }

    private void register(@Nonnull final HandlerBase handler) {
        this.effectHandlers.add(handler);
        LOGGER.debug("Registered handler [%s]", handler.getClass().getName());
    }

    private void init() {
        // This has to be first!
        register(new CommonStateHandler());
        register(new AreaBlockEffects());
        register(new BiomeSoundEffects());
        register(new ParticleSystems());
        register(new AuroraHandler());
        register(new FogHandler());
    }

    private void onConnect() {
        for (final HandlerBase h : this.effectHandlers)
            h.connect0();
    }

    private void onDisconnect() {
        for (final HandlerBase h : this.effectHandlers)
            h.disconnect0();
    }

    public static void connect() {
        if (isConnected) {
            LOGGER.warn("Attempt to initialize EffectManager when it is already initialized");
            disconnect();
        }
        instance_.onConnect();
        isConnected = true;
    }

    public static void disconnect() {
        if (isConnected) {
            instance_.onDisconnect();
            isConnected = false;
        }
    }

    protected static Player getPlayer() {
        return GameUtils.getPlayer();
    }

    protected boolean checkReady(@Nonnull final Minecraft event) {
        if (Minecraft.getInstance().isPaused())
            return false;
        return GameUtils.isInGame();
    }

    public void onTick(@Nonnull final Minecraft event) {
        if (!checkReady(event))
            return;

        final ProfilerFiller profiler = Profiler.get();
        profiler.push("Environs Client Tick");

        final long tick = TickCounter.getTickCount();

        for (final HandlerBase handler : this.effectHandlers) {
            profiler.push(handler.getHandlerName());
            final long mark = System.nanoTime();
            if (handler.doTick(tick))
                handler.process(getPlayer());
            handler.updateTimer(System.nanoTime() - mark);
            profiler.pop();
        }

        profiler.pop();
    }

    public static void diagnosticEvent(@Nonnull final DiagnosticEvent event) {
        if (Config.CLIENT.logging.enableLogging.get())
            instance().effectHandlers.forEach(h -> event.addTimer(h.getTimer()));
    }

    public static void clientTick(@Nonnull final Minecraft event) {
        if (isConnected)
            instance_.onTick(event);
    }
}
