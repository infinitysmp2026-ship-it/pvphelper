package com.pvphelper.core;

import com.pvphelper.config.ConfigManager;
import com.pvphelper.config.PvPHelperConfig;
import com.pvphelper.helpers.LungeHelper;
import com.pvphelper.helpers.PearlCatchHelper;
import com.pvphelper.helpers.TNTCartHelper;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class HelperManager {
	private static final List<AbstractHelper> HELPERS = new ArrayList<>();
	private static TNTCartHelper tnt;
	private static PearlCatchHelper pearl;
	private static LungeHelper lunge;

	private HelperManager() {}

	public static void init() {
		tnt = new TNTCartHelper();
		pearl = new PearlCatchHelper();
		lunge = new LungeHelper();
		HELPERS.clear();
		HELPERS.add(tnt);   // priority 1
		HELPERS.add(pearl); // priority 2
		HELPERS.add(lunge); // priority 3
		for (AbstractHelper helper : HELPERS) {
			SequenceManager.register(helper);
		}
	}

	public static List<AbstractHelper> helpers() {
		return Collections.unmodifiableList(HELPERS);
	}

	public static boolean isMasterEnabled() {
		return ConfigManager.get().masterEnabled;
	}

	public static void toggleMaster(Minecraft mc) {
		PvPHelperConfig c = ConfigManager.get();
		c.masterEnabled = !c.masterEnabled;
		if (!c.masterEnabled) {
			SequenceManager.cancelAll(mc, "master disabled", true);
		}
		ConfigManager.save();
		announce(mc, "Master", c.masterEnabled);
	}

	public static void toggleTnt(Minecraft mc) {
		PvPHelperConfig c = ConfigManager.get();
		c.tnt.enabled = !c.tnt.enabled;
		if (!c.tnt.enabled) tnt.cancel(mc, "disabled", true);
		ConfigManager.save();
		announce(mc, "TNT Cart Helper", c.tnt.enabled);
	}

	public static void togglePearl(Minecraft mc) {
		PvPHelperConfig c = ConfigManager.get();
		c.pearl.enabled = !c.pearl.enabled;
		if (!c.pearl.enabled) pearl.cancel(mc, "disabled", true);
		ConfigManager.save();
		announce(mc, "Pearl Catch Helper", c.pearl.enabled);
	}

	public static void toggleLunge(Minecraft mc) {
		PvPHelperConfig c = ConfigManager.get();
		c.lunge.enabled = !c.lunge.enabled;
		if (!c.lunge.enabled) lunge.cancel(mc, "disabled", true);
		ConfigManager.save();
		announce(mc, "Lunge Helper", c.lunge.enabled);
	}

	/** Enable All / Disable All. */
	public static void setAll(Minecraft mc, boolean enabled) {
		PvPHelperConfig c = ConfigManager.get();
		c.masterEnabled = enabled;
		c.tnt.enabled = enabled;
		c.pearl.enabled = enabled;
		c.lunge.enabled = enabled;
		if (!enabled) {
			SequenceManager.cancelAll(mc, "all helpers disabled", true);
		}
		ConfigManager.save();
		announce(mc, "All helpers", enabled);
	}

	private static void announce(Minecraft mc, String name, boolean on) {
		InteractionManager.message(mc, "PvP Helper - " + name + ": " + (on ? "ON" : "OFF"));
	}
}
