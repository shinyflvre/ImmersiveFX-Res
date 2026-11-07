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

package dynamiclabs.immersivefx.sndctrl.audio.acoustic;

import com.google.common.base.MoreObjects;
import dynamiclabs.immersivefx.lib.collections.ObjectArray;
import dynamiclabs.immersivefx.sndctrl.api.acoustics.AcousticEvent;
import dynamiclabs.immersivefx.sndctrl.api.acoustics.IAcoustic;
import dynamiclabs.immersivefx.sndctrl.api.acoustics.IAcousticFactory;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Plays a group of acoustics simultaneously creating a composite effect
 */
public class SimultaneousAcoustic implements IAcoustic {

    @Nonnull
    private final Identifier name;
    @Nonnull
    private final ObjectArray<IAcoustic> acoustics = new ObjectArray<>();

    public SimultaneousAcoustic(@Nonnull final Identifier name) {
        this.name = Objects.requireNonNull(name);
    }

    public void add(@Nonnull final IAcoustic a) {
        // Ignore null acoustics
        if (!(a instanceof NullAcoustic))
            this.acoustics.add(a);
    }

    public void trim() {
        this.acoustics.trim();
    }

    @Override
    @Nonnull
    public Identifier getName() {
        return this.name;
    }

    @Override
    public void play(@Nonnull final AcousticEvent event) {
        for (final IAcoustic a : this.acoustics)
            a.play(event);
    }

    @Override
    public void playAt(@Nonnull final BlockPos pos, @Nonnull final AcousticEvent event) {
        for (final IAcoustic a : this.acoustics)
            a.playAt(pos, event);
    }

    @Override
    public void playAt(@Nonnull final Vec3 pos, @Nonnull final AcousticEvent event) {
        for (final IAcoustic a : this.acoustics)
            a.playAt(pos, event);
    }

    @Override
    public void playNear(@Nonnull final Entity entity, @Nonnull final AcousticEvent event) {
        for (final IAcoustic a : this.acoustics)
            a.playNear(entity, event);
    }

    @Override
    public void playNear(@Nonnull final Entity entity, @Nonnull final AcousticEvent event, final int minRange, final int maxRange) {
        for (final IAcoustic a : this.acoustics)
            a.playNear(entity, event, minRange, maxRange);
    }

    @Override
    public void playBackground(@Nonnull final AcousticEvent event) {
        for (final IAcoustic a : this.acoustics)
            a.playBackground(event);
    }

    @Override
    public IAcousticFactory getFactory(@Nonnull final AcousticEvent event) {
        if (this.acoustics.size() > 0)
            return this.acoustics.get(0).getFactory();
        return null;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).addValue(getName().toString()).add("entries", this.acoustics.size()).toString();
    }
}
