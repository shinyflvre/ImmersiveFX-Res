/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.fabric.mixins;

import dynamiclabs.immersivefx.fabric.FabricKeyModifiers;
import dynamiclabs.immersivefx.platform.IPlatform;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyMapping.class)
public abstract class MixinKeyMapping {

    @Shadow
    private int clickCount;

    @Inject(method = "consumeClick", at = @At("HEAD"), cancellable = true)
    private void ifx$requireModifier(final CallbackInfoReturnable<Boolean> cir) {
        final IPlatform.KeyModifier modifier = FabricKeyModifiers.get((KeyMapping) (Object) this);
        if (modifier != null && this.clickCount > 0 && !FabricKeyModifiers.isActive(modifier)) {
            this.clickCount = 0;
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getTranslatedKeyMessage", at = @At("RETURN"), cancellable = true)
    private void ifx$modifierName(final CallbackInfoReturnable<Component> cir) {
        final IPlatform.KeyModifier modifier = FabricKeyModifiers.get((KeyMapping) (Object) this);
        if (modifier != null && !((KeyMapping) (Object) this).isUnbound())
            cir.setReturnValue(FabricKeyModifiers.withModifier(modifier, cir.getReturnValue()));
    }
}
