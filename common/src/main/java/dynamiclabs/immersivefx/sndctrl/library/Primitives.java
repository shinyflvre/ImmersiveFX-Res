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

package dynamiclabs.immersivefx.sndctrl.library;

import dynamiclabs.immersivefx.lib.SoundTypeUtils;
import dynamiclabs.immersivefx.lib.Utilities;
import dynamiclabs.immersivefx.lib.resource.ResourceUtils;
import dynamiclabs.immersivefx.mobeffects.MobEffects;
import dynamiclabs.immersivefx.mobeffects.config.Config;
import dynamiclabs.immersivefx.mobeffects.library.Constants;
import dynamiclabs.immersivefx.sndctrl.SoundControl;
import dynamiclabs.immersivefx.sndctrl.api.acoustics.IAcoustic;
import dynamiclabs.immersivefx.sndctrl.api.sound.ISoundCategory;
import dynamiclabs.immersivefx.sndctrl.audio.acoustic.AcousticCompiler;
import dynamiclabs.immersivefx.sndctrl.audio.acoustic.NullAcoustic;
import dynamiclabs.immersivefx.sndctrl.audio.acoustic.SimpleAcoustic;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

public class Primitives {

    private static final float MINECRAFT_VOLUME_SCALE = 0.15F;

    private static final String ARMOR_EQUIP_PREFIX = "primitive/armor/equip/";
    private static final String ARMOR_ACCENT_PREFIX = "primitive/armor/accent/";
    private static final String FOOTSTEP_PREFIX = "primitive/block/step/";
    private static final String VANILLA_FOOTSTEP_PREFIX = "primitive/block/step/vanilla/";
    private static final String SOUND_PREFIX = "primitive/sound/";

    private static final Identifier ARMOR_TEMPLATE = Identifier.fromNamespaceAndPath(SoundControl.MOD_ID, "templates/primitive_armor_accent.json");
    private static final Identifier FOOTSTEP_TEMPLATE = Identifier.fromNamespaceAndPath(SoundControl.MOD_ID, "templates/primitive_block_step.json");

    @Nonnull
    public static String getArmorMaterialName(@Nonnull final ResourceKey<EquipmentAsset> asset) {
        final Identifier id = asset.identifier();
        return "minecraft".equals(id.getNamespace()) ? id.getPath() : id.toString();
    }

    @Nonnull
    private static String getArmorMaterialName(@Nonnull final Equippable equippable) {
        return equippable.assetId()
                .map(Primitives::getArmorMaterialName)
                .orElseGet(() -> equippable.equipSound().value().location().toString());
    }

    @Nonnull
    public static Identifier createArmorToolbarResource(@Nonnull final ArmorMaterial material) {
        return createArmorToolbarResource(getArmorMaterialName(material.assetId()));
    }

    @Nonnull
    public static Identifier createArmorToolbarResource(@Nonnull final Equippable equippable) {
        return createArmorToolbarResource(getArmorMaterialName(equippable));
    }

    @Nonnull
    public static Identifier createArmorToolbarResource(@Nonnull final String materialName) {
        return Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, Utilities.safeResourcePath(ARMOR_EQUIP_PREFIX + materialName));
    }

    @Nonnull
    public static Identifier createArmorAccentResource(@Nonnull final ArmorMaterial material) {
        return createArmorAccentResource(getArmorMaterialName(material.assetId()));
    }

    @Nonnull
    public static Identifier createArmorAccentResource(@Nonnull final Equippable equippable) {
        return createArmorAccentResource(getArmorMaterialName(equippable));
    }

    @Nonnull
    public static Identifier createArmorAccentResource(@Nonnull final String materialName) {
        return Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, Utilities.safeResourcePath(ARMOR_ACCENT_PREFIX + materialName));
    }

    @Nonnull
    public static Identifier createFootstepResource(@Nonnull final SoundType st) {
        return createFootstepResource(SoundTypeUtils.getSoundTypeName(st));
    }

    @Nonnull
    public static Identifier createVanillaFootstepResource(@Nonnull final SoundType st) {
        final String safePath = Utilities.safeResourcePath(VANILLA_FOOTSTEP_PREFIX + SoundTypeUtils.getSoundTypeName(st));
        return Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, safePath);
    }

    @Nonnull
    public static Identifier createFootstepResource(@Nonnull final String soundType) {
        final String safePath = Utilities.safeResourcePath(FOOTSTEP_PREFIX + soundType);
        return Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, safePath);
    }

    @Nonnull
    private static Identifier createSoundLocation(@Nonnull final Identifier loc, @Nonnull final ISoundCategory category) {
        final String safePath = Utilities.safeResourcePath(SOUND_PREFIX + category.getName() + "/" + loc.toString());
        return Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, safePath);
    }

    @Nonnull
    public static IAcoustic getSound(@Nonnull final Identifier loc, @Nonnull final ISoundCategory category) {
        final Identifier resource = createSoundLocation(loc, category);
        IAcoustic acoustic = AcousticLibrary.resolve(resource);
        if (acoustic == NullAcoustic.INSTANCE) {
            final Optional<SoundEvent> se = SoundLibrary.getSound(loc);
            if (se.isPresent() && se.get() != SoundLibrary.MISSING) {
                acoustic = new SimpleAcoustic(se.get(), category);
                AcousticLibrary.addAcoustic(resource, acoustic);
            }
        }
        return acoustic;
    }

    @Nonnull
    public static IAcoustic getArmorToolbarAcoustic(@Nonnull final ArmorMaterial material) {
        return getArmorToolbarAcoustic(createArmorToolbarResource(material), material.equipSound().value());
    }

    @Nonnull
    public static IAcoustic getArmorToolbarAcoustic(@Nonnull final Equippable equippable) {
        return getArmorToolbarAcoustic(createArmorToolbarResource(equippable), equippable.equipSound().value());
    }

    @Nonnull
    private static IAcoustic getArmorToolbarAcoustic(@Nonnull final Identifier loc, @Nonnull final SoundEvent event) {
        IAcoustic acoustic = AcousticLibrary.resolve(loc);
        if (acoustic == NullAcoustic.INSTANCE) {
            if (SoundTypeUtils.isValid(event)) {
                acoustic = new SimpleAcoustic(event, Constants.TOOLBAR);
                AcousticLibrary.addAcoustic(loc, acoustic);
            }
        }
        return acoustic;
    }

    @Nonnull
    public static IAcoustic getArmorAccentAcoustic(@Nonnull final ArmorMaterial material) {
        return getArmorAccentAcoustic(createArmorAccentResource(material), material.equipSound().value());
    }

    @Nonnull
    public static IAcoustic getArmorAccentAcoustic(@Nonnull final Equippable equippable) {
        return getArmorAccentAcoustic(createArmorAccentResource(equippable), equippable.equipSound().value());
    }

    @Nonnull
    private static IAcoustic getArmorAccentAcoustic(@Nonnull final Identifier loc, @Nonnull final SoundEvent event) {
        IAcoustic acoustic = AcousticLibrary.resolve(loc);
        if (acoustic == NullAcoustic.INSTANCE) {
            if (SoundTypeUtils.isValid(event)) {
                acoustic = generateAcousticFromTemplate(ARMOR_TEMPLATE, loc, event.location());
                AcousticLibrary.addAcoustic(loc, acoustic);
                return acoustic;
            }
        }
        return footstepAcousticResolver(loc, event);
    }

    @Nonnull
    public static IAcoustic getFootstepAcoustic(@Nonnull final BlockState state) {
        return getFootstepAcoustic(state.getSoundType());
    }

    @Nonnull
    public static IAcoustic getVanillaFootstepAcoustic(@Nonnull final SoundType soundType) {
        final Identifier loc = createVanillaFootstepResource(soundType);
        return footstepAcousticResolver(loc, soundType, true);
    }

    @Nonnull
    public static IAcoustic getFootstepAcoustic(@Nonnull final SoundType soundType) {
        final Identifier loc = createFootstepResource(soundType);
        return footstepAcousticResolver(loc, soundType, false);
    }

    @Nonnull
    private static IAcoustic footstepAcousticResolver(@Nonnull final Identifier loc, @Nonnull final SoundEvent soundType) {
        IAcoustic acoustic = AcousticLibrary.resolve(loc);
        if (acoustic == NullAcoustic.INSTANCE && SoundTypeUtils.isValid(soundType)) {
            final SimpleAcoustic simple = new SimpleAcoustic(soundType, Constants.FOOTSTEPS);
            simple.getFactory().setVolume(MINECRAFT_VOLUME_SCALE / (Config.FOOTSTEP_VOLUME_DEFAULT / 100F));
            AcousticLibrary.addAcoustic(loc, simple);
            acoustic = simple;
        }
        return acoustic;
    }

    @Nonnull
    private static IAcoustic footstepAcousticResolver(@Nonnull final Identifier loc, @Nonnull final SoundType soundType, final boolean isVanilla) {
        IAcoustic acoustic = AcousticLibrary.resolve(loc);
        if (acoustic == NullAcoustic.INSTANCE) {
            if (SoundTypeUtils.isStepSoundValid(soundType)) {
                if (isVanilla) {
                    final SimpleAcoustic simple = new SimpleAcoustic(soundType.getStepSound(), Constants.FOOTSTEPS);
                    // Minecraft scales footstep sound volume to 15%
                    simple.getFactory().setVolume(soundType.getVolume() * (MINECRAFT_VOLUME_SCALE / (Config.FOOTSTEP_VOLUME_DEFAULT / 100F)));
                    simple.getFactory().setPitch(soundType.getPitch());
                    AcousticLibrary.addAcoustic(loc, simple);
                    acoustic = simple;
                } else {
                    acoustic = generateAcousticFromTemplate(FOOTSTEP_TEMPLATE, loc, soundType.getStepSound().location());
                }
            }
        }
        return acoustic;
    }

    private static IAcoustic generateAcousticFromTemplate(@Nonnull final Identifier template, @Nonnull final Identifier name, @Nonnull final Identifier acousticName) {
        try {
            String jsonResource = ResourceUtils.readResource(template);
            if (jsonResource != null) {
                jsonResource = jsonResource
                        .replaceAll("<NAME>", name.toString())
                        .replaceAll("<ACOUSTIC>", acousticName.toString());
                final List<IAcoustic> acoustic = new AcousticCompiler(MobEffects.MOD_ID).compile(jsonResource);
                return acoustic.get(0);
            }
        } catch(@Nonnull final Throwable ignore) {
        }
        return NullAcoustic.INSTANCE;
    }
}
