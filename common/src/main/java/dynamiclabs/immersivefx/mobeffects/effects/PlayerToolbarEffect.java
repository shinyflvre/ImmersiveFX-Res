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

package dynamiclabs.immersivefx.mobeffects.effects;

import dynamiclabs.immersivefx.mobeffects.misc.EntityInspectionHandler;
import dynamiclabs.immersivefx.mobeffects.MobEffects;
import dynamiclabs.immersivefx.mobeffects.library.ItemData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.Identifier;
import dynamiclabs.immersivefx.mobeffects.library.ItemLibrary;
import dynamiclabs.immersivefx.sndctrl.api.effects.AbstractEntityEffect;
import dynamiclabs.immersivefx.sndctrl.api.effects.IEntityEffectManager;

import javax.annotation.Nonnull;

public class PlayerToolbarEffect extends AbstractEntityEffect {

    private static final Identifier NAME = Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, "toolbar");
    public static final FactoryHandler FACTORY = new FactoryHandler(
            PlayerToolbarEffect.NAME,
            entity -> new PlayerToolbarEffect());

    protected static class HandTracker {

        protected final IEntityEffectManager manager;
        protected final InteractionHand hand;
        protected Item lastHeld;

        protected HandTracker(@Nonnull final IEntityEffectManager manager, @Nonnull final Player player) {
            this(manager, player, InteractionHand.OFF_HAND);
        }

        protected HandTracker(@Nonnull final IEntityEffectManager manager, @Nonnull final Player player, @Nonnull final InteractionHand hand) {
            this.manager = manager;
            this.hand = hand;
            this.lastHeld = getItemForHand(player, hand);
        }

        protected Item getItemForHand(final Player player, final InteractionHand hand) {
            final ItemStack stack = player.getItemInHand(hand);
            return stack.getItem();
        }

        protected boolean triggerNewEquipSound(@Nonnull final Player player) {
            final Item heldItem = getItemForHand(player, this.hand);
            return heldItem != this.lastHeld;
        }

        public void update(@Nonnull final Player player) {
            if (triggerNewEquipSound(player)) {
                final ItemStack currentStack = player.getItemInHand(this.hand);
                if (!currentStack.isEmpty()) {
                    final ItemData data = ItemLibrary.getItemData(currentStack);
                    if (this.manager.isActivePlayer(player))
                        data.playEquipSound();
                    else
                        data.playEquipSound(player.blockPosition());
                }
                this.lastHeld = currentStack.getItem();
            }
        }
    }

    protected static class MainHandTracker extends HandTracker {

        protected int lastSlot;

        public MainHandTracker(@Nonnull final IEntityEffectManager manager, @Nonnull final Player player) {
            super(manager, player, InteractionHand.MAIN_HAND);
            this.lastSlot = player.getInventory().getSelectedSlot();
        }

        @Override
        protected boolean triggerNewEquipSound(@Nonnull final Player player) {
            return this.lastSlot != player.getInventory().getSelectedSlot() || super.triggerNewEquipSound(player);
        }

        @Override
        public void update(@Nonnull final Player player) {
            super.update(player);
            this.lastSlot = player.getInventory().getSelectedSlot();
        }
    }

    protected MainHandTracker mainHand;
    protected HandTracker offHand;

    public PlayerToolbarEffect() {
        super(NAME);
    }

    public void intitialize(@Nonnull final IEntityEffectManager manager) {
        super.intitialize(manager);
        EntityInspectionHandler.track(manager);
        final Player player = (Player) getEntity();
        this.mainHand = new MainHandTracker(manager, player);
        this.offHand = new HandTracker(manager, player);
    }

    @Override
    public void update() {
        final Player player = (Player) getEntity();
        this.mainHand.update(player);
        this.offHand.update(player);
    }

}