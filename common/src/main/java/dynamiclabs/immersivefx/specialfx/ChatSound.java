/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
package dynamiclabs.immersivefx.specialfx;

import dynamiclabs.immersivefx.platform.event.ChatReceivedEvent;
import dynamiclabs.immersivefx.platform.event.ChatSendEvent;
import dynamiclabs.immersivefx.ImmersiveFX;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.ChatType;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;

public class ChatSound {

	public static void onChatSent(final ChatSendEvent event) {
		final LocalPlayer player = Minecraft.getInstance().player;
		if (player == null || event.getMessage().isEmpty())
			return;
		executeChatSound(player.level(), player.getX(), player.getY(), player.getZ());
	}

	public static void onChatReceived(final ChatReceivedEvent event) {
		final Minecraft mc = Minecraft.getInstance();
		final ClientLevel level = mc.level;
		if (level == null || mc.player == null || event.getSender().equals(mc.player.getUUID()))
			return;
		final ChatType.Bound chatType = event.getBoundChatType();
		if (chatType == null || !chatType.chatType().is(ChatType.CHAT))
			return;
		final Player sender = level.getPlayerByUUID(event.getSender());
		if (sender == null)
			return;
		executeChatSound(level, sender.getX(), sender.getY(), sender.getZ());
	}

	public static void executeChatSound(final LevelAccessor world, final double x, final double y, final double z) {
		ImmersivefxRegistry.playSound(SpecialFxConfig.OPTIONS.chat, world, x, y, z, ImmersivefxRegistry.sound("chat"), SoundSource.PLAYERS, 1.0F, 1.0F);
	}
}
