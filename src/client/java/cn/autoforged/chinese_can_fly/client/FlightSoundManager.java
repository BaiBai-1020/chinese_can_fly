package cn.autoforged.chinese_can_fly.client;

import cn.autoforged.chinese_can_fly.config.ModConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.GameType;

public class FlightSoundManager {
	private static FlightSoundInstance currentSound;
	private static boolean wasFlying;
	private static boolean wasFalling;
	private static String currentSoundId;

	public static void init() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			LocalPlayer player = client.player;
			if (player == null) {
				stopSound();
				wasFlying = false;
				wasFalling = false;
				return;
			}

			ModConfig config = ModConfig.get();
			GameType gameType = client.gameMode != null ? client.gameMode.getPlayerMode() : GameType.SURVIVAL;
			boolean isCreative = gameType == GameType.CREATIVE;
			boolean isSpectator = gameType == GameType.SPECTATOR;
			boolean isFlying = player.getAbilities().flying && !isCreative && !isSpectator;

			boolean isFalling = false;
			if (config.playSoundWhenFalling && !isCreative && !isSpectator) {
				if (player.getAbilities().mayfly && !player.getAbilities().flying) {
					isFalling = !player.onGround() && player.getDeltaMovement().y < -0.01
						&& airBlocksBelow(player) > config.fallingSoundMinAirBlocks;
				}
			}

			boolean soundShouldPlay = isFlying || isFalling;
			boolean wasPlaying = wasFlying || wasFalling;

			if (soundShouldPlay && !wasPlaying) {
				startSound(config);
			} else if (!soundShouldPlay && wasPlaying) {
				pauseSound();
			}

			wasFlying = isFlying;
			wasFalling = isFalling;
		});
	}

	private static void startSound(ModConfig config) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return;

		int fadeInTicks = config.fadeInMs > 0 ? Math.max(1, config.fadeInMs / 50) : 0;

		if (currentSound != null && config.flightSoundId.equals(currentSoundId) && currentSound.isValid()
			&& Minecraft.getInstance().getSoundManager().isActive(currentSound)) {
			currentSound.setLooping(config.loopAudio);
			currentSound.fadeIn(config.flightSoundVolume, fadeInTicks);
			return;
		}

		stopSound();
		Identifier soundId = Identifier.parse(config.flightSoundId);
		SoundEvent soundEvent = BuiltInRegistries.SOUND_EVENT.get(soundId);
		if (soundEvent != null) {
			currentSound = new FlightSoundInstance(soundEvent, player, 0);
			currentSound.setLooping(config.loopAudio);
			currentSound.fadeIn(config.flightSoundVolume, fadeInTicks);
			currentSoundId = config.flightSoundId;
			Minecraft.getInstance().getSoundManager().play(currentSound);
		}
	}

	private static void pauseSound() {
		if (currentSound == null) return;
		int fadeOutTicks = ModConfig.get().fadeOutMs > 0 ? Math.max(1, ModConfig.get().fadeOutMs / 50) : 0;
		currentSound.fadeOut(fadeOutTicks);
	}

	private static int airBlocksBelow(LocalPlayer player) {
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		int airCount = 0;
		double checkY = player.getY() - 1.0;
		while (true) {
			pos.set(player.getX(), checkY, player.getZ());
			if (player.level().getBlockState(pos).isAir()) {
				airCount++;
				checkY -= 1.0;
				if (airCount > 256) break;
			} else {
				break;
			}
		}
		return airCount;
	}

	public static void stopSound() {
		if (currentSound != null) {
			currentSound.stopNow();
			Minecraft.getInstance().getSoundManager().stop(currentSound);
			currentSound = null;
			currentSoundId = null;
		}
	}
}
