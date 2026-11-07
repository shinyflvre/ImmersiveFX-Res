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

import dynamiclabs.immersivefx.platform.event.Priority;
import dynamiclabs.immersivefx.environs.library.BiomeInfo;
import dynamiclabs.immersivefx.environs.library.BiomeLibrary;
import dynamiclabs.immersivefx.environs.scanner.BiomeScanner;
import dynamiclabs.immersivefx.lib.TickCounter;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.world.entity.player.Player;
import dynamiclabs.immersivefx.environs.config.Config;
import dynamiclabs.immersivefx.lib.collections.ObjectArray;
import dynamiclabs.immersivefx.lib.events.DiagnosticEvent;
import dynamiclabs.immersivefx.sndctrl.api.acoustics.IAcoustic;
import dynamiclabs.immersivefx.sndctrl.api.acoustics.IAcousticFactory;
import dynamiclabs.immersivefx.sndctrl.audio.AudioEngine;
import dynamiclabs.immersivefx.sndctrl.audio.acoustic.AcousticFactory;

import javax.annotation.Nonnull;
import java.util.Collection;

public class BiomeSoundEffects extends HandlerBase {

    public static final int SCAN_INTERVAL = 4;
    public static final int SPOT_SOUND_MIN_RANGE = 8;
    public static final int SPOT_SOUND_MAX_RANGE = AcousticFactory.SOUND_RANGE;

    // Reusable map for biome acoustic work
    private static final Reference2FloatOpenHashMap<IAcoustic> WORK_MAP = new Reference2FloatOpenHashMap<>(8);

    private static final ReferenceOpenHashSet<IAcoustic> UNIFORM_SET = new ReferenceOpenHashSet<>(8);
    private static final ReferenceOpenHashSet<IAcoustic> VILLAGE_SET = new ReferenceOpenHashSet<>(8);

    static {
        WORK_MAP.defaultReturnValue(0F);
    }

    private final BiomeScanner biomes = new BiomeScanner();
    private final ObjectArray<BackgroundAcousticEmitter> emitters = new ObjectArray<>(8);
    private final ObjectArray<SurroundAcousticEmitter> surroundEmitters = new ObjectArray<>(8);
    private final AmbienceDirections directions = new AmbienceDirections();

    BiomeSoundEffects() {
        super("Biome Sounds");
    }

    @Override
    public boolean doTick(final long tick) {
        return CommonState.getDimensionInfo().playBiomeSounds();
    }

    private boolean doBiomeSounds() {
        return CommonState.isUnderground()
                || !CommonState.isInside()
                || CommonState.getDimensionInfo().alwaysOutside();
    }

    private void generateBiomeSounds() {
        final float area = this.biomes.getBiomeArea();
        for (final Reference2IntMap.Entry<BiomeInfo> kvp : this.biomes.getBiomes().reference2IntEntrySet()) {
            final Collection<IAcoustic> acoustics = kvp.getKey().findSoundMatches();
            final float volume = 0.05F + 0.95F * (kvp.getIntValue() / area);
            for (final IAcoustic acoustic : acoustics) {
                WORK_MAP.addTo(acoustic, volume);
            }
        }
    }

    @Override
    public void process(@Nonnull final Player player) {
        this.emitters.forEach(BackgroundAcousticEmitter::tick);
        this.surroundEmitters.forEach(SurroundAcousticEmitter::tick);
        if ((TickCounter.getTickCount() % SCAN_INTERVAL) == 0) {
            this.biomes.tick();
            handleBiomeSounds(player);
        }
    }

    @Override
    public void onConnect() {
        clearSounds();
    }

    @Override
    public void onDisconnect() {
        clearSounds();
    }

    private void handleBiomeSounds(@Nonnull final Player player) {
        this.biomes.tick();
        WORK_MAP.clear();
        UNIFORM_SET.clear();
        VILLAGE_SET.clear();

        final boolean surround = Config.CLIENT.sound.enable3DAmbience.get();
        boolean surveyDirections = false;

        // Only gather data if the player is alive. If the player is dead the biome sounds will cease playing.
        if (player.isAlive()) {

            final boolean biomeSounds = doBiomeSounds();

            if (biomeSounds) {
                generateBiomeSounds();
                surveyDirections = surround && !WORK_MAP.isEmpty();
            }

            final ObjectArray<IAcoustic> playerSounds = new ObjectArray<>();
            BiomeLibrary.PLAYER_INFO.findSoundMatches(playerSounds);
            final ObjectArray<IAcoustic> villageSounds = new ObjectArray<>();
            BiomeLibrary.VILLAGE_INFO.findSoundMatches(villageSounds);
            VILLAGE_SET.addAll(villageSounds);
            playerSounds.addAll(villageSounds);
            playerSounds.forEach(fx -> WORK_MAP.put(fx, 1.0F));
            UNIFORM_SET.addAll(playerSounds);

            if (biomeSounds) {
                final BiomeInfo playerBiome = CommonState.getPlayerBiome();
                final IAcoustic sound = playerBiome.getSpotSound(RANDOM);
                if (sound != null)
                    sound.playNear(player, SPOT_SOUND_MIN_RANGE, SPOT_SOUND_MAX_RANGE);
            }

            final IAcoustic sound = BiomeLibrary.PLAYER_INFO.getSpotSound(RANDOM);
            if (sound != null)
                sound.playNear(player, SPOT_SOUND_MIN_RANGE, SPOT_SOUND_MAX_RANGE);
        }

        if (surround) {
            if (surveyDirections)
                this.directions.survey();
            else
                this.directions.clear();
            queueSurroundSounds();
            queueAmbientSounds(true);
        } else {
            fadeAllSurround();
            queueAmbientSounds(false);
        }
    }

    private void fadeAllSurround() {
        this.surroundEmitters.removeIf(entry -> {
            if (entry.isDonePlaying()) {
                entry.stop();
                return true;
            }
            if (!entry.isFading())
                entry.fade();
            return false;
        });
    }

    @Nonnull
    private float[] surroundGains(@Nonnull final IAcoustic acoustic, final float directionality) {
        final float[] sectors = UNIFORM_SET.contains(acoustic) ? null : this.directions.getSectorWeights(acoustic);
        return AmbienceDirections.computeGains(sectors, directionality);
    }

    private void queueSurroundSounds() {
        final float directionality = Config.CLIENT.sound.ambienceDirectionality.get() / 100F;

        this.surroundEmitters.removeIf(entry -> {
            if (entry.isDonePlaying()) {
                entry.stop();
                return true;
            }
            final float volume = WORK_MAP.getFloat(entry.getSource());
            if (volume > 0) {
                entry.setVolumeThrottle(volume);
                entry.setGains(surroundGains(entry.getSource(), directionality));
                if (entry.isFading())
                    entry.unfade();
                WORK_MAP.removeFloat(entry.getSource());
            } else if (!entry.isFading()) {
                entry.fade();
            }
            return false;
        });

        // Any sounds left in the list are new and need an emitter group created.
        final ObjectArray<IAcoustic> handled = new ObjectArray<>();
        WORK_MAP.forEach((fx, volume) -> {
            final IAcousticFactory factory = fx.getFactory();
            if (factory != null && !factory.isContinuous())
                return;
            handled.add(fx);
            final SurroundAcousticEmitter e = SurroundAcousticEmitter.create(fx, surroundGains(fx, directionality));
            if (e != null) {
                e.setVolumeThrottle(volume);
                this.surroundEmitters.add(e);
            }
        });
        handled.forEach(WORK_MAP::removeFloat);
    }

    private void queueAmbientSounds(final boolean surround) {
        // Iterate through the existing emitters:
        // * If done, remove
        // * If not in the incoming list, fade
        // * If it does exist, update volume throttle and unfade if needed
        this.emitters.removeIf(entry -> {
            if (entry.isDonePlaying()) {
                return true;
            }
            final float volume = WORK_MAP.getFloat(entry.getSource());
            if (volume > 0) {
                entry.setVolumeThrottle(volume);
                if (entry.isFading())
                    entry.unfade();
                WORK_MAP.removeFloat(entry.getSource());
            } else if (!entry.isFading()) {
                entry.fade();
            }
            return false;
        });

        // Any sounds left in the list are new and need an emitter created.
        WORK_MAP.forEach((fx, volume) -> {
            final BackgroundAcousticEmitter e = surround && VILLAGE_SET.contains(fx) ? AnchoredAcousticEmitter.create(fx) : new BackgroundAcousticEmitter(fx);
            if (e == null)
                return;
            e.setVolumeThrottle(volume);
            this.emitters.add(e);
        });
    }

    public void clearSounds() {
        this.emitters.forEach(BackgroundAcousticEmitter::stop);
        this.emitters.clear();
        this.surroundEmitters.forEach(SurroundAcousticEmitter::stop);
        this.surroundEmitters.clear();
        this.directions.clear();
        WORK_MAP.clear();
        UNIFORM_SET.clear();
        VILLAGE_SET.clear();
        AudioEngine.stopAll();
    }

    @Override
    protected void registerEvents() {
        listen(DiagnosticEvent.BUS, Priority.HIGH, this::diagnostics);
    }

    public void diagnostics(@Nonnull final DiagnosticEvent event) {
        if (Config.CLIENT.logging.enableLogging.get()) {
            this.emitters.forEach(backgroundAcousticEmitter -> event.getLeft().add("EMITTER: " + backgroundAcousticEmitter.toString()));
            this.surroundEmitters.forEach(surround -> event.getLeft().add("SURROUND: " + surround.toString()));
        }
    }
}
