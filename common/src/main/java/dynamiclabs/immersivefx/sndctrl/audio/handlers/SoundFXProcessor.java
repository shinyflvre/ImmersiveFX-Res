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

package dynamiclabs.immersivefx.sndctrl.audio.handlers;

import dynamiclabs.immersivefx.platform.event.ClientTickEvents;
import dynamiclabs.immersivefx.platform.event.Priority;
import dynamiclabs.immersivefx.platform.event.SoundEngineEvents;
import dynamiclabs.immersivefx.platform.event.SoundSourceEvent;
import net.minecraft.client.Minecraft;
import dynamiclabs.immersivefx.lib.Singleton;
import dynamiclabs.immersivefx.lib.Utilities;
import dynamiclabs.immersivefx.lib.collections.ObjectArray;
import dynamiclabs.immersivefx.lib.events.DiagnosticEvent;
import dynamiclabs.immersivefx.lib.logging.IModLog;
import dynamiclabs.immersivefx.lib.threading.Worker;
import dynamiclabs.immersivefx.sndctrl.SoundControl;
import dynamiclabs.immersivefx.sndctrl.api.sound.Category;
import dynamiclabs.immersivefx.sndctrl.api.sound.ISoundCategory;
import dynamiclabs.immersivefx.sndctrl.audio.Conversion;
import dynamiclabs.immersivefx.sndctrl.audio.SoundUtils;
import dynamiclabs.immersivefx.sndctrl.audio.SurroundSoundInstance;
import dynamiclabs.immersivefx.sndctrl.misc.IMixinSoundContext;
import dynamiclabs.immersivefx.sndctrl.mixins.AccessorChannel;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import com.mojang.blaze3d.audio.SoundBuffer;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.resources.sounds.SoundInstance;
import com.mojang.blaze3d.audio.Channel;
import net.minecraft.ChatFormatting;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.openal.AL10;
import dynamiclabs.immersivefx.sndctrl.config.Config;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Supplier;

public final class SoundFXProcessor {

    private static final IModLog LOGGER = SoundControl.LOGGER.createChild(SoundFXProcessor.class);
    private static final int SOUND_PROCESS_ITERATION = 1000 / 20;   // Match MC client tick rate

    /**
     * Sound categories that are ignored when determining special effects.  Things like MASTER, and MUSIC.
     */
    private static final Set<ISoundCategory> IGNORE_CATEGORIES = new ReferenceOpenHashSet<>(4);

    static boolean isAvailable;
    // Sparse array to hold references to the SoundContexts of playing sounds
    private static SourceContext[] sources;
    private static Worker soundProcessor;

    // Use our own thread pool avoiding the common pool.  Thread allocation is better controlled, and we won't run
    // into/cause any problems with other tasks in the common pool.
    private static final Singleton<ExecutorService> threadPool = new Singleton<>(() ->{
        int threads = Config.CLIENT.sound.backgroundThreadWorkers.get();
        if (threads == 0)
            threads = 2;
        LOGGER.info("Threads allocated to SoundControl sound processor: %d", threads);
        final java.util.concurrent.atomic.AtomicInteger count = new java.util.concurrent.atomic.AtomicInteger();
        return Executors.newFixedThreadPool(threads, r -> {
            final Thread t = new Thread(r, "SoundControl FX " + count.incrementAndGet());
            t.setDaemon(true);
            return t;
        });
    });

    private static WorldContext worldContext = new WorldContext();

    private static final Set<Channel> FORCE_MONO = ConcurrentHashMap.newKeySet();

    static {
        SoundEngineEvents.SOURCE_STARTED.register(Priority.NORMAL, e -> {
            if (!e.streaming())
                onPlaySoundSource(e);
        });
        ClientTickEvents.START.register(Priority.HIGH, SoundFXProcessor::onClientTick);
        DiagnosticEvent.BUS.register(Priority.HIGH, SoundFXProcessor::onGatherText);
    }

    private SoundFXProcessor() {
    }

    @Nonnull
    public static WorldContext getWorldContext() {
        return worldContext;
    }

    /**
     * Indicates if the SoundFX feature is available.
     *
     * @return true if the feature is available, false otherwise.
     */
    public static boolean isAvailable() {
        return isAvailable;
    }

    public static void initialize() {
        Effects.initialize();

        sources = new SourceContext[SoundUtils.getMaxSounds()];

        if (soundProcessor == null) {
            soundProcessor = new Worker(
                    "SoundControl Sound Processor",
                    SoundFXProcessor::processSounds,
                    SOUND_PROCESS_ITERATION,
                    LOGGER
            );
            soundProcessor.start();
        }

        // Configure our categories to ignore
        IGNORE_CATEGORIES.clear();
        Category.getCategories().forEach(c -> {
            if (!c.doEffects())
                IGNORE_CATEGORIES.add(c);
        });

        isAvailable = true;
    }

    public static void deinitialize() {
        if (isAvailable()) {
            isAvailable = false;
            if (soundProcessor != null) {
                soundProcessor.stop();
                soundProcessor = null;
            }
            if (sources != null) {
                Arrays.fill(sources, null);
                sources = null;
            }
            Effects.deinitialize();
        }
    }

    /**
     * Callback hook from an injection.  This callback is made on the client thread after the sound source
     * is created, but before it is configured.
     *
     * @param sound The sound that is going to play
     * @param entry The ChannelManager.Entry instance for the sound play
     */
    public static void onSoundPlay(@Nonnull final SoundInstance sound, @Nonnull final ChannelAccess.ChannelHandle entry) {

        if (sound instanceof SurroundSoundInstance) {
            entry.execute(FORCE_MONO::add);
            return;
        }

        if (!isAvailable())
            return;

        final Optional<ISoundCategory> cat = Category.getCategory(sound);
        if (cat.isPresent() && IGNORE_CATEGORIES.contains(cat.get()))
            return;

        // Double suplex!  Queue the operation on the sound executor to do the config work.  This should queue in
        // behind any attempt at getting a sound source.
        entry.execute(source -> {
            final int id = ((AccessorChannel) source).sndctrl_getSource();
            final SourceContext[] array = sources;
            if (id > 0 && array != null && id <= array.length) {
                final SourceContext ctx = new SourceContext();
                ctx.attachSound(sound);
                ctx.enable();
                ctx.exec();
                ((IMixinSoundContext) source).setData(ctx);
                array[id - 1] = ctx;
            }
        });
    }

    /**
     * Callback hook from an injection.  Will be invoked by the sound processing thread when checking status which
     * essentially is a "tick".
     *
     * @param source SoundSource being ticked
     */
    public static void tick(@Nonnull final Channel source) {
        final SourceContext ctx = ((IMixinSoundContext)source).getData();
        if (ctx != null)
            ctx.tick(((AccessorChannel) source).sndctrl_getSource());
    }

    /**
     * Injected into SoundSource and will be invoked when a sound source is being terminated.
     *
     * @param source The sound source that is stopping
     */
    public static void stopSoundPlay(@Nonnull final Channel source) {
        FORCE_MONO.remove(source);
        final IMixinSoundContext data = (IMixinSoundContext) source;
        final SourceContext ctx = data.getData();
        if (ctx != null) {
            data.setData(null);
            final int id = ((AccessorChannel) source).sndctrl_getSource();
            final SourceContext[] array = sources;
            if (array != null && id > 0 && id <= array.length && array[id - 1] == ctx)
                array[id - 1] = null;
        }
    }

    /**
     * Injected into SoundSource and will be invoked when a non-streaming sound data stream is attached to the
     * SoundSource.  Take the opportunity to convert the audio stream into mono format if needed.
     *
     * @param source SoundSource for which the audio buffer is being generated
     * @param buffer The buffer in question.
     */
    @Nonnull
    public static SoundBuffer playBuffer(@Nonnull final Channel source, @Nonnull final SoundBuffer buffer) {

        if (FORCE_MONO.remove(source))
            return Conversion.convert(buffer);

        // If disabled return
        if (!Config.CLIENT.sound.enableMonoConversion.get())
            return buffer;

        final SourceContext ctx = ((IMixinSoundContext) source).getData();

        // If there is no context attached and conversion is enabled do it.  This can happen if enhanced sound
        // processing is turned off.  If there is a context, make sure that the sound is attenuated.
        boolean doConversion = ctx == null || (ctx.getSound() != null && ctx.getSound().getAttenuation() != SoundInstance.Attenuation.NONE
                && !isAtListener(ctx.getSound()));

        if (doConversion)
            return Conversion.convert(buffer);

        return buffer;
    }

    private static final double STEREO_KEEP_DISTANCE_SQ = 2.0D * 2.0D;

    private static boolean isAtListener(@Nonnull final SoundInstance sound) {
        if (sound.isRelative())
            return sound.getX() * sound.getX() + sound.getY() * sound.getY() + sound.getZ() * sound.getZ() <= STEREO_KEEP_DISTANCE_SQ;
        final WorldContext ctx = getWorldContext();
        final Vec3 eye = ctx == null ? null : ctx.playerEyePosition;
        if (eye == null || Vec3.ZERO.equals(eye))
            return false;
        return eye.distanceToSqr(sound.getX(), sound.getY(), sound.getZ()) <= STEREO_KEEP_DISTANCE_SQ;
    }

    public static void onPlaySoundSource(@Nonnull final SoundSourceEvent event) {
        if (event.getSound() instanceof SurroundSoundInstance surround) {
            try {
                SurroundSoundInstance.applyStartOffset(event.getChannel(), surround);
            } catch (@Nonnull final Throwable t) {
                LOGGER.error(t, "Unable to set the start offset of %s", surround.toString());
            }
        }
    }

    /**
     * Invoked on a client tick. Establishes the current world context for further computation..
     *
     * @param event Event trigger in question.
     */
    public static void onClientTick(@Nonnull final Minecraft event) {
        if (isAvailable()) {
            worldContext = new WorldContext();
        }
    }

    /**
     * Separate thread for evaluating the environment for the sound play.  These routines can get a little heavy
     * so offloading to a separate thread to keep it out of either the client tick or sound engine makes sense.
     */
    private static void processSounds() {
        try {
            final ExecutorService pool = threadPool.get();
            assert pool != null;
            final ObjectArray<Future<?>> tasks = new ObjectArray<>(256);
            final SourceContext[] array = sources;
            if (array == null)
                return;
            for (int i = 0; i < array.length; i++) {
                final SourceContext ctx = array[i];
                if (ctx != null && ctx.shouldExecute()) {
                    tasks.add(pool.submit(ctx));
                }
            }

            if (tasks.size() > 0)
                tasks.forEach(t -> {
                    try {
                        t.get();
                    } catch (InterruptedException | ExecutionException ignored) {
                    }
                });
        } catch (@Nonnull final Throwable t) {
            LOGGER.error(t, "Error in SoundContext ForkJoinPool");
        }
    }

    public static void onGatherText(@Nonnull final DiagnosticEvent event) {
        if (isAvailable() && soundProcessor != null) {
            final String msg = soundProcessor.getDiagnosticString();
            if (!StringUtils.isEmpty(msg))
                event.getLeft().add(ChatFormatting.GREEN + msg);
        }
    }

    /**
     * Validates that the current OpenAL state is not in error.  If in an error state an exception will be thrown.
     *
     * @param msg Optional message to be displayed along with error data
     */
    public static void validate(@Nonnull final String msg) {
        validate(() -> msg);
    }

    /**
     * Validates that the current OpenAL state is not in error.  If in an error state an exception will be thrown.
     *
     * @param err Supplier for the error message to post with exception info
     */
    public static void validate(@Nullable final Supplier<String> err) {
        final int error = AL10.alGetError();
        if (error != AL10.AL_NO_ERROR) {
            String errorName = AL10.alGetString(error);
            if (StringUtils.isEmpty(errorName))
                errorName = Integer.toString(error);
            final String msg = Utilities.firstNonNull(err != null ? err.get() : null, "NONE");
            throw new IllegalStateException(String.format("OpenAL Error: %s [%s]", errorName, msg));
        }
    }
}
