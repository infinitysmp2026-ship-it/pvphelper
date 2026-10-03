package com.pvphelper.helpers;

import com.pvphelper.config.ConfigManager;
import com.pvphelper.config.PvPHelperConfig;
import com.pvphelper.core.AbstractHelper;
import com.pvphelper.core.HotbarManager;
import com.pvphelper.core.InteractionManager;
import com.pvphelper.core.ItemUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * Source item -> Lunge Spear -> Lunge (vanilla attack) -> back to the source item.
 * Nothing about any item is modified; only the selected hotbar slot is switched temporarily.
 */
public class LungeHelper extends AbstractHelper {
	private static final int TIMEOUT_TICKS = 40;

	private enum Step { ATTACK, RETURN }

	private Step step = Step.ATTACK;

	public LungeHelper() {
		super("Lunge Helper");
	}

	private PvPHelperConfig.Lunge cfg() {
		return ConfigManager.get().lunge;
	}

	@Override public int priority() { return 3; }
	@Override protected boolean debugEnabled() { return cfg().debug; }

	@Override
	public boolean isEnabled() {
		return ConfigManager.get().masterEnabled && cfg().enabled;
	}

	/** Mandatory exclusions: these items always keep their normal left-click behaviour. */
	public static boolean isExcluded(ItemStack stack) {
		return stack.isEmpty()
			|| stack.is(ItemTags.RAILS)
			|| stack.is(Items.ENDER_PEARL)
			|| stack.is(Items.SHIELD)
			|| stack.is(ItemTags.SWORDS)
			|| stack.is(ItemTags.PICKAXES)
			|| stack.is(ItemTags.AXES)
			|| stack.is(ItemTags.SHOVELS)
			|| stack.is(ItemTags.HOES)
			|| stack.is(Items.MACE)
			|| stack.is(Items.TRIDENT)
			|| stack.has(DataComponents.TOOL)
			|| stack.has(DataComponents.WEAPON)
			|| ItemUtil.isSpear(stack);
	}

	/** Explicit eligibility system: exclusions first, then the configurable list / food option. */
	public boolean isEligible(ItemStack stack) {
		if (isExcluded(stack)) return false;
		if (cfg().eligibleItems.contains(ItemUtil.itemId(stack))) return true;
		return cfg().eligibleFood && stack.has(DataComponents.FOOD);
	}

	private int findSpear(Minecraft mc) {
		boolean needLunge = cfg().requireLungeEnchantment;
		return HotbarManager.find(mc.player,
			s -> ItemUtil.isSpear(s) && (!needLunge || ItemUtil.hasEnchantment(s, Enchantments.LUNGE)));
	}

	@Override
	public boolean canStart(Minecraft mc) {
		if (isActive() || isOnCooldown()) return false;
		if (!isEligible(mc.player.getMainHandItem())) return false;
		int spear = findSpear(mc);
		return spear >= 0 && spear != HotbarManager.getSelected(mc.player);
	}

	@Override
	public boolean start(Minecraft mc) {
		int spearSlot = findSpear(mc);
		if (spearSlot < 0) return false;

		begin(mc, TIMEOUT_TICKS); // STEP 1: remembers the original slot
		if (!selectSlot(mc, spearSlot)) { // STEP 2: switch to the spear
			cancel(mc, "invalid spear slot", true);
			return false;
		}
		step = Step.ATTACK;
		setWait(cfg().activationDelayTicks);
		debug(mc, "switched to spear in slot " + (spearSlot + 1));
		return true;
	}

	@Override
	protected void onTick(Minecraft mc) {
		switch (step) {
			case ATTACK -> {
				if (!ItemUtil.isSpear(mc.player.getMainHandItem())) {
					cancel(mc, "spear disappeared", true);
					return;
				}
				// STEP 3: legitimate vanilla attack with the spear (this is what triggers Lunge)
				InteractionManager.attack(mc);
				step = Step.RETURN;
				setWait(cfg().returnDelayTicks);
			}
			case RETURN -> complete(mc, true, cfg().cooldownTicks); // STEP 4: back to the source item
		}
	}
}
