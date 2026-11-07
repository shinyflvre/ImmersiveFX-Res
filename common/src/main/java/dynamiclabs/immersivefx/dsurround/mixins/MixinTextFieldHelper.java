/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.dsurround.mixins;

import dynamiclabs.immersivefx.specialfx.TypingSounds;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.input.CharacterEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextFieldHelper.class)
public abstract class MixinTextFieldHelper {

    @Inject(method = "charTyped", at = @At("HEAD"))
    private void immersivefx$typed(final CharacterEvent event, final CallbackInfoReturnable<Boolean> cir) {
        if (event.isAllowedChatCharacter())
            TypingSounds.onCharTyped();
    }
}
