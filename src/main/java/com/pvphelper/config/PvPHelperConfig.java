package com.pvphelper.config;

import java.util.ArrayList;
import java.util.List;

public class PvPHelperConfig {
	public boolean masterEnabled = true;
	public Tnt tnt = new Tnt();
	public Pearl pearl = new Pearl();
	public Lunge lunge = new Lunge();

	public static class Tnt {
		public boolean enabled = true;
		/** Ticks waited between the individual steps (min 1, max 10). */
		public int interactionDelayTicks = 1;
		public boolean flintAndSteelFollowUp = true;
		public boolean flameBowFallback = true;
		public boolean debug = false;
	}

	public static class Pearl {
		public boolean enabled = true;
		/** Ticks between the pearl throw and the Wind Charge use (min 1, max 20). */
		public int windChargeDelayTicks = 2;
		/** Cooldown after a finished sequence (0..100 ticks). */
		public int cooldownTicks = 20;
		public boolean debug = false;
	}

	public static class Lunge {
		public boolean enabled = true;
		/** Ticks between selecting the spear and the Lunge attack (min 1, max 10). */
		public int activationDelayTicks = 1;
		/** Ticks between the Lunge attack and returning to the source item (min 1, max 10). */
		public int returnDelayTicks = 1;
		public int cooldownTicks = 5;
		public boolean requireLungeEnchantment = true;
		public boolean eligibleFood = false;
		public List<String> eligibleItems = new ArrayList<>(List.of("minecraft:wind_charge"));
		public boolean debug = false;
	}

	/** Clamps every value into its valid range (protects against hand-edited files). */
	public void sanitize() {
		if (tnt == null) tnt = new Tnt();
		if (pearl == null) pearl = new Pearl();
		if (lunge == null) lunge = new Lunge();
		tnt.interactionDelayTicks = clamp(tnt.interactionDelayTicks, 1, 10);
		pearl.windChargeDelayTicks = clamp(pearl.windChargeDelayTicks, 1, 20);
		pearl.cooldownTicks = clamp(pearl.cooldownTicks, 0, 100);
		lunge.activationDelayTicks = clamp(lunge.activationDelayTicks, 1, 10);
		lunge.returnDelayTicks = clamp(lunge.returnDelayTicks, 1, 10);
		lunge.cooldownTicks = clamp(lunge.cooldownTicks, 0, 100);
		if (lunge.eligibleItems == null) lunge.eligibleItems = new ArrayList<>();
	}

	private static int clamp(int v, int min, int max) {
		return Math.max(min, Math.min(max, v));
	}
}
