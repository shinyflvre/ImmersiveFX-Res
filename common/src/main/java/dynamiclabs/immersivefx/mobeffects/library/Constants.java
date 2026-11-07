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

package dynamiclabs.immersivefx.mobeffects.library;

import dynamiclabs.immersivefx.mobeffects.MobEffects;
import dynamiclabs.immersivefx.mobeffects.config.Config;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import dynamiclabs.immersivefx.sndctrl.api.acoustics.AcousticEvent;
import dynamiclabs.immersivefx.sndctrl.api.acoustics.IAcoustic;
import dynamiclabs.immersivefx.sndctrl.api.sound.Category;
import dynamiclabs.immersivefx.sndctrl.api.sound.ISoundCategory;
import dynamiclabs.immersivefx.sndctrl.audio.acoustic.NullAcoustic;

public final class Constants {
    private Constants() {

    }

    public static final ISoundCategory FOOTSTEPS = new Category("footsteps", "mobeffects.soundcategory.footsteps", () -> Config.CLIENT.footsteps.footstepVolume.get() / 100F, (v) -> Config.CLIENT.footsteps.footstepVolume.set((int)(v * 100)));
    public static final ISoundCategory TOOLBAR = new Category("toolbar", "mobeffects.soundcategory.toolbar", () -> Config.CLIENT.effects.toolbarVolume.get() / 100F, (v) -> Config.CLIENT.effects.toolbarVolume.set((int)(v * 100)));

    public static final Identifier NONE = Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, "empty");

    public static final IAcoustic EMPTY = new NullAcoustic(NONE);
    public static final IAcoustic NOT_EMITTER = new NullAcoustic(Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, "not_emitter"));
    public static final IAcoustic MESSY_GROUND = new NullAcoustic(Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, "messy_ground"));

    public static final AcousticEvent WALK = new AcousticEvent(Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, "walk"), null);
    public static final AcousticEvent WANDER = new AcousticEvent(Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, "wander"), null);
    public static final AcousticEvent SWIM = new AcousticEvent(Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, "swim"), null);
    public static final AcousticEvent RUN = new AcousticEvent(Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, "run"), WALK);
    public static final AcousticEvent JUMP = new AcousticEvent(Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, "jump"), WANDER);
    public static final AcousticEvent LAND = new AcousticEvent(Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, "land"), RUN);
    public static final AcousticEvent CLIMB = new AcousticEvent(Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, "climb"), WALK);
    public static final AcousticEvent CLIMB_RUN = new AcousticEvent(Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, "climb_run"), RUN);
    public static final AcousticEvent DOWN = new AcousticEvent(Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, "down"), WALK);
    public static final AcousticEvent DOWN_RUN = new AcousticEvent(Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, "down_run"), RUN);
    public static final AcousticEvent UP = new AcousticEvent(Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, "up"), WALK);
    public static final AcousticEvent UP_RUN = new AcousticEvent(Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, "up_run"), RUN);

    public static final Identifier LIGHT_ARMOR = Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, "armor_light");
    public static final Identifier MEDIUM_ARMOR = Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, "armor_medium");
    public static final Identifier HEAVY_ARMOR = Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, "armor_heavy");
    public static final Identifier CRYSTAL_ARMOR = Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, "armor_crystal");
    public static final Identifier LIGHT_FOOT_ARMOR = LIGHT_ARMOR;
    public static final Identifier MEDIUM_FOOT_ARMOR = Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, "medium_foot");
    public static final Identifier HEAVY_FOOT_ARMOR = Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, "heavy_foot");
    public static final Identifier CRYSTAL_FOOT_ARMOR = Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, "crystal_foot");
    public static final Identifier NETHERITE_ARMOR = SoundEvents.ARMOR_EQUIP_NETHERITE.value().location();

    public static final Identifier LEATHER_ARMOR_EQUIP = LIGHT_ARMOR;
    public static final Identifier CHAIN_ARMOR_EQUIP = MEDIUM_ARMOR;
    public static final Identifier CRYSTAL_ARMOR_EQUIP = CRYSTAL_ARMOR;
    public static final Identifier PLATE_ARMOR_EQUIP = HEAVY_ARMOR;
    public static final Identifier NETHERITE_ARMOR_EQUIP = NETHERITE_ARMOR;
    public static final Identifier UTILITY_EQUIP = Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, "utility.equip");
    public static final Identifier TOOL_EQUIP = Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, "tool.equip");
    public static final Identifier TOOL_SWING = Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, "tool.swing");
    public static final Identifier SHIELD_USE = Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, "shield.use");
    public static final Identifier SHIELD_EQUIP = Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, "shield.equip");
    public static final Identifier SWORD_SWING = Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, "sword.swing");
    public static final Identifier SWORD_EQUIP = Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, "sword.equip");
    public static final Identifier AXE_SWING = Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, "blunt.swing");
    public static final Identifier AXE_EQUIP = Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, "blunt.equip");
    public static final Identifier BOW_PULL = Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, "bow.pull");
    public static final Identifier BOW_EQUIP = Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, "bow.equip");
    public static final Identifier BOOK_EQUIP = Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, "pageflip");
    public static final Identifier POTION_EQUIP = Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, "potion.equip");

}
