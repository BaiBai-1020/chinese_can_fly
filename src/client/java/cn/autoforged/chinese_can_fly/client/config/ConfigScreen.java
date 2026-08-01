package cn.autoforged.chinese_can_fly.client.config;

import cn.autoforged.chinese_can_fly.ExampleMod;
import cn.autoforged.chinese_can_fly.config.ModConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.List;

public class ConfigScreen extends Screen {
	private final Screen parent;
	private ModConfig config;
	private EditBox addKeywordField;
	private EditBox soundIdField;
	private EditBox volumeField;
	private EditBox checkIntervalField;
	private Button fallDamageToggle;
	private EditBox minAirBlocksField;
	private EditBox fadeInField;
	private EditBox fadeOutField;
	private Button loopToggle;
	private int page;
	private final List<Button> kwWidgets = new ArrayList<>();
	private static final int PAGE_SIZE = 3;

	private static final int LABEL_WIDTH = 80;
	private static final int WIDGET_HEIGHT = 20;
	private static final int ROW_SPACING = 24;
	private static final int LIST_HEIGHT = PAGE_SIZE * WIDGET_HEIGHT;
	private int listX, listY, listWidth;

	public ConfigScreen(Screen parent) {
		super(Component.translatable("screen." + ExampleMod.MOD_ID + ".config.title"));
		this.parent = parent;
		this.config = ModConfig.get();
	}

	private void rebuildKeywords() {
		for (Button b : kwWidgets) this.removeWidget(b);
		kwWidgets.clear();
		int start = page * PAGE_SIZE;
		int end = Math.min(start + PAGE_SIZE, config.triggerKeywords.size());
		for (int i = start; i < end; i++) {
			final int idx = i;
			String kw = config.triggerKeywords.get(i);
			int by = listY + (i - start) * WIDGET_HEIGHT;
			Button kwBtn = this.addRenderableWidget(Button.builder(Component.literal(kw), b -> {}).bounds(listX + 4, by, listWidth - 46, WIDGET_HEIGHT).build());
			kwWidgets.add(kwBtn);
			Button delBtn = this.addRenderableWidget(Button.builder(
				Component.literal("[" + Component.translatable("screen." + ExampleMod.MOD_ID + ".config.remove").getString() + "]"),
				b -> { config.triggerKeywords.remove(idx); config.save(); if (page * PAGE_SIZE >= config.triggerKeywords.size() && page > 0) page--; rebuildKeywords(); }
			).bounds(listX + listWidth - 46, by, 42, WIDGET_HEIGHT).build());
			kwWidgets.add(delBtn);
		}
	}

	@Override
	protected void init() {
		this.listWidth = Math.min(300, this.width - 40);
		this.listX = (this.width - listWidth) / 2;
		this.listY = 30;
		if (page < 0) page = 0;
		rebuildKeywords();

		int rowY = listY + LIST_HEIGHT + 4;
		int totalPages = Math.max(1, (config.triggerKeywords.size() + PAGE_SIZE - 1) / PAGE_SIZE);
		if (totalPages > 1) {
			this.addRenderableWidget(Button.builder(Component.literal("<"), b -> { if (page > 0) { page--; rebuildKeywords(); } }).bounds(listX, rowY, 20, WIDGET_HEIGHT).build());
			this.addRenderableWidget(Button.builder(Component.literal(">"), b -> { if ((page + 1) * PAGE_SIZE < config.triggerKeywords.size()) { page++; rebuildKeywords(); } }).bounds(listX + 24, rowY, 20, WIDGET_HEIGHT).build());
			rowY += WIDGET_HEIGHT + 2;
		}

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
					rebuildKeywords();
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
			this.minecraft.setScreen(parent);
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
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.render(graphics, mouseX, mouseY, delta);
		String titleStr = this.title.getString();
		graphics.drawCenteredString(this.font, titleStr, this.width / 2, 14, 0xFFFFFFFF);
		int labelColor = 0xFFA0A0A0;
		final int lh = this.font.lineHeight;
		drawLabel(graphics, "screen." + ExampleMod.MOD_ID + ".config.sound_id", soundIdField, labelColor, lh);
		drawLabel(graphics, "screen." + ExampleMod.MOD_ID + ".config.volume", volumeField, labelColor, lh);
		drawLabel(graphics, "screen." + ExampleMod.MOD_ID + ".config.check_interval", checkIntervalField, labelColor, lh);
		drawLabel(graphics, "screen." + ExampleMod.MOD_ID + ".config.min_air_blocks", minAirBlocksField, labelColor, lh);
		drawLabel(graphics, "screen." + ExampleMod.MOD_ID + ".config.fade_in", fadeInField, labelColor, lh);
		drawLabel(graphics, "screen." + ExampleMod.MOD_ID + ".config.fade_out", fadeOutField, labelColor, lh);
	}

	private void drawLabel(GuiGraphics graphics, String key, EditBox field, int color, int lineHeight) {
		if (field == null) return;
		String text = Component.translatable(key).getString();
		int labelX = field.getX() - LABEL_WIDTH - 4;
		int labelY = field.getY() + (WIDGET_HEIGHT - lineHeight) / 2;
		graphics.drawString(this.font, text, labelX, labelY, color);
	}

	@Override
	public void onClose() {
		saveAllFields();
		this.minecraft.setScreen(parent);
	}
}
