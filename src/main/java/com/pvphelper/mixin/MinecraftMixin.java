package com.pvphelper.mixin;

import com.pvphelper.core.LeftClickDispatcher;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The ONE and only left-click hook. Everything is routed through {@link LeftClickDispatcher}. */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	@Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
	private void pvphelper$onStartAttack(CallbackInfoReturnable<Boolean> cir) {
		Minecraft mc = (Minecraft) (Object) this;
		if (LeftClickDispatcher.handle(mc)) {
			// Prevent vanilla from starting to break a block while the button stays held.
			((MinecraftAccessor) (Object) this).pvphelper$setMissTime(10);
			cir.setReturnValue(true);
		}
	}
}
