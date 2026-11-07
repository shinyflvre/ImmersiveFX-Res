/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx;

import dynamiclabs.immersivefx.dsurround.DynamicSurroundings;
import dynamiclabs.immersivefx.environs.Environs;
import dynamiclabs.immersivefx.mobeffects.MobEffects;
import dynamiclabs.immersivefx.sndctrl.SoundControl;
import javax.annotation.Nonnull;
import net.neoforged.neoforge.common.ModConfigSpec;

final class ImmersiveFXClient {
    private ImmersiveFXClient() {
    }

    static void buildConfig(@Nonnull final ModConfigSpec.Builder builder) {
        category(builder, "general", "General", "Logging and debug displays");
        dynamiclabs.immersivefx.dsurround.config.Config.build(builder);
        builder.pop();
        category(builder, "soundControl", "Sound Control", "Sound engine, volumes per sound and category, sound effects");
        dynamiclabs.immersivefx.sndctrl.config.Config.build(builder);
        builder.pop();
        category(builder, "environs", "Environs", "Biome ambience, auroras, fog and block effects");
        dynamiclabs.immersivefx.environs.config.Config.build(builder);
        builder.pop();
        category(builder, "mobEffects", "Mob Effects", "Footsteps, armor and item sounds, entity effects");
        dynamiclabs.immersivefx.mobeffects.config.Config.build(builder);
        builder.pop();
    }

    private static void category(@Nonnull final ModConfigSpec.Builder builder, final String key, final String name, final String comment) {
        builder.comment(comment).translation(ImmersiveFX.MOD_ID + ".configuration." + key).push(name);
    }

    static void setSpec(@Nonnull final ModConfigSpec spec) {
        dynamiclabs.immersivefx.dsurround.config.Config.SPEC = spec;
        dynamiclabs.immersivefx.sndctrl.config.Config.SPEC = spec;
        dynamiclabs.immersivefx.environs.config.Config.SPEC = spec;
        dynamiclabs.immersivefx.mobeffects.config.Config.SPEC = spec;
    }

    static void init() {
        new DynamicSurroundings();
        new SoundControl();
        new Environs();
        new MobEffects();
    }
}
