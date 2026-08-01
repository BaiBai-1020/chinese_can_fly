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
	private static final int WIDGET_HEIGHT = 20;
	private static final int ROW_SPACING = 24;
	private static final int LIST_HEIGHT = 60;

	public ConfigScreen(Screen parent) {
		super(Component.translatable("screen." + ExampleMod.MOD_ID + ".config.title"));
		this.parent = parent;
		this.config = ModConfig.get();
	}

	@Override
	protected void init() {
		int listWidth = Math.min(300, this.width - 40);
		int listX = (this.width - listWidth) / 2;

		int listY = 30;
		this.keywordList = new KeywordList(this.minecraft, listWidth, LIST_HEIGHT, listY, WIDGET_HEIGHT);
		this.keywordList.setPosition(listX, listY);
		this.addRenderableWidget(this.keywordList);

		int rowY = listY + LIST_HEIGHT + 4;
		this.addKeywordField = new EditBox(this.font, listX, rowY, listWidth - 50, WIDGET_HEIGHT,
			Component.translatable("screen." + ExampleMod.MOD_ID + ".config.add_hint"));
		this.addRenderableWidget(this.addKeywordField);

		this.addRenderableWidget(Button.builder(
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
		).bounds(listX + listWidth - 45, rowY, 45, WIDGET_HEIGHT).build());

		int fieldStartY = rowY + ROW_SPACING + 8;
		int fieldX = listX + LABEL_WIDTH + 4;
		int fieldWidth = listWidth - LABEL_WIDTH - 4;
		int btnWidth = listWidth;

		this.soundIdField = addFieldRow(fieldStartY, fieldX, fieldWidth, config.flightSoundId);
		this.soundIdField.setMaxLength(200);
		this.volumeField = addFieldRow(fieldStartY + ROW_SPACING, fieldX, fieldWidth, String.valueOf(config.flightSoundVolume));
		this.checkIntervalField = addFieldRow(fieldStartY + ROW_SPACING * 2, fieldX, fieldWidth, String.valueOf(config.checkIntervalTicks));

		int btnY = fieldStartY + ROW_SPACING * 3;
		this.fallDamageToggle = addToggleButton(listX, btnY, btnWidth,
			"screen." + ExampleMod.MOD_ID + ".config.prevent_fall_damage",
			config.preventFallDamageWhenNotFlying,
			v -> { config.preventFallDamageWhenNotFlying = v; config.save(); });

		this.addRenderableWidget(Button.builder(
			Component.translatable("screen." + ExampleMod.MOD_ID + ".config.falling_sound",
				Component.translatable(config.playSoundWhenFalling ? "options.on" : "options.off")),
			btn -> {
				config.playSoundWhenFalling = !config.playSoundWhenFalling;
				config.save();
				btn.setMessage(Component.translatable("screen." + ExampleMod.MOD_ID + ".config.falling_sound",
					Component.translatable(config.playSoundWhenFalling ? "options.on" : "options.off")));
			}
		).bounds(listX, btnY + ROW_SPACING, btnWidth, WIDGET_HEIGHT).build());

		this.minAirBlocksField = addFieldRow(btnY + ROW_SPACING * 2, fieldX, fieldWidth, String.valueOf(config.fallingSoundMinAirBlocks));
		this.fadeInField = addFieldRow(btnY + ROW_SPACING * 3, fieldX, fieldWidth, String.valueOf(config.fadeInMs));
		this.fadeOutField = addFieldRow(btnY + ROW_SPACING * 4, fieldX, fieldWidth, String.valueOf(config.fadeOutMs));

		this.loopToggle = addToggleButton(listX, btnY + ROW_SPACING * 5, btnWidth,
			"screen." + ExampleMod.MOD_ID + ".config.loop_audio",
			config.loopAudio,
			v -> { config.loopAudio = v; config.save(); });

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, btn -> {
			saveAllFields();
			this.minecraft.gui.setScreen(parent);
		}).bounds(this.width / 2 - 50, this.height - 28, 100, WIDGET_HEIGHT).build());
	}

	private EditBox addFieldRow(int y, int x, int w, String value) {
		EditBox field = new EditBox(this.font, x, y, w, WIDGET_HEIGHT, Component.empty());
		field.setValue(value);
		this.addRenderableWidget(field);
		return field;
	}

	private Button addToggleButton(int x, int y, int w, String key, boolean current, java.util.function.Consumer<Boolean> setter) {
		return this.addRenderableWidget(Button.builder(
			Component.translatable(key, Component.translatable(current ? "options.on" : "options.off")),
			btn -> {
				boolean next = !current;
				setter.accept(next);
				btn.setMessage(Component.translatable(key, Component.translatable(next ? "options.on" : "options.off")));
			}
		).bounds(x, y, w, WIDGET_HEIGHT).build());
	}

	private void saveAllFields() {
		String newSoundId = soundIdField.getValue().trim();
		if (!newSoundId.isEmpty() && newSoundId.contains(":")) { config.flightSoundId = newSoundId; }
		try { config.flightSoundVolume = clamp(Float.parseFloat(volumeField.getValue().trim()), 0f, 1f); } catch (NumberFormatException ignored) {}
		try { config.checkIntervalTicks = Math.max(20, Integer.parseInt(checkIntervalField.getValue().trim())); } catch (NumberFormatException ignored) {}
		try { config.fallingSoundMinAirBlocks = Math.max(0, Integer.parseInt(minAirBlocksField.getValue().trim())); } catch (NumberFormatException ignored) {}
		try { config.fadeInMs = Math.max(0, Integer.parseInt(fadeInField.getValue().trim())); } catch (NumberFormatException ignored) {}
		try { config.fadeOutMs = Math.max(0, Integer.parseInt(fadeOutField.getValue().trim())); } catch (NumberFormatException ignored) {}
		config.save();
	}

	private static float clamp(float v, float min, float max) { return Math.max(min, Math.min(max, v)); }

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		super.extractRenderState(graphics, mouseX, mouseY, delta);
		String titleStr = this.title.getString();
		int tw = this.font.width(titleStr); graphics.text(this.font, titleStr, this.width / 2 - tw / 2, 14, 0xFFFFFFFF, false);
		int labelColor = 0xFFA0A0A0;
		final int lh = this.font.lineHeight;
		drawLabel(graphics, "screen." + ExampleMod.MOD_ID + ".config.sound_id", soundIdField, labelColor, lh);
		drawLabel(graphics, "screen." + ExampleMod.MOD_ID + ".config.volume", volumeField, labelColor, lh);
		drawLabel(graphics, "screen." + ExampleMod.MOD_ID + ".config.check_interval", checkIntervalField, labelColor, lh);
		drawLabel(graphics, "screen." + ExampleMod.MOD_ID + ".config.min_air_blocks", minAirBlocksField, labelColor, lh);
		drawLabel(graphics, "screen." + ExampleMod.MOD_ID + ".config.fade_in", fadeInField, labelColor, lh);
		drawLabel(graphics, "screen." + ExampleMod.MOD_ID + ".config.fade_out", fadeOutField, labelColor, lh);
	}

	private void drawLabel(GuiGraphicsExtractor graphics, String key, EditBox field, int color, int lineHeight) {
		if (field == null) return;
		String text = Component.translatable(key).getString();
		int labelX = field.getX() - LABEL_WIDTH - 4;
		int labelY = field.getY() + (WIDGET_HEIGHT - lineHeight) / 2;
		graphics.text(this.font, text, labelX, labelY, color, false);
	}

	@Override
	public void onClose() {
		saveAllFields();
		this.minecraft.gui.setScreen(parent);
	}

	private class KeywordList extends ObjectSelectionList<KeywordEntry> {
		private final int listX;
		public KeywordList(Minecraft minecraft, int width, int height, int y0, int itemHeight) {
			super(minecraft, width, height, y0, itemHeight);
			this.listX = (Minecraft.getInstance().getWindow().getGuiScaledWidth() - width) / 2;
			refresh();
		}
		public void refresh() {
			this.clearEntries();
			int idx = 0;
			for (String keyword : config.triggerKeywords) {
				this.addEntry(new KeywordEntry(keyword, this, idx++));
			}
		}
		@Override
		public int getRowWidth() { return this.width - 20; }
		public int getScrollbarPosition() { return this.listX + this.width - 6; }
		protected void renderSelection(GuiGraphicsExtractor g, int y, int w, int h, int o, int i) {}
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			int rowWidth = getRowWidth();
			String removeLabel = "[" + Component.translatable("screen." + ExampleMod.MOD_ID + ".config.remove").getString() + "]";
			int removeRight = this.listX + rowWidth - 4;
			int removeLeft = removeRight - font.width(removeLabel) - 4;
			for (KeywordEntry entry : this.children()) {
				double entryY = this.getY() + entry.index * WIDGET_HEIGHT;
				if (mouseY >= entryY && mouseY < entryY + WIDGET_HEIGHT) {
					if (mouseX >= removeLeft && mouseX <= removeRight) {
						config.triggerKeywords.remove(entry.keyword);
						config.save();
						refresh();
						return true;
					}
				}
			}
			return false;
		}
	}

	private class KeywordEntry extends ObjectSelectionList.Entry<KeywordEntry> {
		final String keyword;
		private final KeywordList owner;
		final int index;
		public KeywordEntry(String keyword, KeywordList owner, int index) { this.keyword = keyword; this.owner = owner; this.index = index; }

		@Override
		public void extractContent(GuiGraphicsExtractor graphics, int x, int y, boolean hovered, float delta) {
			int sx = owner.listX + 4;
			int sy = owner.getY() + index * WIDGET_HEIGHT + (WIDGET_HEIGHT - font.lineHeight) / 2;
			graphics.text(font, keyword, sx, sy, 0xFFFFFFFF, false);
			String removeLabel = "[" + Component.translatable("screen." + ExampleMod.MOD_ID + ".config.remove").getString() + "]";
			graphics.text(font, removeLabel, sx + owner.getRowWidth() - font.width(removeLabel) - 4, sy, 0xFFFF5555, false);
		}

		@Override
		public Component getNarration() { return Component.literal(keyword); }
	}
}
