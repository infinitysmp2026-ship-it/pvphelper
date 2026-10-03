package com.pvphelper.core;

import com.pvphelper.PvPHelperClient;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Reusable sequence/state machine base: idle + active state, step waiting, timeout,
 * cancellation, cleanup and an optional cooldown.
 */
public abstract class Sequence {
	public enum State { IDLE, ACTIVE }

	private final String name;
	private State state = State.IDLE;
	private int age;
	private int timeoutTicks;
	private int waitTicks;
	private int cooldownRemaining;
	private int originalSlot = -1;
	private int expectedSlot = -1;
	private ResourceKey<Level> dimension;

	protected Sequence(String name) {
		this.name = name;
	}

	public final String name() { return name; }
	public final boolean isActive() { return state == State.ACTIVE; }
	public final boolean isOnCooldown() { return cooldownRemaining > 0; }
	public final int originalSlot() { return originalSlot; }
	final int expectedSlot() { return expectedSlot; }

	/** Lower number = higher priority. */
	public abstract int priority();

	protected abstract boolean debugEnabled();

	/** Called once per tick while ACTIVE and not waiting. */
	protected abstract void onTick(Minecraft mc);

	/** Called whenever the sequence returns to IDLE. */
	protected void onReset() {}

	/** Enters the ACTIVE state. Must only be called when mc.player and mc.level exist. */
	protected final void begin(Minecraft mc, int timeoutTicks) {
		this.state = State.ACTIVE;
		this.age = 0;
		this.timeoutTicks = timeoutTicks;
		this.waitTicks = 0;
		this.originalSlot = HotbarManager.getSelected(mc.player);
		this.expectedSlot = this.originalSlot;
		this.dimension = mc.level.dimension();
	}

	/** Wait this many ticks (minimum 1) before the next onTick call. */
	protected final void setWait(int ticks) {
		this.waitTicks = Math.max(1, ticks);
	}

	/** Selects a hotbar slot as part of the sequence (so it is not seen as an unexpected change). */
	protected final boolean selectSlot(Minecraft mc, int slot) {
		if (!HotbarManager.select(mc.player, slot)) {
			return false;
		}
		this.expectedSlot = slot;
		return true;
	}

	final boolean dimensionChanged(Minecraft mc) {
		return mc.level == null || dimension == null || !dimension.equals(mc.level.dimension());
	}

	final void tickCooldown() {
		if (cooldownRemaining > 0) {
			cooldownRemaining--;
		}
	}

	final void tickActive(Minecraft mc) {
		age++;
		if (age > timeoutTicks) {
			cancel(mc, "timeout", true);
			return;
		}
		if (waitTicks > 0) {
			waitTicks--;
			if (waitTicks > 0) {
				return;
			}
		}
		onTick(mc);
	}

	protected final void complete(Minecraft mc, boolean restoreSlot, int cooldownTicks) {
		debug(mc, "finished");
		finish(mc, restoreSlot, cooldownTicks);
	}

	public final void cancel(Minecraft mc, String reason, boolean restoreSlot) {
		if (state != State.ACTIVE) {
			return;
		}
		debug(mc, "cancelled: " + reason);
		finish(mc, restoreSlot, 0);
	}

	private void finish(Minecraft mc, boolean restoreSlot, int cooldownTicks) {
		if (restoreSlot && mc != null && mc.player != null && originalSlot >= 0) {
			HotbarManager.select(mc.player, originalSlot);
		}
		state = State.IDLE;
		waitTicks = 0;
		cooldownRemaining = Math.max(0, cooldownTicks);
		originalSlot = -1;
		expectedSlot = -1;
		dimension = null;
		onReset();
	}

	protected final void debug(Minecraft mc, String message) {
		if (!debugEnabled()) {
			return;
		}
		PvPHelperClient.LOGGER.info("[{}] {}", name, message);
		if (mc != null && mc.player != null) {
			InteractionManager.message(mc, "[" + name + "] " + message);
		}
	}
}
