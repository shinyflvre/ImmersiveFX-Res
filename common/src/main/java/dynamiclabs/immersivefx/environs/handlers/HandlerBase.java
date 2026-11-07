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

import dynamiclabs.immersivefx.platform.event.EventHook;
import dynamiclabs.immersivefx.platform.event.Priority;
import dynamiclabs.immersivefx.platform.event.Subscription;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import com.google.common.base.MoreObjects;
import dynamiclabs.immersivefx.lib.math.TimerEMA;
import net.minecraft.world.entity.player.Player;
import dynamiclabs.immersivefx.lib.random.XorShiftRandom;

import javax.annotation.Nonnull;
import java.util.Random;

public class HandlerBase {

    protected static final Random RANDOM = XorShiftRandom.current();

    private final String handlerName;
    private final TimerEMA timer;
    private final List<Subscription> subscriptions = new ArrayList<>();

    HandlerBase(@Nonnull final String name) {
        this.handlerName = name;
        this.timer = new TimerEMA(this.handlerName);
    }

    @Nonnull
    public TimerEMA getTimer() {
        return this.timer;
    }

    /**
     * Used to obtain the handler name for logging purposes.
     *
     * @return Name of the handler
     */
    @Nonnull
    public final String getHandlerName() {
        return this.handlerName;
    }

    /**
     * Indicates whether the handler needs to be invoked for the given tick.
     *
     * @return true that the handler needs to be invoked, false otherwise
     */
    public boolean doTick(final long tick) {
        return true;
    }

    /**
     * Meat of the handlers processing logic. Will be invoked if doTick() returns
     * true.
     *
     * @param player The player currently behind the keyboard.
     */
    public void process(@Nonnull final Player player) {

    }

    /**
     * Called when the client is connecting to a server. Useful for initializing
     * data to a baseline state.
     */
    public void onConnect() {
    }

    /**
     * Called when the client disconnects from a server. Useful for cleaning up
     * state space.
     */
    public void onDisconnect() {
    }

    //////////////////////////////
    //
    // DO NOT HOOK THESE EVENTS!
    //
    //////////////////////////////
    final void updateTimer(final long nanos) {
        this.timer.update(nanos);
    }

    final void connect0() {
        onConnect();
        registerEvents();
    }

    final void disconnect0() {
        for (final Subscription s : this.subscriptions)
            s.unsubscribe();
        this.subscriptions.clear();
        onDisconnect();
    }

    protected void registerEvents() {
    }

    protected final <E> void listen(@Nonnull final EventHook<E> hook, @Nonnull final Priority priority, @Nonnull final Consumer<? super E> listener) {
        this.subscriptions.add(hook.register(priority, listener));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("name", getHandlerName()).toString();
    }
}
