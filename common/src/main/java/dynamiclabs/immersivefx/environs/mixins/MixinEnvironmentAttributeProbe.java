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

package dynamiclabs.immersivefx.environs.mixins;

import dynamiclabs.immersivefx.environs.fog.BiomeFogColor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.attribute.EnvironmentAttributeProbe;
import net.minecraft.world.attribute.GaussianSampler;
import net.minecraft.world.attribute.SpatialAttributeInterpolator;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnvironmentAttributeProbe.class)
public abstract class MixinEnvironmentAttributeProbe {

    @Shadow
    @Final
    private SpatialAttributeInterpolator biomeInterpolator;

    @Inject(method = "tick(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/phys/Vec3;)V", at = @At("TAIL"))
    private void environs$biomeFogColor(Level level, Vec3 position, CallbackInfo ci) {
        if (!(level instanceof ClientLevel) || !BiomeFogColor.isActive())
            return;

        this.biomeInterpolator.clear();
        GaussianSampler.sample(
                position.scale(0.25),
                level.getBiomeManager()::getNoiseBiomeAtQuart,
                (weight, biome) -> this.biomeInterpolator.accumulate(weight, BiomeFogColor.attributes(biome.value()))
        );
    }
}
