package com.pvphelper.input;

import com.mojang.blaze3d.platform.InputConstants;
import com.pvphelper.core.HelperManager;
import com.pvphelper.screen.PvPHelperConfigScreen;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/** All keybinds are normal KeyMappings and can be rebound in Options > Controls > Key Binds. */
public final class KeybindManager {
	private static KeyMapping toggleMaster;
	private static KeyMapping toggleTnt;
	private static KeyMapping togglePearl;
	private static KeyMapping toggleLunge;
	private static KeyMapping openSettings;

	private KeybindManager() {}

	public static void register() {
		toggleMaster = create("key.pvphelper.toggle_master", GLFW.GLFW_KEY_UNKNOWN);
		toggleTnt = create("key.pvphelper.toggle_tnt", GLFW.GLFW_KEY_UNKNOWN);
		togglePearl = create("key.pvphelper.toggle_pearl", GLFW.GLFW_KEY_UNKNOWN);
		toggleLunge = create("key.pvphelper.toggle_lunge", GLFW.GLFW_KEY_UNKNOWN);
		openSettings = create("key.pvphelper.open_settings", GLFW.GLFW_KEY_RIGHT_SHIFT);
	}

	private static KeyMapping create(String translationKey, int defaultKey) {
		return KeyBindingHelper.registerKeyBinding(
			new KeyMapping(translationKey, InputConstants.Type.KEYSYM, defaultKey, KeyMapping.Category.MISC));
	}

	public static void tick(Minecraft mc) {
		while (toggleMaster.consumeClick()) HelperManager.toggleMaster(mc);
		while (toggleTnt.consumeClick()) HelperManager.toggleTnt(mc);
		while (togglePearl.consumeClick()) HelperManager.togglePearl(mc);
		while (toggleLunge.consumeClick()) HelperManager.toggleLunge(mc);
		while (openSettings.consumeClick()) {
			if (mc.screen == null) {
				mc.setScreen(new PvPHelperConfigScreen(null));
			}
		}
	}
}
