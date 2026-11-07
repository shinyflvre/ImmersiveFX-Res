/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.fabric.mixins;

import dynamiclabs.immersivefx.platform.event.EntityEvents;
import dynamiclabs.immersivefx.platform.event.ItemResultEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FurnaceResultSlot.class)
public class MixinFurnaceResultSlot {

    @Shadow
    @Final
    private Player player;

    @Shadow
    private int removeCount;

    @Inject(method = "checkTakeAchievements", at = @At(value = "FIELD", target = "Lnet/minecraft/world/inventory/FurnaceResultSlot;removeCount:I", opcode = Opcodes.PUTFIELD))
    private void ifx$itemSmelted(final ItemStack carried, final CallbackInfo ci) {
        if (this.removeCount != 0 && EntityEvents.ITEM_SMELTED.hasListeners())
            EntityEvents.ITEM_SMELTED.post(new ItemResultEvent(this.player, carried));
    }
}
