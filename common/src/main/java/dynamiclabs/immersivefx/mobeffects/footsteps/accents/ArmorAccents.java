/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
/*
 *  Dynamic Surroundings
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

package dynamiclabs.immersivefx.mobeffects.footsteps.accents;

import dynamiclabs.immersivefx.mobeffects.config.Config;
import dynamiclabs.immersivefx.mobeffects.library.ItemData;
import dynamiclabs.immersivefx.mobeffects.library.ItemLibrary;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import dynamiclabs.immersivefx.lib.collections.ObjectArray;
import dynamiclabs.immersivefx.sndctrl.api.acoustics.IAcoustic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class ArmorAccents implements IFootstepAccentProvider {

    @Nullable
    protected IAcoustic resolveArmor(@Nonnull final ItemStack stack) {
        final ItemData id = ItemLibrary.getItemData(stack);
        if (id.isArmor()) {
            return ((ItemData.ArmorItemData) id).getArmorSound(stack);
        }
        return null;
    }

    protected IAcoustic resolveFootArmor(@Nonnull final ItemStack stack) {
        final ItemData id = ItemLibrary.getItemData(stack);
        if (id.isArmor()) {
            return ((ItemData.ArmorItemData) id).getFootArmorSound(stack);
        }
        return null;
    }

    @Override
    public boolean isEnabled() {
        return Config.CLIENT.footsteps.enableArmorAccents.get();
    }

    @Override
    public void provide(
            @Nonnull final LivingEntity entity,
            @Nonnull final BlockPos blockPos,
            @Nonnull final BlockState posState,
            @Nonnull final ObjectArray<IAcoustic> acoustics) {
        final ItemStack armor = ItemData.effectiveArmorItemStack(entity);
        final ItemStack foot = ItemData.footArmorItemStack(entity);
        final IAcoustic armorAddon = resolveArmor(armor);
        IAcoustic footAddon = resolveFootArmor(foot);

        if (armorAddon != null) {
            acoustics.add(armorAddon);
            if (armorAddon == footAddon)
                footAddon = null;
        }

        if (footAddon != null)
            acoustics.add(footAddon);
    }

}