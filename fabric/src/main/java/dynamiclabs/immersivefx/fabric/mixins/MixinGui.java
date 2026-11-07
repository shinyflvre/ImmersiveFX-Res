/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.fabric.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import dynamiclabs.immersivefx.platform.event.ClientEvents;
import dynamiclabs.immersivefx.platform.event.ScreenOpeningEvent;
import javax.annotation.Nullable;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class MixinGui {

    @Shadow
    @Nullable
    private Screen screen;

    @Inject(method = "setScreen", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/Gui;screen:Lnet/minecraft/client/gui/screens/Screen;", opcode = Opcodes.PUTFIELD))
    private void ifx$screenOpening(final Screen argument, final CallbackInfo ci, @Local(argsOnly = true) final Screen newScreen) {
        if (newScreen != null && ClientEvents.SCREEN_OPENING.hasListeners())
            ClientEvents.SCREEN_OPENING.post(new ScreenOpeningEvent(this.screen, newScreen));
    }
}
