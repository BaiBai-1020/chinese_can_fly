package cn.autoforged.chinese_can_fly.handler;

import cn.autoforged.chinese_can_fly.ExampleMod;
import cn.autoforged.chinese_can_fly.config.ModConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FlightHandler {
	private static final Set<UUID> SESSION_TRIGGERED = new HashSet<>();
	private static final Set<UUID> MOD_SET_FLIGHT = new HashSet<>();
	private static int tickCounter = 0;

	public static void init() {
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			SESSION_TRIGGERED.remove(handler.player.getUUID());
			MOD_SET_FLIGHT.remove(handler.player.getUUID());
		});

		ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
			String text = message.signedContent();
			ModConfig config = ModConfig.get();
			for (String keyword : config.triggerKeywords) {
				if (text.contains(keyword)) {
					SESSION_TRIGGERED.add(sender.getUUID());
					sender.sendSystemMessage(Component.translatable("message." + ExampleMod.MOD_ID + ".flight_activated").withStyle(style -> style.withColor(TextColor.fromRgb(0x55FF55))));
					break;
				}
			}
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			tickCounter++;
			boolean doFlightCheck = tickCounter >= ModConfig.get().checkIntervalTicks;
			if (doFlightCheck) {
				tickCounter = 0;
			}
			checkAllPlayers(server, doFlightCheck);
		});
	}

	private static void checkAllPlayers(MinecraftServer server, boolean doFlightCheck) {
		ModConfig config = ModConfig.get();
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			boolean isCreative = player.gameMode.getGameModeForPlayer() == GameType.CREATIVE;
			boolean isSpectator = player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR;

			if (doFlightCheck) {
				updateFlightPermission(player, config, isCreative, isSpectator);
			}
		}
	}

	private static void updateFlightPermission(ServerPlayer player, ModConfig config, boolean isCreative, boolean isSpectator) {
		String locale = player.clientInformation().language();
		boolean localeMatch = locale.startsWith("zh_");
		boolean triggered = SESSION_TRIGGERED.contains(player.getUUID());
		boolean shouldFly = localeMatch || triggered;

		if (shouldFly) {
			if (!isCreative && !isSpectator && !player.getAbilities().mayfly) {
				player.getAbilities().mayfly = true;
				player.onUpdateAbilities();
			}
			MOD_SET_FLIGHT.add(player.getUUID());
		} else if (MOD_SET_FLIGHT.contains(player.getUUID())) {
			player.getAbilities().mayfly = false;
			player.getAbilities().flying = false;
			player.onUpdateAbilities();
			MOD_SET_FLIGHT.remove(player.getUUID());
		}
	}

	public static boolean isFlightPermitted(Player player) {
		return MOD_SET_FLIGHT.contains(player.getUUID());
	}
}
