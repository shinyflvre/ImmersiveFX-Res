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
import dynamiclabs.immersivefx.specialfx.DropItemSounds;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {

    @Inject(method = "handleDamageEvent(Lnet/minecraft/world/damagesource/DamageSource;)V", at = @At("HEAD"))
    private void dsurround_hurtStatus(final DamageSource source, final CallbackInfo ci) {
        final LivingEntity self = (LivingEntity) (Object) this;
        BloodParticles.onClientHurtStatus(self);
        BloodSounds.onDamageEvent(self, source);
    }

    @Inject(method = "drop(Lnet/minecraft/world/item/ItemStack;ZLnet/minecraft/util/Prediction;)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At("HEAD"))
    private void dsurround_predictedDrop(final ItemStack itemStack, final boolean thrownFromHand, final Prediction prediction,
                                         final CallbackInfoReturnable<ItemEntity> cir) {
        if ((Object) this instanceof LocalPlayer player && player.level().isClientSide() && !itemStack.isEmpty()) {
            DropItemSounds.onLocalDrop(player);
        }
    }
}
