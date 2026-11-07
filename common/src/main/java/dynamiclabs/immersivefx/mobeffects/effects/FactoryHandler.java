/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
/*
 *  Dynamic Surroundings: Mob Effects
 *  Copyright (C) 2019  OreCruncher
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

package dynamiclabs.immersivefx.mobeffects.effects;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.Identifier;
import dynamiclabs.immersivefx.mobeffects.library.EffectLibrary;
import dynamiclabs.immersivefx.sndctrl.api.effects.AbstractEntityEffect;
import dynamiclabs.immersivefx.sndctrl.api.effects.IEntityEffectFactoryHandler;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class FactoryHandler implements IEntityEffectFactoryHandler {

    private final Identifier name;
    private final Function<LivingEntity, AbstractEntityEffect> factory;

    public FactoryHandler(@Nonnull final Identifier res, @Nonnull final Function<LivingEntity, AbstractEntityEffect> factory) {
        this.name = res;
        this.factory = factory;
    }

    @Override
    @Nonnull
    public Identifier getName() {
        return this.name;
    }

    @Override
    public boolean appliesTo(@Nonnull final LivingEntity entity) {
        return EffectLibrary.hasEffect(entity, getName());
    }

    @Override
    @Nonnull
    public AbstractEntityEffect get(@Nonnull final LivingEntity entity) {
        return this.factory.apply(entity);
    }
}
