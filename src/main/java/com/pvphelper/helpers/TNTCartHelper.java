package com.pvphelper.helpers;

import com.pvphelper.config.ConfigManager;
import com.pvphelper.config.PvPHelperConfig;
import com.pvphelper.core.AbstractHelper;
import com.pvphelper.core.HotbarManager;
import com.pvphelper.core.InteractionManager;
import com.pvphelper.core.ItemUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Rail -> TNT Minecart -> Flint &amp; Steel (or Flame Bow). The Crossbow is NEVER selected here.
 */
public class TNTCartHelper extends AbstractHelper {
	private static final int TIMEOUT_TICKS = 60;

	private enum Step { RAIL_CONFIRM, PLACE_CART, FOLLOW_UP }

	private Step step = Step.RAIL_CONFIRM;
	private BlockPos railPos;

	public TNTCartHelper() {
		super("TNT Cart Helper");
	}

	private PvPHelperConfig.Tnt cfg() {
		return ConfigManager.get().tnt;
	}

	@Override public int priority() { return 1; }
	@Override protected boolean debugEnabled() { return cfg().debug; }

	@Override
	public boolean isEnabled() {
		return ConfigManager.get().masterEnabled && cfg().enabled;
	}

	@Override
	public boolean canStart(Minecraft mc) {
		if (isActive() || isOnCooldown()) return false;
		if (!mc.player.getMainHandItem().is(ItemTags.RAILS)) return false;
		if (HotbarManager.find(mc.player, s -> s.is(Items.TNT_MINECART)) < 0) return false;
		return InteractionManager.targetedBlock(mc) != null;
	}

	@Override
	public boolean start(Minecraft mc) {
		BlockHitResult hit = InteractionManager.targetedBlock(mc);
		if (hit == null) return false;
		railPos = InteractionManager.placementPos(mc, hit);

		begin(mc, TIMEOUT_TICKS);
		step = Step.RAIL_CONFIRM;
		setWait(cfg().interactionDelayTicks);
		debug(mc, "placing rail at " + railPos.toShortString());
		// STEP 1: normal vanilla rail placement
		InteractionManager.useItem(mc);
		return true;
	}

	@Override
	protected void onTick(Minecraft mc) {
		switch (step) {
			case RAIL_CONFIRM -> {
				// STEP 2: confirm the rail really exists (client state, rolled back if the server refused)
				if (!mc.level.getBlockState(railPos).is(BlockTags.RAILS)) {
					cancel(mc, "rail could not be placed", true);
					return;
				}
				// STEP 3: switch to the TNT minecart
				int cartSlot = HotbarManager.find(mc.player, s -> s.is(Items.TNT_MINECART));
				if (cartSlot < 0) {
					cancel(mc, "TNT minecart disappeared", true);
					return;
				}
				selectSlot(mc, cartSlot);
				step = Step.PLACE_CART;
				setWait(cfg().interactionDelayTicks);
			}
			case PLACE_CART -> {
				// STEP 4: place the minecart on the new rail
				if (!mc.player.getMainHandItem().is(Items.TNT_MINECART)) {
					cancel(mc, "TNT minecart is no longer selected", true);
					return;
				}
				if (!InteractionManager.useItemOnBlock(mc, railPos)) {
					cancel(mc, "TNT minecart could not be placed", true);
					return;
				}
				step = Step.FOLLOW_UP;
				setWait(cfg().interactionDelayTicks);
			}
			case FOLLOW_UP -> {
				// STEP 5: Flint & Steel first, Flame Bow second, otherwise stop. Crossbow: never.
				int slot = -1;
				if (cfg().flintAndSteelFollowUp) {
					slot = HotbarManager.find(mc.player, s -> s.is(Items.FLINT_AND_STEEL));
				}
				if (slot < 0 && cfg().flameBowFallback) {
					slot = HotbarManager.find(mc.player,
						s -> s.is(Items.BOW) && ItemUtil.hasEnchantment(s, Enchantments.FLAME));
				}
				if (slot >= 0) {
					selectSlot(mc, slot);
					debug(mc, "selected follow-up item in slot " + (slot + 1));
				} else {
					debug(mc, "no follow-up item available, stopping");
				}
				complete(mc, false, 0);
			}
		}
	}
}
