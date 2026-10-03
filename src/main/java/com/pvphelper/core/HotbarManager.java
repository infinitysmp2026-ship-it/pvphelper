package com.pvphelper.core;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

/** Shared hotbar helper: scan slots 0-8, remember/restore slots, switch safely. */
public final class HotbarManager {
	public static final int HOTBAR_SIZE = 9;

	private HotbarManager() {}

	public static boolean isValidSlot(int slot) {
		return slot >= 0 && slot < HOTBAR_SIZE;
	}

	public static int getSelected(Player player) {
		return player.getInventory().getSelectedSlot();
	}

	public static ItemStack stackAt(Player player, int slot) {
		if (!isValidSlot(slot)) {
			return ItemStack.EMPTY;
		}
		return player.getInventory().getItem(slot);
	}

	/** First hotbar slot (0-8) whose stack matches, or -1. */
	public static int find(Player player, Predicate<ItemStack> matcher) {
		Inventory inventory = player.getInventory();
		for (int i = 0; i < HOTBAR_SIZE; i++) {
			ItemStack stack = inventory.getItem(i);
			if (!stack.isEmpty() && matcher.test(stack)) {
				return i;
			}
		}
		return -1;
	}

	/** Switches the selected slot using the normal client mechanism (the client syncs it to the server). */
	public static boolean select(Player player, int slot) {
		if (!isValidSlot(slot)) {
			return false;
		}
		if (getSelected(player) != slot) {
			player.getInventory().setSelectedSlot(slot);
		}
		return true;
	}
}
