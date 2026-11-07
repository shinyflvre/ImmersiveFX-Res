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

package dynamiclabs.immersivefx.sndctrl.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.audio.Library;
import dynamiclabs.immersivefx.sndctrl.audio.SoundUtils;
import dynamiclabs.immersivefx.sndctrl.config.Config;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import java.nio.IntBuffer;

@Mixin(Library.class)
public class MixinSoundSystem {

    @Shadow
    private long currentDevice;

    @ModifyConstant(method = "init", constant = @Constant(intValue = 8))
    private int initialize(int v) {
        return Config.CLIENT.sound.streamingSoundCount.get();
    }

    @ModifyReturnValue(method = "createAttributes", at = @At("RETURN"))
    private IntBuffer sndctrl_contextAttributes(IntBuffer original, @Local(argsOnly = true) MemoryStack stack) {
        return SoundUtils.configureContextAttributes(stack, original, this.currentDevice);
    }
}
