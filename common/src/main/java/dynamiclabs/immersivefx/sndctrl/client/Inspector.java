/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
/*
 * Dynamic Surroundings: Sound Control
 * Copyright (C) 2019  OreCruncher
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
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package dynamiclabs.immersivefx.sndctrl.client;

import dynamiclabs.immersivefx.platform.Platform;
import net.minecraft.client.Minecraft;
import dynamiclabs.immersivefx.ImmersiveFX;
import com.google.common.collect.ImmutableList;
import dynamiclabs.immersivefx.lib.GameUtils;
import dynamiclabs.immersivefx.lib.TickCounter;
import dynamiclabs.immersivefx.lib.blockstate.BlockStateMatcher;
import dynamiclabs.immersivefx.lib.events.DiagnosticEvent;
import dynamiclabs.immersivefx.sndctrl.SoundControl;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.StringUtils;
import dynamiclabs.immersivefx.sndctrl.config.Config;
import dynamiclabs.immersivefx.sndctrl.events.BlockInspectionEvent;
import dynamiclabs.immersivefx.sndctrl.events.EntityInspectionEvent;
import dynamiclabs.immersivefx.sndctrl.library.AudioEffectLibrary;
import dynamiclabs.immersivefx.sndctrl.library.LegacyMaterial;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

public class Inspector {

    private static final String TEXT_BLOCKSTATE = ChatFormatting.DARK_PURPLE + "<BlockState>";
    private static final String TEXT_TAGS = ChatFormatting.DARK_PURPLE + "<Tags>";

    private static List<String> diagnostics = ImmutableList.of();

    private Inspector() {

    }

    private static List<String> getTags(@Nonnull final BlockState state) {
        return state.getBlock().builtInRegistryHolder().tags().map(t -> "#" + t.location().toString()).collect(Collectors.toList());
    }

    private static void gatherBlockText(final ItemStack stack, final List<String> text, final BlockState state,
                                        final BlockPos pos) {

        if (!stack.isEmpty()) {
            text.add(ChatFormatting.RED + stack.getDisplayName().getString());
            final String itemName = stack.getItem().getName(stack).getString();
            if (!StringUtils.isEmpty(itemName)) {
                text.add("ITEM: " + itemName);
                text.add(ChatFormatting.DARK_AQUA + stack.getItem().getClass().getName());
            }
        }

        if (state != null) {
            final BlockStateMatcher info = BlockStateMatcher.create(state);
            if (!info.isEmpty()) {
                text.add("BLOCK: " + info.toString());
                text.add(ChatFormatting.DARK_AQUA + info.getBlock().getClass().getName());
                text.add("Material: " + LegacyMaterial.of(state).name());
                final SoundType st = state.getSoundType();
                text.add("Step Sound: " + st.getStepSound().location().toString());
                text.add("Reflectivity: " + AudioEffectLibrary.getReflectivity(state));
                text.add("Occlusion: " + AudioEffectLibrary.getOcclusion(state));
                text.add(TEXT_BLOCKSTATE);
                final CompoundTag nbt = NbtUtils.writeBlockState(state);
                text.add(nbt.toString());
                final List<String> tagNames = getTags(state);
                if (tagNames.size() > 0) {
                    text.add(TEXT_TAGS);
                    for (final String ore : tagNames)
                        text.add(ChatFormatting.GOLD + ore);
                }
            }
        }

    }

    private static boolean isHolding() {
        final Player player = GameUtils.getPlayer();
        if (player == null)
            return false;
        final ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
        return !held.isEmpty() && held.getItem() == Items.CARROT_ON_A_STICK;
    }

    public static void onClientTick(@Nonnull final Minecraft event) {

        if (TickCounter.getTickCount() % 5 == 0) {

            diagnostics = ImmutableList.of();

            if (Config.CLIENT.logging.enableLogging.get() && isHolding()) {
                final Level world = GameUtils.getWorld();
                if (GameUtils.getMC().crosshairPickEntity != null) {
                    final EntityInspectionEvent evt = new EntityInspectionEvent(GameUtils.getMC().crosshairPickEntity);
                    evt.data.add(ChatFormatting.RED + "Entity " + evt.entity.toString());
                    EntityInspectionEvent.BUS.post(evt);
                    diagnostics = evt.data;
                } else {
                    final HitResult current = GameUtils.getMC().hitResult;
                    if (current instanceof BlockHitResult) {
                        final BlockHitResult trace = (BlockHitResult) current;
                        if (trace.getType() != HitResult.Type.MISS) {

                            final BlockState state = world.getBlockState(trace.getBlockPos());

                            if (!state.isAir()) {
                                final BlockInspectionEvent evt = new BlockInspectionEvent(trace, world, state, trace.getBlockPos());
                                BlockInspectionEvent.BUS.post(evt);
                                diagnostics = evt.data;
                            }
                        }
                    }
                }
            }
        }
    }

    public static void onBlockInspectionEvent(@Nonnull final BlockInspectionEvent event) {
        final ItemStack stack = Platform.get().getCloneItemStack(event.state, event.world, event.pos, GameUtils.getPlayer());
        gatherBlockText(stack, event.data, event.state, event.pos);
    }

    public static void onGatherText(@Nonnull final DiagnosticEvent event) {
        event.getLeft().addAll(diagnostics);
    }
}
