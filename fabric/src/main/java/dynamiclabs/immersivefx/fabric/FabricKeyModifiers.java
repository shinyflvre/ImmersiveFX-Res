/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.fabric;

import dynamiclabs.immersivefx.platform.IPlatform;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class FabricKeyModifiers {

    private static final Map<KeyMapping, IPlatform.KeyModifier> MODIFIERS = Collections.synchronizedMap(new WeakHashMap<>());

    private FabricKeyModifiers() {
    }

    static void set(@Nonnull final KeyMapping key, @Nonnull final IPlatform.KeyModifier modifier) {
        if (modifier == IPlatform.KeyModifier.NONE)
            MODIFIERS.remove(key);
        else
            MODIFIERS.put(key, modifier);
    }

    @Nullable
    public static IPlatform.KeyModifier get(@Nonnull final KeyMapping key) {
        return MODIFIERS.isEmpty() ? null : MODIFIERS.get(key);
    }

    public static boolean isActive(@Nonnull final IPlatform.KeyModifier modifier) {
        final Minecraft mc = Minecraft.getInstance();
        return switch (modifier) {
            case NONE -> true;
            case CONTROL -> mc.hasControlDown();
            case SHIFT -> mc.hasShiftDown();
            case ALT -> mc.hasAltDown();
        };
    }

    @Nonnull
    public static Component withModifier(@Nonnull final IPlatform.KeyModifier modifier, @Nonnull final Component key) {
        final String name = switch (modifier) {
            case NONE -> null;
            case CONTROL -> "immersivefx.key.modifier.control";
            case SHIFT -> "immersivefx.key.modifier.shift";
            case ALT -> "immersivefx.key.modifier.alt";
        };
        return name == null ? key : Component.translatable(name, key);
    }
}
