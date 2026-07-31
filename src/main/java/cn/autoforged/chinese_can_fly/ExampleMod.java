package cn.autoforged.chinese_can_fly;

import cn.autoforged.chinese_can_fly.config.ModConfig;
import cn.autoforged.chinese_can_fly.handler.FlightHandler;
import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.ResourceLocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleMod implements ModInitializer {
	public static final String MOD_ID = "chinese_can_fly";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModConfig.get();
		ModSounds.init();
		FlightHandler.init();
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
}
