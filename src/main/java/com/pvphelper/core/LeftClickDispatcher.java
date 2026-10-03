package com.pvphelper.core;

import net.minecraft.client.Minecraft;

/**
 * The single central left-click dispatcher. Priority: TNT (1) > Pearl (2) > Lunge (3) > vanilla.
 * A click is handed to at most ONE helper; if nobody owns it, vanilla behaviour is preserved.
 */
public final class LeftClickDispatcher {
	private static boolean suppressed;

	private LeftClickDispatcher() {}

	/** Set while a helper itself performs a vanilla attack, so the click is never dispatched again. */
	static void setSuppressed(boolean value) {
		suppressed = value;
	}

	/** @return true when the click was consumed by a helper (vanilla must NOT process it). */
	public static boolean handle(Minecraft mc) {
		if (suppressed) {
			return false;
		}
		if (mc.player == null || mc.level == null || mc.gameMode == null || mc.screen != null) {
			return false;
		}
		if (mc.player.isSpectator() || !HelperManager.isMasterEnabled()) {
			return false;
		}

		AbstractHelper owner = null;
		for (AbstractHelper helper : HelperManager.helpers()) { // ordered by priority
			if (helper.isEnabled() && helper.canStart(mc)) {
				owner = helper;
				break;
			}
		}

		Sequence active = SequenceManager.active();
		if (active != null) {
			if (owner != null && owner.priority() < active.priority()) {
				active.cancel(mc, "a higher-priority helper took over", true);
			} else {
				// A sequence is running: swallow the click so it cannot hit with an internally selected item.
				return true;
			}
		}

		if (owner == null) {
			return false;
		}
		return owner.start(mc);
	}
}
