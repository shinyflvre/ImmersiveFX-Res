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

import dynamiclabs.immersivefx.platform.event.PlaySoundEvent;
import dynamiclabs.immersivefx.ImmersiveFX;
import com.google.gson.reflect.TypeToken;
import dynamiclabs.immersivefx.dsurround.DynamicSurroundings;
import dynamiclabs.immersivefx.lib.logging.IModLog;
import dynamiclabs.immersivefx.lib.resource.IResourceAccessor;
import dynamiclabs.immersivefx.lib.resource.ResourceUtils;
import dynamiclabs.immersivefx.lib.service.IModuleService;
import dynamiclabs.immersivefx.lib.service.ModuleServiceManager;
import dynamiclabs.immersivefx.lib.validation.MapValidator;
import dynamiclabs.immersivefx.lib.validation.Validators;
import dynamiclabs.immersivefx.mobeffects.MobEffects;
import dynamiclabs.immersivefx.mobeffects.config.Config;
import dynamiclabs.immersivefx.mobeffects.library.config.EntityConfig;
import dynamiclabs.immersivefx.sndctrl.api.acoustics.Library;
import dynamiclabs.immersivefx.sndctrl.api.sound.SoundBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public final class EffectLibrary {

    private static final IModLog LOGGER = MobEffects.LOGGER.createChild(EffectLibrary.class);

    private static final Identifier PLAYER = Identifier.withDefaultNamespace("player");
    private static final EntityEffectInfo DEFAULT = new EntityEffectInfo();
    private static EntityEffectInfo playerEffects = DEFAULT;

    private static final Object2ObjectOpenHashMap<Identifier, EntityEffectInfo> effectConfiguration = new Object2ObjectOpenHashMap<>();
    private static final Reference2ObjectOpenHashMap<Class<? extends Entity>, EntityEffectInfo> effects = new Reference2ObjectOpenHashMap<>();
    private static final Set<Identifier> blockedSounds = new ObjectOpenHashSet<>();

    private static final Map<Identifier, SoundEvent> soundReplace = new Object2ObjectOpenHashMap<>();

    private EffectLibrary() {

    }

    static void initialize() {
        ModuleServiceManager.instance().add(new EffectLibraryService());
    }

    public static boolean hasEffect(@Nonnull final Entity entity, @Nonnull final Identifier loc) {
        return getEffectInfo(entity).effects.contains(loc);
    }

    @Nonnull
    private static EntityEffectInfo getEffectInfo(@Nonnull final Entity entity) {
        if (entity instanceof Player)
            return playerEffects;
        EntityEffectInfo eei = effects.get(entity.getClass());
        if (eei == null) {
            // Do we have a config for it?
            eei = effectConfiguration.get(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()));
            if (eei == null)
                eei = DEFAULT;
            effects.put(entity.getClass(), eei);
        }
        return eei;
    }

    public static void soundPlay(@Nonnull PlaySoundEvent e) {
        final SoundInstance theSound = e.getSound();
        if (theSound != null) {
            final Identifier soundResource = theSound.getIdentifier();
            if (blockedSounds.contains(soundResource)) {
                e.setSound(null);
            } else {
                final SoundEvent evt = soundReplace.get(soundResource);
                if (evt != null && theSound instanceof AbstractSoundInstance asi) {
                    e.setSound(SoundBuilder.builder(evt).from(asi).build());
                }
            }
        }
    }

    private static class EffectLibraryService implements IModuleService {

        private static final Type entityConfigType = TypeToken.getParameterized(Map.class, String.class, EntityConfig.class).getType();

        static {
            Validators.registerValidator(entityConfigType, new MapValidator<String, EntityConfig>());
        }

        @Override
        public String name() {
            return "MobEffectsLibrary";
        }

        @Override
        public void start() {

            // Seed our configuration with known entities that have defaults
            BuiltInRegistries.ENTITY_TYPE.forEach(e -> {
                if (e.getCategory() != MobCategory.MISC)
                    effectConfiguration.put(BuiltInRegistries.ENTITY_TYPE.getKey(e), DEFAULT);
            });

            // Apply configuration.  These will replace defaults as needed.
            final Collection<IResourceAccessor> configs = ResourceUtils.findConfigs(DynamicSurroundings.MOD_ID, DynamicSurroundings.DATA_PATH, "mobeffects.json");

            IResourceAccessor.process(configs, accessor -> {
                Map<String, EntityConfig> cfg = accessor.as(entityConfigType);
                for (final Map.Entry<String, EntityConfig> kvp : cfg.entrySet()) {
                    final EntityEffectInfo eei = new EntityEffectInfo(kvp.getValue());
                    final Identifier loc = Library.resolveResource(MobEffects.MOD_ID, kvp.getKey());
                    effectConfiguration.put(loc, eei);
                    if (loc.equals(PLAYER)) {
                        playerEffects = eei;
                    }

                    for (final String r : kvp.getValue().blockedSounds) {
                        try {
                            blockedSounds.add(Identifier.parse(r));
                        } catch (@Nonnull final Throwable t) {
                            MobEffects.LOGGER.error(t, "Not a valid sound resource location: %s", r);
                        }
                    }
                }
            });

            // Replace our bow loose sounds
            final Identifier bowLoose = Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, "bow.loose");
            Library.getSound(bowLoose).ifPresent(se -> {
                soundReplace.put(Identifier.withDefaultNamespace("entity.arrow.shoot"), se);
                soundReplace.put(Identifier.withDefaultNamespace("entity.skeleton.shoot"), se);
            });
        }

        @Override
        public void log() {
            if (Config.CLIENT.logging.enableLogging.get()) {
                MobEffects.LOGGER.debug("MobEffect Registry");
                MobEffects.LOGGER.debug("==================");
                for (final Object2ObjectMap.Entry<Identifier, EntityEffectInfo> kvp : effectConfiguration.object2ObjectEntrySet()) {
                    MobEffects.LOGGER.debug("%s: %s", kvp.getKey(), kvp.getValue());
                }
            }
        }

        @Override
        public void stop() {
            effectConfiguration.clear();
            effects.clear();
            blockedSounds.clear();
            soundReplace.clear();
        }
    }
}
