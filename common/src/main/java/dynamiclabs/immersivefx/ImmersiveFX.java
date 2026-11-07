/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx;

import dynamiclabs.immersivefx.platform.Platform;
import dynamiclabs.immersivefx.specialfx.Immersivefx;
import dynamiclabs.immersivefx.specialfx.SpecialFxConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class ImmersiveFX {
    public static final String MOD_ID = "immersivefx";
    public static final String CONFIG_FILE = "immersivefx.toml";
    public static final java.util.List<String> PART_IDS = java.util.List.of("dsurround", "sndctrl", "environs", "mobeffects", MOD_ID);
    public static final ModConfigSpec SPEC;

    private static boolean initialized;

    static {
        final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        ImmersiveFXClient.buildConfig(builder);
        builder.comment("Sounds and particles for everyday actions").translation(MOD_ID + ".configuration.specialFx").push("Special FX");
        SpecialFxConfig.build(builder);
        builder.pop();
        SPEC = builder.build();
        ImmersiveFXClient.setSpec(SPEC);
    }

    private ImmersiveFX() {
    }

    public static synchronized void init() {
        if (initialized)
            return;
        initialized = true;
        Platform.get().registerClientConfig(SPEC, CONFIG_FILE);
        ImmersiveFXClient.init();
        new Immersivefx();
        ClientEventSubscribers.register();
    }
}
