package org.xodium.vanillaplus.managers

import kotlinx.serialization.json.Json
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.ConfigData
import java.io.File

/** Manages loading and saving the configuration file. */
internal object ConfigManager {
    private val json =
        Json {
            prettyPrint = true
            encodeDefaults = true
            ignoreUnknownKeys = true
        }

    /**
     * Loads or creates the configuration file.
     * @param fileName The name of the configuration file.
     * @return The loaded or default configuration data.
     */
    fun load(fileName: String = "config.json"): ConfigData {
        val file = File(instance.dataFolder, fileName)
        if (!instance.dataFolder.exists()) instance.dataFolder.mkdirs()

        return if (file.exists()) {
            try {
                val text = file.readText()
                val cfg = json.decodeFromString(ConfigData.serializer(), text)
                // Re-write normalized/pretty file (optional)
                try {
                    file.writeText(json.encodeToString(ConfigData.serializer(), cfg))
                } catch (writeEx: Exception) {
                    instance.logger.warning("Failed to re-write $fileName: ${writeEx.message}")
                }
                instance.logger.info("Loaded configuration from $fileName")
                cfg
            } catch (ex: Exception) {
                instance.logger.warning("Failed to load $fileName, using defaults and writing new file: ${ex.message}")
                val cfg = ConfigData()
                try {
                    file.writeText(json.encodeToString(ConfigData.serializer(), cfg))
                } catch (writeEx: Exception) {
                    instance.logger.severe("Failed to write default $fileName: ${writeEx.message}")
                }
                cfg
            }
        } else {
            val cfg = ConfigData()
            try {
                file.writeText(json.encodeToString(ConfigData.serializer(), cfg))
                instance.logger.info("Created default $fileName")
            } catch (ex: Exception) {
                instance.logger.severe("Failed to create $fileName: ${ex.message}")
            }
            cfg
        }
    }
}
