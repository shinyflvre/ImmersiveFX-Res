/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.environs.handlers;

import dynamiclabs.immersivefx.lib.GameUtils;
import dynamiclabs.immersivefx.lib.random.XorShiftRandom;
import dynamiclabs.immersivefx.sndctrl.api.acoustics.IAcoustic;
import dynamiclabs.immersivefx.sndctrl.api.acoustics.IAcousticFactory;
import dynamiclabs.immersivefx.sndctrl.audio.SoundState;
import dynamiclabs.immersivefx.sndctrl.audio.SurroundSoundInstance;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

final class AnchoredAcousticEmitter extends BackgroundAcousticEmitter {

    private static final double RANGE_SQ = 64D * 64D;
    private static final double MIN_DISTANCE = 2D;
    private static final int RESCAN_TICKS = 20;
    private static final Random RANDOM = new XorShiftRandom();

    @Nonnull
    private final SurroundSoundInstance sound;
    @Nonnull
    private final Predicate<Entity> preferred;
    @Nullable
    private Entity anchor;
    private boolean wasPlaying;
    private int rescan;

    private AnchoredAcousticEmitter(@Nonnull final IAcoustic acoustic, @Nonnull final SurroundSoundInstance sound) {
        super(acoustic, sound);
        this.sound = sound;
        this.preferred = preferredFor(acoustic);
        pickAnchor();
        aim();
    }

    @Nullable
    static AnchoredAcousticEmitter create(@Nonnull final IAcoustic acoustic) {
        final IAcousticFactory factory = acoustic.getFactory();
        if (factory == null)
            return null;
        return new AnchoredAcousticEmitter(acoustic, new SurroundSoundInstance(factory.createSound(), Vec3.ZERO, 0F, 1F));
    }

    @Override
    public void tick() {
        final boolean playing = this.sound.getState() == SoundState.PLAYING;
        if (this.wasPlaying && !playing) {
            pickAnchor();
        } else if (!playing && !isValid(this.anchor) && --this.rescan <= 0) {
            pickAnchor();
        }
        this.wasPlaying = playing;
        if (!playing || isValid(this.anchor))
            aim();
        super.tick();
    }

    private static boolean isValid(@Nullable final Entity entity) {
        if (entity == null || entity.isRemoved())
            return false;
        final Vec3 listener = SurroundSoundInstance.getListenerPosition();
        return listener != null && entity.distanceToSqr(listener) <= RANGE_SQ;
    }

    private void aim() {
        final Vec3 listener = SurroundSoundInstance.getListenerPosition();
        if (listener == null || this.anchor == null) {
            this.sound.setOffset(Vec3.ZERO);
            return;
        }
        final Vec3 dir = this.anchor.getEyePosition().subtract(listener);
        final double len = dir.length();
        this.sound.setOffset(len < MIN_DISTANCE ? Vec3.ZERO : dir.scale(SurroundAcousticEmitter.EMITTER_DISTANCE / len));
    }

    private void pickAnchor() {
        this.rescan = RESCAN_TICKS;
        this.anchor = null;
        final ClientLevel world = GameUtils.getWorld();
        final Vec3 listener = SurroundSoundInstance.getListenerPosition();
        if (world == null || listener == null)
            return;
        final List<Entity> favoured = new ArrayList<>();
        final List<Entity> villagers = new ArrayList<>();
        for (final Entity e : world.entitiesForRendering()) {
            if (e.isRemoved() || e.distanceToSqr(listener) > RANGE_SQ)
                continue;
            if (this.preferred.test(e))
                favoured.add(e);
            else if (e instanceof Villager)
                villagers.add(e);
        }
        final List<Entity> pool = favoured.isEmpty() ? villagers : favoured;
        if (!pool.isEmpty())
            this.anchor = pool.get(RANDOM.nextInt(pool.size()));
    }

    @Nonnull
    private static Predicate<Entity> preferredFor(@Nonnull final IAcoustic acoustic) {
        final String path = acoustic.getName().getPath();
        if (path.contains("anvil"))
            return e -> e instanceof Villager v && (hasProfession(v, VillagerProfession.ARMORER) || hasProfession(v, VillagerProfession.WEAPONSMITH) || hasProfession(v, VillagerProfession.TOOLSMITH));
        if (path.contains("rooster"))
            return e -> e instanceof Chicken || (e instanceof Villager v && hasProfession(v, VillagerProfession.FARMER));
        return e -> false;
    }

    private static boolean hasProfession(@Nonnull final Villager villager, @Nonnull final ResourceKey<VillagerProfession> profession) {
        return villager.getVillagerData().profession().is(profession);
    }
}
