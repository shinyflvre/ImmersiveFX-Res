/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
/*
 * Dynamic Surroundings: Sound Control
 * Copyright (C) 2019  OreCruncher
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

package dynamiclabs.immersivefx.sndctrl.events;

import dynamiclabs.immersivefx.platform.event.Cancellable;
import dynamiclabs.immersivefx.platform.event.EventHook;

@SuppressWarnings("unused")
public class AudioEvent {
    public AudioEvent() {
    }

    /**
     * Raised by sound system to determine if music should fade in volume. Canceling
     * the event will cause the MusicFader to fade music. Otherwise, the volume will
     * return to normal levels. As an example, Dynamic Surroundings battle music
     * will fade out the background music while battle music is playing.
     */
    public final static class MusicFadeAudioEvent extends AudioEvent implements Cancellable {

        public static final EventHook<MusicFadeAudioEvent> BUS = new EventHook<>("MusicFadeAudioEvent");

        private boolean canceled;

        public MusicFadeAudioEvent() {
        }

        @Override
        public boolean isCanceled() {
            return this.canceled;
        }

        @Override
        public void setCanceled(final boolean canceled) {
            this.canceled = canceled;
        }
    }

}
