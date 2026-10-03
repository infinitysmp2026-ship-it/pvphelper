package com.pvphelper;

import com.pvphelper.config.ConfigManager;
import com.pvphelper.core.HelperManager;
import com.pvphelper.core.SequenceManager;
import com.pvphelper.input.KeybindManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PvPHelperClient implements ClientModInitializer {
	public static final String MOD_ID = "pvphelper";
	public static final Logger LOGGER = LoggerFactory.getLogger("PvPHelper");

	@Override
	public void onInitializeClient() {
		ConfigManager.load();
		HelperManager.init();
		KeybindManager.register();

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			KeybindManager.tick(client);
			SequenceManager.tick(client);
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
			SequenceManager.cancelAll(client, "disconnected", false));

		LOGGER.info("PvP Helper (client-side) initialised");
	}
}
