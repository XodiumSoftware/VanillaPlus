package org.xodium.vanillaplus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ConfigManager {
    private static final String CONFIG_FILE = "config.json";
    private final VanillaPlus vp = VanillaPlus.getInstance();
    private final Path dataFolderPath = vp.getDataFolder().toPath();
    private final Path dataFilePath = dataFolderPath.resolve(CONFIG_FILE);

    {
        try {
            Files.createDirectories(dataFolderPath);
            if (!Files.exists(dataFilePath)) {
                Files.createFile(dataFilePath);
                Files.write(dataFilePath, "{}".getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setData(String key, JsonElement value) {
        try {
            JsonObject currentData = getAllData();
            currentData.add(key, value);
            Files.write(dataFilePath, new Gson().toJson(currentData).getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JsonElement getData(String key) {
        try {
            JsonObject currentData = getAllData();
            return currentData.get(key);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private JsonObject getAllData() throws IOException {
        return JsonParser.parseString(new String(Files.readAllBytes(dataFilePath), StandardCharsets.UTF_8))
                .getAsJsonObject();
    }

}