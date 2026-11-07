/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.menu;

import dynamiclabs.immersivefx.sndctrl.library.IndividualSoundConfig;
import dynamiclabs.immersivefx.sndctrl.library.SoundLibrary;
import dynamiclabs.immersivefx.specialfx.SpecialFxConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BooleanSupplier;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.ModConfigSpec;

final class MenuCatalog {
    private static final String MENU = "immersivefx.menu.";
    static final String SOUND_LIST = "soundList";

    record Page(String id, int icon, String group, Component title, Component subtitle, List<MenuRow> rows) {
        boolean anyChanged() {
            for (final MenuRow row : rows)
                if (row.changed())
                    return true;
            return false;
        }
    }

    private final MenuRow.Host host;
    private final boolean canPlay;
    private List<MenuRow> rows = new ArrayList<>();

    MenuCatalog(final MenuRow.Host host, final boolean canPlay) {
        this.host = host;
        this.canPlay = canPlay;
    }

    List<Page> pages() {
        final List<Page> pages = new ArrayList<>();
        pages.add(sound());
        pages.add(soundList());
        pages.add(ambience());
        pages.add(footsteps());
        pages.add(mobEffects());
        pages.add(effects());
        pages.add(atmosphere());
        pages.add(lightLevel());
        pages.add(specialFx());
        pages.add(advanced());
        return pages;
    }

    private Page page(final String id, final int icon, final String group) {
        final Page page = new Page(id, icon, group, Component.translatable(MENU + "page." + id), Component.translatable(MENU + "page." + id + ".desc"), rows);
        rows = new ArrayList<>();
        return page;
    }

    private Page sound() {
        final var sound = dynamiclabs.immersivefx.sndctrl.config.Config.CLIENT.sound;
        section("spatial");
        toggle("enhancedSounds", sound.enableEnhancedSounds).restart();
        toggle("hrtf", sound.enableHRTF).restart();
        toggle("monoConversion", sound.enableMonoConversion);
        slider("panningSoftness", sound.panningSoftness, 5, "%");
        section("occlusion");
        final MenuRow occlusion = toggle("occlusion", sound.enableOcclusionCalcs);
        toggle("occludeWeather", sound.occludeWeather).child(sound.enableOcclusionCalcs::getRaw, occlusion.title);
        toggle("occludeRecords", sound.occludeRecords).child(sound.enableOcclusionCalcs::getRaw, occlusion.title);
        section("loops");
        final MenuRow crossfade = toggle("loopCrossfade", sound.loopCrossfade);
        slider("loopCrossfadeLength", sound.loopCrossfadeLength, 100, " ms").child(sound.loopCrossfade::getRaw, crossfade.title);
        slider("fadeInTime", sound.fadeInTime, 100, " ms");
        section("ownSounds");
        final MenuRow own = toggle("playerSounds2D", sound.enable2DPlayerSounds);
        list("playerSounds2DList", sound.playerSounds2D, "soundId").child(sound.enable2DPlayerSounds::getRaw, own.title);
        section("other");
        slider("cullInterval", sound.cullInterval, 20, " ticks");
        list("startupSounds", sound.startupSoundList, "soundId");
        return page("sound", MenuStyle.ICON_SOUND, "audio");
    }

    private Page soundList() {
        final Map<Identifier, IndividualSoundConfig> defaults = new HashMap<>();
        for (final String entry : dynamiclabs.immersivefx.sndctrl.config.Config.Client.defaultSoundConfig) {
            final IndividualSoundConfig config = IndividualSoundConfig.createFrom(entry);
            if (config != null)
                defaults.put(config.getLocation(), config);
        }
        for (final IndividualSoundConfig config : SoundLibrary.getSortedSoundConfigurations()) {
            final IndividualSoundConfig fallback = defaults.get(config.getLocation());
            rows.add(new SoundRow(host, config, fallback != null ? fallback : plain(config), canPlay));
        }
        return page(SOUND_LIST, MenuStyle.ICON_SOUND_LIST, "audio");
    }

    private static IndividualSoundConfig plain(final IndividualSoundConfig config) {
        final IndividualSoundConfig plain = new IndividualSoundConfig(config);
        plain.setIsBlocked(false);
        plain.setIsCulled(false);
        plain.setVolumeScaleInt(IndividualSoundConfig.DEFAULT_VOLUME_SCALE);
        return plain;
    }

    private Page ambience() {
        final var client = dynamiclabs.immersivefx.environs.config.Config.CLIENT;
        section("volume");
        slider("biomeVolume", client.sound.biomeSoundVolume, 5, "%");
        slider("spotVolume", client.sound.spotSoundVolume, 5, "%");
        slider("waterfallVolume", client.sound.waterfallSoundVolume, 5, "%");
        section("ambience3d");
        final MenuRow ambience = toggle("ambience3D", client.sound.enable3DAmbience);
        slider("ambienceDirectionality", client.sound.ambienceDirectionality, 5, "%").child(client.sound.enable3DAmbience::getRaw, ambience.title);
        section("waterfalls");
        toggle("occludeWaterfall", client.sound.occludeWaterfall);
        slider("waterfallCutoff", client.effects.waterfallCutoff, 1, "");
        section("world");
        slider("seaLevel", client.biome.worldSealevelOverride, 1, "");
        list("dimensionBlacklist", client.biome.biomeSoundBlacklist, "dimensionId");
        return page("ambience", MenuStyle.ICON_AMBIENCE, "audio");
    }

    private Page footsteps() {
        final var steps = dynamiclabs.immersivefx.mobeffects.config.Config.CLIENT.footsteps;
        section("footstepSounds");
        final MenuRow sounds = toggle("footstepSounds", steps.enableFootstepSounds);
        final BooleanSupplier soundsOn = steps.enableFootstepSounds::getRaw;
        slider("footstepVolume", steps.footstepVolume, 5, "%").child(soundsOn, sounds.title);
        toggle("armorAccents", steps.enableArmorAccents).child(soundsOn, sounds.title);
        toggle("rainAccents", steps.enableRainSplashAccent).child(soundsOn, sounds.title);
        toggle("waterAccents", steps.enableWaterLoggedAccent).child(soundsOn, sounds.title);
        toggle("cadence", steps.firstPersonFootstepCadence).child(soundsOn, sounds.title);
        toggle("quadruped", steps.footstepsAsQuadruped).child(soundsOn, sounds.title);
        section("footprints");
        final MenuRow prints = toggle("footprints", steps.enableFootprintParticles);
        choice("footprintStyle", steps.playerFootprintStyle).child(steps.enableFootprintParticles::getRaw, prints.title);
        return page("footsteps", MenuStyle.ICON_FOOTSTEPS, "audio");
    }

    private Page mobEffects() {
        final var effects = dynamiclabs.immersivefx.mobeffects.config.Config.CLIENT.effects;
        section("itemSounds");
        final MenuRow toolbar = toggle("toolbar", effects.enableToolbarEffect).restart();
        slider("toolbarVolume", effects.toolbarVolume, 5, "%").child(effects.enableToolbarEffect::getRaw, toolbar.title);
        toggle("bow", effects.enableBowEffect).restart();
        toggle("swing", effects.enableSwingEffect).restart();
        section("mobVisuals");
        toggle("hidePotionParticles", effects.hidePlayerPotionParticles);
        toggle("breath", effects.showBreath).restart();
        toggle("arrowTrail", effects.showArrowTrail);
        section("range");
        slider("effectRange", dynamiclabs.immersivefx.sndctrl.config.Config.CLIENT.effects.effectRange, 4, "").restart();
        return page("mobEffects", MenuStyle.ICON_MOBS, "audio");
    }

    private Page effects() {
        final var effects = dynamiclabs.immersivefx.environs.config.Config.CLIENT.effects;
        section("nature");
        toggle("fireflies", effects.enableFireFlies).restart();
        toggle("waterfallSplash", effects.enableWaterfalls).restart();
        section("jets");
        toggle("steamJets", effects.enableSteamJets).restart();
        toggle("fireJets", effects.enableFireJets).restart();
        toggle("bubbleJets", effects.enableBubbleJets).restart();
        toggle("dustJets", effects.enableDustJets).restart();
        toggle("fountainJets", effects.enableFountainJets).restart();
        section("water");
        final MenuRow ripples = toggle("waterRipples", effects.enableWaterRipples);
        choice("rippleStyle", effects.waterRippleStyle).child(effects.enableWaterRipples::getRaw, ripples.title);
        toggle("disableUnderwater", effects.disableUnderwaterParticles).restart();
        return page("effects", MenuStyle.ICON_PARTICLES, "visuals");
    }

    private Page atmosphere() {
        final var client = dynamiclabs.immersivefx.environs.config.Config.CLIENT;
        final var fog = client.fog;
        section("fog");
        final MenuRow master = toggle("fog", fog.enableFog);
        final BooleanSupplier fogOn = fog.enableFog::getRaw;
        toggle("biomeFog", fog.enableBiomeFog).child(fogOn, master.title);
        toggle("elevationHaze", fog.enableElevationHaze).child(fogOn, master.title);
        final MenuRow morning = toggle("morningFog", fog.enableMorningFog).child(fogOn, master.title);
        slider("morningFogChance", fog.morningFogChance, 1, "%").child(() -> fog.enableFog.getRaw() && fog.enableMorningFog.getRaw(), morning.title);
        toggle("bedrockFog", fog.enableBedrockFog).child(fogOn, master.title);
        toggle("weatherFog", fog.enableWeatherFog).child(fogOn, master.title);
        slider("minFogDistance", fog.minFogDistance, 5, "%").child(fogOn, master.title);
        section("aurora");
        final MenuRow aurora = toggle("aurora", client.aurora.auroraEnabled);
        slider("auroraBands", client.aurora.maxBands, 1, "").child(client.aurora.auroraEnabled::getRaw, aurora.title);
        return page("atmosphere", MenuStyle.ICON_ATMOSPHERE, "visuals");
    }

    private Page lightLevel() {
        final var light = dynamiclabs.immersivefx.dsurround.config.Config.CLIENT.lightLevel;
        section("lightLevel");
        choice("lightMode", light.mode);
        choice("lightColors", light.colorSet);
        slider("lightRange", light.range, 4, "");
        toggle("lightHideSafe", light.hideSafe);
        slider("lightThreshold", light.lightSpawnThreshold, 1, "");
        return page("lightLevel", MenuStyle.ICON_LIGHT, "visuals");
    }

    private Page specialFx() {
        final SpecialFxConfig.Options fx = SpecialFxConfig.OPTIONS;
        section("master");
        final MenuRow master = toggle("fxEnabled", fx.enabled);
        section("fxCombat");
        effect("bloodSplatter", fx.bloodSplatter, master);
        final MenuRow blood = toggle("bloodParticles", fx.bloodParticles).needs(fx.enabled::getRaw, master.title);
        slider("bloodParticleAmount", fx.bloodParticleAmount, 10, "%").child(() -> fx.enabled.getRaw() && fx.bloodParticles.getRaw(), blood.title);
        section("fxItems");
        effect("crafting", fx.crafting, master);
        effect("smelting", fx.smelting, master);
        effect("itemDrop", fx.itemDrop, master);
        effect("workstationClicks", fx.workstationClicks, master);
        effect("inventoryOpen", fx.inventoryOpen, master);
        section("fxWorld");
        final MenuRow spawn = effect("mobSpawn", fx.mobSpawn, master);
        toggle("mobSpawnSmoke", fx.mobSpawnSmoke).child(() -> fx.enabled.getRaw(), spawn.title);
        final MenuRow ore = effect("oreBreak", fx.oreBreak, master);
        toggle("oreBreakReplace", fx.oreBreakReplace).child(() -> fx.enabled.getRaw() && fx.oreBreak.enabled.getRaw(), ore.title);
        final MenuRow stone = effect("stoneBreak", fx.stoneBreak, master);
        toggle("stoneBreakReplace", fx.stoneBreakReplace).child(() -> fx.enabled.getRaw() && fx.stoneBreak.enabled.getRaw(), stone.title);
        section("fxPlayer");
        effect("hunger", fx.hunger, master);
        effect("bed", fx.bed, master);
        effect("chat", fx.chat, master);
        effect("typing", fx.typing, master);
        return page("specialFx", MenuStyle.ICON_SPECIAL, "extras");
    }

    private MenuRow effect(final String key, final SpecialFxConfig.Effect effect, final MenuRow master) {
        final MenuRow row = toggle(key, effect.enabled).needs(SpecialFxConfig.OPTIONS.enabled::getRaw, master.title);
        final ModConfigSpec.IntValue volume = effect.volume;
        final MenuRow slider = new MenuRow.Slider(host, Component.translatable(MENU + "option.fxVolume", name(key)), Component.empty(), volume::getRaw,
                next -> ImmersiveFXScreen.preview(volume, next), next -> ImmersiveFXScreen.commit(volume, next), volume.getDefault(), 0, 100, 5, "%");
        slider.child(() -> SpecialFxConfig.OPTIONS.enabled.getRaw() && effect.enabled.getRaw(), row.title);
        rows.add(slider);
        return row;
    }

    private Page advanced() {
        final var sound = dynamiclabs.immersivefx.sndctrl.config.Config.CLIENT;
        final var environs = dynamiclabs.immersivefx.environs.config.Config.CLIENT;
        final var mobs = dynamiclabs.immersivefx.mobeffects.config.Config.CLIENT;
        final var core = dynamiclabs.immersivefx.dsurround.config.Config.CLIENT;
        section("performance");
        slider("streamingSounds", sound.sound.streamingSoundCount, 1, "").restart();
        slider("workers", sound.sound.backgroundThreadWorkers, 1, "").restart();
        toggle("replaceRandoms", sound.effects.fixupRandoms).restart();
        section("updates");
        toggle("versionCheck", core.logging.onlineVersionCheck);
        section("logging");
        final MenuRow coreLog = toggle("debugCore", core.logging.enableLogging);
        number("maskCore", core.logging.flagMask).child(core.logging.enableLogging::getRaw, coreLog.title);
        final MenuRow soundLog = toggle("debugSound", sound.logging.enableLogging);
        number("maskSound", sound.logging.flagMask).child(sound.logging.enableLogging::getRaw, soundLog.title);
        final MenuRow environsLog = toggle("debugEnvirons", environs.logging.enableLogging);
        number("maskEnvirons", environs.logging.flagMask).child(environs.logging.enableLogging::getRaw, environsLog.title);
        final MenuRow mobsLog = toggle("debugMobs", mobs.logging.enableLogging);
        number("maskMobs", mobs.logging.flagMask).child(mobs.logging.enableLogging::getRaw, mobsLog.title);
        return page("advanced", MenuStyle.ICON_ADVANCED, "extras");
    }

    private void section(final String key) {
        rows.add(new MenuRow.Section(host, Component.translatable(MENU + "section." + key), false));
    }

    private static Component name(final String key) {
        return Component.translatable(MENU + "option." + key);
    }

    private static Component about(final String key) {
        final String id = MENU + "about." + key;
        return Language.getInstance().has(id) ? Component.translatable(id) : Component.empty();
    }

    private MenuRow toggle(final String key, final ModConfigSpec.BooleanValue value) {
        final MenuRow row = new MenuRow.Switch(host, name(key), about(key), value::getRaw, on -> ImmersiveFXScreen.commit(value, on), value.getDefault());
        rows.add(row);
        return row;
    }

    private <T extends Enum<T>> MenuRow choice(final String key, final ModConfigSpec.EnumValue<T> value) {
        final T[] options = value.getDefault().getDeclaringClass().getEnumConstants();
        final List<Component> labels = new ArrayList<>();
        for (final T option : options)
            labels.add(Component.translatable(MENU + "option." + key + "." + option.name().toLowerCase(Locale.ROOT)));
        final MenuRow row = new MenuRow.Choice(host, name(key), about(key), labels, () -> Arrays.asList(options).indexOf(value.getRaw()),
                index -> ImmersiveFXScreen.commit(value, options[index]), value.getDefault().ordinal());
        rows.add(row);
        return row;
    }

    private MenuRow slider(final String key, final ModConfigSpec.IntValue value, final int step, final String unit) {
        final ModConfigSpec.ValueSpec spec = ImmersiveFXScreen.specOf(value);
        final ModConfigSpec.Range<Integer> range = spec.getRange();
        final int min = range != null ? range.getMin() : 0;
        final int max = range != null ? range.getMax() : 100;
        final MenuRow row = new MenuRow.Slider(host, name(key), about(key), value::getRaw, next -> ImmersiveFXScreen.preview(value, next), next -> ImmersiveFXScreen.commit(value, next),
                value.getDefault(), min, max, step, unit);
        rows.add(row);
        return row;
    }

    private MenuRow number(final String key, final ModConfigSpec.IntValue value) {
        final MenuRow row = new MenuRow.NumberField(host, name(key), about(key), value::getRaw, next -> ImmersiveFXScreen.commit(value, next), value.getDefault());
        rows.add(row);
        return row;
    }

    private MenuRow list(final String key, final ModConfigSpec.ConfigValue<List<? extends String>> value, final String placeholder) {
        final List<String> fallback = new ArrayList<>(value.getDefault());
        final MenuRow row = new MenuRow.StringList(host, name(key), about(key), () -> new ArrayList<>(value.getRaw()), next -> ImmersiveFXScreen.commit(value, next), fallback,
                Component.translatable(MENU + "placeholder." + placeholder));
        rows.add(row);
        return row;
    }
}
