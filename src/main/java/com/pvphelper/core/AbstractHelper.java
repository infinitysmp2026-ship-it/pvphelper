package com.pvphelper.core;

import net.minecraft.client.Minecraft;

/** Base class of the three helpers. */
public abstract class AbstractHelper extends Sequence {
	protected AbstractHelper(String name) {
		super(name);
	}

	public abstract boolean isEnabled();

	/** True when this helper owns a left click performed right now. Must be free of side effects. */
	public abstract boolean canStart(Minecraft mc);

	/** Starts the sequence. Returns true when the click was taken over. */
	public abstract boolean start(Minecraft mc);
}
