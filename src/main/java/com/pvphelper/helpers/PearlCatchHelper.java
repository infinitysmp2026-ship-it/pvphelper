package com.pvphelper.helpers;

import com.pvphelper.config.ConfigManager;
import com.pvphelper.config.PvPHelperConfig;
import com.pvphelper.core.AbstractHelper;
import com.pvphelper.core.HotbarManager;
import com.pvphelper.core.InteractionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Ender Pearl -> Wind Charge. The player keeps full control of the aim; only the item sequence is assisted.
 */
public class PearlCatchHelper extends AbstractHelper {
	private static final int TIMEOUT_TICKS = 80;

	private enum Step { CHECK_THROW, USE_WIND, RETURN }

	private Step step = Step.CHECK_THROW;
	private ItemStack pearlBefore = ItemStack.EMPTY;
	private int countBefore;

	public PearlCatchHelper() {
		super("Pearl Catch Helper");
	}

	private PvPHelperConfig.Pearl cfg() {
		return ConfigManager.get().pearl;
	}

	@Override public int priority() { return 2; }
	@Override protected boolean debugEnabled() { return cfg().debug; }

	@Override
	public boolean isEnabled() {
		return ConfigManager.get().masterEnabled && cfg().enabled;
	}

	@Override
	public boolean canStart(Minecraft mc) {
		if (isActive() || isOnCooldown()) return false;
		ItemStack held = mc.player.getMainHandItem();
		if (!held.is(Items.ENDER_PEARL)) return false;
		if (mc.player.getCooldowns().isOnCooldown(held)) return false;
		return HotbarManager.find(mc.player, s -> s.is(Items.WIND_CHARGE)) >= 0;
	}

	@Override
	public boolean start(Minecraft mc) {
		ItemStack held = mc.player.getMainHandItem();
		pearlBefore = held.copy();
		countBefore = held.getCount();

		begin(mc, TIMEOUT_TICKS);
		step = Step.CHECK_THROW;
		setWait(1);
		debug(mc, "throwing ender pearl");
		// STEP 1: normal vanilla use (identical to a right click)
		InteractionManager.useItem(mc);
		return true;
	}

	@Override
	protected void onTick(Minecraft mc) {
		switch (step) {
			case CHECK_THROW -> {
				ItemStack now = HotbarManager.stackAt(mc.player, originalSlot());
				boolean consumed = now.isEmpty() || now.getCount() < countBefore;
				boolean onCooldown = mc.player.getCooldowns().isOnCooldown(pearlBefore);
				if (!consumed && !onCooldown) {
					cancel(mc, "ender pearl throw failed", true);
					return;
				}
				// STEP 2: pearl initiated -> switch to the wind charge
				int windSlot = HotbarManager.find(mc.player, s -> s.is(Items.WIND_CHARGE));
				if (windSlot < 0) {
					cancel(mc, "no wind charge in hotbar", true);
					return;
				}
				selectSlot(mc, windSlot);
				step = Step.USE_WIND;
				setWait(cfg().windChargeDelayTicks);
			}
			case USE_WIND -> {
				// STEP 3: use the wind charge after the configured delay
				ItemStack held = mc.player.getMainHandItem();
				if (!held.is(Items.WIND_CHARGE)) {
					cancel(mc, "wind charge is no longer selected", true);
					return;
				}
				if (mc.player.getCooldowns().isOnCooldown(held)) {
					cancel(mc, "wind charge is on cooldown", true);
					return;
				}
				InteractionManager.useItem(mc);
				step = Step.RETURN;
				setWait(1);
			}
			case RETURN -> complete(mc, true, cfg().cooldownTicks); // never leave the player on the wind charge
		}
	}
}
