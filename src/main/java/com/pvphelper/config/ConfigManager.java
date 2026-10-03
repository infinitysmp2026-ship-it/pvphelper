package com.pvphelper.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pvphelper.PvPHelperClient;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static PvPHelperConfig config = new PvPHelperConfig();

	private ConfigManager() {}

	private static Path path() {
		return FabricLoader.getInstance().getConfigDir().resolve("pvphelper.json");
	}

	public static PvPHelperConfig get() {
		return config;
	}

	public static void load() {
		Path file = path();
		if (Files.exists(file)) {
			try {
				String json = Files.readString(file, StandardCharsets.UTF_8);
				PvPHelperConfig loaded = GSON.fromJson(json, PvPHelperConfig.class);
				if (loaded != null) {
					config = loaded;
				}
			} catch (Exception e) {
				PvPHelperClient.LOGGER.error("Could not read config, using defaults", e);
				config = new PvPHelperConfig();
			}
		}
		config.sanitize();
		save();
	}

	public static void save() {
		config.sanitize();
		try {
			Files.createDirectories(path().getParent());
			Files.writeString(path(), GSON.toJson(config), StandardCharsets.UTF_8);
		} catch (IOException e) {
			PvPHelperClient.LOGGER.error("Could not save config", e);
		}
	}
}
