package cn.autoforged.chinese_can_fly.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ModConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("chinese_can_fly.json");
	private static ModConfig instance;

	public List<String> triggerKeywords = new ArrayList<>(List.of("中国人能飞"));
	public String flightSoundId = "chinese_can_fly:flight_sound";
	public float flightSoundVolume = 1.0f;
	public int checkIntervalTicks = 20;
	public boolean preventFallDamageWhenNotFlying = false;
	public boolean playSoundWhenFalling = true;
	public int fallingSoundMinAirBlocks = 4;
	public int fadeInMs = 1000;
	public int fadeOutMs = 50;
	public boolean loopAudio = true;

	public void validate() {
		if (checkIntervalTicks < 20) checkIntervalTicks = 20;
	}

	public static ModConfig get() {
		if (instance == null) {
			instance = load();
		}
		return instance;
	}

	private static ModConfig load() {
		if (Files.exists(CONFIG_PATH)) {
			try {
				String json = Files.readString(CONFIG_PATH);
				ModConfig config = GSON.fromJson(json, ModConfig.class);
				config.validate();
				return config;
			} catch (IOException e) {
			}
		}
		ModConfig config = new ModConfig();
		config.save();
		return config;
	}

	public void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			Files.writeString(CONFIG_PATH, GSON.toJson(this));
		} catch (IOException e) {
		}
	}

	public static void reload() {
		instance = load();
	}
}
