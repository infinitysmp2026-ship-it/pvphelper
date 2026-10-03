package com.pvphelper.core;

import com.pvphelper.mixin.MinecraftAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Wrapper around the legitimate vanilla client interaction paths. */
public final class InteractionManager {
	private InteractionManager() {}

	/** Equivalent to the player pressing the use key once (normal vanilla right click). */
	public static void useItem(Minecraft mc) {
		((MinecraftAccessor) (Object) mc).pvphelper$startUseItem();
	}

	/** Equivalent to a vanilla left click. The dispatcher is suppressed so it is never re-entered. */
	public static boolean attack(Minecraft mc) {
		LeftClickDispatcher.setSuppressed(true);
		try {
			return ((MinecraftAccessor) (Object) mc).pvphelper$startAttack();
		} finally {
			LeftClickDispatcher.setSuppressed(false);
		}
	}

	/** The block currently targeted by the crosshair, or null. */
	public static BlockHitResult targetedBlock(Minecraft mc) {
		HitResult hit = mc.hitResult;
		if (hit instanceof BlockHitResult blockHit && blockHit.getType() == HitResult.Type.BLOCK) {
			return blockHit;
		}
		return null;
	}

	/** Where a block item used on this hit result will end up. */
	public static BlockPos placementPos(Minecraft mc, BlockHitResult hit) {
		BlockPos clicked = hit.getBlockPos();
		if (mc.level.getBlockState(clicked).canBeReplaced()) {
			return clicked;
		}
		return clicked.relative(hit.getDirection());
	}

	/**
	 * Uses the main-hand item on the given block (normal useItemOn). Prefers the real crosshair hit when it
	 * already targets that block; otherwise uses the top face of the block.
	 */
	public static boolean useItemOnBlock(Minecraft mc, BlockPos pos) {
		BlockHitResult crosshair = targetedBlock(mc);
		BlockHitResult hit;
		if (crosshair != null && crosshair.getBlockPos().equals(pos)) {
			hit = crosshair;
		} else {
			hit = new BlockHitResult(new Vec3(pos.getX() + 0.5, pos.getY() + 0.125, pos.getZ() + 0.5), Direction.UP, pos, false);
		}
		InteractionResult result = mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);
		if (result.consumesAction()) {
			mc.player.swing(InteractionHand.MAIN_HAND);
			return true;
		}
		return false;
	}

	/** Short action-bar message. */
	public static void message(Minecraft mc, String text) {
		if (mc.player != null) {
			mc.player.displayClientMessage(Component.literal(text), true);
		}
	}
}
