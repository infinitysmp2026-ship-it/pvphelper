package com.pvphelper.core;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public final class ItemUtil {
	private ItemUtil() {}

	public static boolean hasEnchantment(ItemStack stack, ResourceKey<Enchantment> key) {
		ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
		for (Holder<Enchantment> holder : enchantments.keySet()) {
			if (holder.is(key)) {
				return true;
			}
		}
		return false;
	}

	public static String itemId(ItemStack stack) {
		return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
	}

	/** Matches every spear variant by registry path (wooden_spear, iron_spear, ...). */
	public static boolean isSpear(ItemStack stack) {
		return !stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().endsWith("_spear");
	}
}
