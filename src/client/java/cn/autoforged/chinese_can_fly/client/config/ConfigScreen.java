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
	private EditBox soundIdField, volumeField, checkIntervalField, minAirBlocksField, fadeInField, fadeOutField;
	private Button fallDamageToggle, loopToggle;
	private int page;
	private final List<Button> kwWidgets = new ArrayList<>();
	private static final int PAGE_SIZE = 3;
	private static final int LABEL_WIDTH = 60;
	private static final int WIDGET_HEIGHT = 20;
	private static final int COL_GAP = 12;
	private static final int ROW_H = 24;
	private static final int LIST_H = PAGE_SIZE * WIDGET_HEIGHT;
	private int col1X, col2X, colW, fieldW, listX, listWidth;

	public ConfigScreen(Screen parent) {
		super(Component.translatable("screen." + ExampleMod.MOD_ID + ".config.title"));
		this.parent = parent;
		this.config = ModConfig.get();
	}

	private void rebuildKeywords() {
		for (Button b : kwWidgets) this.removeWidget(b);
		kwWidgets.clear();
		int start = page * PAGE_SIZE, end = Math.min(start + PAGE_SIZE, config.triggerKeywords.size());
		for (int i = start; i < end; i++) {
			final int idx = i; int by = 30 + (i - start) * WIDGET_HEIGHT;
			kwWidgets.add(this.addRenderableWidget(Button.builder(Component.literal(config.triggerKeywords.get(i)), b -> {}).bounds(listX + 4, by, listWidth - 46, WIDGET_HEIGHT).build()));
			kwWidgets.add(this.addRenderableWidget(Button.builder(
				Component.literal("[" + Component.translatable("screen." + ExampleMod.MOD_ID + ".config.remove").getString() + "]"),
				b -> { config.triggerKeywords.remove(idx); config.save(); if (page * PAGE_SIZE >= config.triggerKeywords.size() && page > 0) page--; rebuildKeywords(); }
			).bounds(listX + listWidth - 46, by, 42, WIDGET_HEIGHT).build()));
		}
	}

	@Override
	protected void init() {
		this.listWidth = Math.min(300, this.width - 40);
		this.listX = (this.width - listWidth) / 2;
		this.colW = (listWidth - COL_GAP) / 2;
		this.col1X = listX;
		this.col2X = listX + colW + COL_GAP;
		this.fieldW = colW - LABEL_WIDTH - 4;
		if (page < 0) page = 0;
		rebuildKeywords();

		int rowY = 30 + LIST_H + 4;
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
			btn -> { String t = addKeywordField.getValue().trim(); if (!t.isEmpty() && !config.triggerKeywords.contains(t)) { config.triggerKeywords.add(t); addKeywordField.setValue(""); config.save(); rebuildKeywords(); } }
		).bounds(listX + listWidth - 45, rowY, 45, WIDGET_HEIGHT).build());

		rowY += ROW_H + 6;
		this.soundIdField = addField(col1X, rowY, fieldW, config.flightSoundId); this.soundIdField.setMaxLength(200);
		this.volumeField = addField(col2X, rowY, fieldW, String.valueOf(config.flightSoundVolume));
		rowY += ROW_H;
		this.checkIntervalField = addField(col1X, rowY, fieldW, String.valueOf(config.checkIntervalTicks));
		this.minAirBlocksField = addField(col2X, rowY, fieldW, String.valueOf(config.fallingSoundMinAirBlocks));
		rowY += ROW_H;
		this.fallDamageToggle = addToggleBtn(col1X, rowY, colW, "prevent_fall_damage", config.preventFallDamageWhenNotFlying, v -> { config.preventFallDamageWhenNotFlying = v; config.save(); });
		this.addRenderableWidget(Button.builder(toggleMsg("falling_sound", config.playSoundWhenFalling), btn -> {
			config.playSoundWhenFalling = !config.playSoundWhenFalling; config.save();
			btn.setMessage(toggleMsg("falling_sound", config.playSoundWhenFalling));
		}).bounds(col2X, rowY, colW, WIDGET_HEIGHT).build());
		rowY += ROW_H;
		this.fadeInField = addField(col1X, rowY, fieldW, String.valueOf(config.fadeInMs));
		this.fadeOutField = addField(col2X, rowY, fieldW, String.valueOf(config.fadeOutMs));
		rowY += ROW_H;
		this.loopToggle = addToggleBtn(col1X, rowY, colW, "loop_audio", config.loopAudio, v -> { config.loopAudio = v; config.save(); });

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, btn -> { saveAllFields(); this.minecraft.setScreen(parent); })
			.bounds(this.width / 2 - 50, this.height - 28, 100, WIDGET_HEIGHT).build());
	}

	private Component toggleMsg(String key, boolean on) { return Component.translatable("screen." + ExampleMod.MOD_ID + ".config." + key, Component.translatable(on ? "options.on" : "options.off")); }
	private EditBox addField(int x, int y, int w, String val) { EditBox f = new EditBox(this.font, x + LABEL_WIDTH + 4, y, w, WIDGET_HEIGHT, Component.empty()); f.setValue(val); this.addRenderableWidget(f); return f; }
	private Button addToggleBtn(int x, int y, int w, String key, boolean cur, java.util.function.Consumer<Boolean> setter) {
		return this.addRenderableWidget(Button.builder(toggleMsg(key, cur), btn -> { boolean n = !cur; setter.accept(n); btn.setMessage(toggleMsg(key, n)); }).bounds(x, y, w, WIDGET_HEIGHT).build());
	}

	private void saveAllFields() {
		String ns = soundIdField.getValue().trim(); if (!ns.isEmpty() && ns.contains(":")) config.flightSoundId = ns;
		try { config.flightSoundVolume = clamp(Float.parseFloat(volumeField.getValue().trim()), 0f, 1f); } catch (NumberFormatException ignored) {}
		try { config.checkIntervalTicks = Math.max(20, Integer.parseInt(checkIntervalField.getValue().trim())); } catch (NumberFormatException ignored) {}
		try { config.fallingSoundMinAirBlocks = Math.max(0, Integer.parseInt(minAirBlocksField.getValue().trim())); } catch (NumberFormatException ignored) {}
		try { config.fadeInMs = Math.max(0, Integer.parseInt(fadeInField.getValue().trim())); } catch (NumberFormatException ignored) {}
		try { config.fadeOutMs = Math.max(0, Integer.parseInt(fadeOutField.getValue().trim())); } catch (NumberFormatException ignored) {}
		config.save();
	}
	private static float clamp(float v, float min, float max) { return Math.max(min, Math.min(max, v)); }

	@Override
	public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
		super.render(g, mouseX, mouseY, delta);
		g.drawCenteredString(this.font, this.title.getString(), this.width / 2, 14, 0xFFFFFFFF);
		int lc = 0xFFA0A0A0, lh = this.font.lineHeight;
		drawLabel(g, "sound_id", soundIdField, lc, lh);
		drawLabel(g, "volume", volumeField, lc, lh);
		drawLabel(g, "check_interval", checkIntervalField, lc, lh);
		drawLabel(g, "min_air_blocks", minAirBlocksField, lc, lh);
		drawLabel(g, "fade_in", fadeInField, lc, lh);
		drawLabel(g, "fade_out", fadeOutField, lc, lh);
	}
	private void drawLabel(GuiGraphics g, String key, EditBox field, int color, int lh) {
		if (field == null) return;
		String txt = Component.translatable("screen." + ExampleMod.MOD_ID + ".config." + key).getString();
		g.drawString(this.font, txt, field.getX() - LABEL_WIDTH - 4, field.getY() + (WIDGET_HEIGHT - lh) / 2, color);
	}

	@Override public void onClose() { saveAllFields(); this.minecraft.setScreen(parent); }
}
