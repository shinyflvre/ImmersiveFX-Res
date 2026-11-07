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

package dynamiclabs.immersivefx.dsurround.mixins;

import dynamiclabs.immersivefx.specialfx.BloodParticles;
import dynamiclabs.immersivefx.specialfx.BloodSounds;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntity {

    @Inject(method = "hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z", at = @At("HEAD"))
    private void dsurround_clientAttack(final DamageSource source, final float damage, final CallbackInfoReturnable<Boolean> cir) {
        final Entity self = (Entity) (Object) this;
        if (self instanceof LivingEntity living && self.level().isClientSide()) {
            BloodParticles.onClientAttack(living, source, damage);
            BloodSounds.onLocalAttack(living, source);
        }
    }
}
