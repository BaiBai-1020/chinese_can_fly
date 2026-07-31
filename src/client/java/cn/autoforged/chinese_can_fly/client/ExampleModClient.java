package cn.autoforged.chinese_can_fly.client;

import net.fabricmc.api.ClientModInitializer;

public class ExampleModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		FlightSoundManager.init();
	}
}