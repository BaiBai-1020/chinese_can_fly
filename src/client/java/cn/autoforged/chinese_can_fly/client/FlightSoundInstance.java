package cn.autoforged.chinese_can_fly.client;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public class FlightSoundInstance extends AbstractTickableSoundInstance {
	private final LocalPlayer player;
	private float targetVolume;
	private int fadeInTicks;
	private int fadeOutTicks;
	private int fadeElapsed;
	private float fadeStartVolume;
	private boolean fadingIn;
	private boolean fadingOut;

	public FlightSoundInstance(SoundEvent soundEvent, LocalPlayer player, float volume) {
		super(soundEvent, SoundSource.PLAYERS, RandomSource.create());
		this.player = player;
		this.volume = volume;
		this.targetVolume = volume;
		this.looping = true;
		this.relative = false;
		this.x = player.getX();
		this.y = player.getY();
		this.z = player.getZ();
	}

	public void setVolume(float volume) {
		this.volume = volume;
	}

	public void setLooping(boolean looping) {
		this.looping = looping;
	}

	public void fadeIn(float targetVolume, int fadeInTicks) {
		this.fadingIn = true;
		this.fadingOut = false;
		this.targetVolume = targetVolume;
		this.fadeStartVolume = this.volume;
		this.fadeInTicks = fadeInTicks;
		this.fadeElapsed = 0;
		if (fadeInTicks <= 0) {
			this.volume = targetVolume;
			this.fadingIn = false;
		}
	}

	public void fadeOut(int fadeOutTicks) {
		this.fadingOut = true;
		this.fadingIn = false;
		this.fadeStartVolume = this.volume;
		this.fadeOutTicks = fadeOutTicks;
		this.fadeElapsed = 0;
		if (fadeOutTicks <= 0) {
			this.volume = 0;
			this.fadingOut = false;
		}
	}

	@Override
	public boolean canStartSilent() {
		return true;
	}

	public void stopNow() {
		this.stop();
	}

	public boolean isValid() {
		return this.player != null && !this.player.isRemoved();
	}

	@Override
	public void tick() {
		if (this.player == null || this.player.isRemoved()) {
			this.stop();
			return;
		}
		this.x = this.player.getX();
		this.y = this.player.getY();
		this.z = this.player.getZ();

		if (fadingIn) {
			fadeElapsed++;
			float progress = fadeInTicks > 0 ? Math.min(1.0f, (float) fadeElapsed / fadeInTicks) : 1.0f;
			volume = fadeStartVolume + (targetVolume - fadeStartVolume) * progress;
			if (progress >= 1.0f) {
				fadingIn = false;
				volume = targetVolume;
			}
		} else if (fadingOut) {
			fadeElapsed++;
			float progress = fadeOutTicks > 0 ? Math.min(1.0f, (float) fadeElapsed / fadeOutTicks) : 1.0f;
			volume = fadeStartVolume * (1.0f - progress);
			if (progress >= 1.0f) {
				fadingOut = false;
				volume = 0;
			}
		}
	}
}
