package cn.autoforged.chinese_can_fly;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {
	public static final SoundEvent FLIGHT_SOUND = register("flight_sound");

	private static SoundEvent register(String name) {
		Identifier id = Identifier.fromNamespaceAndPath(ExampleMod.MOD_ID, name);
		return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
	}

	public static void init() {}
}
