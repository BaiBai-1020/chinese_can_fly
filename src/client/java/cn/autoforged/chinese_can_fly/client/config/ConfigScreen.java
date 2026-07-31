package cn.autoforged.chinese_can_fly.client.config;

import cn.autoforged.chinese_can_fly.ExampleMod;
import cn.autoforged.chinese_can_fly.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ConfigScreen extends Screen {
	private final Screen parent;
	private ModConfig config;
	private KeywordList keywordList;
	private EditBox addKeywordField;
	private EditBox soundIdField;
	private EditBox volumeField;
	private EditBox checkIntervalField;
	private Button fallDamageToggle;
	private EditBox minAirBlocksField;
	private EditBox fadeInField;
	private EditBox fadeOutField;
	private Button loopToggle;

	private static final int LABEL_WIDTH = 80;

	public ConfigScreen(Screen parent) {
		super(Component.translatable("screen." + ExampleMod.MOD_ID + ".config.title"));
		this.parent = parent;
		this.config = ModConfig.get();
	}

	@Override
	protected void init() {
		int listWidth = Math.min(280, this.width - 40);
		int listX = (this.width - listWidth) / 2;
		int listY = 28;

		int keywordListHeight = Math.max(30, Math.min(60, (this.height - 280) / 3));
		this.keywordList = new KeywordList(this.minecraft, listWidth, keywordListHeight, listY, 20);
		this.keywordList.setPosition(listX, listY);
		this.addRenderableWidget(this.keywordList);

		int addFieldY = listY + keywordListHeight + 2;
		this.addKeywordField = new EditBox(this.font, listX, addFieldY, listWidth - 50, 20,
			Component.translatable("screen." + ExampleMod.MOD_ID + ".config.add_hint"));
		this.addRenderableWidget(this.addKeywordField);

		Button addBtn = Button.builder(
			Component.translatable("screen." + ExampleMod.MOD_ID + ".config.add"),
			btn -> {
				String text = addKeywordField.getValue().trim();
				if (!text.isEmpty() && !config.triggerKeywords.contains(text)) {
					config.triggerKeywords.add(text);
					addKeywordField.setValue("");
					config.save();
					keywordList.refresh();
				}
			}
		).bounds(listX + listWidth - 45, addFieldY, 45, 20).build();
		this.addRenderableWidget(addBtn);

		int fieldStartY = addFieldY + 24;
		int fieldWidth = listWidth - LABEL_WIDTH - 4;
		int fieldX = listX + LABEL_WIDTH + 4;

		this.soundIdField = new EditBox(this.font, fieldX, fieldStartY, fieldWidth, 20,
			Component.translatable("screen." + ExampleMod.MOD_ID + ".config.sound_id"));
		this.soundIdField.setMaxLength(200);
		this.soundIdField.setValue(config.flightSoundId);
		this.addRenderableWidget(this.soundIdField);

		this.volumeField = new EditBox(this.font, fieldX, fieldStartY + 24, fieldWidth, 20,
			Component.translatable("screen." + ExampleMod.MOD_ID + ".config.volume"));
		this.volumeField.setValue(String.valueOf(config.flightSoundVolume));
		this.addRenderableWidget(this.volumeField);

		this.checkIntervalField = new EditBox(this.font, fieldX, fieldStartY + 48, fieldWidth, 20,
			Component.translatable("screen." + ExampleMod.MOD_ID + ".config.check_interval"));
		this.checkIntervalField.setValue(String.valueOf(config.checkIntervalTicks));
		this.addRenderableWidget(this.checkIntervalField);

		this.fallDamageToggle = Button.builder(
			Component.translatable("screen." + ExampleMod.MOD_ID + ".config.prevent_fall_damage",
				Component.translatable(config.preventFallDamageWhenNotFlying ? "options.on" : "options.off")),
			btn -> {
				config.preventFallDamageWhenNotFlying = !config.preventFallDamageWhenNotFlying;
				config.save();
				btn.setMessage(Component.translatable("screen." + ExampleMod.MOD_ID + ".config.prevent_fall_damage",
					Component.translatable(config.preventFallDamageWhenNotFlying ? "options.on" : "options.off")));
			}
		).bounds(listX, fieldStartY + 72, listWidth, 20).build();
		this.addRenderableWidget(this.fallDamageToggle);

		Button fallingSoundToggle = Button.builder(
			Component.translatable("screen." + ExampleMod.MOD_ID + ".config.falling_sound",
				Component.translatable(config.playSoundWhenFalling ? "options.on" : "options.off")),
			btn -> {
				config.playSoundWhenFalling = !config.playSoundWhenFalling;
				config.save();
				btn.setMessage(Component.translatable("screen." + ExampleMod.MOD_ID + ".config.falling_sound",
					Component.translatable(config.playSoundWhenFalling ? "options.on" : "options.off")));
			}
		).bounds(listX, fieldStartY + 96, listWidth, 20).build();
		this.addRenderableWidget(fallingSoundToggle);

		this.minAirBlocksField = new EditBox(this.font, fieldX, fieldStartY + 120, fieldWidth, 20,
			Component.translatable("screen." + ExampleMod.MOD_ID + ".config.min_air_blocks"));
		this.minAirBlocksField.setValue(String.valueOf(config.fallingSoundMinAirBlocks));
		this.addRenderableWidget(this.minAirBlocksField);

		this.fadeInField = new EditBox(this.font, fieldX, fieldStartY + 144, fieldWidth, 20,
			Component.translatable("screen." + ExampleMod.MOD_ID + ".config.fade_in"));
		this.fadeInField.setValue(String.valueOf(config.fadeInMs));
		this.addRenderableWidget(this.fadeInField);

		this.fadeOutField = new EditBox(this.font, fieldX, fieldStartY + 168, fieldWidth, 20,
			Component.translatable("screen." + ExampleMod.MOD_ID + ".config.fade_out"));
		this.fadeOutField.setValue(String.valueOf(config.fadeOutMs));
		this.addRenderableWidget(this.fadeOutField);

		this.loopToggle = Button.builder(
			Component.translatable("screen." + ExampleMod.MOD_ID + ".config.loop_audio",
				Component.translatable(config.loopAudio ? "options.on" : "options.off")),
			btn -> {
				config.loopAudio = !config.loopAudio;
				config.save();
				btn.setMessage(Component.translatable("screen." + ExampleMod.MOD_ID + ".config.loop_audio",
					Component.translatable(config.loopAudio ? "options.on" : "options.off")));
			}
		).bounds(listX, fieldStartY + 192, listWidth, 20).build();
		this.addRenderableWidget(this.loopToggle);

		Button doneBtn = Button.builder(
			CommonComponents.GUI_DONE,
			btn -> {
				saveAllFields();
				this.minecraft.gui.setScreen(parent);
			}
		).bounds(this.width / 2 - 50, this.height - 24, 100, 20).build();
		this.addRenderableWidget(doneBtn);
	}

	private void saveAllFields() {
		String newSoundId = soundIdField.getValue().trim();
		if (!newSoundId.isEmpty() && newSoundId.contains(":")) {
			config.flightSoundId = newSoundId;
		}
		try {
			float vol = Float.parseFloat(volumeField.getValue().trim());
			config.flightSoundVolume = Math.max(0.0f, Math.min(1.0f, vol));
		} catch (NumberFormatException ignored) {
		}
		try {
			int check = Integer.parseInt(checkIntervalField.getValue().trim());
			config.checkIntervalTicks = Math.max(20, check);
		} catch (NumberFormatException ignored) {
		}
		try {
			int minAir = Integer.parseInt(minAirBlocksField.getValue().trim());
			config.fallingSoundMinAirBlocks = Math.max(0, minAir);
		} catch (NumberFormatException ignored) {
		}
		try {
			int fadeIn = Integer.parseInt(fadeInField.getValue().trim());
			config.fadeInMs = Math.max(0, fadeIn);
		} catch (NumberFormatException ignored) {
		}
		try {
			int fadeOut = Integer.parseInt(fadeOutField.getValue().trim());
			config.fadeOutMs = Math.max(0, fadeOut);
		} catch (NumberFormatException ignored) {
		}
		config.save();
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		super.extractRenderState(graphics, mouseX, mouseY, delta);
		String titleStr = this.title.getString();
		int titleWidth = this.font.width(titleStr);
		graphics.text(this.font, titleStr, this.width / 2 - titleWidth / 2, 10, 0xFFFFFF, false);
		if (this.soundIdField != null) {
			int labelColor = 0xA0A0A0;
			graphics.text(this.font,
				Component.translatable("screen." + ExampleMod.MOD_ID + ".config.sound_id").getString(),
				this.soundIdField.getX() - LABEL_WIDTH - 4, this.soundIdField.getY() + 5, labelColor, false);
			graphics.text(this.font,
				Component.translatable("screen." + ExampleMod.MOD_ID + ".config.volume").getString(),
				this.volumeField.getX() - LABEL_WIDTH - 4, this.volumeField.getY() + 5, labelColor, false);
			graphics.text(this.font,
				Component.translatable("screen." + ExampleMod.MOD_ID + ".config.check_interval").getString(),
				this.checkIntervalField.getX() - LABEL_WIDTH - 4, this.checkIntervalField.getY() + 5, labelColor, false);
			graphics.text(this.font,
				Component.translatable("screen." + ExampleMod.MOD_ID + ".config.min_air_blocks").getString(),
				this.minAirBlocksField.getX() - LABEL_WIDTH - 4, this.minAirBlocksField.getY() + 5, labelColor, false);
			graphics.text(this.font,
				Component.translatable("screen." + ExampleMod.MOD_ID + ".config.fade_in").getString(),
				this.fadeInField.getX() - LABEL_WIDTH - 4, this.fadeInField.getY() + 5, labelColor, false);
			graphics.text(this.font,
				Component.translatable("screen." + ExampleMod.MOD_ID + ".config.fade_out").getString(),
				this.fadeOutField.getX() - LABEL_WIDTH - 4, this.fadeOutField.getY() + 5, labelColor, false);
		}
	}

	@Override
	public void onClose() {
		saveAllFields();
		this.minecraft.gui.setScreen(parent);
	}

	private class KeywordList extends ObjectSelectionList<KeywordEntry> {
		public KeywordList(Minecraft minecraft, int width, int height, int y0, int itemHeight) {
			super(minecraft, width, height, y0, itemHeight);
			refresh();
		}

		public void refresh() {
			this.clearEntries();
			for (String keyword : config.triggerKeywords) {
				this.addEntry(new KeywordEntry(keyword, this));
			}
		}

		@Override
		public int getRowWidth() {
			return this.width - 20;
		}

		@Override
		protected int getScrollbarPosition() {
			return this.getRight() - 6;
		}
	}

	private class KeywordEntry extends ObjectSelectionList.Entry<KeywordEntry> {
		private final String keyword;
		private final KeywordList owner;

		public KeywordEntry(String keyword, KeywordList owner) {
			this.keyword = keyword;
			this.owner = owner;
		}

		@Override
		public void extractContent(GuiGraphicsExtractor graphics, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean hovered, float delta) {
			graphics.text(font, keyword, x + 4, y + 4, 0xFFFFFF, false);
			String removeLabel = "[" + Component.translatable("screen." + ExampleMod.MOD_ID + ".config.remove").getString() + "]";
			graphics.text(font, removeLabel, x + width - 40, y + 4, 0xFF5555, false);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			int rowLeft = owner.getRowLeft();
			int rowWidth = owner.getRowWidth();
			if (mouseX >= rowLeft + rowWidth - 44 && mouseX <= rowLeft + rowWidth - 4) {
				config.triggerKeywords.remove(keyword);
				config.save();
				owner.refresh();
				return true;
			}
			return false;
		}

		@Override
		public Component getNarration() {
			return Component.literal(keyword);
		}
	}
}
