/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.dsurround.mixins;

import dynamiclabs.immersivefx.specialfx.TypingSounds;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.input.CharacterEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiLineEditBox.class)
public abstract class MixinMultiLineEditBox {

    @Inject(method = "charTyped", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/MultilineTextField;insertText(Ljava/lang/String;)V"))
    private void immersivefx$typed(final CharacterEvent event, final CallbackInfoReturnable<Boolean> cir) {
        TypingSounds.onCharTyped();
    }
}
