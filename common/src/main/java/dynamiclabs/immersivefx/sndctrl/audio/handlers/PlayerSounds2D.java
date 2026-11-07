/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.sndctrl.audio.handlers;

import dynamiclabs.immersivefx.platform.StreamingSoundInstance;
import dynamiclabs.immersivefx.platform.Platform;
import dynamiclabs.immersivefx.platform.event.PlaySoundEvent;
import dynamiclabs.immersivefx.ImmersiveFX;
import dynamiclabs.immersivefx.sndctrl.config.Config;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.phys.Vec3;

public final class PlayerSounds2D {
    public static final double MAX_DISTANCE = 2.5D;
    public static final double UNDERWATER_DISTANCE = 1.0D;

    public static final List<String> DEFAULT_SOUNDS = List.of(
            "minecraft:entity.player.splash",
            "minecraft:entity.player.splash.high_speed",
            "minecraft:entity.player.swim",
            "minecraft:ambient.underwater.enter",
            "minecraft:ambient.underwater.exit",
            "mobeffects:water.water_through",
            "mobeffects:water.water_wander",
            "mobeffects:water_stereofix.water_through",
            "mobeffects:water_stereofix.water_wander");

    private static List<? extends String> cachedList;
    private static Set<Identifier> cachedIds = Set.of();

    private PlayerSounds2D() {
    }

    private static Set<Identifier> ids() {
        final List<? extends String> list = Config.CLIENT.sound.playerSounds2D.get();
        if (list != cachedList) {
            final Set<Identifier> set = new HashSet<>();
            for (final String s : list) {
                final Identifier id = Identifier.tryParse(s.trim());
                if (id != null)
                    set.add(id);
            }
            cachedIds = set;
            cachedList = list;
        }
        return cachedIds;
    }

    public static void onPlaySound(@Nonnull final PlaySoundEvent event) {
        final SoundInstance sound = event.getSound();
        if (sound == null || sound.isRelative() || sound.getAttenuation() == SoundInstance.Attenuation.NONE
                || sound instanceof TickableSoundInstance)
            return;
        if (!Config.CLIENT.sound.enable2DPlayerSounds.get() || !ids().contains(sound.getIdentifier()))
            return;
        final LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.distanceToSqr(sound.getX(), sound.getY(), sound.getZ()) > MAX_DISTANCE * MAX_DISTANCE)
            return;
        if (player.isEyeInFluid(FluidTags.WATER)) {
            final Vec3 eye = player.getEyePosition();
            Vec3 dir = new Vec3(sound.getX(), sound.getY(), sound.getZ()).subtract(eye);
            dir = dir.lengthSqr() < 0.01D ? new Vec3(0, -1, 0) : dir.normalize();
            final Vec3 pos = eye.add(dir.scale(UNDERWATER_DISTANCE));
            event.setSound(new Relative(sound, false, pos.x, pos.y, pos.z, SoundInstance.Attenuation.LINEAR));
        } else {
            event.setSound(new Relative(sound, true, 0D, 0D, 0D, SoundInstance.Attenuation.NONE));
        }
    }

    private static final class Relative implements SoundInstance, StreamingSoundInstance {
        private final SoundInstance delegate;
        private final boolean relative;
        private final double x, y, z;
        private final Attenuation attenuation;

        Relative(@Nonnull final SoundInstance delegate, final boolean relative, final double x, final double y, final double z,
                 @Nonnull final Attenuation attenuation) {
            this.delegate = delegate;
            this.relative = relative;
            this.x = x;
            this.y = y;
            this.z = z;
            this.attenuation = attenuation;
        }

        @Override
        public Identifier getIdentifier() {
            return this.delegate.getIdentifier();
        }

        @Override
        public @Nullable WeighedSoundEvents getOrResolve(final SoundManager soundManager) {
            return this.delegate.getOrResolve(soundManager);
        }

        @Override
        public @Nullable Sound getSound() {
            return this.delegate.getSound();
        }

        @Override
        public @Nullable WeighedSoundEvents getSoundEvent() {
            return this.delegate.getSoundEvent();
        }

        @Override
        public SoundSource getSource() {
            return this.delegate.getSource();
        }

        @Override
        public boolean isLooping() {
            return this.delegate.isLooping();
        }

        @Override
        public boolean isRelative() {
            return this.relative;
        }

        @Override
        public int getDelay() {
            return this.delegate.getDelay();
        }

        @Override
        public float getVolume() {
            return this.delegate.getVolume();
        }

        @Override
        public float getPitch() {
            return this.delegate.getPitch();
        }

        @Override
        public double getX() {
            return this.x;
        }

        @Override
        public double getY() {
            return this.y;
        }

        @Override
        public double getZ() {
            return this.z;
        }

        @Override
        public Attenuation getAttenuation() {
            return this.attenuation;
        }

        @Override
        public boolean canStartSilent() {
            return this.delegate.canStartSilent();
        }

        @Override
        public boolean canPlaySound() {
            return this.delegate.canPlaySound();
        }

        @Override
        public CompletableFuture<AudioStream> getStream(final SoundBufferLibrary soundBuffers, final Sound sound, final boolean looping) {
            return Platform.get().getStream(this.delegate, soundBuffers, sound, looping);
        }
    }
}
