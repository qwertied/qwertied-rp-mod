package qwertied.mod;

import net.fabricmc.api.ModInitializer;
import qwertied.mod.item.ModRP;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QwertiedResoursepackMod implements ModInitializer {
	public static final String MOD_ID = "qwertied_mod_id";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private final ModRP modRP = new ModRP();

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing QwertiedResoursepackMod");
		try {
			modRP.initializeResourcePackUpdater();
			LOGGER.info("Resource pack updater initialized successfully");
		} catch (Exception e) {
			LOGGER.error("Failed to initialize resource pack updater", e);
		}
		LOGGER.info("QwertiedResoursepackMod initialization complete!");
	}
}