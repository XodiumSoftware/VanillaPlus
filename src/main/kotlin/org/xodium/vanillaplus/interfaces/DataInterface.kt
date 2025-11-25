package org.xodium.vanillaplus.interfaces

import kotlinx.serialization.json.Json
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CentralConfigData
import org.xodium.vanillaplus.features.BooksFeature
import org.xodium.vanillaplus.features.CauldronFeature
import java.io.File

/** Represents a contract for data within the system. */
internal interface DataInterface {
    companion object {
        private const val CONFIG_FILE = "config.json"
    }

    val json: Json
        get() =
            Json {
                prettyPrint = true
                ignoreUnknownKeys = true
            }

    val configData: CentralConfigData
        get() = CentralConfigData(booksFeature = BooksFeature.config, cauldronFeature = CauldronFeature.config)

    /** Loads configuration from JSON file. */
    fun load() {
        val config = File(instance.dataFolder, CONFIG_FILE)
        if (!config.exists()) {
            instance.dataFolder.mkdirs()
            save()
            instance.logger.info("Created new config file.")
        } else {
            try {
                val loadedConfig = json.decodeFromString(CentralConfigData.serializer(), config.readText())
                updateConfigData(loadedConfig)
                instance.logger.info("Loaded configs from file.")
            } catch (e: Exception) {
                instance.logger.warning("Failed to load configs: ${e.message}, using defaults")
                save()
            }
        }
    }

    /** Saves configuration to JSON file. */
    fun save() {
        try {
            File(instance.dataFolder, CONFIG_FILE).writeText(
                json.encodeToString(CentralConfigData.serializer(), configData),
            )
            instance.logger.info("Saved configs")
        } catch (e: Exception) {
            instance.logger.warning("Failed to save configs: ${e.message}")
        }
    }

    /** Updates the actual feature configurations with loaded data */
    private fun updateConfigData(loadedConfig: CentralConfigData) {
        BooksFeature.config = loadedConfig.booksFeature
        CauldronFeature.config = loadedConfig.cauldronFeature
    }
}
