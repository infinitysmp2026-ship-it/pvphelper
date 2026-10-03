package com.pvphelper.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Exposes the vanilla attack / use-item actions so helpers use the normal client code paths. */
@Mixin(Minecraft.class)
public interface MinecraftAccessor {
	@Invoker("startAttack")
	boolean pvphelper$startAttack();

	@Invoker("startUseItem")
	void pvphelper$startUseItem();

	@Accessor("missTime")
	void pvphelper$setMissTime(int value);
}
