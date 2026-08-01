package cn.autoforged.chinese_can_fly.client.config;

import cn.autoforged.chinese_can_fly.ExampleMod;
import cn.autoforged.chinese_can_fly.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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
	private EditBox soundIdField, volumeField, checkIntervalField, minAirBlocksField, fadeInField, fadeOutField;
	private Button fallDamageToggle, loopToggle;

	private static final int LABEL_WIDTH = 60;
	private static final int WIDGET_HEIGHT = 20;
	private static final int COL_GAP = 12;
	private static final int ROW_H = 24;
	private static final int LIST_HEIGHT = 60;

	private int col1X, col2X, colW, fieldW, listX;

	public ConfigScreen(Screen parent) {
		super(Component.translatable("screen." + ExampleMod.MOD_ID + ".config.title"));
		this.parent = parent;
		this.config = ModConfig.get();
	}

	@Override
	protected void init() {
		int listWidth = Math.min(300, this.width - 40);
		this.listX = (this.width - listWidth) / 2;
		this.colW = (listWidth - COL_GAP) / 2;
		this.col1X = listX;
		this.col2X = listX + colW + COL_GAP;
		this.fieldW = colW - LABEL_WIDTH - 4;

		int listY = 30;
		this.keywordList = new KeywordList(this.minecraft, listWidth, LIST_HEIGHT, listY, WIDGET_HEIGHT);
		this.keywordList.setPosition(listX, listY);
		this.addRenderableWidget(this.keywordList);

		int addY = listY + LIST_HEIGHT + 4;
		this.addKeywordField = new EditBox(this.font, listX, addY, listWidth - 50, WIDGET_HEIGHT,
			Component.translatable("screen." + ExampleMod.MOD_ID + ".config.add_hint"));
		this.addRenderableWidget(this.addKeywordField);
		this.addRenderableWidget(Button.builder(
			Component.translatable("screen." + ExampleMod.MOD_ID + ".config.add"),
			btn -> { String t = addKeywordField.getValue().trim(); if (!t.isEmpty() && !config.triggerKeywords.contains(t)) { config.triggerKeywords.add(t); addKeywordField.setValue(""); config.save(); keywordList.refresh(); } }
		).bounds(listX + listWidth - 45, addY, 45, WIDGET_HEIGHT).build());

		int rowY = addY + ROW_H + 6;
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
		g.drawString(this.font, Component.translatable("screen." + ExampleMod.MOD_ID + ".config." + key).getString(), field.getX() - LABEL_WIDTH - 4, field.getY() + (WIDGET_HEIGHT - lh) / 2, color);
	}

	@Override public void onClose() { saveAllFields(); this.minecraft.setScreen(parent); }

	private class KeywordList extends ObjectSelectionList<KeywordEntry> {
		public KeywordList(Minecraft mc, int w, int h, int top, int ih) { super(mc, w, h, top, ih); refresh(); }
		void refresh() { clearEntries(); for (String kw : config.triggerKeywords) addEntry(new KeywordEntry(kw)); }
		@Override public int getRowWidth() { return this.width - 20; }
		public int getScrollbarPosition() { return this.getRight() - 6; }
	}
	private class KeywordEntry extends ObjectSelectionList.Entry<KeywordEntry> {
		final String kw;
		KeywordEntry(String kw) { this.kw = kw; }
		@Override public void render(GuiGraphics g, int idx, int y, int x, int ew, int eh, int mx, int my, boolean hov, float delta) {
			int cy = y + (eh - font.lineHeight) / 2;
			g.drawString(font, kw, x + 4, cy, 0xFFFFFFFF);
			String rl = "[" + Component.translatable("screen." + ExampleMod.MOD_ID + ".config.remove").getString() + "]";
			g.drawString(font, rl, x + keywordList.getRowWidth() - font.width(rl) - 4, cy, 0xFFFF5555);
		}
		@Override public boolean mouseClicked(double mx, double my, int btn) {
			int rl = keywordList.getRowLeft(), rw = keywordList.getRowWidth();
			String rls = "[" + Component.translatable("screen." + ExampleMod.MOD_ID + ".config.remove").getString() + "]";
			if (mx >= rl + rw - font.width(rls) - 8 && mx <= rl + rw) { config.triggerKeywords.remove(kw); config.save(); keywordList.refresh(); return true; }
			return false;
		}
		@Override public Component getNarration() { return Component.literal(kw); }
	}
}
