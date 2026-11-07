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

import dynamiclabs.immersivefx.platform.event.RightClickBlockEvent;
import dynamiclabs.immersivefx.ImmersiveFX;
import dynamiclabs.immersivefx.sndctrl.api.effects.IEntityEffectManager;
import dynamiclabs.immersivefx.mobeffects.misc.EntityInspectionHandler;
import dynamiclabs.immersivefx.lib.GameUtils;
import dynamiclabs.immersivefx.lib.TickCounter;
import dynamiclabs.immersivefx.mobeffects.MobEffects;
import dynamiclabs.immersivefx.mobeffects.library.ItemData;
import dynamiclabs.immersivefx.mobeffects.library.ItemLibrary;
import dynamiclabs.immersivefx.sndctrl.api.effects.AbstractEntityEffect;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntitySwingEffect extends AbstractEntityEffect {

    // Grace for time between the right click being triggered and when the effect update happens
    private static final long RIGHT_CLICK_GRACE = 5;

    private static final Identifier NAME = Identifier.fromNamespaceAndPath(MobEffects.MOD_ID, "swing");
    public static final FactoryHandler FACTORY = new FactoryHandler(
            EntitySwingEffect.NAME,
            entity -> new EntitySwingEffect());

    private static long lastRightClick;

    @Nullable
    protected LivingEntity.SwingDescription lastSwing;
    protected boolean isSwinging = false;

    public EntitySwingEffect() {
        super(NAME);
    }

    @Override
    public void intitialize(@Nonnull final IEntityEffectManager manager) {
        super.intitialize(manager);
        EntityInspectionHandler.track(manager);
    }

    @Override
    public void update() {

        final LivingEntity entity = getEntity();

        // Boats are strange - ignore them for now
        if (entity.getVehicle() instanceof AbstractBoat)
            return;

        // Is the swing in motion
        final LivingEntity.SwingDescription swing = entity.getCurrentSwing();
        if (swing != null && swing == this.lastSwing) {
            if (!this.isSwinging) {
                if (isClickOK(entity)) {
                    if (freeSwing(entity)) {
                        final ItemStack currentItem = entity.getItemInHand(swing.hand());
                        final ItemData data = ItemLibrary.getItemData(currentItem);
                        if (isActivePlayer(entity))
                            data.playSwingSound();
                        else
                            data.playSwingSound(entity.blockPosition());
                    }
                }
            }

            this.isSwinging = true;

        } else {
            this.isSwinging = false;
        }

        this.lastSwing = swing;
    }

    protected static boolean freeSwing(@Nonnull final LivingEntity entity) {
        final BlockHitResult result = rayTraceBlock(entity);
        return result.getType() == HitResult.Type.MISS;
    }

    protected static double getReach(@Nonnull final LivingEntity entity) {
        if (entity instanceof Player player)
            return player.blockInteractionRange();

        // From EntityAIAttackMelee::getAttackReachSqr - approximate
        return entity.getBbWidth() * 2F + 0.6F; // 0.6 == default entity width
    }

    protected static BlockHitResult rayTraceBlock(@Nonnull final LivingEntity entity) {
        double range = getReach(entity);
        final Vec3 eyes = entity.getEyePosition(1F);
        final Vec3 look = entity.getViewVector(1F);
        final Vec3 rangedLook = eyes.add(look.x * range, look.y * range, look.z * range);
        return entity.level().clip(new ClipContext(eyes, rangedLook, ClipContext.Block.OUTLINE, ClipContext.Fluid.SOURCE_ONLY, entity));
    }

    private boolean isClickOK(@Nonnull final LivingEntity entity) {
        return entity == GameUtils.getPlayer() && (lastRightClick < (TickCounter.getTickCount() - RIGHT_CLICK_GRACE));
    }

    public static void onRightClick(@Nonnull final RightClickBlockEvent event) {
        if (event.getLevel().isClientSide()) {
            lastRightClick = TickCounter.getTickCount();
        }
    }

}
