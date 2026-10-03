package com.pvphelper.screen;

import com.pvphelper.config.ConfigManager;
import com.pvphelper.config.PvPHelperConfig;
import com.pvphelper.core.HelperManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class PvPHelperConfigScreen extends Screen {
	private static final int COL_W = 150;
	private static final int GAP = 10;
	private static final int ROW = 24;

	private final Screen parent;

	public PvPHelperConfigScreen(Screen parent) {
		super(Component.literal("PvP Helper"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		PvPHelperConfig c = ConfigManager.get();
		int left = (this.width - (COL_W * 3 + GAP * 2)) / 2;
		int x1 = left;
		int x2 = left + COL_W + GAP;
		int x3 = left + (COL_W + GAP) * 2;

		// Master row
		addRenderableWidget(Button.builder(Component.literal("Enable All"), b -> {
			HelperManager.setAll(this.minecraft, true);
			rebuildWidgets();
		}).bounds(this.width / 2 - 155, 24, 150, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Disable All"), b -> {
			HelperManager.setAll(this.minecraft, false);
			rebuildWidgets();
		}).bounds(this.width / 2 + 5, 24, 150, 20).build());

		int top = 68;

		// TNT Cart Helper
		int y = top;
		addRenderableWidget(toggle(x1, y, "TNT Cart Helper", () -> c.tnt.enabled, v -> c.tnt.enabled = v)); y += ROW;
		addRenderableWidget(new IntSlider(x1, y, "Step delay (ticks)", 1, 10, c.tnt.interactionDelayTicks, v -> c.tnt.interactionDelayTicks = v)); y += ROW;
		addRenderableWidget(toggle(x1, y, "Flint & Steel", () -> c.tnt.flintAndSteelFollowUp, v -> c.tnt.flintAndSteelFollowUp = v)); y += ROW;
		addRenderableWidget(toggle(x1, y, "Flame Bow fallback", () -> c.tnt.flameBowFallback, v -> c.tnt.flameBowFallback = v)); y += ROW;
		addRenderableWidget(toggle(x1, y, "Debug", () -> c.tnt.debug, v -> c.tnt.debug = v));

		// Pearl Catch Helper
		y = top;
		addRenderableWidget(toggle(x2, y, "Pearl Catch Helper", () -> c.pearl.enabled, v -> c.pearl.enabled = v)); y += ROW;
		addRenderableWidget(new IntSlider(x2, y, "Wind Charge delay", 1, 20, c.pearl.windChargeDelayTicks, v -> c.pearl.windChargeDelayTicks = v)); y += ROW;
		addRenderableWidget(new IntSlider(x2, y, "Cooldown", 0, 100, c.pearl.cooldownTicks, v -> c.pearl.cooldownTicks = v)); y += ROW;
		addRenderableWidget(toggle(x2, y, "Debug", () -> c.pearl.debug, v -> c.pearl.debug = v));

		// Lunge Helper
		y = top;
		addRenderableWidget(toggle(x3, y, "Lunge Helper", () -> c.lunge.enabled, v -> c.lunge.enabled = v)); y += ROW;
		addRenderableWidget(new IntSlider(x3, y, "Activation delay", 1, 10, c.lunge.activationDelayTicks, v -> c.lunge.activationDelayTicks = v)); y += ROW;
		addRenderableWidget(new IntSlider(x3, y, "Return delay", 1, 10, c.lunge.returnDelayTicks, v -> c.lunge.returnDelayTicks = v)); y += ROW;
		addRenderableWidget(new IntSlider(x3, y, "Cooldown", 0, 100, c.lunge.cooldownTicks, v -> c.lunge.cooldownTicks = v)); y += ROW;
		addRenderableWidget(toggle(x3, y, "Require Lunge", () -> c.lunge.requireLungeEnchantment, v -> c.lunge.requireLungeEnchantment = v)); y += ROW;
		addRenderableWidget(toggle(x3, y, "Food is eligible", () -> c.lunge.eligibleFood, v -> c.lunge.eligibleFood = v)); y += ROW;
		addRenderableWidget(toggle(x3, y, "Debug", () -> c.lunge.debug, v -> c.lunge.debug = v));

		addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
			.bounds(this.width / 2 - 100, this.height - 26, 200, 20).build());
	}

	private Button toggle(int x, int y, String label, BooleanSupplier getter, Consumer<Boolean> setter) {
		return Button.builder(toggleText(label, getter.getAsBoolean()), button -> {
			setter.accept(!getter.getAsBoolean());
			button.setMessage(toggleText(label, getter.getAsBoolean()));
			ConfigManager.save();
		}).bounds(x, y, COL_W, 20).build();
	}

	private static Component toggleText(String label, boolean on) {
		return Component.literal(label + ": " + (on ? "ON" : "OFF"));
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);
		graphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFFFF);
	}

	@Override
	public void removed() {
		ConfigManager.save();
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.parent);
	}

	/** Integer slider (ticks). Values are written to the config immediately and saved when the screen closes. */
	private static class IntSlider extends AbstractSliderButton {
		private final String label;
		private final int min;
		private final int max;
		private final IntConsumer onChange;

		IntSlider(int x, int y, String label, int min, int max, int value, IntConsumer onChange) {
			super(x, y, COL_W, 20, Component.empty(), (double) (value - min) / (double) (max - min));
			this.label = label;
			this.min = min;
			this.max = max;
			this.onChange = onChange;
			updateMessage();
		}

		private int current() {
			return min + (int) Math.round(this.value * (max - min));
		}

		@Override
		protected void updateMessage() {
			setMessage(Component.literal(label + ": " + current()));
		}

		@Override
		protected void applyValue() {
			onChange.accept(current());
		}
	}
}
