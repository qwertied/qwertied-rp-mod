package qwertied.mod.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ModRP {

    private static final Logger LOGGER = LoggerFactory.getLogger("QwertiedResourcepackMod");
    private static final String GITHUB_API_URL = "https://api.github.com/repos/qwertied/qwertied-s-resoursepack/releases/latest";

    public void initializeResourcePackUpdater() {
        LOGGER.info("Initializing resource pack updater");
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            LOGGER.info("Server started event triggered, updating resource pack");
            updateResourcePack();
        });
        // Добавим вызов updateResourcePack() сразу при инициализации
        updateResourcePack();
    }

    private void updateResourcePack() {
        LOGGER.info("Starting resource pack update process");
        try {
            JsonObject latestRelease = getLatestReleaseInfo();
            LOGGER.info("Latest release info: {}", latestRelease);

            if (latestRelease.has("assets") && latestRelease.getAsJsonArray("assets").size() > 0) {
                JsonObject asset = latestRelease.getAsJsonArray("assets").get(0).getAsJsonObject();
                String downloadUrl = asset.get("browser_download_url").getAsString();
                String resourcePackName = asset.get("name").getAsString();
                String version = latestRelease.get("tag_name").getAsString();

                LOGGER.info("Resource pack details: Name={}, Version={}, URL={}", resourcePackName, version, downloadUrl);

                File resourcePacksFolder = new File(MinecraftClient.getInstance().runDirectory, "resourcepacks");
                File resourcePackFile = new File(resourcePacksFolder, resourcePackName);

                if (!resourcePackFile.exists() || !isUpToDate(resourcePackFile, version)) {
                    LOGGER.info("Downloading new version of resource pack");
                    downloadResourcePack(downloadUrl, resourcePackFile);
                    updateVersionFile(resourcePackFile, version);
                    LOGGER.info("Resource pack updated successfully");
                } else {
                    LOGGER.info("Resource pack is already up to date");
                }
            } else {
                LOGGER.warn("No assets found in the latest release");
            }
        } catch (IOException e) {
            LOGGER.error("Error updating resource pack", e);
        }
    }

    private JsonObject getLatestReleaseInfo() throws IOException {
        LOGGER.info("Fetching latest release info from GitHub");
        URL url = new URL(GITHUB_API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json");

        try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
            JsonObject result = JsonParser.parseReader(reader).getAsJsonObject();
            LOGGER.info("GitHub API response received successfully");
            return result;
        } finally {
            connection.disconnect();
        }
    }

    private boolean isUpToDate(File resourcePackFile, String latestVersion) throws IOException {
        File versionFile = new File(resourcePackFile.getParentFile(), resourcePackFile.getName() + ".version");
        if (versionFile.exists()) {
            String currentVersion = FileUtils.readFileToString(versionFile, "UTF-8");
            LOGGER.info("Current version: {}, Latest version: {}", currentVersion, latestVersion);
            return currentVersion.equals(latestVersion);
        }
        LOGGER.info("Version file does not exist, assuming update is needed");
        return false;
    }

    private void downloadResourcePack(String downloadUrl, File resourcePackFile) throws IOException {
        LOGGER.info("Downloading resource pack from: {}", downloadUrl);
        FileUtils.copyURLToFile(new URL(downloadUrl), resourcePackFile);
        LOGGER.info("Resource pack downloaded to: {}", resourcePackFile.getAbsolutePath());
    }

    private void updateVersionFile(File resourcePackFile, String version) throws IOException {
        File versionFile = new File(resourcePackFile.getParentFile(), resourcePackFile.getName() + ".version");
        FileUtils.writeStringToFile(versionFile, version, "UTF-8");
        LOGGER.info("Version file updated: {}", versionFile.getAbsolutePath());
    }
}