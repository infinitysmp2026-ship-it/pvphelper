package com.pvphelper.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import java.util.ArrayList;
import java.util.List;

/** Ticks all sequences and enforces the global fail-safe rules. */
public final class SequenceManager {
	private static final List<Sequence> SEQUENCES = new ArrayList<>();

	private SequenceManager() {}

	public static void register(Sequence sequence) {
		SEQUENCES.add(sequence);
	}

	/** The currently active sequence, or null. Only one sequence can be active at a time. */
	public static Sequence active() {
		for (Sequence s : SEQUENCES) {
			if (s.isActive()) {
				return s;
			}
		}
		return null;
	}

	public static void tick(Minecraft mc) {
		for (Sequence s : SEQUENCES) {
			s.tickCooldown();
		}
		Sequence active = active();
		if (active == null) {
			return;
		}

		LocalPlayer player = mc.player;
		if (player == null || mc.level == null || mc.gameMode == null) {
			active.cancel(mc, "no world / disconnected", false);
			return;
		}
		if (!player.isAlive()) {
			active.cancel(mc, "player died", false);
			return;
		}
		if (active.dimensionChanged(mc)) {
			active.cancel(mc, "dimension changed", false);
			return;
		}
		if (mc.screen != null) {
			active.cancel(mc, "a GUI was opened", true);
			return;
		}
		if (HotbarManager.getSelected(player) != active.expectedSlot()) {
			// The player switched slots himself: respect that, do not fight it.
			active.cancel(mc, "slot changed unexpectedly", false);
			return;
		}
		active.tickActive(mc);
	}

	public static void cancelAll(Minecraft mc, String reason, boolean restoreSlot) {
		for (Sequence s : SEQUENCES) {
			s.cancel(mc, reason, restoreSlot);
		}
	}
}
